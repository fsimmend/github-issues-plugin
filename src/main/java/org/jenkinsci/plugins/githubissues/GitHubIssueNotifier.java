/**
 * Copyright (c) 2016-present, Daniel Lo Nigro (Daniel15)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package org.jenkinsci.plugins.githubissues;

import com.cloudbees.jenkins.GitHubRepositoryName;
import com.coravy.hudson.plugins.github.GithubProjectProperty;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Notifier that creates GitHub issues when builds fail, and automatically closes the issue once the build starts
 * passing again.
 */
public class GitHubIssueNotifier extends Notifier implements SimpleBuildStep {

    private String issueTitle;
    private String issueBody;
    private String issueLabel;
    private String issueRepo;
    private boolean reopenIssue = true;
    private boolean appendIssue = true;

    /**
     * Initialises the {@link GitHubIssueNotifier} instance.
     *
     * @param issueTitle the issue title
     * @param issueBody  the issue body
     * @param issueLabel the issue label
     * @param issueRepo the issue repo
     * @param reopenIssue reopen the issue
     * @param appendIssue append to existing issue
     */
    @DataBoundConstructor
    public GitHubIssueNotifier(String issueTitle, String issueBody, String issueLabel, String issueRepo, boolean reopenIssue, boolean appendIssue) {
        this.issueTitle = issueTitle;
        this.issueBody = issueBody;
        this.issueLabel = issueLabel;
        this.issueRepo = issueRepo;
        this.reopenIssue = reopenIssue;
        this.appendIssue = appendIssue;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public GitHubIssueNotifier.DescriptorImpl getDescriptor() {
        return (GitHubIssueNotifier.DescriptorImpl) super.getDescriptor();
    }

    /**
     * Gets the GitHub repository for the specified job.
     * @param job The job
     * @return The GitHub repository
     */
    public GHRepository getRepoForJob(Job<?, ?> job) {
        final String repoUrl;
        if (StringUtils.isNotBlank(this.issueRepo)) {
            repoUrl = this.issueRepo;
        } else {
            GithubProjectProperty foo = job.getProperty(GithubProjectProperty.class);
            repoUrl = foo.getProjectUrlStr();
        }
        GitHubRepositoryName repoName = GitHubRepositoryName.create(repoUrl);
        return repoName == null ? null : repoName.resolveOne();
    }

    @Override
    public void perform(
        @Nonnull Run<?, ?> run,
        @Nonnull FilePath workspace,
        @Nonnull Launcher launcher,
        @Nonnull TaskListener listener
    ) throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();

        Result result = run.getResult();

        final GitHubIssueAction previousGitHubIssueAction;
        final Build previousBuild = (Build) run.getPreviousBuild();
        if (previousBuild == null) {
            previousGitHubIssueAction = null;
        } else {
            previousGitHubIssueAction = previousBuild.getAction(GitHubIssueAction.class);
        }

        final Integer existingIssueNumber;
        if (previousGitHubIssueAction == null) {
            existingIssueNumber = null;
        } else {
            existingIssueNumber = previousGitHubIssueAction.getIssueNumber();
        }

        GHRepository repo = getRepoForJob(run.getParent());
        if (repo == null) {
            logger.println("WARNING: No GitHub config available for this job, GitHub Issue Notifier will not run!");
            return;
        }

        // Return early without initialising the GitHub API client, if we can avoid it
        if (result == Result.SUCCESS && existingIssueNumber==null) {
            // The best case - Successful build with no open issue :D
            return;
        } else if ((result == Result.FAILURE || result == Result.UNSTABLE) && existingIssueNumber!=null) {
            // Issue was already created for a previous failure
            logger.format(
                "GitHub Issue Notifier: Build is still failing and issue #%s already exists. Not sending anything to GitHub issues%n",
                    existingIssueNumber
            );
            final GHIssue issue = repo.getIssue(existingIssueNumber);
            if (issue != null) {
                if (issue.getState().equals(GHIssueState.CLOSED) && this.reopenIssue) {
                    issue.reopen();
                }
                if (this.appendIssue) {
                    String issueBody = this.getIssueBody();
                    if (StringUtils.isBlank(issueBody)) {
                        issueBody = this.getDescriptor().getIssueBody();
                    }
                    issue.comment(IssueCreator.formatText(issueBody, run, listener, workspace));
                }
            }
            previousGitHubIssueAction.setBuildResult(result);
            run.addAction(previousGitHubIssueAction);
            return;
        }

        if (result == Result.FAILURE || result == Result.UNSTABLE) {
            GHIssue issue = IssueCreator.createIssue(run, this, repo, listener, workspace);
            logger.format("GitHub Issue Notifier: Build has started failing, filed GitHub issue #%s%n", issue.getNumber());
            run.addAction(new GitHubIssueAction(issue, result));
        } else if (result == Result.SUCCESS) {
            logger.format("GitHub Issue Notifier: Build was fixed, closing GitHub issue #%s%n", existingIssueNumber);
            GHIssue issue = repo.getIssue(existingIssueNumber);
            issue.comment("Build was fixed!");
            issue.close();
        }
    }

    /**
     * Returns the issue title.
     *
     * @return the issue title
     */
    public String getIssueTitle() {
        return issueTitle;
    }

    /**
     * Returns the issue body.
     *
     * @return the issue body
     */
    public String getIssueBody() {
        return issueBody;
    }

    /**
     * Returns the issue label.
     *
     * @return the issue label
     */
    public String getIssueLabel() {
        return issueLabel;
    }

    public boolean isReopenIssue() {
        return reopenIssue;
    }

    public boolean isAppendIssue() {
        return appendIssue;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String issueTitle = "$JOB_NAME $BUILD_DISPLAY_NAME failed";
        private String issueBody =
            "Build '$JOB_NAME' is failing!\n\n" +
            "Last 50 lines of build output:\n\n" +
            "```\n" +
            "${OUTPUT, lines=50}\n" +
            "```\n\n" + "" +
            "[View full output]($BUILD_URL)";
        private String issueLabel;

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            issueTitle = formData.getString("issueTitle");
            issueBody = formData.getString("issueBody");
            issueLabel = formData.getString("issueLabel");
            save();
            return super.configure(req, formData);
        }

        /**
         * Title of the issue to create on GitHub
         *
         * @return issueTitle
         */
        public String getIssueTitle() {
            return issueTitle;
        }

        /**
         * Body of the issue to create on GitHub
         *
         * @return issueBody
         */
        public String getIssueBody() {
            return issueBody;
        }

        /**
         * Label to use for the issues created on GitHub.
         *
         * @return issueLabel
         */
        public String getIssueLabel() {
            return issueLabel;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Create GitHub issue on failure";
        }
    }
}

/*
 * Copyright (c) 2016-present, Daniel Lo Nigro (Daniel15)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package org.jenkinsci.plugins.githubissues;

import hudson.model.BuildBadgeAction;
import hudson.model.Result;
import org.jenkinsci.plugins.github.util.XSSApi;
import org.kohsuke.github.GHIssue;

public class GitHubIssueAction implements BuildBadgeAction {

    private int issueNumber;
    private final String issueUrl;
    private Result buildResult;

    public GitHubIssueAction(GHIssue issue, Result buildResult) {
        this.issueNumber = issue.getNumber();
        this.issueUrl = XSSApi.asValidHref(issue.getHtmlUrl().toString());
        this.buildResult = buildResult;
    }

    @Override
    public String getDisplayName() {
        return "GitHub Issue";
    }

    @Override
    public String getIconFileName() {
        return "/plugin/github-issues/img/issueLogo.png";
    }

    @Override
    public String getUrlName() {
        return XSSApi.asValidHref(issueUrl);
    }

    public int getIssueNumber() {
        return issueNumber;
    }

    public boolean isFailedBuild() {
        return  buildResult.equals(Result.FAILURE);
    }

    public boolean isUnstableBuild() {
        return  buildResult.equals(Result.UNSTABLE);
    }

    public void setBuildResult(Result buildResult) {
        this.buildResult = buildResult;
    }

    public String getIssueUrl() {
        return issueUrl;
    }
}

/*
 * Copyright (c) 2016-present, Daniel Lo Nigro (Daniel15)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package org.jenkinsci.plugins.githubissues;

import hudson.model.Action;

public class GitHubIssueAction implements Action {

    private final int issueNumber;

    public GitHubIssueAction(int issueNumber) {
        this.issueNumber = issueNumber;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }

    public int getIssueNumber() {
        return issueNumber;
    }
}

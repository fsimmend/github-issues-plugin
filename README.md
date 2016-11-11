Jenkins GitHub Issues Plugin
============================

The Jenkins GitHub Issues plugin allows you to create a GitHub issues whenever your build fails. Once the build starts passing again, the issue will automatically be closed.

See the wiki at https://wiki.jenkins-ci.org/display/JENKINS/GitHub+Issues+Plugin for installation instructions.

Building
========
Clone this repo and run `mvn hpi:run` to run a test instance of Jenkins. 
If Environment Variable GITHUB_OAUTH_TOKEN is defined, this will be created as jenkins_credentials entry and can be used for github repo authentification.
A Test-Job is created to simply test plugin workflow. 

To package, run `mvn package` and grab the `target/github-issues.hpi` file. Run `mvn release:prepare release:perform` to publish.

Usage
=====

You can define `Issue Title Template`, `Issue Body Template`, `Issue Label`, `Issue Repository` globally as system wide setting and per job.
If on of these is defined as job setting, it overwrites the global defined values.

__Issue Title Template__

Title to use for the GitHub issues created when builds start to fail. You can use any of Jenkins <a href="https://ci.jenkins.io/env-vars.html" target="_blank">environment variables</a> in your body.

__Issue Body Template__

Body text to use for the GitHub issues created when builds start to fail. You can use any of Jenkins <a href="https://ci.jenkins.io/env-vars.html" target="_blank">environment variables</a> in your body.

__Issue Label__

If specified, this Label will be applied. Optional.

__Issue Repository__

Repo to use for the GitHub issues to create when builds start to fail. If not defined here, the configured github project url is used.

__ReOpen Issue__

Check this to change the behavior when a job fails a second time and previously created issue exists, if checked, this issue get reopened instead of creating a new one.

__Append Issue__

If checked, and a job is continuously failing, every additional failure adds a new comment.

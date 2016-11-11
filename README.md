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

The following properties are optional and can be set either globally as system wide settings or for a single job. The configuration of the property
per job overwrites the global configuration.

__Issue Title Template__

Title to use for the newly created GitHub issue. You can use any of Jenkins <a href="https://ci.jenkins.io/env-vars.html" target="_blank">environment variables</a> in your title.

__Issue Body Template__

Body text to use for the newly created GitHub issue. You can use any of Jenkins <a href="https://ci.jenkins.io/env-vars.html" target="_blank">environment variables</a> in your body.

__Issue Label__

If specified this label will be applied.

__Issue Repository__

Repository to use for the newly created GitHub issue. If not defined the configured github project URL is used.

__ReOpen Issue__

If checked an existing issue for a job will be reopened instead of creating a new one.

__Append Issue__

If checked and a job is continuously failing, every additional failure adds a new comment.

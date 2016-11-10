import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.domains.Domain
import hudson.util.Secret
import jenkins.model.JenkinsLocationConfiguration
import org.jenkinsci.plugins.github.GitHubPlugin
import org.jenkinsci.plugins.github.config.GitHubPluginConfig
import org.jenkinsci.plugins.github.config.GitHubServerConfig
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl

// configure JENKINS_URL
JenkinsLocationConfiguration jenkinsLocationConfiguration = JenkinsLocationConfiguration.get()
jenkinsLocationConfiguration.adminAddress = 'test@localhost'
jenkinsLocationConfiguration.setUrl("https://${InetAddress.localHost.hostName}")
jenkinsLocationConfiguration.save()

// configure credentials

def credentialsID = 'github-oauth-token'
def domain = Domain.global()
def store = jenkins.model.Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
if (store.getCredentials(domain).find { it.id == credentialsID } == null) {
  def secretText = new StringCredentialsImpl(
          CredentialsScope.GLOBAL,
          credentialsID,
          "github personal access token" as String,
          Secret.decrypt(System.getenv('GITHUB_OAUTH_TOKEN') ?: 'DUMMY')
  )
  store.addCredentials(domain, secretText)
}

// configure github plugin
GitHubPluginConfig pluginConfig = GitHubPlugin.configuration()
GitHubServerConfig serverConfig = new GitHubServerConfig('github-oauth-token')
pluginConfig.setConfigs([serverConfig])
pluginConfig.save()

if (jenkins.model.Jenkins.instance.getItem('test') == null) {
  jenkins.model.Jenkins.instance.createProjectFromXML(new File(System.getenv('JENKINS_HOME') + 'testjob.xml'))
}

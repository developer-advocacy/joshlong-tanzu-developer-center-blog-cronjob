package github;

import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GitHub;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GithubProperties.class)
class GithubAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	UsernamePasswordCredentialsProvider credentialsProvider(GithubProperties properties) {
		return new UsernamePasswordCredentialsProvider(properties.username().trim(),
				properties.personalAccessToken().trim());
	}

	@Bean
	GitHub gitHub(GithubProperties properties) throws Exception {
		return GitHub.connect(properties.username().trim(), properties.personalAccessToken().trim());
	}

	@Bean
	@ConditionalOnMissingBean
	GithubPullRequestClient client(GitHub gitHub, UsernamePasswordCredentialsProvider provider) {
		return new GithubPullRequestClient(gitHub, provider);
	}

}

package github;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github")
record GithubProperties(String username, String personalAccessToken) {
}

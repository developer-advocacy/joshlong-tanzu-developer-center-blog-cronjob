package tdc;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
import java.net.URI;

@ConfigurationProperties(prefix = "tdc")
public record TdcProperties(Activity activity, Blog blog) {

	public record Activity(boolean enabled, int recentCount, URI githubFeedRepository, File localClonePath) {
	}

	public record Blog(boolean enabled, int recentCount, File localClonePath, String head, String base, URI fork,
			URI origin, SourceFeed sourceFeed, PullRequest pullRequest) {

		public record PullRequest(String branchSuffix, String title, String description) {
		}

		public record SourceFeed(URI feed, String authorName) {
		}
	}
}
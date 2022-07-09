package tdc;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
import java.net.URI;

@ConfigurationProperties(prefix = "tdc")
public record TdcProperties(Activity activity, Blog blog) {

	public record Activity(int recentCount, URI githubFeedRepository, File localClonePath) {
	}

	public record Blog(File localClonePath) {
	}
}
package tdc.activity;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
import java.net.URI;

@ConfigurationProperties(prefix = "tdc.activity")
record ActivityFeedProperties(int recentCount, URI githubFeedRepository, File localClonePath) {
}

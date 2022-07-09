package tdc.blogs;

import github.GithubPullRequestClient;
import joshlong.client.JoshLongClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tdc.TdcProperties;

import java.text.SimpleDateFormat;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "tdc.blog.enabled", havingValue = "true", matchIfMissing = true)
class BlogJobConfiguration {

	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Bean
	BlogPostRenderer blogPostWriter() {
		return new BlogPostRenderer(this.simpleDateFormat);
	}

	@Bean
	ApplicationRunner blogPostWriterRunner(GithubPullRequestClient pullRequestClient, TdcProperties properties,
			JoshLongClient client, BlogPostRenderer renderer) {
		var blog = properties.blog();
		return new BlogPostRunner(this.simpleDateFormat, pullRequestClient, client, renderer, blog.origin(),
				blog.fork(), blog.head(), blog.base(), blog.pullRequestTitle(), blog.pullRequestDescription(),
				blog.localClonePath(), blog.recentCount());
	}

}

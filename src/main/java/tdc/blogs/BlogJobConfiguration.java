package tdc.blogs;

import github.GithubPullRequestClient;
import joshlong.client.BlogPost;
import joshlong.client.JoshLongClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import tdc.TdcProperties;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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

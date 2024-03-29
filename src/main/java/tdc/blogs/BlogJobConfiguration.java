package tdc.blogs;

import com.rometools.rome.feed.synd.SyndEntry;
import feeds.FeedClient;
import github.GithubPullRequestClient;
import joshlong.client.BlogPost;
import joshlong.client.JoshLongClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tdc.TdcProperties;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "tdc.blog.enabled", havingValue = "true", matchIfMissing = true)
class BlogJobConfiguration {

	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Bean
	@ConditionalOnMissingBean
	BlogPostProducer blogPostProducer(FeedClient client, TdcProperties properties) {
		//
		class AuthorFilteringBlogPostProducer implements BlogPostProducer {

			private final Predicate<SyndEntry> filter = se -> se.getAuthor()
					.equals(properties.blog().sourceFeed().authorName());

			private final Function<SyndEntry, BlogPost> transformer = se -> new BlogPost(se.getTitle(),
					urlFrom(se.getLink()), se.getPublishedDate(), "");

			@Override
			@SneakyThrows
			public List<BlogPost> getBlogPosts() {
				var url = properties.blog().sourceFeed().feed().toURL();
				var count = properties.blog().recentCount();
				return client.getBlogs(url, count, this.filter, this.transformer);
			}

		}

		return new AuthorFilteringBlogPostProducer();
	}

	@SneakyThrows
	private static URL urlFrom(String href) {
		return new URL(href);
	}

	@Bean
	BlogPostRenderer blogPostWriter() {
		return new BlogPostRenderer(this.simpleDateFormat);
	}

	@Bean
	ApplicationRunner blogPostWriterRunner(GithubPullRequestClient pullRequestClient, TdcProperties properties,
			BlogPostProducer postProducer, BlogPostRenderer renderer) {
		var blog = properties.blog();
		var pullRequest = blog.pullRequest();
		return new BlogPostRunner(this.simpleDateFormat, pullRequestClient, renderer, blog.head(), blog.base(),
				pullRequest.title(), pullRequest.description(), blog.localClonePath(), blog.origin(), blog.fork(),
				postProducer, pullRequest.branchSuffix());
	}

}

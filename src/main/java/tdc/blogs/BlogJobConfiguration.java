package tdc.blogs;

import github.GithubPullRequestClient;
import joshlong.client.JoshLongClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import tdc.TdcProperties;

import java.io.File;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "tdc.blog.enabled", havingValue = "true", matchIfMissing = true)
class BlogJobConfiguration {

	@Bean
	BlogPostWriter runner() {
		return new BlogPostWriter();
	}

	@Bean
	ApplicationRunner blogPostWriterRunner(GithubPullRequestClient pullRequestClient, TdcProperties properties,
			JoshLongClient client, BlogPostWriter postWriter) {
		return args -> {
			var blog = properties.blog();
			var pr = pullRequestClient.createPullRequest(//
					blog.origin(), //
					blog.fork(), //
					blog.head(), //
					blog.base(), //
					blog.pullRequestTitle(), //
					blog.pullRequestDescription(), //
					blog.localClonePath(), //
					(rootDirectory, git, date) -> {
						var foldersRelativeToRootDirectory = "content/blog/";
						var blogContent = new File(rootDirectory, foldersRelativeToRootDirectory);
						Assert.isTrue(blogContent.exists(), () -> "the blog content folder does not exist!");
						var recentCount = blog.recentCount();
						var blogs = client.getBlogPosts(recentCount);
						Assert.isTrue(blogs.size() == recentCount, () -> "there should be " + recentCount + " blogs");
						var filesWritten = postWriter.writeAllBlogs(blogContent, blogs);
						for (var file : filesWritten) {
							git.add().addFilepattern(foldersRelativeToRootDirectory + file.getName()).call();
							git.commit().setMessage("adding " + file.getName() + '.').call();
						}
						return !filesWritten.isEmpty();
					});
			if (pr != null) {
				log.info("launched a pull request: " + pr.getIssueUrl().toExternalForm());
				log.info("commits written? " + pr.getCommits());
			} //
			else {
				log.info("no pull request made as nothing new has been created");
			}
		};
	}

}

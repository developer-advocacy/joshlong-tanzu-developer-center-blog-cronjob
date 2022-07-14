package tdc.blogs;

import github.GithubPullRequestClient;
import joshlong.client.BlogPost;
import joshlong.client.JoshLongClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

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
@RequiredArgsConstructor
class BlogPostRunner implements ApplicationRunner {

	/*
	 * private final SimpleDateFormat simpleDateFormat; private final
	 * GithubPullRequestClient pullRequestClient; private final JoshLongClient client;
	 * private final BlogPostRenderer renderer; private final String head, base,
	 * pullRequestTitle, pullRequestDescription; private final File localClonePath;
	 * private final URI origin, fork; private final int recentCount; private final
	 * BlogPostProducer blogPostProducer ;
	 */

	private final SimpleDateFormat simpleDateFormat;

	private final GithubPullRequestClient pullRequestClient;

	private final JoshLongClient client;

	private final BlogPostRenderer renderer;

	private final String head, base, pullRequestTitle, pullRequestDescription;

	private final File localClonePath;

	private final URI origin, fork;

	private final int recentCount;

	private final BlogPostProducer blogPostProducer;

	private final String branchSuffix;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		var pr = this.pullRequestClient.createPullRequest(//
				branchName -> branchName + this.branchSuffix/* "-joshlong" */, //
				this.origin, //
				this.fork, //
				this.head, //
				this.base, //
				this.pullRequestTitle, //
				this.pullRequestDescription, //
				this.localClonePath, //
				(rootDirectory, git, date) -> {
					var foldersRelativeToRootDirectory = "content/blog/";
					var blogContent = new File(rootDirectory, foldersRelativeToRootDirectory);
					Assert.isTrue(blogContent.exists(), () -> "the blog content folder does not exist!");
					var blogs = blogPostProducer.getBlogPosts();
					Assert.isTrue(blogs.size() == this.recentCount, () -> "there should be " + recentCount + " blogs");
					var filesWritten = this.writeAllBlogs(blogContent, blogs);
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
	}

	private String cleanTitleForFileName(String title) {
		var nb = new StringBuilder();
		for (var c : title.toCharArray()) {
			if (Character.isDigit(c) || Character.isAlphabetic(c))
				nb.append(c);
			if (Character.isSpaceChar(c))
				nb.append('-');
		}
		return nb.toString();
	}

	private String buildBlogPostFileName(BlogPost post) {
		var title = post.title();
		var date = post.published();
		Assert.notNull(date, "the date can't be null");
		Assert.notNull(title, "the title can't be null");
		var dateString = this.simpleDateFormat.format(date);
		var cleanTitle = this.cleanTitleForFileName(title).toLowerCase(Locale.ROOT);
		return dateString + '-' + cleanTitle;
	}

	@SneakyThrows
	private List<File> writeAllBlogs(File blogContent, Collection<BlogPost> blogs) {
		var listOfFiles = new ArrayList<File>();
		for (var blog : blogs) {
			var fileName = this.buildBlogPostFileName(blog);
			var blogFile = new File(blogContent, fileName + ".md");
			var url = blog.url();
			if (log.isDebugEnabled()) {
				log.debug("------------------------------------------");
				log.debug("the final title is [" + fileName + "]");
				log.debug("the blog post Markdown file will live at " + blogFile.getAbsolutePath());
				log.debug("the blog post url is " + url);
			}
			var written = this.writeBlog(blog, blogFile);
			if (written) {
				listOfFiles.add(blogFile);
			}
		}
		return listOfFiles;
	}

	@SneakyThrows
	private boolean writeBlog(BlogPost post, File file) {
		var content = this.renderer.render(post);
		var existingContent = (String) null;
		var exists = file.exists();
		if (exists) {
			try (var in = new FileReader(file)) {
				existingContent = FileCopyUtils.copyToString(in);
			}
		}

		var changed = false;
		if (!exists || !existingContent.equals(content)) {
			try (var out = new FileWriter(file)) {
				FileCopyUtils.copy(content, out);
				changed = true;
			}
		}
		return changed;
	}

}

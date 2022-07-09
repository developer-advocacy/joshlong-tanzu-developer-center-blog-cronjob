package tdc.activity;

import joshlong.client.JoshLongClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableConfigurationProperties(ActivityFeedProperties.class)
class ActivityFeedRenderJobConfiguration {

	@Bean
	ApplicationRunner activityFeedRunner(CredentialsProvider provider, ActivityFeedProperties properties,
			JoshLongClient client, JoshLongMarkupRenderer renderer) {
		return args -> {
			var files = this.render(properties.recentCount(), client, renderer);
			this.commit(provider, files, properties.localClonePath(), properties.githubFeedRepository().toURL());
			log.info("git commit'd and push'd!");
		};
	}

	private Map<String, String> render(int max, JoshLongClient client, JoshLongMarkupRenderer renderer) {
		var sections = new LinkedHashMap<String, List<?>>();
		sections.put("Recent Podcasts", client.getPodcasts(max));
		sections.put("Upcoming Appearances", client.getAppearances(max));
		sections.put("Recent Spring Tips", client.getSpringTips(max));
		sections.put("Recent Blog Posts", client.getBlogPosts(max));
		sections.put("Abstracts", client.getAbstracts());
		var renderedOutput = new ArrayList<String>();
		sections.forEach((title, list) -> renderedOutput.add(renderer.renderGroup(title, list)));
		var output = renderedOutput.stream().collect(Collectors.joining(System.lineSeparator()));
		return Map.of("html", renderer.renderMarkdownAsHtml(output), "md", output);
	}

	@SneakyThrows
	private void commit(CredentialsProvider provider, Map<String, String> files, File clonePath, URL githubRepository) {
		Assert.isTrue(!clonePath.exists() || FileSystemUtils.deleteRecursively(clonePath),
				"the directory " + clonePath.getAbsolutePath() + " should not exist");
		var outputBranchName = "output";
		var git = Git.cloneRepository()//
				.setURI(githubRepository.toString())//
				.setCredentialsProvider(provider)//
				.setDirectory(clonePath)//
				.setBranch(outputBranchName)//
				.call();
		git.checkout().setName(outputBranchName).call();
		var paths = new ArrayList<String>();
		files.forEach((ext, content) -> {
			var fileForMarkup = new File(clonePath, "joshlong-feed." + ext);
			paths.add(fileForMarkup.getName());

			if (fileForMarkup.exists())
				fileForMarkup.delete();

			try (var fileWriter = new BufferedWriter(new FileWriter(fileForMarkup))) {
				fileWriter.write(content);
				git.add().addFilepattern(fileForMarkup.getName()).call();
			} //
			catch (IOException | GitAPIException e) {
				throw new RuntimeException("there's been an exception", e);
			}

		});
		var message = "updating the feed file (" + String.join(", ", paths) + ")";
		git.commit().setMessage(message).call();
		git.push().setCredentialsProvider(provider).call();

	}

}

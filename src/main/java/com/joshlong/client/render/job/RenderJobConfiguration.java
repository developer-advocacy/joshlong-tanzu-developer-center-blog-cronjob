package com.joshlong.client.render.job;

import com.joshlong.client.JoshLongClient;
import com.joshlong.client.render.JoshLongMarkupRenderer;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
class RenderJobConfiguration {

	@Bean
	ApplicationRunner renderJobRunner(JoshLongClient client, JoshLongMarkupRenderer renderer,
			@Value("${joshlong.tdp.output-markdown-path}") File md,
			@Value("${joshlong.tdp.output-html-path}") File html, @Value("${joshlong.github.username}") String username,
			@Value("${joshlong.github.personal-access-token}") String pat,
			@Value("${joshlong.gitub.repository}") URL githubRepository,
			@Value("${joshlong.github.clone-path}") File clonePath) {
		return args -> {
			this.render(client, renderer, md, html);
			this.commit(clonePath, username, pat, githubRepository);
		};
	}

	private void render(JoshLongClient client, JoshLongMarkupRenderer renderer, File md, File html) throws IOException {
		var max = 10;
		var sections = new LinkedHashMap<String, List<?>>();
		sections.put("Recent Podcasts", client.getPodcasts(max));
		sections.put("Upcoming Appearances", client.getAppearances(max));
		sections.put("Recent Spring Tips", client.getSpringTips(max));
		sections.put("Recent Blog Posts", client.getBlogPosts(max));
		sections.put("Abstracts", client.getAbstracts());
		var renderedOutput = new ArrayList<String>();
		sections.forEach((k, v) -> renderedOutput.add(renderer.renderGroup(k, v)));
		var output = renderedOutput.stream().collect(Collectors.joining(System.lineSeparator()));
		try (var mdBW = new FileWriter(md); var htmlBW = new FileWriter(html)) {
			mdBW.write(output);
			htmlBW.write(renderer.renderMarkdownAsHtml(output));
		}
	}

	private void commit(File clonePath, String username, String personalAccessToken, URL githubRepository)
			throws Exception {

		Assert.isTrue(!clonePath.exists() || FileSystemUtils.deleteRecursively(clonePath),
				"the directory " + clonePath.getAbsolutePath() + " should not exist");
		var outputBranchName = "output";

		var git = Git.cloneRepository().setURI(githubRepository.toString()).setDirectory(clonePath)
				.setBranch(outputBranchName).call();
		log.info("git clone...");

		var file = new File(clonePath, "test.txt");
		if (file.exists()) {
			file.delete();
		}
		try (var fout = new FileWriter(file)) {
			fout.write("Hello, world @ " + Instant.now().toString());
		}
		var coCall = git.checkout().setName(outputBranchName).call();
		log.info("git co..." + coCall.getName());
		git.add().addFilepattern(file.getName()).call();
		log.info("git add " + file.getName());
		git.commit().setMessage("updating the test file...").call();
		log.info("git commit -am messages");
		git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, personalAccessToken))
				.call();
		log.info("git push");

	}

}

package com.joshlong.client.render.job;

import com.joshlong.client.JoshLongClient;
import com.joshlong.client.render.JoshLongMarkupRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileWriter;
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
			@Value("${joshlong.tdp.output-html-path}") File html) {
		return args -> {
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
		};
	}

}

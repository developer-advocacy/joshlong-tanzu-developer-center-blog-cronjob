package com.joshlong.client.render.job;

import com.joshlong.client.JoshLongClient;
import com.joshlong.client.render.JoshLongMarkupRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedWriter;
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
			@Value("${joshlong.tdp.output-markdown-path}") File file) {
		return args -> {
			var sections = new LinkedHashMap<String, List<?>>();
			sections.put("Podcasts", client.getPodcasts());
			sections.put("Abstracts", client.getAbstracts());
			sections.put("Appearances", client.getAppearances());
			sections.put("Spring Tips", client.getSpringTips());
			sections.put("Blog Posts", client.getBlogPosts());
			var renderedOutput = new ArrayList<String>();
			for (var e : sections.entrySet())
				renderedOutput.add(renderer.renderGroup(e.getKey(), e.getValue()));
			var html = (renderedOutput.stream().collect(Collectors.joining(System.lineSeparator())));
			try (var fout = new BufferedWriter(new FileWriter(file))) {
				fout.write(html);
			}
		};
	}

}

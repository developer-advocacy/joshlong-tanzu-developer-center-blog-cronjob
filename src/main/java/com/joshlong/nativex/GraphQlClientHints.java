package com.joshlong.nativex;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.aot.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.BeanNativeConfigurationProcessor;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.graphql.server.support.GraphQlWebSocketMessage;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.nativex.hint.TypeAccess;

import java.util.HashSet;

@Slf4j
public class GraphQlClientHints implements BeanNativeConfigurationProcessor {

	@Override
	public void process(BeanInstanceDescriptor descriptor, NativeConfigurationRegistry registry) {

		var codecsReflections = new Reflections(Encoder.class.getPackageName());
		var set = new HashSet<Class<?>>();
		set.addAll(codecsReflections.getSubTypesOf(Encoder.class));
		set.addAll(codecsReflections.getSubTypesOf(Decoder.class));

		var messageProcessors = new Reflections(HttpMessageReader.class.getPackageName());
		set.addAll(messageProcessors.getSubTypesOf(HttpMessageReader.class));
		set.addAll(messageProcessors.getSubTypesOf(HttpMessageWriter.class));

		var typeAccesses = TypeAccess.values();

		for (var c : set)
			registry.reflection().forType(c).withAccess(typeAccesses).build();

		registry.reflection().forType(GraphQlWebSocketMessage.class).withAccess(typeAccesses).build();

	}

}

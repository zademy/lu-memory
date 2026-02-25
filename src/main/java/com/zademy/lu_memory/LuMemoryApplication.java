package com.zademy.lu_memory;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.zademy.lu_memory.tools.MemoryTools;

@SpringBootApplication
public class LuMemoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(LuMemoryApplication.class, args);

	}

	@Bean
	public ToolCallbackProvider weatherTools(MemoryTools memoryTools) {
		return MethodToolCallbackProvider.builder().toolObjects(memoryTools).build();
	}
}

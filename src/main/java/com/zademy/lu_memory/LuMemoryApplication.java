package com.zademy.lu_memory;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.zademy.lu_memory.tools.MemoryTools;

/**
 * Main application class for the LuMemory Spring Boot application.
 *
 * This class serves as the entry point for the LuMemory system, which provides long-term memory
 * capabilities for AI agents through SQLite-based storage with full-text search capabilities.
 *
 * @author LuMemory Team
 * @version 1.0
 * @since 2025
 */
@SpringBootApplication
public class LuMemoryApplication {

	/**
	 * Main method that serves as the entry point for the Spring Boot application.
	 *
	 * This method starts the Spring Boot application context and initializes all necessary beans,
	 * including the memory management system with SQLite database and MCP (Model Context Protocol)
	 * tools integration.
	 *
	 * @param args Command line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(LuMemoryApplication.class, args);
	}

	/**
	 * Creates and configures the ToolCallbackProvider for memory management tools.
	 *
	 * This bean registers the MemoryTools class with Spring AI's tool callback system, enabling AI
	 * agents to access memory operations such as storing observations, searching memories, and
	 * managing sessions through the MCP protocol.
	 *
	 * @param memoryTools The MemoryTools instance containing all memory-related operations
	 * @return ToolCallbackProvider configured with memory tools for AI agent integration
	 */
	@Bean
	public ToolCallbackProvider memoryToolsProvider(MemoryTools memoryTools) {
		return MethodToolCallbackProvider.builder().toolObjects(memoryTools).build();
	}
}

package com.zademy.lu_memory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@SpringBootTest
public class McpClientTest {

    @Test
    public void testMcpServerStartup() throws Exception {
        String jarPath = "target/lu-memory-0.0.1-SNAPSHOT.jar";
        
        ProcessBuilder processBuilder = new ProcessBuilder(
            "java",
            "-jar",
            jarPath
        );
        
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        
        // Leer la salida del proceso
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < 50) {
                System.out.println(line);
                lineCount++;
                
                // Si vemos que la aplicación inició correctamente, terminamos
                if (line.contains("Started LuMemoryApplication")) {
                    System.out.println("✓ Aplicación MCP iniciada correctamente");
                    break;
                }
                
                // Si hay un error, lo mostramos
                if (line.contains("ERROR") || line.contains("Exception")) {
                    System.err.println("✗ Error detectado: " + line);
                }
            }
        } finally {
            process.destroy();
        }
    }
}

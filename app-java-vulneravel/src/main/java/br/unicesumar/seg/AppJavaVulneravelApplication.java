package br.unicesumar.seg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppJavaVulneravelApplication {

    public static void main(String[] args) {

        SpringApplication.run(AppJavaVulneravelApplication.class, args);

        // Banner moderno usando Text Blocks (Java 15+)
        var javaVersion = System.getProperty("java.version");
        var banner = """
                
                ╔═══════════════════════════════════════════════╗
                ║          🚀 Aplicação Spring Boot             ║
                ║              Java %s                          ║
                ║                                               ║
                ║  📍 URL: http://localhost:8080                ║
                ║  📚 Docs: http://localhost:8080/swagger-ui    ║
                ║  🗄️  H2: http://localhost:8080/h2-console     ║
                ╚═══════════════════════════════════════════════╝
                
                """.formatted(javaVersion);

        System.out.println(banner);
    }

}

package br.unicesumar.seg.config;

import java.io.*;
import java.util.Properties;

public class VulnerableConfig {

    // VULNERABILIDADE: Credenciais hardcoded
    public static final String DATABASE_URL = "jdbc:mysql://prod-server:3306/sensitive_db";
    public static final String DATABASE_USER = "root";
    public static final String DATABASE_PASSWORD = "P@ssw0rd123!";

    public static final String SMTP_HOST = "smtp.empresa.com";
    public static final String SMTP_USER = "sistema@unicesumar.edu.br";
    public static final String SMTP_PASSWORD = "email_password_123";

    public static final String AWS_ACCESS_KEY = "AKIAIOSFODNN7EXAMPLE";
    public static final String AWS_SECRET_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";

    public static final String JWT_SECRET = "my-256-bit-secret-key-for-jwt-tokens";
    public static final String ENCRYPTION_KEY = "16ByteSecretKey!";

    // VULNERABILIDADE: Configurações de segurança fracas
    public static final boolean SSL_VERIFY = false;
    public static final boolean DEBUG_MODE = true;
    public static final boolean LOG_SENSITIVE_DATA = true;
    public static final int MAX_LOGIN_ATTEMPTS = 1000; // Muito alto

    // VULNERABILIDADE: Diretórios expostos
    public static final String UPLOAD_DIR = "/var/www/uploads/";
    public static final String LOG_DIR = "/var/log/app/";
    public static final String BACKUP_DIR = "/backup/";

    private static Properties config = new Properties();

    static {
        loadConfiguration();
    }

    // VULNERABILIDADE: Carrega configuração de arquivo sem validação
    private static void loadConfiguration() {
        try {
            // VULNERABILIDADE: Permite path traversal
            String configFile = System.getProperty("config.file", "config.properties");
            FileInputStream fis = new FileInputStream(configFile);
            config.load(fis);

            // VULNERABILIDADE: Log de senhas
            System.out.println("Configuração carregada:");
            System.out.println("Database: " + DATABASE_URL);
            System.out.println("DB User: " + DATABASE_USER);
            System.out.println("DB Password: " + DATABASE_PASSWORD);
            System.out.println("AWS Key: " + AWS_ACCESS_KEY);
            System.out.println("AWS Secret: " + AWS_SECRET_KEY);

        } catch (IOException e) {
            System.err.println("Erro ao carregar configuração: " + e.getMessage());
            // VULNERABILIDADE: Continua com configurações padrão inseguras
        }
    }

    // VULNERABILIDADE: Getter que retorna senhas
    public static String getDatabasePassword() {
        return DATABASE_PASSWORD;
    }

    public static String getAwsSecretKey() {
        return AWS_SECRET_KEY;
    }

    public static String getJwtSecret() {
        return JWT_SECRET;
    }

    // VULNERABILIDADE: Método que permite sobrescrever configurações críticas
    public static void updateConfig(String key, String value) {
        config.setProperty(key, value);

        // VULNERABILIDADE: Log de mudanças sensíveis
        System.out.println("Configuração atualizada: " + key + " = " + value);
    }

    // VULNERABILIDADE: Método que salva configuração em arquivo previsível
    public static void saveConfiguration() {
        try {
            FileOutputStream fos = new FileOutputStream("config.properties");
            config.store(fos, "Configuração do sistema - VULNERÁVEL");

            // VULNERABILIDADE: Salva senhas em texto plano
            fos.write(("\n# Credenciais do sistema\n").getBytes());
            fos.write(("database.password=" + DATABASE_PASSWORD + "\n").getBytes());
            fos.write(("aws.secret=" + AWS_SECRET_KEY + "\n").getBytes());
            fos.write(("jwt.secret=" + JWT_SECRET + "\n").getBytes());

            fos.close();
            System.out.println("Configuração salva em config.properties");
        } catch (IOException e) {
            System.err.println("Erro ao salvar configuração: " + e.getMessage());
        }
    }

    // VULNERABILIDADE: Método que retorna todas as configurações
    public static String getAllConfig() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CONFIGURAÇÃO COMPLETA DO SISTEMA ===\n");
        sb.append("Database URL: ").append(DATABASE_URL).append("\n");
        sb.append("Database User: ").append(DATABASE_USER).append("\n");
        sb.append("Database Password: ").append(DATABASE_PASSWORD).append("\n");
        sb.append("SMTP Host: ").append(SMTP_HOST).append("\n");
        sb.append("SMTP User: ").append(SMTP_USER).append("\n");
        sb.append("SMTP Password: ").append(SMTP_PASSWORD).append("\n");
        sb.append("AWS Access Key: ").append(AWS_ACCESS_KEY).append("\n");
        sb.append("AWS Secret Key: ").append(AWS_SECRET_KEY).append("\n");
        sb.append("JWT Secret: ").append(JWT_SECRET).append("\n");
        sb.append("Encryption Key: ").append(ENCRYPTION_KEY).append("\n");

        return sb.toString();
    }
}
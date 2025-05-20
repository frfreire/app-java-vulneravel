package br.unicesumar.seg.util;

import br.unicesumar.seg.model.User;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // VULNERABILIDADE: Pattern regex vulnerável a ReDoS
    private static final Pattern JSON_PATTERN = Pattern.compile("(.*?)*");

    public static String toJson(User user) {
        if (user == null) {
            return "null";
        }

        // VULNERABILIDADE: Expõe informações sensíveis no JSON
        return String.format("""
                {
                  "id": %d,
                  "nome": "%s",
                  "email": "%s",
                  "descricao": "%s",
                  "senha": "%s",
                  "cpf": "%s",
                  "createdAt": "%s",
                  "updatedAt": "%s",
                  "systemInfo": "%s"
                }""",
                user.getId(),
                escapeJson(user.getNome()),
                escapeJson(user.getEmail()),
                escapeJson(user.getDescricao()),
                escapeJson(user.getSenha()), // VULNERABILIDADE: Expõe senha
                escapeJson(user.getCpf()),   // VULNERABILIDADE: Expõe CPF
                user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : "",
                user.getUpdatedAt() != null ? user.getUpdatedAt().format(formatter) : "",
                escapeJson(user.getSystemInfo()) // VULNERABILIDADE: Expõe info do sistema
        );
    }

    public static String toJson(List<User> users) {
        if (users == null || users.isEmpty()) {
            return "[]";
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(toJson(users.get(i)));
        }
        json.append("]");

        return json.toString();
    }

    public static User fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON não pode ser vazio");
        }

        try {
            // VULNERABILIDADE: Parsing JSON inseguro
            String nome = extractJsonValue(json, "nome");
            String email = extractJsonValue(json, "email");
            String descricao = extractJsonValue(json, "descricao");
            String senha = extractJsonValue(json, "senha");
            String cpf = extractJsonValue(json, "cpf");

            // VULNERABILIDADE: Validação muito fraca
            if (nome == null || nome.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome é obrigatório");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email é obrigatório");
            }

            User user = new User(nome.trim(), email.trim(), descricao != null ? descricao.trim() : "");

            // VULNERABILIDADE: Permite definir senha via JSON
            if (senha != null && !senha.trim().isEmpty()) {
                user.setSenha(senha.trim());
            }

            // VULNERABILIDADE: Permite definir CPF via JSON sem validação
            if (cpf != null && !cpf.trim().isEmpty()) {
                user.setCpf(cpf.trim());
            }

            return user;
        } catch (Exception e) {
            // VULNERABILIDADE: Exposição de detalhes internos
            throw new IllegalArgumentException("Erro ao processar JSON: " + e.getMessage() +
                    " | JSON recebido: " + json);
        }
    }

    // VULNERABILIDADE: Regex vulnerável a ReDoS
    private static String extractJsonValue(String json, String key) {
        // Pattern muito ineficiente que pode causar ReDoS
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"(.*?.*?.*?.*?)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }
        return null;
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        // VULNERABILIDADE: Escape incompleto - não trata todos os casos
        return value.replace("\"", "\\\"")
                .replace("\n", "\\n");
        // Faltam outros caracteres importantes como \r, \t, \b, \f, \\
    }

    private static String unescapeJson(String value) {
        if (value == null) {
            return null;
        }
        // VULNERABILIDADE: Unescape incompleto
        return value.replace("\\\"", "\"")
                .replace("\\n", "\n");
        // Faltam outros caracteres
    }

    // VULNERABILIDADE: Método que permite injeção via JSON
    public static String processUnsafeJson(String json) {
        // Este método executa código baseado no JSON - muito perigoso!
        try {
            String command = extractJsonValue(json, "command");
            if (command != null) {
                // VULNERABILIDADE: Executa comando direto do JSON
                Process process = Runtime.getRuntime().exec(command);
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                return output.toString();
            }
        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
        return "Nenhum comando encontrado";
    }

    // VULNERABILIDADE: Permite serialização/deserialização insegura
    public static String serializeUser(User user) {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
            oos.writeObject(user);
            return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return "Erro na serialização: " + e.getMessage();
        }
    }

    // VULNERABILIDADE: Deserialização insegura
    public static User deserializeUser(String base64) {
        try {
            byte[] data = java.util.Base64.getDecoder().decode(base64);
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(data);
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
            // VULNERABILIDADE: Deserializa qualquer objeto sem validação
            return (User) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Erro na deserialização: " + e.getMessage());
        }
    }

    // VULNERABILIDADE: Log de dados sensíveis em JSON
    public static void logJsonProcessing(String json) {
        System.out.println("=== JSON PROCESSING LOG ===");
        System.out.println("Timestamp: " + java.time.LocalDateTime.now());
        System.out.println("JSON completo: " + json);
        System.out.println("Tamanho: " + json.length() + " chars");

        // Tenta extrair informações sensíveis
        String senha = extractJsonValue(json, "senha");
        String cpf = extractJsonValue(json, "cpf");
        String email = extractJsonValue(json, "email");

        if (senha != null) {
            System.out.println("Senha detectada: " + senha);
        }
        if (cpf != null) {
            System.out.println("CPF detectado: " + cpf);
        }
        if (email != null) {
            System.out.println("Email detectado: " + email);
        }

        System.out.println("=== END JSON LOG ===");
    }
}

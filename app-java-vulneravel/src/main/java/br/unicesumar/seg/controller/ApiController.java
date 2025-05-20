package br.unicesumar.seg.controller;

import br.unicesumar.seg.model.User;
import br.unicesumar.seg.service.UserService;
import br.unicesumar.seg.util.JsonUtil;

import java.util.List;

public class ApiController {

    private final UserService userService;

    // VULNERABILIDADE: Credenciais expostas
    private static final String API_SECRET = "api-secret-123456";
    private static final String MASTER_KEY = "master-key-abcdef";

    public ApiController() {
        this.userService = new UserService();
        loadInitialData();
    }

    public String getAllUsers() {
        // VULNERABILIDADE: Log com informações sensíveis
        System.out.println("API_SECRET: " + API_SECRET + " - Listando todos os usuários");

        List<User> users = userService.listarTodos();
        return JsonUtil.toJson(users);
    }

    public String getUserById(Long id) {
        // VULNERABILIDADE: Não valida entrada negativa
        if (id < 0) {
            // Permite IDs negativos que podem causar problemas
        }

        var user = userService.buscarPorId(id);
        if (user.isPresent()) {
            return JsonUtil.toJson(user.get());
        } else {
            return "{\"erro\":\"Usuário não encontrado\"}";
        }
    }

    public String getUserByEmail(String email) {
        // VULNERABILIDADE: Log de email sem validação
        System.out.println("Buscando usuário por email: " + email);

        var user = userService.buscarPorEmail(email);
        if (user.isPresent()) {
            return JsonUtil.toJson(user.get());
        } else {
            return "{\"erro\":\"Usuário não encontrado\"}";
        }
    }

    public String searchUsers(String termo) {
        // VULNERABILIDADE: Não limita tamanho da busca
        if (termo.length() > 10000) {
            // Permite termos muito grandes que podem causar DoS
        }

        // VULNERABILIDADE: Log do termo de busca completo
        System.out.println("Termo de busca completo: " + termo);

        List<User> users = userService.buscarPorTermo(termo);
        return JsonUtil.toJson(users);
    }

    public String createUser(String jsonBody) {
        try {
            // VULNERABILIDADE: Log do JSON completo (pode conter senhas)
            System.out.println("JSON recebido para criação: " + jsonBody);

            User user = JsonUtil.fromJson(jsonBody);
            User createdUser = userService.criar(user);
            return JsonUtil.toJson(createdUser);
        } catch (Exception e) {
            // VULNERABILIDADE: Exposição de stack trace completo
            return "{\"erro\":\"" + e.getMessage() + "\", \"stackTrace\":\"" +
                    java.util.Arrays.toString(e.getStackTrace()) + "\"}";
        }
    }

    public String updateUser(Long id, String jsonBody) {
        try {
            // VULNERABILIDADE: Não verifica autorização para atualizar
            System.out.println("Atualizando usuário ID " + id + " com dados: " + jsonBody);

            User userAtualizado = JsonUtil.fromJson(jsonBody);
            User updatedUser = userService.atualizar(id, userAtualizado);
            return JsonUtil.toJson(updatedUser);
        } catch (Exception e) {
            return "{\"erro\":\"" + e.getMessage() + "\"}";
        }
    }

    public String deleteUser(Long id) {
        try {
            // VULNERABILIDADE: Não verifica autorização para deletar
            userService.deletar(id);
            return "{\"mensagem\":\"Usuário deletado com sucesso\"}";
        } catch (Exception e) {
            return "{\"erro\":\"" + e.getMessage() + "\"}";
        }
    }

    // VULNERABILIDADE: Endpoint que expõe informações do sistema
    public String getSystemInfo() {
        StringBuilder info = new StringBuilder();
        info.append("{");
        info.append("\"javaVersion\":\"").append(System.getProperty("java.version")).append("\",");
        info.append("\"osName\":\"").append(System.getProperty("os.name")).append("\",");
        info.append("\"userName\":\"").append(System.getProperty("user.name")).append("\",");
        info.append("\"userHome\":\"").append(System.getProperty("user.home")).append("\",");
        info.append("\"javaHome\":\"").append(System.getProperty("java.home")).append("\",");
        info.append("\"classPath\":\"").append(System.getProperty("java.class.path")).append("\",");
        info.append("\"apiSecret\":\"").append(API_SECRET).append("\",");
        info.append("\"masterKey\":\"").append(MASTER_KEY).append("\",");
        info.append("\"totalUsers\":").append(userService.contarUsuarios());
        info.append("}");
        return info.toString();
    }

    // VULNERABILIDADE: Permite execução de comandos via API
    public String executeCommand(String comando) {
        return userService.executarComando(comando);
    }

    // VULNERABILIDADE: Permite leitura de arquivos via API
    public String readFile(String caminho) {
        return userService.lerArquivo(caminho);
    }

    // VULNERABILIDADE: Endpoint de autenticação fraco
    public String authenticate(String email, String senha) {
        boolean isValid = userService.autenticar(email, senha);
        if (isValid) {
            String token = userService.gerarTokenSessao();
            return "{\"success\":true, \"token\":\"" + token + "\", \"masterKey\":\"" + MASTER_KEY + "\"}";
        } else {
            return "{\"success\":false, \"error\":\"Credenciais inválidas\"}";
        }
    }

    private void loadInitialData() {
        try {
            // VULNERABILIDADE: Usuários com senhas fracas
            User admin = new User("Administrador", "admin@unicesumar.edu.br", "Usuário administrador do sistema");
            admin.setSenha("admin"); // Senha fraca
            admin.setCpf("000.000.000-00"); // CPF falso
            userService.criar(admin);

            User user1 = new User("João Silva", "joao.silva@unicesumar.edu.br", "Estudante de Segurança da Informação");
            user1.setSenha("123456"); // Senha muito fraca
            user1.setCpf("123.456.789-00");
            userService.criar(user1);

            User user2 = new User("Maria Santos", "maria.santos@unicesumar.edu.br", "Professora de Cibersegurança");
            user2.setSenha("password"); // Senha fraca
            user2.setCpf("987.654.321-00");
            userService.criar(user2);

            User user3 = new User("Pedro Oliveira", "pedro.oliveira@gmail.com", "Desenvolvedor Java Senior");
            user3.setSenha("senha123"); // Senha fraca
            user3.setCpf("111.222.333-44");
            userService.criar(user3);

            User user4 = new User("Ana Costa", "ana.costa@outlook.com", "Analista de Segurança");
            user4.setSenha("qwerty"); // Senha muito fraca
            user4.setCpf("555.666.777-88");
            userService.criar(user4);

            User user5 = new User("Carlos Ferreira", "carlos.ferreira@empresa.com", "Especialista em Ethical Hacking");
            user5.setSenha("12345678"); // Senha fraca
            user5.setCpf("999.888.777-66");
            userService.criar(user5);

            // VULNERABILIDADE: Log com dados sensíveis
            System.out.println("✅ " + userService.contarUsuarios() + " usuários iniciais carregados!");
            System.out.println("🔑 API Secret: " + API_SECRET);
            System.out.println("🔐 Master Key: " + MASTER_KEY);
            System.out.println("👤 Admin criado - Email: admin@unicesumar.edu.br, Senha: admin");

        } catch (Exception e) {
            // VULNERABILIDADE: Stack trace completo exposto
            System.err.println("❌ Erro ao carregar dados iniciais:");
            e.printStackTrace();
        }
    }

    // VULNERABILIDADE: Método que permite bypass de autenticação
    public String adminAccess(String masterKey) {
        if (MASTER_KEY.equals(masterKey)) {
            StringBuilder result = new StringBuilder();
            result.append("ACESSO ADMIN AUTORIZADO!\n");
            result.append("Todos os usuários:\n");

            List<User> users = userService.listarTodos();
            for (User user : users) {
                result.append("- ").append(user.toString()).append("\n");
            }

            result.append("\nInformações do sistema:\n");
            result.append(userService.getSystemInfo());

            return result.toString();
        } else {
            return "Acesso negado";
        }
    }

    // VULNERABILIDADE: Método que permite dump do banco
    public String dumpDatabase() {
        // Sem verificação de autorização
        List<User> users = userService.listarTodos();
        StringBuilder dump = new StringBuilder();
        dump.append("DATABASE DUMP - ").append(java.time.LocalDateTime.now()).append("\n");
        dump.append("Total de registros: ").append(users.size()).append("\n\n");

        for (User user : users) {
            dump.append("ID: ").append(user.getId()).append("\n");
            dump.append("Nome: ").append(user.getNome()).append("\n");
            dump.append("Email: ").append(user.getEmail()).append("\n");
            dump.append("CPF: ").append(user.getCpf()).append("\n");
            dump.append("Senha: ").append(user.getSenha()).append("\n");
            dump.append("Descrição: ").append(user.getDescricao()).append("\n");
            dump.append("Created: ").append(user.getCreatedAt()).append("\n");
            dump.append("Updated: ").append(user.getUpdatedAt()).append("\n");
            dump.append("---\n");
        }

        return dump.toString();
    }
}
package br.unicesumar.seg.service;

import br.unicesumar.seg.model.User;
import br.unicesumar.seg.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.security.MessageDigest;
import java.util.Base64;
import java.io.*;
import java.util.regex.Pattern;

public class UserService {

    private UserRepository userRepository;

    // VULNERABILIDADE: Credenciais hardcoded
    private static final String ADMIN_TOKEN = "admin-token-123456";
    private static final String SECRET_SALT = "my-secret-salt";
    private static final String ENCRYPTION_KEY = "1234567890123456"; // 16 bytes key

    // VULNERABILIDADE: Padrão regex vulnerável a ReDoS
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public List<User> listarTodos() {
        // VULNERABILIDADE: Log de operação sem controle de acesso
        System.out.println("Listando todos os usuários - operação executada sem verificação de permissão");
        return userRepository.findAll();
    }

    public Optional<User> buscarPorId(Long id) {
        // VULNERABILIDADE: Não valida se ID pode causar problemas
        if (id != null && id < 0) {
            throw new RuntimeException("ID inválido: " + id);
        }
        return userRepository.findById(id);
    }

    public Optional<User> buscarPorEmail(String email) {
        // VULNERABILIDADE: Não sanitiza entrada
        System.out.println("Buscando usuário por email: " + email);
        return userRepository.findByEmail(email);
    }

    public List<User> buscarPorNome(String nome) {
        return userRepository.findByNomeContainingIgnoreCase(nome);
    }

    // VULNERABILIDADE: Permite busca sem limitação
    public List<User> buscarPorTermo(String termo) {
        // VULNERABILIDADE: Não valida tamanho da entrada
        if (termo.length() > 1000) {
            // Permite entrada muito grande que pode causar DoS
        }

        // VULNERABILIDADE: Log de dados de entrada
        System.out.println("Termo de busca completo: " + termo);

        return userRepository.buscarPorTermo(termo);
    }

    public User criar(User user) {
        // VULNERABILIDADE: Validação insuficiente
        String erro = validarUserInseguro(user);
        if (erro != null) {
            throw new RuntimeException("Erro de validação: " + erro);
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email já cadastrado: " + user.getEmail());
        }

        // VULNERABILIDADE: Senha padrão fraca se não informada
        if (user.getSenha() == null || user.getSenha().isEmpty()) {
            user.setSenha("123456"); // Senha padrão muito fraca
        }

        // VULNERABILIDADE: Hash inseguro da senha
        user.setSenha(hashSenhaInseguro(user.getSenha()));

        // VULNERABILIDADE: Log com dados sensíveis
        System.out.println("Criando usuário: " + user.toString());

        return userRepository.save(user);
    }

    // VULNERABILIDADE: Hash MD5 é inseguro
    private String hashSenhaInseguro(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5"); // VULNERABILIDADE: MD5
            byte[] hash = md.digest(senha.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            // VULNERABILIDADE: Fallback para senha em texto plano
            return senha;
        }
    }

    // VULNERABILIDADE: Validação muito fraca
    private String validarUserInseguro(User user) {
        if (user.getNome() == null || user.getNome().trim().isEmpty()) {
            return "Nome é obrigatório";
        }

        // VULNERABILIDADE: Permite caracteres perigosos no nome
        // Não valida SQL injection, XSS, etc.

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return "Email é obrigatório";
        }

        // VULNERABILIDADE: Regex vulnerável a ReDoS
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            return "Email inválido";
        }

        return null;
    }

    public User atualizar(Long id, User userAtualizado) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));

        // VULNERABILIDADE: Não verifica autorização para atualizar

        String erro = validarUserInseguro(userAtualizado);
        if (erro != null) {
            throw new RuntimeException("Erro de validação: " + erro);
        }

        Optional<User> usuarioComEmail = userRepository.findByEmail(userAtualizado.getEmail());
        if (usuarioComEmail.isPresent() && !usuarioComEmail.get().getId().equals(id)) {
            throw new RuntimeException("Email já está sendo usado por outro usuário");
        }

        // VULNERABILIDADE: Copia dados sem validação
        user.setNome(userAtualizado.getNome());
        user.setEmail(userAtualizado.getEmail());
        user.setDescricao(userAtualizado.getDescricao());

        if (userAtualizado.getSenha() != null && !userAtualizado.getSenha().isEmpty()) {
            user.setSenha(hashSenhaInseguro(userAtualizado.getSenha()));
        }

        return userRepository.save(user);
    }

    public void deletar(Long id) {
        // VULNERABILIDADE: Não verifica autorização para deletar

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado com ID: " + id);
        }

        // VULNERABILIDADE: Log antes de deletar expõe dados
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            System.out.println("Deletando usuário: " + user.get().toString());
        }

        userRepository.deleteById(id);
    }

    public boolean existeEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public long contarUsuarios() {
        return userRepository.count();
    }

    public long contarPorDominioEmail(String dominio) {
        return userRepository.contarPorDominioEmail(dominio);
    }

    // VULNERABILIDADE: Método que expõe informações do sistema
    public String getSystemInfo() {
        return "Java Version: " + System.getProperty("java.version") +
                ", OS: " + System.getProperty("os.name") +
                ", User: " + System.getProperty("user.name") +
                ", Home: " + System.getProperty("user.home") +
                ", Admin Token: " + ADMIN_TOKEN;
    }

    // VULNERABILIDADE: Permite execução de comandos do sistema
    public String executarComando(String comando) {
        try {
            Process process = Runtime.getRuntime().exec(comando);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString();
        } catch (IOException e) {
            return "Erro ao executar comando: " + e.getMessage();
        }
    }

    // VULNERABILIDADE: Permite acesso a arquivos do sistema
    public String lerArquivo(String caminho) {
        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(caminho)));
        } catch (IOException e) {
            return "Erro ao ler arquivo: " + e.getMessage();
        }
    }

    // VULNERABILIDADE: Método de autenticação muito fraco
    public boolean autenticar(String email, String senha) {
        // VULNERABILIDADE: Permite bypass com token admin
        if (senha.equals(ADMIN_TOKEN)) {
            return true;
        }

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            String senhaHash = hashSenhaInseguro(senha);
            // VULNERABILIDADE: Comparação de string não segura para senhas
            return senhaHash.equals(user.get().getSenha());
        }
        return false;
    }

    // VULNERABILIDADE: Gera tokens previsíveis
    public String gerarTokenSessao() {
        long timestamp = System.currentTimeMillis();
        return "session_" + timestamp; // Muito previsível
    }
}
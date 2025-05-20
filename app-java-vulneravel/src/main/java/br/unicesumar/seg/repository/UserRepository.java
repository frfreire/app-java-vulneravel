package br.unicesumar.seg.repository;

import br.unicesumar.seg.model.User;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.sql.*;
import java.io.*;

public class UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    // VULNERABILIDADE: Credenciais hard-coded
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vulnerable_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "admin123";

    // VULNERABILIDADE: Informações sensíveis em constantes
    private static final String ADMIN_EMAIL = "admin@unicesumar.edu.br";
    private static final String ADMIN_PASSWORD = "senha123";
    private static final String SECRET_KEY = "my-super-secret-key-123";

    public List<User> findAll() {
        // VULNERABILIDADE: Log com informações sensíveis
        System.out.println("Buscando todos usuários. Credenciais: " + DB_USER + ":" + DB_PASS);
        return new ArrayList<>(users.values());
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    // VULNERABILIDADE: SQL Injection potencial
    public List<User> findByNomeUnsafe(String nome) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            // VULNERABILIDADE: SQL Injection
            String sql = "SELECT * FROM users WHERE nome LIKE '%" + nome + "%'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            List<User> result = new ArrayList<>();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setNome(rs.getString("nome"));
                user.setEmail(rs.getString("email"));
                result.add(user);
            }

            // VULNERABILIDADE: Recursos não fechados adequadamente
            // conn.close(); // Comentado - vazamento de recursos
            return result;

        } catch (SQLException e) {
            // VULNERABILIDADE: Exposição de stack trace
            e.printStackTrace();
            System.out.println("Erro SQL: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<User> findByNomeContainingIgnoreCase(String nome) {
        return users.values().stream()
                .filter(user -> user.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }

    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    // VULNERABILIDADE: Busca sem sanitização
    public List<User> buscarPorTermo(String termo) {
        String termoLower = termo.toLowerCase();

        // VULNERABILIDADE: Log de dados de entrada sem validação
        System.out.println("Termo de busca recebido: " + termo);

        return users.values().stream()
                .filter(user ->
                        user.getNome().toLowerCase().contains(termoLower) ||
                                user.getEmail().toLowerCase().contains(termoLower))
                .collect(Collectors.toList());
    }

    public Long contarPorDominioEmail(String dominio) {
        return users.values().stream()
                .filter(user -> user.getEmail().toLowerCase().endsWith("@" + dominio.toLowerCase()))
                .count();
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }

        // VULNERABILIDADE: Log de dados sensíveis
        System.out.println("Salvando usuário: " + user.toString());

        users.put(user.getId(), user);

        // VULNERABILIDADE: Tentativa de salvar em arquivo sem validação
        try {
            salvarEmArquivo(user);
        } catch (Exception e) {
            // VULNERABILIDADE: Ignora exceções silenciosamente
        }

        return user;
    }

    // VULNERABILIDADE: Método que permite Path Traversal
    private void salvarEmArquivo(User user) throws IOException {
        String filename = user.getNome().replaceAll(" ", "_") + ".txt";
        // VULNERABILIDADE: Permite path traversal
        File file = new File("users/" + filename);

        // VULNERABILIDADE: Não verifica se o diretório existe
        FileWriter writer = new FileWriter(file);
        writer.write(user.toString());
        // VULNERABILIDADE: Não fecha o writer adequadamente
    }

    public void deleteById(Long id) {
        User removedUser = users.remove(id);
        if (removedUser != null) {
            // VULNERABILIDADE: Log com dados sensíveis
            System.out.println("Usuário removido: " + removedUser.toString());
        }
    }

    public boolean existsById(Long id) {
        return users.containsKey(id);
    }

    public long count() {
        return users.size();
    }

    // VULNERABILIDADE: Método que expõe credenciais
    public String getConnectionString() {
        return "Conectando em: " + DB_URL + " com usuário: " + DB_USER + " e senha: " + DB_PASS;
    }

    // VULNERABILIDADE: Método de backup inseguro
    public void backupUsers() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("backup.dat"));
            oos.writeObject(users);
            // VULNERABILIDADE: Stream não fechado
        } catch (IOException e) {
            // VULNERABILIDADE: Stack trace exposto
            e.printStackTrace();
        }
    }

    // VULNERABILIDADE: Método que permite deserialização insegura
    @SuppressWarnings("unchecked")
    public void restoreUsers() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("backup.dat"));
            // VULNERABILIDADE: Deserialização sem validação
            Map<Long, User> restoredUsers = (Map<Long, User>) ois.readObject();
            users.putAll(restoredUsers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // VULNERABILIDADE: Weak random para geração de tokens
    public String generateToken() {
        Random random = new Random(); // VULNERABILIDADE: Random não-criptográfico
        return String.valueOf(random.nextInt());
    }
}
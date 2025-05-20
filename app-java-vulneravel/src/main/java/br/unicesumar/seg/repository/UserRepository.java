package br.unicesumar.seg.repository;

import br.unicesumar.seg.model.User;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<User> findAll() {
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

    public List<User> findByNomeContainingIgnoreCase(String nome) {
        return users.values().stream()
                .filter(user -> user.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }

    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    public List<User> buscarPorTermo(String termo) {
        String termoLower = termo.toLowerCase();
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
        users.put(user.getId(), user);
        return user;
    }

    public void deleteById(Long id) {
        users.remove(id);
    }

    public boolean existsById(Long id) {
        return users.containsKey(id);
    }

    public long count() {
        return users.size();
    }
}
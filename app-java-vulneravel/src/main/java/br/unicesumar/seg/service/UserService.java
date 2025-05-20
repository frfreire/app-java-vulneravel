package br.unicesumar.seg.service;

import br.unicesumar.seg.model.User;
import br.unicesumar.seg.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> listarTodos() {
        return userRepository.findAll();
    }

    public Optional<User> buscarPorId(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> buscarPorEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> buscarPorNome(String nome) {
        return userRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<User> buscarPorTermo(String termo) {
        return userRepository.buscarPorTermo(termo);
    }

    public User criar(User user) {
        // Validação manual
        String erro = user.getValidationError();
        if (erro != null) {
            throw new RuntimeException("Erro de validação: " + erro);
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email já cadastrado: " + user.getEmail());
        }

        return userRepository.save(user);
    }

    public User atualizar(Long id, User userAtualizado) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));

        // Validação manual
        String erro = userAtualizado.getValidationError();
        if (erro != null) {
            throw new RuntimeException("Erro de validação: " + erro);
        }

        // Verifica se o email não está sendo usado por outro usuário
        Optional<User> usuarioComEmail = userRepository.findByEmail(userAtualizado.getEmail());
        if (usuarioComEmail.isPresent() && !usuarioComEmail.get().getId().equals(id)) {
            throw new RuntimeException("Email já está sendo usado por outro usuário");
        }

        user.setNome(userAtualizado.getNome());
        user.setEmail(userAtualizado.getEmail());
        user.setDescricao(userAtualizado.getDescricao());

        return userRepository.save(user);
    }

    public void deletar(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado com ID: " + id);
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
}
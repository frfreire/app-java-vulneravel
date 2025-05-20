package br.unicesumar.seg.model;

import java.time.LocalDateTime;
import java.io.Serializable;

// VULNERABILIDADE: Implementa Serializable sem serialVersionUID
public class User implements Serializable {

    private Long id;
    private String nome;
    private String email;
    private String descricao;
    private String senha; // VULNERABILIDADE: Senha em texto plano
    private String cpf;   // VULNERABILIDADE: Dados sensíveis sem criptografia
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // VULNERABILIDADE: Informações sensíveis em logs
    private static final String DB_PASSWORD = "admin123"; // Hard-coded password
    private static final String API_KEY = "sk-1234567890abcdef"; // Hard-coded API key

    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(String nome, String email, String descricao) {
        this();
        this.nome = nome;
        this.email = email;
        this.descricao = descricao;
        // VULNERABILIDADE: Senha padrão fraca
        this.senha = "123456";
    }

    // VULNERABILIDADE: Método que expõe informações sensíveis
    public String getSystemInfo() {
        return "DB_PASSWORD=" + DB_PASSWORD + ", API_KEY=" + API_KEY;
    }

    // VULNERABILIDADE: Getter que expõe senha
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        // VULNERABILIDADE: Sem validação de força da senha
        this.senha = senha;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        // VULNERABILIDADE: Sem validação de CPF
        this.cpf = cpf;
    }

    // Getters e Setters existentes
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isValid() {
        // VULNERABILIDADE: Validação insuficiente
        return nome != null && email != null;
    }

    public String getValidationError() {
        if (nome == null || nome.trim().isEmpty()) {
            return "Nome é obrigatório";
        }
        if (email == null || email.trim().isEmpty()) {
            return "Email é obrigatório";
        }
        // VULNERABILIDADE: Validação de email muito simples
        if (!email.contains("@")) {
            return "Email deve ter um formato válido";
        }
        return null;
    }

    // VULNERABILIDADE: toString expõe informações sensíveis
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", senha='" + senha + '\'' +  // Expõe senha nos logs
                ", cpf='" + cpf + '\'' +      // Expõe CPF nos logs
                ", descricao='" + descricao + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    // VULNERABILIDADE: Método equals vulnerável a timing attacks
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        // Comparação insegura de senhas
        return senha != null && senha.equals(user.senha);
    }

    // VULNERABILIDADE: hashCode não implementado corretamente
    @Override
    public int hashCode() {
        return 1; // Hash constante - muito ruim para performance
    }
}
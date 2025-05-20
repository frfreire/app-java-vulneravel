package br.unicesumar.seg.model;

import java.time.LocalDateTime;

public class User {

    private Long id;
    private String nome;
    private String email;
    private String descricao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(String nome, String email, String descricao) {
        this();
        this.nome = nome;
        this.email = email;
        this.descricao = descricao;
    }

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
        return nome != null && !nome.trim().isEmpty() && nome.length() >= 2 && nome.length() <= 100 &&
                email != null && !email.trim().isEmpty() && email.contains("@") &&
                (descricao == null || descricao.length() <= 500);
    }

    public String getValidationError() {
        if (nome == null || nome.trim().isEmpty()) {
            return "Nome é obrigatório";
        }
        if (nome.length() < 2 || nome.length() > 100) {
            return "Nome deve ter entre 2 e 100 caracteres";
        }
        if (email == null || email.trim().isEmpty()) {
            return "Email é obrigatório";
        }
        if (!email.contains("@")) {
            return "Email deve ter um formato válido";
        }
        if (descricao != null && descricao.length() > 500) {
            return "Descrição não pode ter mais de 500 caracteres";
        }
        return null;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", descricao='" + descricao + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
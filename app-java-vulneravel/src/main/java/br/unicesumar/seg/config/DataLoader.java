package br.unicesumar.seg.config;

import br.unicesumar.seg.model.User;
import br.unicesumar.seg.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Verifica se já existem dados
        if (userRepository.count() == 0) {
            System.out.println("🔄 Carregando dados iniciais...");

            // Criando usuários de exemplo
            User user1 = new User("João Silva", "joao.silva@unicesumar.edu.br", "Estudante de Segurança da Informação");
            User user2 = new User("Maria Santos", "maria.santos@unicesumar.edu.br", "Professora de Cibersegurança");
            User user3 = new User("Pedro Oliveira", "pedro.oliveira@gmail.com", "Desenvolvedor Java Senior");
            User user4 = new User("Ana Costa", "ana.costa@outlook.com", "Analista de Segurança");
            User user5 = new User("Carlos Ferreira", "carlos.ferreira@empresa.com", "Especialista em Ethical Hacking");

            // Salvando no repositório
            userRepository.save(user1);
            userRepository.save(user2);
            userRepository.save(user3);
            userRepository.save(user4);
            userRepository.save(user5);

            System.out.println("✅ " + userRepository.count() + " usuários carregados com sucesso!");
            System.out.println("🎓 Sistema UNICESUMAR pronto para uso!");
        } else {
            System.out.println("📊 Dados já existem. Total de usuários: " + userRepository.count());
        }
    }
}

package com.freelancer.freelanceros.config;

import com.freelancer.freelanceros.model.User;
import com.freelancer.freelanceros.model.Workspace;
import com.freelancer.freelanceros.repository.UserRepository;
import com.freelancer.freelanceros.repository.WorkspaceRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataInitializer(UserRepository userRepository,
                              WorkspaceRepository workspaceRepository,
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        Workspace workspace = new Workspace();
        workspace.setName("Demo Workspace");
        workspaceRepository.save(workspace);

        User user = new User();
        user.setName("Admin");
        user.setEmail("admin@freelanceros.local");
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setWorkspace(workspace);
        userRepository.save(user);
    }
}

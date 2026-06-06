package hexlet.code.component;

import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    private final UserRepository userRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final LabelRepository labelRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            TaskStatusRepository taskStatusRepository,
            LabelRepository labelRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.labelRepository = labelRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        initAdmin();
        initTaskStatuses();
        initLabels();
    }

    private void initAdmin() {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            userRepository.save(admin);
        }
    }

    private void initTaskStatuses() {
        createStatusIfAbsent("Draft", "draft");
        createStatusIfAbsent("To Review", "to_review");
        createStatusIfAbsent("To Be Fixed", "to_be_fixed");
        createStatusIfAbsent("To Publish", "to_publish");
        createStatusIfAbsent("Published", "published");
    }

    private void initLabels() {
        createLabelIfAbsent("feature");
        createLabelIfAbsent("bug");
    }

    private void createStatusIfAbsent(String name, String slug) {
        if (taskStatusRepository.findBySlug(slug).isEmpty()) {
            TaskStatus taskStatus = new TaskStatus();
            taskStatus.setName(name);
            taskStatus.setSlug(slug);
            taskStatusRepository.save(taskStatus);
        }
    }

    private void createLabelIfAbsent(String name) {
        if (labelRepository.findByName(name).isEmpty()) {
            Label label = new Label();
            label.setName(name);
            labelRepository.save(label);
        }
    }
}

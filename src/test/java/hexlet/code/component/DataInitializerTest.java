package hexlet.code.component;

import hexlet.code.app.AppApplication;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.support.BaseSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AppApplication.class)
class DataInitializerTest extends BaseSpringBootTest {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Test
    void testDefaultTaskStatusesInitialized() {
        assertThat(taskStatusRepository.findBySlug("draft")).isPresent();
        assertThat(taskStatusRepository.findBySlug("to_review")).isPresent();
        assertThat(taskStatusRepository.findBySlug("to_be_fixed")).isPresent();
        assertThat(taskStatusRepository.findBySlug("to_publish")).isPresent();
        assertThat(taskStatusRepository.findBySlug("published")).isPresent();
        assertThat(taskStatusRepository.count()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void testDefaultLabelsInitialized() {
        assertThat(labelRepository.findByName("feature")).isPresent();
        assertThat(labelRepository.findByName("bug")).isPresent();
        assertThat(labelRepository.count()).isGreaterThanOrEqualTo(2);
    }
}

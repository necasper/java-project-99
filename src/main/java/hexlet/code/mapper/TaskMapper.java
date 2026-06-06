package hexlet.code.mapper;

import hexlet.code.dto.TaskCreateDto;
import hexlet.code.dto.TaskDto;
import hexlet.code.dto.TaskUpdateDto;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class TaskMapper {

    public TaskDto toDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setIndex(task.getIndex());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setTitle(task.getName());
        dto.setContent(task.getDescription());
        dto.setStatus(task.getTaskStatus().getSlug());
        if (task.getAssignee() != null) {
            dto.setAssigneeId(task.getAssignee().getId());
        }
        if (task.getLabels() != null) {
            dto.setTaskLabelIds(task.getLabels().stream()
                    .map(Label::getId)
                    .sorted(Comparator.naturalOrder())
                    .toList());
        }
        return dto;
    }

    public Task toEntity(TaskCreateDto dto, TaskStatus taskStatus, User assignee, List<Label> labels) {
        Task task = new Task();
        task.setName(dto.getTitle());
        task.setIndex(dto.getIndex());
        task.setDescription(dto.getContent());
        task.setTaskStatus(taskStatus);
        task.setAssignee(assignee);
        task.getLabels().addAll(labels);
        return task;
    }

    public void applyPartialUpdate(TaskUpdateDto dto, Task task, TaskStatus taskStatus, User assignee) {
        if (dto.getTitle() != null) {
            task.setName(dto.getTitle());
        }
        if (dto.getIndex() != null) {
            task.setIndex(dto.getIndex());
        }
        if (dto.getContent() != null) {
            task.setDescription(dto.getContent());
        }
        if (taskStatus != null) {
            task.setTaskStatus(taskStatus);
        }
        if (dto.getAssigneeId() != null) {
            task.setAssignee(assignee);
        }
    }
}

package hexlet.code.mapper;

import hexlet.code.dto.TaskStatusCreateDto;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.dto.TaskStatusUpdateDto;
import hexlet.code.model.TaskStatus;
import org.springframework.stereotype.Component;

@Component
public class TaskStatusMapper {

    public TaskStatusDto toDto(TaskStatus taskStatus) {
        TaskStatusDto dto = new TaskStatusDto();
        dto.setId(taskStatus.getId());
        dto.setName(taskStatus.getName());
        dto.setSlug(taskStatus.getSlug());
        dto.setCreatedAt(taskStatus.getCreatedAt());
        return dto;
    }

    public TaskStatus toEntity(TaskStatusCreateDto dto) {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName(dto.getName());
        taskStatus.setSlug(dto.getSlug());
        return taskStatus;
    }

    public void applyPartialUpdate(TaskStatusUpdateDto dto, TaskStatus taskStatus) {
        if (dto.getName() != null) {
            taskStatus.setName(dto.getName());
        }
        if (dto.getSlug() != null) {
            taskStatus.setSlug(dto.getSlug());
        }
    }
}

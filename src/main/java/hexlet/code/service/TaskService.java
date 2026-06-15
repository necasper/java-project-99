package hexlet.code.service;

import hexlet.code.dto.TaskCreateDto;
import hexlet.code.dto.TaskDto;
import hexlet.code.dto.TaskFilterParams;
import hexlet.code.dto.TaskUpdateDto;
import java.util.List;

public interface TaskService {

    List<TaskDto> findAll(TaskFilterParams filter);

    TaskDto findById(Long id);

    TaskDto create(TaskCreateDto dto);

    TaskDto update(Long id, TaskUpdateDto dto);

    void delete(Long id);
}

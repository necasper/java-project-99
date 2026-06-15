package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDto;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.dto.TaskStatusUpdateDto;

import java.util.List;

public interface TaskStatusService {

    List<TaskStatusDto> findAll();

    TaskStatusDto findById(Long id);

    TaskStatusDto create(TaskStatusCreateDto dto);

    TaskStatusDto update(Long id, TaskStatusUpdateDto dto);

    void delete(Long id);
}

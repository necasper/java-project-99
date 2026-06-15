package hexlet.code.service;

import hexlet.code.dto.LabelCreateDto;
import hexlet.code.dto.LabelDto;
import hexlet.code.dto.LabelUpdateDto;

import java.util.List;

public interface LabelService {

    List<LabelDto> findAll();

    LabelDto findById(Long id);

    LabelDto create(LabelCreateDto dto);

    LabelDto update(Long id, LabelUpdateDto dto);

    void delete(Long id);
}

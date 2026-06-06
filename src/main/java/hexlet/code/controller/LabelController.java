package hexlet.code.controller;

import hexlet.code.dto.LabelCreateDto;
import hexlet.code.dto.LabelDto;
import hexlet.code.dto.LabelUpdateDto;
import hexlet.code.service.LabelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/labels")
public class LabelController {

    private final LabelService labelService;

    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @GetMapping
    public List<LabelDto> getAll() {
        return labelService.findAll();
    }

    @GetMapping("/{id}")
    public LabelDto getById(@PathVariable Long id) {
        return labelService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDto create(@Valid @RequestBody LabelCreateDto dto) {
        return labelService.create(dto);
    }

    @PutMapping("/{id}")
    public LabelDto update(@PathVariable Long id, @Valid @RequestBody LabelUpdateDto dto) {
        return labelService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        labelService.delete(id);
    }
}

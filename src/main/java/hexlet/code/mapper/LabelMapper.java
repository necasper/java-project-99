package hexlet.code.mapper;

import hexlet.code.dto.LabelCreateDto;
import hexlet.code.dto.LabelDto;
import hexlet.code.dto.LabelUpdateDto;
import hexlet.code.model.Label;
import org.springframework.stereotype.Component;

@Component
public class LabelMapper {

    public LabelDto toDto(Label label) {
        LabelDto dto = new LabelDto();
        dto.setId(label.getId());
        dto.setName(label.getName());
        dto.setCreatedAt(label.getCreatedAt());
        return dto;
    }

    public Label toEntity(LabelCreateDto dto) {
        Label label = new Label();
        label.setName(dto.getName());
        return label;
    }

    public Label map(LabelDto dto) {
        Label label = new Label();
        label.setId(dto.getId());
        label.setName(dto.getName());
        label.setCreatedAt(dto.getCreatedAt());
        return label;
    }

    public void applyPartialUpdate(LabelUpdateDto dto, Label label) {
        if (dto.getName() != null) {
            label.setName(dto.getName());
        }
    }
}

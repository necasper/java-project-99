package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskDto {

    private Long id;

    private Integer index;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    private String title;

    private String content;

    private String status;
}

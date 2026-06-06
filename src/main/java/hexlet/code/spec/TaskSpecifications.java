package hexlet.code.spec;

import hexlet.code.dto.TaskFilterParams;
import hexlet.code.model.Task;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> withFilter(TaskFilterParams params) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.getTitleCont() != null && !params.getTitleCont().isBlank()) {
                String pattern = "%" + params.getTitleCont().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), pattern));
            }
            if (params.getAssigneeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignee").get("id"), params.getAssigneeId()));
            }
            if (params.getStatus() != null && !params.getStatus().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("taskStatus").get("slug"), params.getStatus()));
            }
            if (params.getLabelId() != null) {
                query.distinct(true);
                Join<Object, Object> labels = root.join("labels");
                predicates.add(criteriaBuilder.equal(labels.get("id"), params.getLabelId()));
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

package hexlet.code.handler;

import hexlet.code.exception.BadRequestException;
import hexlet.code.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final Pattern CONSTRAINT_PATTERN = Pattern.compile(
            "constraint \\[(.+?)]",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern QUOTED_CONSTRAINT_PATTERN = Pattern.compile(
            "constraint \"(.+?)\"",
            Pattern.CASE_INSENSITIVE
    );

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleAuthentication(AuthenticationException ex) {
        return error("Unauthorized");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(ResourceNotFoundException ex) {
        return error(ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBadRequest(BadRequestException ex) {
        return error(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "invalid",
                        (first, second) -> first
                ));
        return error("Validation failed", validationErrors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleDataIntegrity(DataIntegrityViolationException ex) {
        String constraint = extractConstraintName(ex);
        if (constraint != null && !constraint.isBlank()) {
            return error("Integrity constraint violation: " + constraint);
        }
        return error("Integrity constraint violation");
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleAccessDenied(RuntimeException ex) {
        return error("Forbidden");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleException(Exception ex) {
        log.error("Unhandled application exception", ex);
        return error("Internal server error");
    }

    private String extractConstraintName(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        if (message == null) {
            return null;
        }

        Matcher bracketMatcher = CONSTRAINT_PATTERN.matcher(message);
        if (bracketMatcher.find()) {
            return bracketMatcher.group(1);
        }

        Matcher quotedMatcher = QUOTED_CONSTRAINT_PATTERN.matcher(message);
        if (quotedMatcher.find()) {
            return quotedMatcher.group(1);
        }
        return null;
    }

    private Map<String, Object> error(String message) {
        return error(message, null);
    }

    private Map<String, Object> error(String message, Map<String, String> errors) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", message);
        if (errors != null && !errors.isEmpty()) {
            response.put("errors", errors);
        }
        return response;
    }
}

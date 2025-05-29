package com.example.keycloak_auth_service.exception;


import com.example.keycloak_auth_service.util.ExceptionStringUtil;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RestExceptionHandlerTest {

    private RestExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler();
    }

    @Test
    void whenValidationException_thenBadRequestAndFieldErrors() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(
                new Object(), "dto");
        binding.addError(new FieldError("dto", "firstName", "must not be blank"));
        binding.addError(new org.springframework.validation.ObjectError(
                "dto", "Something else went wrong"));

        Method dummy = this.getClass().getMethod("dummyEndpoint", DummyDto.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(dummy, 0),
                binding);

        ResponseEntity<ApplicationError> resp = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        ApplicationError err = resp.getBody();
        assertEquals(ExceptionStringUtil.BAD_REQUEST_ERROR_TITLE, err.getTitle());

        List<String> msgs = err.getMessages();
        assertThat(msgs).containsExactlyInAnyOrder(
                "firstName : must not be blank",
                "Something else went wrong");
    }

    public void dummyEndpoint(@Validated DummyDto dto) {
    }

    static class DummyDto {
    }

    @Test
    void whenIllegalArgument_thenBadRequestWithMessage() {
        IllegalArgumentException ex = new IllegalArgumentException("bad arg");
        ResponseEntity<ApplicationError> resp =
                handler.handleIllegalArgumentExceptionException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        ApplicationError err = resp.getBody();
        assertEquals(ExceptionStringUtil.BAD_REQUEST_ERROR_TITLE, err.getTitle());
        assertEquals("bad arg", err.getMessage());
    }

    @Test
    void whenHttpMessageNotReadable_thenBadRequestGeneric() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("oops");
        ResponseEntity<ApplicationError> resp =
                handler.handleHttpMessageNotReadableException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        ApplicationError err = resp.getBody();
        assertEquals(ExceptionStringUtil.BAD_REQUEST_ERROR_TITLE, err.getTitle());
        assertEquals("Неверное тело запроса", err.getMessage());
    }


    @Test
    void whenEntityNotFound_thenNotFoundWithExceptionMessage() {
        EntityNotFoundException ex = new EntityNotFoundException("user not found");
        ResponseEntity<ApplicationError> resp =
                handler.handleEntityNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        ApplicationError err = resp.getBody();
        assertEquals(ExceptionStringUtil.NOT_FOUND_ERROR_TITLE, err.getTitle());
        assertEquals("user not found", err.getMessage());
    }

    @Test
    void whenAccessDenied_thenForbiddenWithMessage() {
        CustomAccessDeniedException ex =
                new CustomAccessDeniedException("no rights");
        ResponseEntity<ApplicationError> resp =
                handler.handleCustomAccessDeniedException(ex);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        ApplicationError err = resp.getBody();
        assertEquals("Forbidden", err.getTitle());
        assertEquals("no rights", err.getMessage());
    }

    @Test
    void whenGenericException_thenInternalServerError() {
        Exception ex = new Exception("boom");
        ResponseEntity<ApplicationError> resp =
                handler.handleException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        ApplicationError err = resp.getBody();
        assertEquals(ExceptionStringUtil.INTERNAL_SERVER_ERROR_ERROR_TITLE, err.getTitle());
        assertEquals("Внутренняя ошибка сервера", err.getMessage());
    }
}
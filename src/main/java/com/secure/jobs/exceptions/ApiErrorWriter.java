package com.secure.jobs.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ApiErrorWriter {

    private ApiErrorWriter() {}

    public static void write(
            ObjectMapper objectMapper,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {

        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");

        ApiError body = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}

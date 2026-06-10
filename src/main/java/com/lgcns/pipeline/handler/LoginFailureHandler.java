package com.lgcns.pipeline.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@NullMarked
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        System.out.println("*** LoginFailure=" + exception.getMessage());

        ObjectMapper objMapper = new ObjectMapper();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println(objMapper.writeValueAsString(Map.of("error", "ERROR_LOGIN")));
        out.close();
    }

}

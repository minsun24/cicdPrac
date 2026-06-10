package com.lgcns.pipeline.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

/**
 * [전역 예외 처리기]
 * 애플리케이션 내의 모든 Controller에서 예외가 발생하면 이 핸들러 클래스가 감지하여 가로챕니다.
 * 서버 내부 에러 구조를 클라이언트에게 감추고, 표준화된 HTTP 상태 코드와 메시지를 응답하기 위해 사용합니다.
 */
@RestControllerAdvice
public class ControllerExceptionHandler {

    /**
     * 1. 잘못된 인자값 전달 예외 처리 (HTTP 400 Bad Request)
     * 예: 서비스 로직에서 조건이 안 맞아 `throw new IllegalArgumentException("유효하지 않은 Id입니다.");`를 던진 경우
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalExceptionHandler(IllegalArgumentException e) {
        String message = e.getMessage();
        // HTTP 상태 코드 400에 "Warn: 에러메시지" 문자열을 바디에 담아 응답
        return ResponseEntity.badRequest().body("Warn: " + message);
    }

    /**
     * 2. 권한 거부 예외 처리 (HTTP 403 Forbidden)
     * 스프링 시큐리티의 메소드 보안(@PreAuthorize 등) 권한 체크에서 탈락했을 때 발생합니다.
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AuthorizationDeniedException e) {
        // HTTP 상태 코드 403에 {"error": "Access Denied.."} 형태의 JSON 객체를 바디에 담아 응답
        return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
    }

    /**
     * 3. 커스텀 JWT 예외 처리 (HTTP 401 Unauthorized)
     * JwtUtil에서 토큰 검증 실패 시(만료, 위조 등) 던지도록 설계한 커스텀 예외인 `JwtException`을 처리합니다.
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handlerJwtException(JwtException e) {
        // HTTP 상태 코드 401 인증 실패 상태와 함께 에러 내용을 JSON 구조로 응답
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }

    /**
     * 4. 존재하지 않는 URL 요청 예외 처리 (HTTP 404 Not Found)
     * 사용자가 서버에 존재하지 않는 API 엔드포인트(URL)로 요청을 보냈을 때 발생합니다.
     * (주의: application.yml에 throw-exception-if-no-handler-found: true 설정이 있어야 이 예외가 활성화됩니다.)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NoHandlerFoundException e) {
        // HTTP 상태 코드 404와 에러 경로 메시지를 응답
        return ResponseEntity.status(404).body(e.getMessage());
    }

    /**
     * 5. 최상위 예외 처리 (HTTP 500 Internal Server Error)
     * 위에서 명시적으로 정의하지 않은 '나머지 모든 예외'(NullPointerException, DB 에러 등)를 최종적으로 처리합니다.
     * 예측하지 못한 개발자의 실수나 시스템 에러를 잡아내는 그물망 역할을 합니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOthersExceptionHandler(Exception e) {
        String message = e.getMessage();

        // 개발 단계에서 백엔드 콘솔 콘솔창에 디버깅용 에러 로그(스택 트레이스)를 출력하도록 합니다.
        e.printStackTrace(System.out);

        // 외부 클라이언트에게는 구체적인 시스템 에러 코드를 숨기고 HTTP 500 코드와 공통 에러 포맷만 응답합니다.
        return ResponseEntity.internalServerError().body("Error: " + message);
    }
}

package com.lgcns.pipeline.handler;

import com.lgcns.pipeline.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * [로그인 성공 핸들러]
 * 스프링 시큐리티의 기본 formLogin() 절차를 통해 사용자 인증(ID/PW 대조)이 성공하면
 * 시큐리티 시스템이 이를 감지하여 이 클래스의 onAuthenticationSuccess 메서드를 자동으로 실행합니다.
 */
@NullMarked
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    // JWT 토큰 생성을 주도하는 JwtUtil을 주입받습니다.
    private final JwtUtil jwtUtil;

    /**
     * 로그인 성공 시 수행될 핵심 비즈니스 로직
     *
     * @param request        클라이언트의 요청 정보 객체
     * @param response       서버가 클라이언트에게 보낼 응답 제어 객체
     * @param authentication 인증에 성공한 사용자의 세부 정보(Principal, 권한 등)를 담고 있는 시큐리티 핵심 객체
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 디버깅용 콘솔 출력: 성공한 인증 객체의 내부 상태(유저명, 권한 등)를 확인합니다.
        System.out.println("*** SuccessHandler.auth=" + authentication);

        // 1. [핵심] 인증 객체를 JwtUtil에 토스하여 JWT 토큰 쌍을 발급받습니다.
        //    JwtUtil 내부에서는 authentication에서 UserDTO를 꺼내어
        //    10분짜리 'accessToken'과 24시간짜리 'refreshToken'이 추가된 Map<String, Object>을 반환합니다.
        Map<String, Object> claims = jwtUtil.authenticationToClaims(authentication);

        // 2. 자바 객체(Map)를 클라이언트가 이해할 수 있는 JSON 문자열로 직렬화하기 위해 잭슨 라이브러리를 생성합니다.
        ObjectMapper objMapper = new ObjectMapper();

        // 3. HTTP 응답의 헤더(Header) 정보를 세팅합니다.
        //    클라이언트에게 "우리가 지금 보내는 데이터는 일반 텍스트가 아니라 JSON 포맷이야"라고 명시합니다.
        response.setContentType("application/json;charset=UTF-8"); // 한글 깨짐 방지를 위해 charset 추가 권장

        // 4. 응답 바디(Body)에 데이터를 써 내려가기 위한 스트림 출구(PrintWriter)를 엽니다.
        PrintWriter out = response.getWriter();

        // 5. 발급된 토큰들과 유저 정보가 담긴 claims 맵 객체를
        //    JSON 문자열(ex. {"id":1, "email":"..", "accessToken":"eyJ...", "refreshToken":"eyJ..."})로 변환하여 출력합니다.
        out.println(objMapper.writeValueAsString(claims));

        // 6. 사용한 입출력 스트림을 안전하게 닫아 데이터 전송을 완료합니다.
        out.close();
    }
}

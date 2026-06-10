package com.lgcns.pipeline.filter;

import com.lgcns.pipeline.user.UserDTO;
import com.lgcns.pipeline.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@NullMarked
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    // 1. JWT 토큰 검증을 생략할 공통 URL 패턴 정의 (로그인, 회원가입, API 문서 등)
    private static final String[] EXCLUDE_PATTERNS = {
            "/api/users/login",    // 로그인 요청은 토큰이 없는 상태로 들어오므로 제외
            "/api/users",          // 회원가입 요청도 제외
            "/favicon.ico",
            "/actuator/**",
            "/*.html",
            "/swagger-ui/**",
            "/pipeline/api-docs/**",
            "/broadcast/**",       // 💡 SockJS 핸드셰이크 및 웹소켓 커넥션 경로 통째로 제외
            "/app/**"              // @MessageMapping으로 향하는 내부 메시지 통로 제외
    };

    private final JwtUtil jwtUtil;

    // 스프링에서 제공하는 경로 매칭 유틸리티 (ex. /actuator/** 패턴 분석용)
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 자바 객체를 JSON 문자열로 직렬화하기 위한 잭슨 오브젝트 맵퍼
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * [필터 통과 여부 결정 메서드]
     * 들어온 요청의 URL이 EXCLUDE_PATTERNS에 해당하면 true를 리턴하여 토큰 검증(doFilterInternal)을 건너뜁니다.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        System.out.println("** 현재 요청 경로:path = " + path);

        // 패턴 배열을 스트림으로 돌려 현재 경로와 매칭되는 게 하나라도 있으면 true 반환
        return Arrays.stream(EXCLUDE_PATTERNS)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * [에러 응답 전송 유틸리티 메서드]
     * 필터 단계에서 토큰 오류가 발생했을 때 컨트롤러까지 가지 못하므로,
     * 여기서 직접 클라이언트(브라우저)에게 JSON 형태의 에러 응답을 직접 내려줍니다.
     */
    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8"); // 한글 깨짐 방지 및 JSON 명시
        PrintWriter out = response.getWriter();
        // {"error": "메시지내용"} 형태의 JSON으로 파싱하여 전송
        out.println(objectMapper.writeValueAsString(Map.of("error", message)));
        out.close();
    }

    /**
     * [필터의 핵심 실무 로직]
     * 매 API 요청마다 헤더를 체크하고 JWT 토큰의 유효성을 검증합니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. HTTP 요청 헤더에서 Authorization 값을 꺼냅니다.
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 2. 헤더가 비어있거나 'Bearer '로 시작하지 않는 경우 규격 외 요청이므로 인증 실패 처리
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, "Missing or invalid Authorization header");
            return;
        }

        try {
            // 3. 'Bearer ' 뒷부분의 실제 순수 JWT 토큰 문자열만 잘라내어 검증 진행
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(token); // 내부 서명 및 만료일자 검증

            // 4. 검증 성공 시 토큰 내부 데이터(Claims)에서 유저 정보 추출
            Integer id = (Integer) claims.get("id");
            List<String> roleNames = (List<String>) claims.get("roleNames");

            // 5. 시큐리티 세션 콘텍스트에 저장할 UserDTO 객체 복원 생성
            UserDTO dto = new UserDTO(
                    id.longValue(),
                    (String) claims.get("email"),
                    "", // 비밀번호는 토큰에 담지 않으므로 빈 값 처리
                    (String) claims.get("name"),
                    roleNames
            );

            // 🛠️ [에러 해결]: String 기반의 roleNames를 스프링 시큐리티 인증용 GrantedAuthority 객체로 변환
            List<SimpleGrantedAuthority> authorities = roleNames.stream()
                    .map(SimpleGrantedAuthority::new) // "ROLE_USER" 등을 SimpleGrantedAuthority 객체로 매핑
                    .toList();

            // 6. 스프링 시큐리티 전용 인증 토큰(Authentication) 객체 생성
            //    (Principal: 유저DTO, Credentials: 비밀번호인증이 끝났으므로 null, Authorities: 권한 리스트)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(dto, null, authorities);

            // 7. SecurityContextHolder 관제탑에 이 유저가 인증 성공된 주체임을 등록
            //    이 처리를 마쳐야 이후 API(컨트롤러) 권한 체크 단계나 서비스 레이어에서 유저 정보를 꺼내 쓸 수 있습니다.
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // 토큰이 만료되었거나 서명이 위조된 경우 이곳으로 튕겨 나옵니다.
            sendError(response, "ERROR_ACCESS_TOKEN:" + e.getMessage());
            return; // 다음 필터로 가지 못하게 차단
        }

        // 8. 모든 검증이 성공적이면 사슬의 다음 보안 필터 혹은 실제 컨트롤러로 요청을 토스합니다.
        filterChain.doFilter(request, response);
    }
}

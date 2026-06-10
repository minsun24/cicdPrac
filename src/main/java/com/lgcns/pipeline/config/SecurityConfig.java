package com.lgcns.pipeline.config;

import com.lgcns.pipeline.filter.CustomAuthenticationFilter;
import com.lgcns.pipeline.handler.CustomAccessDeniedHandler;
import com.lgcns.pipeline.handler.LoginFailureHandler;
import com.lgcns.pipeline.handler.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableMethodSecurity   // Controller 메서드 단위에서 @PreAuthorize("hasRole('ROLE_ADMIN')") 같은 권한 체크 어노테이션을 활성화합니다.
@Configuration          // 스프링의 설정 클래스임을 명시하고 빈(Bean) 등록을 가능하게 합니다.
@RequiredArgsConstructor // final이 붙은 필드들을 자동으로 주입해주는 롬복 생성자 어노테이션입니다.
public class SecurityConfig {

    // 커스텀하게 구현된 핸들러 및 필터들을 주입받습니다.
    private final LoginSuccessHandler successHandler;      // 폼 로그인 성공 시 호출될 핸들러
    private final LoginFailureHandler failureHandler;      // 폼 로그인 실패 시 호출될 핸들러
    private final CustomAccessDeniedHandler accessDeniedHandler; // 인가(권한) 거부 발생 시 처리할 핸들러
    private final CustomAuthenticationFilter jwtAuthenticationFilter; // 매 요청마다 JWT 토큰을 검증할 커스텀 필터

    /**
     * HTTP 보안 설정을 구성하는 핵심 메서드 (스프링 시큐리티의 필터 체인 조립)
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        System.out.println("** SecurityConfig.filterChain - 시큐리티 필터 체인 구성 시작");

        http
                // 1. CSRF(Cross-Site Request Forgery) 보호 비활성화
                //    REST API 환경에서는 세션을 쓰지 않고 JWT 토큰을 사용하므로 CSRF 공격으로부터 상대적으로 안전하여 끕니다.
                .csrf(AbstractHttpConfigurer::disable)

                // 2. CORS(Cross-Origin Resource Sharing) 설정 적용
                //    하단에 정의된 corsConfigurationSource() 설정을 시큐리티 시스템에 주입합니다.
                .cors(config -> config.configurationSource(corsConfigurationSource()))

                // 3. 세션 관리 정책 설정: STATELESS (무상태)
                //    서버에서 세션을 관리하지 않도록 설정합니다. (JWT 인증 기반 웹앱의 필수 설정)
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. formLogin 기반 인증 설정 (전통적인 아이디/비밀번호 로그인 처리 방식)
                .formLogin(form -> form
                        .loginPage("/api/users/login") // 로그인 처리를 담당할 URL 지정
                        .successHandler(successHandler) // 로그인 성공 시 수행할 커스텀 로직 연결 (여기서 보통 JWT 토큰을 발급해서 응답함)
                        .failureHandler(failureHandler) // 로그인 실패 시 수행할 커스텀 로직 연결
                )

                // 5. 예외 처리(Exception Handling) 설정
                //    인증은 되었으나 해당 자원에 접근할 권한이 없을 때(403 Forbidden) 작동할 핸들러 등록
                .exceptionHandling(config -> config.accessDeniedHandler(accessDeniedHandler))

                // 6. 커스텀 JWT 필터 위치 지정
                //    UsernamePasswordAuthenticationFilter(기본 로그인 필터)가 작동하기 '직전'에
                //    jwtAuthenticationFilter(JWT 검증 필터)를 먼저 실행하도록 설정합니다.
                //    즉, 매 API 요청마다 헤더의 JWT 토큰을 먼저 검사하여 인증 정보가 있으면 로그인을 통과시켜 줍니다.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // 설정된 HttpSecurity 객체를 빌드하여 반환
    }

    /**
     * 인증 매니저(AuthenticationManager) 빈 등록
     * 스프링 시큐리티에서 실제 인증(아이디, 비밀번호 대조 등)을 총괄하는 핵심 컴포넌트입니다.
     * 커스텀 로그인 로직을 짤 때 직접 호출하여 인증을 유도할 수 있습니다.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * CORS(교차 출처 자원 공유) 설정 정의 메서드
     * 프론트엔드(ex. React - localhost:3000)와 백엔드(ex. Spring Boot - localhost:8080)의 포트나 도메인이 다를 때
     * 브라우저에서 차단되는 현상을 해결하기 위한 허용 정책을 세팅합니다.
     */
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 1. 모든 출처(도메인)에서의 요청을 허용합니다. (실제 운영 환경에서는 특정 도메인만 명시하는 것이 좋습니다.)
        config.setAllowedOriginPatterns(List.of("*"));

        // 2. 허용할 HTTP Method 지정
        config.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.OPTIONS.name(),
                HttpMethod.DELETE.name()));

        // 3. 브라우저가 요청 시 전송할 수 있는 HTTP 헤더 허용 리스트 지정
        //    Authorization: JWT 토큰 전송용
        //    Content-Type: JSON 데이터 요청용 (application/json)
        config.setAllowedHeaders(List.of(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CACHE_CONTROL,
                HttpHeaders.CONTENT_TYPE));

        // 4. 자격증명(Credentials) 허용 설정
        //    쿠키(Cookie)나 인증 헤더(Authorization)를 통한 자격 증명을 프론트엔드 요청 시 허용할 것인지 여부 (true)
        config.setAllowCredentials(true);

        // 5. 위의 설정을 모든 URL 경로("/**")에 적용하겠다고 선언
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}

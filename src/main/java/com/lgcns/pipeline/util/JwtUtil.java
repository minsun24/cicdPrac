package com.lgcns.pipeline.util;

import com.lgcns.pipeline.exception.JwtException;
import com.lgcns.pipeline.user.UserDTO;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

/**
 * JWT(JSON Web Token)의
 * [생성, 발급, 검증] 을 담당하는 유틸리티 클래스
 * 스프링 빈(Component)으로 등록되어 필요한 서비스나 필터에서 주입받아 사용
 */
@Component
public class JwtUtil {

    // 토큰 서명(Signature) 및 검증에 사용할 암호화 키 객체
    private final SecretKey secretKey;

    /**
     * 생성자: application.yml 등의 설정 파일에 정의된
     * secret.yml 파일에서 jwt 문자열값을 읽어와
     * HMAC-SHA 알고리즘에 적합한 SecretKey 객체로 안전하게 변환하여 저장합니다.
     */
    public JwtUtil(@Value("${jwt.secret}") String jwtSecret) {
        // 문자열을 UTF-8 바이트 배열로 변환한 뒤, 이를 기반으로 HMAC 서명 키를 생성
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 토큰 검증 및 페이로드 추출 메서드
     *
     * @param token 검증할 JWT 문자열
     * @return 토큰 내부에 저장된 데이터 (Claims / Payload)
     */
    public Map<String, Object> validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)   // 서명 검증에 사용할 비밀키 설정
                    .build()                 // 파서 빌드
                    .parseSignedClaims(token)// 토큰 구조 해석 및 서명의 유효성 검증 수행
                    .getPayload();           // 검증이 완료된 내부 데이터(바디) 추출 및 반환
        } catch (ExpiredJwtException e) {
            // 토큰의 유효 시간이 만료된 경우 예외 발생
            throw new JwtException("Token Expired");
        } catch (io.jsonwebtoken.JwtException e) {
            // 서명 불일치, 구조 오류 등 jjwt 라이브러리 수준에서 발생하는 기타 JWT 에러 처리
            throw new JwtException("Jwt Error: " + e.getMessage());
        } catch (Exception e) {
            // 그 외 예측하지 못한 런타임 에러 처리
            throw new JwtException("Error: " + e.getMessage());
        }
    }

    /**
     * 인증 객체를 기반으로 Access Token과 Refresh Token을 일괄 발급하는 메서드
     *
     * @param authentication 스프링 시큐리티 인증 성공 후 세션/콘텍스트에 저장된 인증 객체
     * @return Access/Refresh 토큰 및 사용자 정보가 포함된 Map
     */
    public Map<String, Object> authenticationToClaims(Authentication authentication) {
        // 1. 인증 객체에서 사용자의 세부 정보(Principal)를 꺼내어 UserDTO로 캐스팅
        UserDTO dto = (UserDTO) authentication.getPrincipal();

        if (dto == null) {
            throw new JwtException("Invalid Authentication");
        }

        System.out.println("***** dto.getRoleNames() = " + dto.getRoleNames());

        // 2. 토큰의 페이로드(Claims)에 담을 사용자 정보 맵(Map) 생성
        //    보안을 위해 비밀번호 필드는 빈 문자열("")로 처리하여 포함시킵니다.
        Map<String, Object> claims = new UserDTO(
                dto.getId(),
                dto.getEmail(),
                "",
                dto.getName(),
                dto.getRoleNames()
        ).getClaims();

        // 3. 생성된 사용자 정보 맵을 기반으로 두 종류의 토큰을 생성하여 추가
        // Access Token 발급: 유효기간 10분 지정 (짧은 주기, API 요청용)
        claims.put("accessToken", generateToken(claims, 10));

        // Refresh Token 발급: 유효기간 24시간 지정 (긴 주기, 토큰 재발급용)
        claims.put("refreshToken", generateToken(claims, 60 * 24));

        // 4. 최종적으로 토큰들이 포함된 유저 데이터 맵을 리턴 (컨트롤러 등에서 응답 바디로 사용)
        return claims;
    }

    /**
     * JWT 토큰을 빌더 패턴으로 실제 생성하는 내부 메서드
     *
     * @param valueMap 토큰 바디(Claims)에 주입할 데이터 구조
     * @param min      토큰의 만료 시간 (분 단위)
     * @return 최종 생성된 컴팩트한 JWT 문자열
     */
    public String generateToken(Map<String, Object> valueMap, int min) {
        return Jwts.builder()
                // 헤더 설정: 토큰의 타입이 JWT임을 명시
                .header().add("typ", "JWT").and()
                // 클레임 설정: 전달받은 유저 정보 Map 데이터를 바디에 통째로 주입
                .claims().add(valueMap).and()
                // 발급 시간(Issued At): 현재 시간 기록
                .issuedAt(Date.from(ZonedDateTime.now().toInstant()))
                // 만료 시간(Expiration): 현재 시간에 파라미터로 넘어온 분(min)만큼 더해서 설정
                .expiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant()))
                // 서명(Sign): 서명 키와 알고리즘 정보를 이용해 토큰의 무결성 보장 선언
                .signWith(secretKey)
                // 직렬화 및 압축하여 문자열(ex. eyJhbGciOi...)로 변환
                .compact();
    }
}

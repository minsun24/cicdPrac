package com.lgcns.pipeline.user;

import com.lgcns.pipeline.exception.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping()
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid UserRegistDTO dto) {
        return ResponseEntity.ok(userService.registUser(dto));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping("/{userId}")
    @Tag(name = "고객 정보 얻기", description = "API 설명 부분...")
    @Operation(summary = "URL 링크 설명", description = "펼쳤을 때 설명 부분...")
    @Parameters({
            @Parameter(name = "id", description = "유저 아이디", example = "1"),})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "해당 유저가 없습니다.")
    })
    public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }


    @GetMapping("profile")
    public ResponseEntity<UserDTO> profile(Authentication auth) {
        UserDTO dto = (UserDTO) auth.getPrincipal();
        if (dto == null) throw new JwtException("Not valid user!!");

        return ResponseEntity.ok(userService.getUser(dto.getId()));
    }

    /**
     * 프로필 이름 수정 API
     * PUT http://localhost:8080/api/users/edit-profile
     */
    @PutMapping("/edit-profile")
    public ResponseEntity<UserDTO> editProfile(
            Authentication auth, // 💡 시큐리티 필터가 저장한 유저 정보(Principal)를 그대로 꺼내옵니다.
            @RequestBody String name  // 클라이언트가 보낸 {"name": "..."} 데이터를 매핑합니다.
    ) {
        // 1. Filter를 거쳐 인증된 유저가 맞는지 최소한의 검증 (Null marked 환경 고려)
        UserDTO loginUser = (UserDTO) auth.getPrincipal();
        if (loginUser == null) {
            throw new JwtException("Not valid user!!");
        }

        // 2. 입력값 검증 (이름이 비어있는지 확인)
        if (loginUser.getName() == null || loginUser.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("변경할 이름을 입력해주세요.");
            // 💡 여기서 예외를 던지면 우리가 만든 'ControllerExceptionHandler'가 감지하여 400 에러를 리턴합니다!
        }

        // 3. 서비스 레이어에 비즈니스 로직 위임 (유저 ID와 새 이름을 전달)
        UserDTO updatedUser = userService.updateUserName(loginUser.getId(), name);

        // 4. 성공 응답 반환
        return ResponseEntity.ok(updatedUser);
    }

    //
//### add-role
//    PATCH {{baseUrl}}/1/roles
//    Authorization: Bearer {{auth_token}}
//    Content-Type: application/json
//
//    {
//        "role": "ROLE_ADMIN"
//    }
//
    // 관리자 권한 추가
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PatchMapping("/{userId}/roles")
    public ResponseEntity<UserDTO> addAdminRole(Authentication auth, @PathVariable Long userId, @RequestBody @Valid UserRolesDTO dto) {
        UserDTO loginUser = (UserDTO) auth.getPrincipal();
        if (loginUser == null || Objects.equals(loginUser.getId(), userId)) {
            throw new JwtException("Not valid user!!");
        }
        return ResponseEntity.ok(userService.editUserRole(userId, dto));
    }


    //### remove-role
//    DELETE {{baseUrl}}/1/roles
//    Authorization: Bearer {{auth_token}}

    @DeleteMapping("/{userId}/roles")
    public ResponseEntity<UserDTO> deleteAdminRole(Authentication auth, @PathVariable Long userId) {
        UserDTO loginUser = (UserDTO) auth.getPrincipal();
        if (loginUser == null) {
            throw new JwtException("Not valid user!!");
        }
        return ResponseEntity.ok(userService.deleteAdminRole(userId));
    }


}

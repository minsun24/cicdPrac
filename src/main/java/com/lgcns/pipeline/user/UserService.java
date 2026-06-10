package com.lgcns.pipeline.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * User의 ROLE 수정
     * UserRole 목록을 인자로 받아 덮어씌우기 한다.
     *
     * @param id
     * @param dto
     */
    @Transactional
    public UserDTO editUserRole(Long id, @Valid UserRolesDTO dto) {
        User user = userRepository.findByIdForUpdate(id).orElseThrow();

        user.clearRoles();
        dto.getRoles().forEach(user::addRole);
        userRepository.save(user);

        return userMapper.toDTO(user);
    }

    public void deleteUserRole(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
        }
        user.setRoles(null);
        userRepository.save(user);
    }

    public UserDTO registUser(UserRegistDTO userRegistDTO) {
        User newUser = userRepository.save(userMapper.toEntity(userRegistDTO));
        return userMapper.toDTO(newUser);
    }

    public UserDTO getUser(Long userId) {
        User user = userRepository.findUserById(userId);
        return userMapper.toDTO(userRepository.findUserById(userId));
    }

    @Transactional
    public UserDTO updateUserName(Long id, String newName) {
        User userEntity = userRepository.findUserById(id);
        if (userEntity == null) {
            throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
        }
        // 2. 관리 대상인 엔티티의 값을 직접 수정합니다.
        userEntity.setName(newName);
        return userMapper.toDTO(userEntity);
    }

    @Transactional
    public UserDTO addAdminRole(Long userId, UserRole adminRole) {
        User userEntity = userRepository.findUserById(userId);

        if (userEntity == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        // 2. 엔티티 객체의 리스트에 새로운 역할을 추가합니다.
        userEntity.addRole(adminRole);
        User savedUser = userRepository.save(userEntity);

        return userMapper.toDTO(savedUser);
    }

    public @Nullable UserDTO deleteAdminRole(Long userId) {
        User userEntity = userRepository.findUserById(userId);

        if (userEntity == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        // 2. 엔티티 객체의 리스트에 새로운 역할을 추가합니다.
        userEntity.deleteRole(UserRole.ROLE_ADMIN);
        User savedUser = userRepository.save(userEntity);

        return userMapper.toDTO(savedUser);
    }
}

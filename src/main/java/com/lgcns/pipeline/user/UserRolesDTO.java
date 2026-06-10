package com.lgcns.pipeline.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserRolesDTO {

    @Size(min = 1)  // 최소한 1개 이상 존재해야 함(USER_ROLE은 기본으로 포함되어야 함)
    private List<UserRole> roles;
}

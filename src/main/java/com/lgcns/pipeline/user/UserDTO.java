package com.lgcns.pipeline.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"authorities"
        , "accountNonExpired"
        , "claims"
        , "accountNonLocked"
        , "credentialsNonExpired"
        , "enabled"
        , "username"
        , "password"})
public class UserDTO {
    private Long id;
    private String email;
    private String password;
    private String name;
    private List<String> roleNames;

//    public UserDTO(Long id, String email, String password, String name, List<String> roleNames) {
//        super();
//        this.id = id;
//        this.email = email;
//        this.password = password;
//        this.name = name;
//        this.roleNames = roleNames;
//    }

    // to make JWT token (client에게 내려 줄 값)
    public Map<String, Object> getClaims() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("email", email);
        map.put("name", name);
        map.put("password", password);
        map.put("roleNames", roleNames);

        return map;
    }


}

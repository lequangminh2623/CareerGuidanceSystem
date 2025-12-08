package com.lqm.user_service.services;

import com.lqm.user_service.dtos.UserLoginDTO;

import java.util.Map;

public interface AuthService {
    String login(UserLoginDTO dto) throws Exception;

    Map<String, Object> loginWithGoogle(String token);
}

package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.AdminUserLoginDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "api-gateway", path = "/user-service/api/internal/auth", contextId = "authClient")
public interface AuthClient {

    @GetMapping("/{email}")
    AdminUserLoginDTO getUserForAuth(@PathVariable("email") String email);

}

package com.iden.services;

import com.iden.configs.FeignConfig;
import com.iden.data.InitialUser;
import com.iden.data.InitialUser1;
import com.iden.data.UserClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "userClient", url = "http://localhost:6066", configuration = FeignConfig.class)
public interface IUserClient {
    @GetMapping("/api/info/user/info")
    ResponseEntity<UserClientResponse> getUserInfo(@RequestHeader("Authorization") String token);

    @PostMapping("/api/info/user/change/email")
    ResponseEntity<UserClientResponse> changeEmail(@RequestHeader("Authorization") String token, @RequestBody  String newEmail);

    @PostMapping("/api/info/user/create")
    ResponseEntity<UserClientResponse> initialUser(@RequestBody InitialUser initialUser);

    @PostMapping("/api/info/user/create")
    ResponseEntity<UserClientResponse> initialUser2(@RequestBody InitialUser1 initialUser);
}

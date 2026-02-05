package com.prism.statistics.presentation.user;

import com.prism.statistics.application.user.UserService;
import com.prism.statistics.application.user.dto.request.ChangeNicknameRequest;
import com.prism.statistics.application.user.dto.response.ChangedNicknameResponse;
import com.prism.statistics.application.user.dto.response.UserInfoResponse;
import com.prism.statistics.global.auth.AuthUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserInfoResponse> findMyInfo(AuthUserId authUserId) {
        UserInfoResponse response = userService.findUserInfo(authUserId.userId());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/nickname")
    public ResponseEntity<ChangedNicknameResponse> changeNickname(
            AuthUserId authUserId,
            @Valid @RequestBody ChangeNicknameRequest request
    ) {
        ChangedNicknameResponse response = userService.changedNickname(authUserId.userId(), request);

        return ResponseEntity.ok(response);
    }
}

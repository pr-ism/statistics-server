package com.prism.statistics.application.user;

import com.prism.statistics.application.user.dto.request.ChangeNicknameRequest;
import com.prism.statistics.application.user.dto.response.ChangedNicknameResponse;
import com.prism.statistics.application.user.exception.UserNotFoundException;
import com.prism.statistics.domain.user.User;
import com.prism.statistics.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public ChangedNicknameResponse changedNickname(Long id, ChangeNicknameRequest request) {
        User user = userRepository.findById(id)
                                  .orElseThrow(() -> new UserNotFoundException());

        user.changeNickname(request.changedNickname());

        return ChangedNicknameResponse.create(user);
    }
}

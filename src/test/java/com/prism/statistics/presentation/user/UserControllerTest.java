package com.prism.statistics.presentation.user;

import static com.prism.statistics.docs.RestDocsConfiguration.field;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.user.UserService;
import com.prism.statistics.application.user.dto.request.ChangeNicknameRequest;
import com.prism.statistics.application.user.dto.response.ChangedNicknameResponse;
import com.prism.statistics.application.user.dto.response.UserInfoResponse;
import com.prism.statistics.application.user.exception.UserNotFoundException;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class UserControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private UserService userService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 회원_정보_조회_성공_테스트() throws Exception {
        // given
        UserInfoResponse response = new UserInfoResponse("테스트닉네임");

        given(userService.findUserInfo(7L)).willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                                                     get("/users/me")
                                                             .header("Authorization", "Bearer access-token")
                                             )
                                             .andExpect(status().isOk())
                                             .andExpect(jsonPath("$.nickname").value("테스트닉네임"));

        회원_정보_조회_문서화(resultActions);
    }

    private void 회원_정보_조회_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        responseFields(
                                fieldWithPath("nickname").description("회원 닉네임")
                        )
                )
        );
    }

    @Test
    void 인증_정보가_없으면_회원_정보를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(get("/users/me"))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("$.errorCode").value("A04"))
               .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 999L)
    void 존재하지_않는_사용자는_회원_정보를_조회할_수_없다() throws Exception {
        // given
        willThrow(new UserNotFoundException()).given(userService).findUserInfo(999L);

        // when & then
        mockMvc.perform(get("/users/me"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.errorCode").value("U01"))
               .andExpect(jsonPath("$.message").value("존재하지 않는 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 닉네임_변경_성공_테스트() throws Exception {
        // given
        ChangeNicknameRequest request = new ChangeNicknameRequest("새로운닉네임");
        ChangedNicknameResponse response = new ChangedNicknameResponse("새로운닉네임");

        given(userService.changedNickname(7L, request)).willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                                                     patch("/users/me/nickname")
                                                             .header("Authorization", "Bearer access-token")
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(request))
                                             )
                                             .andExpect(status().isOk())
                                             .andExpect(jsonPath("$.changedNickname").value("새로운닉네임"));

        닉네임_변경_문서화(resultActions);
    }

    private void 닉네임_변경_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        requestFields(
                                fieldWithPath("changedNickname").description("변경할 닉네임")
                                                                .attributes(field("constraints", "빈 값은 허용하지 않음"))
                        ),
                        responseFields(
                                fieldWithPath("changedNickname").description("변경된 닉네임")
                        )
                )
        );
    }

    @Test
    void 인증_정보가_없으면_닉네임을_변경할_수_없다() throws Exception {
        // given
        ChangeNicknameRequest request = new ChangeNicknameRequest("새로운닉네임");

        // when & then
        mockMvc.perform(
                       patch("/users/me/nickname")
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(objectMapper.writeValueAsString(request))
               )
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("$.errorCode").value("A04"))
               .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 변경할_닉네임이_비어_있으면_변경할_수_없다() throws Exception {
        // given
        ChangeNicknameRequest request = new ChangeNicknameRequest("");

        // when & then
        mockMvc.perform(
                       patch("/users/me/nickname")
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(objectMapper.writeValueAsString(request))
               )
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errorCode").value("D03"))
               .andExpect(jsonPath("$.message").value("변경할 닉네임은 비어 있을 수 없습니다."));
    }

    @Test
    @WithOAuth2User(userId = 999L)
    void 존재하지_않는_사용자는_닉네임을_변경할_수_없다() throws Exception {
        // given
        ChangeNicknameRequest request = new ChangeNicknameRequest("새로운닉네임");

        willThrow(new UserNotFoundException()).given(userService).changedNickname(999L, request);

        // when & then
        mockMvc.perform(
                       patch("/users/me/nickname")
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(objectMapper.writeValueAsString(request))
               )
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.errorCode").value("U01"))
               .andExpect(jsonPath("$.message").value("존재하지 않는 회원"));
    }
}

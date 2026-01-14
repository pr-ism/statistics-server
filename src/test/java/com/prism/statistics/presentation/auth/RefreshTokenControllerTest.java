package com.prism.statistics.presentation.auth;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.auth.GenerateTokenService;
import com.prism.statistics.application.auth.dto.response.TokenResponse;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class RefreshTokenControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    GenerateTokenService generateTokenService;

    @Test
    void 토큰_재발급_시_성공_테스트() throws Exception {
        String refreshToken = "old-refresh-token";
        TokenResponse tokenDto = new TokenResponse("new-access-token", "new-refresh-token", "Bearer");

        given(generateTokenService.refreshToken(refreshToken)).willReturn(tokenDto);

        ResultActions resultActions = mockMvc.perform(
                                                     post("/refresh-token")
                                                             .cookie(new Cookie("refreshToken", refreshToken)
                                                     )
                                             )
                                             .andExpect(status().isOk())
                                             .andExpect(cookie().exists("refreshToken"))
                                             .andExpect(cookie().value("refreshToken", tokenDto.refreshToken()))
                                             .andExpect(cookie().httpOnly("refreshToken", true))
                                             .andExpect(cookie().secure("refreshToken", true))
                                             .andExpect(cookie().path("refreshToken", "/"))
                                             .andExpect(cookie().exists("accessToken"))
                                             .andExpect(cookie().value("accessToken", tokenDto.accessToken()))
                                             .andExpect(cookie().httpOnly("accessToken", true))
                                             .andExpect(cookie().secure("accessToken", true))
                                             .andExpect(cookie().path("accessToken", "/"))
                                             .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, iterableWithSize(2)))
                                             .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(allOf(
                                                     containsString("refreshToken="),
                                                     containsString("Max-Age="),
                                                     containsString("SameSite=None")
                                             ))))
                                             .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(allOf(
                                                     containsString("accessToken="),
                                                     containsString("Max-Age="),
                                                     containsString("SameSite=None")
                                             ))));

        토큰_재발급_문서화(resultActions);
    }

    private void 토큰_재발급_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestCookies(
                                cookieWithName("refreshToken").description("토큰 재발급을 위한 refresh token cookie")
                        ),
                        responseCookies(
                                cookieWithName("accessToken").description("새롭게 발급된 access token"),
                                cookieWithName("refreshToken").description("새롭게 발급된 refresh token")
                        )
                )
        );
    }

    @Test
    void refreshToken_쿠키가_없으면_토큰을_재발급_할_수_없다() throws Exception {
        mockMvc.perform(post("/refresh-token")
                        .with(
                                request -> {
                                    request.setCookies((Cookie[]) null);
                                    return request;
                                }
                        )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshToken_쿠키값이_비어_있으면_토큰을_재발급_할_수_없다() throws Exception {
        mockMvc.perform(post("/refresh-token")
                        .cookie(new Cookie("refreshToken", ""))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("A00"))
                .andExpect(jsonPath("$.message").value("토큰 재발급 실패"));
    }
}

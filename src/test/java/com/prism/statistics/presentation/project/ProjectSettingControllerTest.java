package com.prism.statistics.presentation.project;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.project.ProjectSettingService;
import com.prism.statistics.application.project.dto.request.UpdateCoreTimeRequest;
import com.prism.statistics.application.project.dto.request.UpdateSizeGradeThresholdRequest;
import com.prism.statistics.application.project.dto.request.UpdateSizeWeightRequest;
import com.prism.statistics.application.project.dto.response.CoreTimeResponse;
import com.prism.statistics.application.project.dto.response.SizeGradeThresholdResponse;
import com.prism.statistics.application.project.dto.response.SizeWeightResponse;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import java.math.BigDecimal;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@SuppressWarnings("NonAsciiCharacters")
class ProjectSettingControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private ProjectSettingService projectSettingService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 코어타임_설정_조회_성공_테스트() throws Exception {
        // given
        CoreTimeResponse response = new CoreTimeResponse(LocalTime.of(10, 0), LocalTime.of(18, 0));
        given(projectSettingService.findCoreTime(7L, 1L)).willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/projects/1/settings/core-time")
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value("10:00:00"))
                .andExpect(jsonPath("$.endTime").value("18:00:00"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 코어타임_설정_수정_성공_테스트() throws Exception {
        // given
        UpdateCoreTimeRequest request = new UpdateCoreTimeRequest(LocalTime.of(9, 0), LocalTime.of(17, 0));
        CoreTimeResponse response = new CoreTimeResponse(LocalTime.of(9, 0), LocalTime.of(17, 0));
        given(projectSettingService.updateCoreTime(7L, 1L, request)).willReturn(response);

        String requestJson = """
                {"startTime": "09:00:00", "endTime": "17:00:00"}
                """;

        // when & then
        mockMvc.perform(
                        put("/projects/1/settings/core-time")
                                .header("Authorization", "Bearer access-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value("09:00:00"))
                .andExpect(jsonPath("$.endTime").value("17:00:00"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 사이즈_가중치_설정_조회_성공_테스트() throws Exception {
        // given
        SizeWeightResponse response = new SizeWeightResponse(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
        given(projectSettingService.findSizeWeight(7L, 1L)).willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/projects/1/settings/size-weight")
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.additionWeight").value(1))
                .andExpect(jsonPath("$.deletionWeight").value(1))
                .andExpect(jsonPath("$.fileWeight").value(1));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 사이즈_가중치_설정_수정_성공_테스트() throws Exception {
        // given
        UpdateSizeWeightRequest request = new UpdateSizeWeightRequest(
                new BigDecimal("2.0"), new BigDecimal("0.5"), new BigDecimal("3.0")
        );
        SizeWeightResponse response = new SizeWeightResponse(
                new BigDecimal("2.0"), new BigDecimal("0.5"), new BigDecimal("3.0")
        );
        given(projectSettingService.updateSizeWeight(7L, 1L, request)).willReturn(response);

        // when & then
        mockMvc.perform(
                        put("/projects/1/settings/size-weight")
                                .header("Authorization", "Bearer access-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.additionWeight").value(2.0))
                .andExpect(jsonPath("$.deletionWeight").value(0.5))
                .andExpect(jsonPath("$.fileWeight").value(3.0));
    }

    @Test
    void 인증_정보가_없으면_코어타임을_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/1/settings/core-time")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    void 인증_정보가_없으면_사이즈_가중치를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/1/settings/size-weight")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 사이즈_등급_임계값_조회_성공_테스트() throws Exception {
        // given
        SizeGradeThresholdResponse response = new SizeGradeThresholdResponse(10, 100, 300, 1000);
        given(projectSettingService.findSizeGradeThreshold(7L, 1L)).willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/projects/1/settings/size-grade-threshold")
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sThreshold").value(10))
                .andExpect(jsonPath("$.mThreshold").value(100))
                .andExpect(jsonPath("$.lThreshold").value(300))
                .andExpect(jsonPath("$.xlThreshold").value(1000));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 사이즈_등급_임계값_수정_성공_테스트() throws Exception {
        // given
        UpdateSizeGradeThresholdRequest request = new UpdateSizeGradeThresholdRequest(20, 200, 500, 2000);
        SizeGradeThresholdResponse response = new SizeGradeThresholdResponse(20, 200, 500, 2000);
        given(projectSettingService.updateSizeGradeThreshold(7L, 1L, request)).willReturn(response);

        // when & then
        mockMvc.perform(
                        put("/projects/1/settings/size-grade-threshold")
                                .header("Authorization", "Bearer access-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sThreshold").value(20))
                .andExpect(jsonPath("$.mThreshold").value(200))
                .andExpect(jsonPath("$.lThreshold").value(500))
                .andExpect(jsonPath("$.xlThreshold").value(2000));
    }

    @Test
    void 인증_정보가_없으면_사이즈_등급_임계값을_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/1/settings/size-grade-threshold")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }
}

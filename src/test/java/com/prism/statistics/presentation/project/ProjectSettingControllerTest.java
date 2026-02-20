package com.prism.statistics.presentation.project;

import static com.prism.statistics.docs.RestDocsConfiguration.field;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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
import org.springframework.test.web.servlet.ResultActions;

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
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/settings/core-time", 1L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value("10:00:00"))
                .andExpect(jsonPath("$.endTime").value("18:00:00"));

        코어타임_설정_조회_문서화(resultActions);
    }

    private void 코어타임_설정_조회_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 ID")
                        ),
                        responseFields(
                                fieldWithPath("startTime").description("코어타임 시작 시간 (HH:mm:ss)"),
                                fieldWithPath("endTime").description("코어타임 종료 시간 (HH:mm:ss)")
                        )
                )
        );
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
        ResultActions resultActions = mockMvc.perform(
                        put("/projects/{projectId}/settings/core-time", 1L)
                                .header("Authorization", "Bearer access-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value("09:00:00"))
                .andExpect(jsonPath("$.endTime").value("17:00:00"));

        코어타임_설정_수정_문서화(resultActions);
    }

    private void 코어타임_설정_수정_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 ID")
                        ),
                        requestFields(
                                fieldWithPath("startTime").description("코어타임 시작 시간 (HH:mm:ss)")
                                        .attributes(field("constraints", "필수, 종료 시간보다 이전이어야 함")),
                                fieldWithPath("endTime").description("코어타임 종료 시간 (HH:mm:ss)")
                                        .attributes(field("constraints", "필수, 시작 시간보다 이후이어야 함"))
                        ),
                        responseFields(
                                fieldWithPath("startTime").description("수정된 코어타임 시작 시간 (HH:mm:ss)"),
                                fieldWithPath("endTime").description("수정된 코어타임 종료 시간 (HH:mm:ss)")
                        )
                )
        );
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 사이즈_가중치_설정_조회_성공_테스트() throws Exception {
        // given
        SizeWeightResponse response = new SizeWeightResponse(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
        given(projectSettingService.findSizeWeight(7L, 1L)).willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/settings/size-weight", 1L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.additionWeight").value(1))
                .andExpect(jsonPath("$.deletionWeight").value(1))
                .andExpect(jsonPath("$.fileWeight").value(1));

        사이즈_가중치_설정_조회_문서화(resultActions);
    }

    private void 사이즈_가중치_설정_조회_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 ID")
                        ),
                        responseFields(
                                fieldWithPath("additionWeight").description("추가 라인 가중치"),
                                fieldWithPath("deletionWeight").description("삭제 라인 가중치"),
                                fieldWithPath("fileWeight").description("파일 가중치")
                        )
                )
        );
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
        ResultActions resultActions = mockMvc.perform(
                        put("/projects/{projectId}/settings/size-weight", 1L)
                                .header("Authorization", "Bearer access-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.additionWeight").value(2.0))
                .andExpect(jsonPath("$.deletionWeight").value(0.5))
                .andExpect(jsonPath("$.fileWeight").value(3.0));

        사이즈_가중치_설정_수정_문서화(resultActions);
    }

    private void 사이즈_가중치_설정_수정_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 ID")
                        ),
                        requestFields(
                                fieldWithPath("additionWeight").description("추가 라인 가중치")
                                        .attributes(field("constraints", "필수, 0보다 커야 함")),
                                fieldWithPath("deletionWeight").description("삭제 라인 가중치")
                                        .attributes(field("constraints", "필수, 0보다 커야 함")),
                                fieldWithPath("fileWeight").description("파일 가중치")
                                        .attributes(field("constraints", "필수, 0보다 커야 함"))
                        ),
                        responseFields(
                                fieldWithPath("additionWeight").description("수정된 추가 라인 가중치"),
                                fieldWithPath("deletionWeight").description("수정된 삭제 라인 가중치"),
                                fieldWithPath("fileWeight").description("수정된 파일 가중치")
                        )
                )
        );
    }

    @Test
    void 인증_정보가_없으면_코어타임을_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/settings/core-time", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    void 인증_정보가_없으면_사이즈_가중치를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/settings/size-weight", 1L)
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
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/settings/size-grade-threshold", 1L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sThreshold").value(10))
                .andExpect(jsonPath("$.mThreshold").value(100))
                .andExpect(jsonPath("$.lThreshold").value(300))
                .andExpect(jsonPath("$.xlThreshold").value(1000));

        사이즈_등급_임계값_조회_문서화(resultActions);
    }

    private void 사이즈_등급_임계값_조회_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 ID")
                        ),
                        responseFields(
                                fieldWithPath("sThreshold").description("S 등급 임계값"),
                                fieldWithPath("mThreshold").description("M 등급 임계값"),
                                fieldWithPath("lThreshold").description("L 등급 임계값"),
                                fieldWithPath("xlThreshold").description("XL 등급 임계값")
                        )
                )
        );
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 사이즈_등급_임계값_수정_성공_테스트() throws Exception {
        // given
        UpdateSizeGradeThresholdRequest request = new UpdateSizeGradeThresholdRequest(20, 200, 500, 2000);
        SizeGradeThresholdResponse response = new SizeGradeThresholdResponse(20, 200, 500, 2000);
        given(projectSettingService.updateSizeGradeThreshold(7L, 1L, request)).willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        put("/projects/{projectId}/settings/size-grade-threshold", 1L)
                                .header("Authorization", "Bearer access-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sThreshold").value(20))
                .andExpect(jsonPath("$.mThreshold").value(200))
                .andExpect(jsonPath("$.lThreshold").value(500))
                .andExpect(jsonPath("$.xlThreshold").value(2000));

        사이즈_등급_임계값_수정_문서화(resultActions);
    }

    private void 사이즈_등급_임계값_수정_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 ID")
                        ),
                        requestFields(
                                fieldWithPath("sThreshold").description("S 등급 임계값")
                                        .attributes(field("constraints", "필수, 0보다 커야 함")),
                                fieldWithPath("mThreshold").description("M 등급 임계값")
                                        .attributes(field("constraints", "필수, 0보다 커야 함")),
                                fieldWithPath("lThreshold").description("L 등급 임계값")
                                        .attributes(field("constraints", "필수, 0보다 커야 함")),
                                fieldWithPath("xlThreshold").description("XL 등급 임계값")
                                        .attributes(field("constraints", "필수, 0보다 커야 함"))
                        ),
                        responseFields(
                                fieldWithPath("sThreshold").description("수정된 S 등급 임계값"),
                                fieldWithPath("mThreshold").description("수정된 M 등급 임계값"),
                                fieldWithPath("lThreshold").description("수정된 L 등급 임계값"),
                                fieldWithPath("xlThreshold").description("수정된 XL 등급 임계값")
                        )
                )
        );
    }

    @Test
    void 인증_정보가_없으면_사이즈_등급_임계값을_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/settings/size-grade-threshold", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }
}

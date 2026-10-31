package com.company.aneon.f01;

import java.util.UUID;

import com.company.aneon.AbstractMySqlIntegrationTest;
import com.company.aneon.TestSeed;
import com.company.aneon.f01.repository.KhuVucRepository;
import com.company.aneon.f01.repository.TangRepository;
import com.company.aneon.f01.repository.ToaNhaRepository;
import com.company.aneon.f01.repository.ViTriRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Luong web F01 + hop dong loi/envelope tren MySQL that. Danh tinh local stub
 * qua header (application-test.yml, D0008). Ma ban ghi duy nhat moi lan chay
 * de IT dung chung DB (qldapm_aneon_it) khong dau voi du lieu cu.
 */
@AutoConfigureMockMvc
class F01WebFlowIT extends AbstractMySqlIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ToaNhaRepository toaNhaRepository;
    @Autowired TangRepository tangRepository;
    @Autowired KhuVucRepository khuVucRepository;
    @Autowired ViTriRepository viTriRepository;
    @Autowired JdbcTemplate jdbc;

    private TestSeed.SpaceChain chain;
    private final UUID actorId = UUID.randomUUID();
    private String code1;
    private String code2;
    private String code4;
    private String code5;

    @BeforeEach
    void seed() {
        TestSeed.role(jdbc, "MANAGEMENT");
        TestSeed.role(jdbc, "OPERATIONS");
        TestSeed.role(jdbc, "CUSTOMER");
        TestSeed.account(jdbc, actorId, "MANAGEMENT", null);
        chain = TestSeed.space(toaNhaRepository, tangRepository, khuVucRepository, viTriRepository);
        String suffix = Long.toString(System.nanoTime() % 100000000L);
        code1 = "MBWEB1" + suffix;
        code2 = "MBWEB2" + suffix;
        code4 = "MBWEB4" + suffix;
        code5 = "MBWEB5" + suffix;
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void khongXacThucTra401VoiEnvelopeChuan() throws Exception {
        mockMvc.perform(get("/api/v1/f01/premises"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED"));
    }

    @Test
    void taoMatBang201VaEnvelopeChuan() throws Exception {
        String body = """
                {"code":"%s","name":"Mat bang web","zoneId":"%s",
                 "totalArea":1000,"status":"VACANT","type":"STORE"}
                """.formatted(code1, chain.zoneId());
        mockMvc.perform(post("/api/v1/f01/premises")
                        .header("X-Actor-Id", actorId)
                        .header("X-Actor-Roles", "MANAGEMENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data.code").value(code1))
                .andExpect(jsonPath("$.data.status").value("VACANT"));
    }

    @Test
    void taoMatBangTrungMaTra409BusinessConflict() throws Exception {
        String body = """
                {"code":"%s","name":"Mat bang web 2","zoneId":"%s",
                 "totalArea":1000,"status":"VACANT"}
                """.formatted(code2, chain.zoneId());
        for (int i = 0; i < 2; i++) {
            var resultActions = mockMvc.perform(post("/api/v1/f01/premises")
                    .header("X-Actor-Id", actorId)
                    .header("X-Actor-Roles", "MANAGEMENT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));
            if (i == 0) {
                resultActions.andExpect(status().isCreated());
            } else {
                resultActions.andExpect(status().isConflict())
                        .andExpect(jsonPath("$.errorCode").value("BUSINESS_CONFLICT"));
            }
        }
    }

    @Test
    void enumTiengVietKhongDuocNhanTra400() throws Exception {
        String body = """
                {"code":"MBWEB3","name":"Mat bang web 3","zoneId":"%s",
                 "totalArea":1000,"status":"TRONG"}
                """.formatted(chain.zoneId());
        mockMvc.perform(post("/api/v1/f01/premises")
                        .header("X-Actor-Id", actorId)
                        .header("X-Actor-Roles", "MANAGEMENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void khongCoQuyenTra403() throws Exception {
        mockMvc.perform(get("/api/v1/f01/premises")
                        .header("X-Actor-Id", UUID.randomUUID())
                        .header("X-Actor-Roles", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void capNhatTrangThaiGhiLichSuCungGiaoDich() throws Exception {
        String createBody = """
                {"code":"%s","name":"Mat bang web 4","zoneId":"%s",
                 "totalArea":500,"status":"VACANT"}
                """.formatted(code4, chain.zoneId());
        String response = mockMvc.perform(post("/api/v1/f01/premises")
                        .header("X-Actor-Id", actorId)
                        .header("X-Actor-Roles", "MANAGEMENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        String premiseId = com.jayway.jsonpath.JsonPath.read(response, "$.data.id");

        // OPERATIONS co f01:status:write
        mockMvc.perform(patch("/api/v1/f01/premises/" + premiseId + "/status")
                        .header("X-Actor-Id", actorId)
                        .header("X-Actor-Roles", "OPERATIONS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"LEASED","reason":"Cho thue IT","version":0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStatus").value("LEASED"));

        mockMvc.perform(get("/api/v1/f01/premises/" + premiseId + "/history")
                        .header("X-Actor-Id", actorId)
                        .header("X-Actor-Roles", "MANAGEMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].action").value("UPDATE_STATUS"))
                .andExpect(jsonPath("$.data.content[0].newValueJson").exists());
    }

    @Test
    void publicSearchChoPhepVoiDanhTacKhongXacThuc() throws Exception {
        mockMvc.perform(get("/api/v1/f10/promotions-events/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void phanTrangSaiTra400() throws Exception {
        mockMvc.perform(get("/api/v1/f01/premises")
                        .header("X-Actor-Id", actorId)
                        .header("X-Actor-Roles", "MANAGEMENT")
                        .param("size", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void utilizationSummaryVaContent() throws Exception {
        String createBody = """
                {"code":"%s","name":"Mat bang web 5","zoneId":"%s",
                 "totalArea":800,"status":"LEASED"}
                """.formatted(code5, chain.zoneId());
        mockMvc.perform(post("/api/v1/f01/premises")
                        .header("X-Actor-Id", actorId)
                        .header("X-Actor-Roles", "MANAGEMENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/f01/utilization")
                        .header("X-Actor-Id", actorId)
                        .header("X-Actor-Roles", "MANAGEMENT")
                        .param("status", "LEASED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.total").isNumber())
                .andExpect(jsonPath("$.data.summary.occupied").isNumber())
                .andExpect(jsonPath("$.data.content").isArray());
    }
}

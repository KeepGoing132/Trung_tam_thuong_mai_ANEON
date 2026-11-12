package com.company.aneon.shared.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.company.aneon.AbstractMySqlIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@AutoConfigureMockMvc
class AuthFlowIT extends AbstractMySqlIntegrationTest {
    private static final String PASSWORD = "Mat-khau-hop-le-123";
    private static final String NEW_PASSWORD = "Mat-khau-moi-456";
    @Autowired MockMvc mvc;
    @Autowired AuthService auth;
    @Autowired ObjectMapper json;

    @Test
    void customerRegistrationRecoveryAndLogout() throws Exception {
        String name = "kh-" + UUID.randomUUID();
        var data = new AuthDtos.Register(name, "Khach Demo", "", PASSWORD);
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(data)))
                .andExpect(status().isForbidden());
        Cookie session = session(mvc.perform(body(post("/api/v1/auth/register"), data)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("CUSTOMER")).andReturn());
        mvc.perform(get("/api/v1/auth/me").cookie(session)).andExpect(status().isOk()).andExpect(jsonPath("$.data.username").value(name));
        mvc.perform(get("/api/v1/f01/premises").cookie(session).header("X-Actor-Id", UUID.randomUUID())
                .header("X-Actor-Roles", "MANAGEMENT")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/auth/logout").cookie(session)).andExpect(status().isForbidden());
        mvc.perform(web(post("/api/v1/auth/logout")).cookie(session).header("Sec-Fetch-Site", "cross-site")).andExpect(status().isForbidden());

        var codeResult = mvc.perform(body(post("/api/v1/auth/recovery-code"), new AuthDtos.ConfirmPassword(PASSWORD)).cookie(session))
                .andExpect(status().isOk()).andReturn();
        String code = json.readTree(codeResult.getResponse().getContentAsString()).path("data").path("code").asText();
        var recovery = new AuthDtos.Recovery(name, code, NEW_PASSWORD);
        mvc.perform(body(post("/api/v1/auth/recover"), recovery)).andExpect(status().isOk());
        mvc.perform(body(post("/api/v1/auth/recover"), recovery)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/auth/me").cookie(session)).andExpect(status().isUnauthorized());
        mvc.perform(body(post("/api/v1/auth/login"), new AuthDtos.Login(name, PASSWORD))).andExpect(status().isUnauthorized());
        Cookie newSession = login(name, NEW_PASSWORD);
        mvc.perform(web(post("/api/v1/auth/logout")).cookie(newSession)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/auth/me").cookie(newSession)).andExpect(status().isUnauthorized());
    }

    @Test
    void adminProvisioningPasswordRotationAndAccountLock() throws Exception {
        String adminName = "ad-" + UUID.randomUUID();
        auth.bootstrapAdmin(adminName, PASSWORD);
        Cookie admin = login(adminName, PASSWORD);
        mvc.perform(get("/api/v1/auth/staff").cookie(admin)).andExpect(status().isForbidden());
        Cookie changedAdmin = changePassword(admin, PASSWORD, NEW_PASSWORD);
        mvc.perform(get("/api/v1/auth/me").cookie(admin)).andExpect(status().isUnauthorized());

        String username = "nv-" + UUID.randomUUID();
        var request = new AuthDtos.StaffRequest(username, "Nhan vien Demo", null, "MANAGEMENT");
        var staff = mvc.perform(body(post("/api/v1/auth/staff"), request).cookie(changedAdmin)).andExpect(status().isCreated()).andReturn();
        var credentials = json.readTree(staff.getResponse().getContentAsString()).path("data");
        String temporary = credentials.path("temporaryPassword").asText();
        String id = credentials.path("account").path("id").asText();
        Cookie first = login(username, temporary);
        mvc.perform(get("/api/v1/f01/premises").cookie(first)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("PASSWORD_CHANGE_REQUIRED"));
        Cookie changed = changePassword(first, temporary, PASSWORD);
        mvc.perform(get("/api/v1/f01/premises").cookie(changed)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/auth/staff").cookie(changed)).andExpect(status().isForbidden());
        mvc.perform(body(put("/api/v1/auth/staff/" + id + "/status"), new AuthDtos.SetAccountStatus(true)).cookie(changedAdmin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("LOCKED"));
        mvc.perform(get("/api/v1/f01/premises").cookie(changed)).andExpect(status().isUnauthorized());
        mvc.perform(body(post("/api/v1/auth/login"), new AuthDtos.Login(username, PASSWORD))).andExpect(status().isUnauthorized());
    }

    @Test
    void rateLimitCountsFailedTransactions() throws Exception {
        var login = new AuthDtos.Login("unknown-" + UUID.randomUUID(), PASSWORD);
        for (int i = 0; i < 8; i++) mvc.perform(body(post("/api/v1/auth/login"), login)).andExpect(status().isUnauthorized());
        mvc.perform(body(post("/api/v1/auth/login"), login)).andExpect(status().isTooManyRequests());
    }

    private Cookie login(String name, String password) throws Exception {
        return session(mvc.perform(body(post("/api/v1/auth/login"), new AuthDtos.Login(name, password))).andExpect(status().isOk()).andReturn());
    }

    private Cookie changePassword(Cookie cookie, String current, String replacement) throws Exception {
        return session(mvc.perform(body(put("/api/v1/auth/password"), new AuthDtos.ChangePassword(current, replacement)).cookie(cookie))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.mustChangePassword").value(false)).andReturn());
    }

    private MockHttpServletRequestBuilder body(MockHttpServletRequestBuilder request, Object value) throws Exception {
        return web(request).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(value));
    }

    private static MockHttpServletRequestBuilder web(MockHttpServletRequestBuilder request) {
        return request.header("X-ANEON-Request", "aneon-web");
    }

    private static Cookie session(MvcResult result) {
        String cookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(cookie).startsWith("aneon_session=").contains("HttpOnly", "SameSite=Lax");
        return new Cookie("aneon_session", cookie.split(";", 2)[0].substring("aneon_session=".length()));
    }
}

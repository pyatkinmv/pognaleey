package ru.pyatkinmv.pognaleey.security;

import jakarta.annotation.Nullable;
import lombok.SneakyThrows;
import org.flywaydb.core.internal.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.pyatkinmv.pognaleey.DatabaseCleaningTest;
import ru.pyatkinmv.pognaleey.dto.ManualGuidesCreateDtoList;
import ru.pyatkinmv.pognaleey.dto.UserDto;
import ru.pyatkinmv.pognaleey.model.UserRole;
import ru.pyatkinmv.pognaleey.repository.UserRepository;
import ru.pyatkinmv.pognaleey.util.Utils;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class JwtSecurityTest extends DatabaseCleaningTest {
    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @Test
    public void accessSecuredEndpointWithValidToken() throws Exception {
        var user = registerUser();
        var token = loginAndReturnToken(user.username(), PASSWORD);
        accessSecuredEndpoint(token).andExpect(status().isOk());
    }

    @Test
    public void accessSecuredEndpointWithoutToken() throws Exception {
        accessSecuredEndpoint(null).andExpect(status().isOk());
    }

    @Test
    public void accessSecuredEndpointWithInvalidToken() throws Exception {
        accessSecuredEndpoint("invalid-header.payload.signature").andExpect(status().isForbidden());
    }

    @SneakyThrows
    private String loginAndReturnToken(String username, String password) {
        String loginRequestJson = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

        return mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    public void accessAdminEndpointWithAdminToken() throws Exception {
        var admin = userRepository.findByUsername(registerUser().username())
                .map(it -> {
                    it.setRole(UserRole.ADMIN);
                    return userRepository.save(it);
                }).orElseThrow();
        var adminToken = loginAndReturnToken(admin.getUsername(), PASSWORD);
        accessAdminEndpoint(adminToken).andExpect(status().isOk());
    }

    @Test
    public void accessAdminEndpointWithRegularUserToken() throws Exception {
        var user = registerUser();
        var token = loginAndReturnToken(user.username(), PASSWORD);
        accessAdminEndpoint(token).andExpect(status().isForbidden());
    }

    @Test
    public void accessAdminEndpointWithoutToken() throws Exception {
        accessAdminEndpoint(null).andExpect(status().isForbidden());
    }

    @SneakyThrows
    private UserDto registerUser() {
        String registerRequest = "{\"username\": \"test-user\", \"password\": \"password123\"}";

        var responseString = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonUtils.parseJson(responseString, UserDto.class);
    }

    @SneakyThrows
    private ResultActions accessSecuredEndpoint(@Nullable String token) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (token != null) {
            headers.setBearerAuth(token);
        }

        return mockMvc.perform(post("/travel-inquiries")
                .headers(headers)
                .content("{\"preferences\": \"food\", \"to\": \"asia\"}"));
    }

    @SneakyThrows
    private ResultActions accessAdminEndpoint(@Nullable String token) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        var content = new ManualGuidesCreateDtoList(List.of());

        return mockMvc.perform(post("/admin/createGuides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Utils.toJson(content))
                .headers(headers));
    }
}



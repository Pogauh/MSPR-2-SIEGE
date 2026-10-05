package com.backendSiege.api.controller;

import com.backendSiege.api.mapper.UserMapperImpl;
import com.backendSiege.api.security.JwtAuthenticationFilter;
import com.backendSiege.domain.exceptions.IdentifiantsInvalidesException;
import com.backendSiege.domain.exceptions.RoleInterditALInscriptionException;
import com.backendSiege.domain.models.Role;
import com.backendSiege.domain.models.User;
import com.backendSiege.domain.services.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(UserMapperImpl.class)
class AuthControllerTest {

    private static final UUID ID = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    AuthService authService;

    @Test
    void register_transmet_les_informations_et_renvoie_le_jeton() throws Exception {
        when(authService.register("neo@siege.fr", "secret", Role.ROLE_QUALITY)).thenReturn("jeton-qualite");

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"neo@siege.fr\",\"password\":\"secret\",\"role\":\"ROLE_QUALITY\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"token\":\"jeton-qualite\"}"));
    }

    @Test
    void register_en_administrateur_refuse_par_le_service_est_interdit() throws Exception {
        when(authService.register("neo@siege.fr", "secret", Role.ROLE_ADMIN)).thenThrow(new RoleInterditALInscriptionException(Role.ROLE_ADMIN));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"neo@siege.fr\",\"password\":\"secret\",\"role\":\"ROLE_ADMIN\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    void register_sans_role_transmet_un_role_nul_au_service() throws Exception {
        when(authService.register("neo@siege.fr", "secret", null)).thenReturn("jeton-hq");

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"neo@siege.fr\",\"password\":\"secret\"}"))
                .andExpect(content().string("{\"token\":\"jeton-hq\"}"));
    }

    @Test
    void register_avec_un_role_inconnu_est_une_requete_invalide() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"neo@siege.fr\",\"password\":\"secret\",\"role\":\"ROLE_INCONNU\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_renvoie_le_jeton_et_le_depose_dans_un_cookie_httponly_d_un_jour() throws Exception {
        when(authService.login("neo@siege.fr", "secret")).thenReturn("jeton");

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"neo@siege.fr\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"token\":\"jeton\"}"))
                .andExpect(cookie().value("token", "jeton"))
                .andExpect(cookie().httpOnly("token", true))
                .andExpect(cookie().path("token", "/"))
                .andExpect(cookie().maxAge("token", 86_400));
    }

    @Test
    void login_refuse_laisse_remonter_l_erreur_et_ne_pose_pas_de_cookie() throws Exception {
        when(authService.login("neo@siege.fr", "faux")).thenThrow(new IdentifiantsInvalidesException());

        assertThatThrownBy(() -> mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"neo@siege.fr\",\"password\":\"faux\"}")))
                .hasRootCauseInstanceOf(IdentifiantsInvalidesException.class);
    }

    @Test
    void me_renvoie_l_utilisateur_connecte_sans_son_mot_de_passe() throws Exception {
        when(authService.getCurrentUser("neo@siege.fr")).thenReturn(new User(ID, "neo@siege.fr", "hache", Set.of(Role.ROLE_QUALITY), "colombie"));

        mockMvc.perform(get("/api/auth/me").principal(new UsernamePasswordAuthenticationToken("neo@siege.fr", null)))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":\"" + ID + "\",\"email\":\"neo@siege.fr\",\"roles\":[\"ROLE_QUALITY\"],\"countryId\":\"colombie\"}"));
    }

    @Test
    void me_d_un_utilisateur_sans_pays_renvoie_un_pays_nul() throws Exception {
        when(authService.getCurrentUser("neo@siege.fr")).thenReturn(new User(ID, "neo@siege.fr", "hache", Set.of(Role.ROLE_HQ), null));

        mockMvc.perform(get("/api/auth/me").principal(new UsernamePasswordAuthenticationToken("neo@siege.fr", null)))
                .andExpect(content().string("{\"id\":\"" + ID + "\",\"email\":\"neo@siege.fr\",\"roles\":[\"ROLE_HQ\"],\"countryId\":null}"));
    }

    @Test
    void le_cookie_du_jeton_est_la_seule_chose_posee_par_la_connexion() throws Exception {
        when(authService.login("neo@siege.fr", "secret")).thenReturn("jeton");

        Cookie[] cookies = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"neo@siege.fr\",\"password\":\"secret\"}"))
                .andReturn().getResponse().getCookies();

        assertThat(cookies).extracting(Cookie::getName).containsExactly("token");
    }
}

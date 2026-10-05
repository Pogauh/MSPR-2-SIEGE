package com.backendSiege.infrastructure.security;

import com.backendSiege.domain.models.Role;
import com.backendSiege.domain.models.User;
import com.backendSiege.infrastructure.config.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenAdapterTest {

    private static final String SECRET = "1234567890123456789012345678901234567890123456789012345678901234";
    private static final User MARIE = new User(UUID.randomUUID(), "marie@siege.fr", "hache", Set.of(Role.ROLE_QUALITY), "colombie");
    private static final User PAUL = new User(UUID.randomUUID(), "paul@siege.fr", "hache", Set.of(Role.ROLE_HQ), null);

    private final JwtTokenAdapter adapter = new JwtTokenAdapter(new JwtProperties(SECRET, Duration.ofHours(24)));

    @Test
    void le_jeton_porte_l_email_les_roles_et_une_validite_d_un_jour() {
        String jeton = adapter.generate(MARIE);

        var claims = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).build()
                .parseSignedClaims(jeton).getPayload();
        assertThat(claims.getSubject()).isEqualTo("marie@siege.fr");
        assertThat(claims.get("roles", List.class)).containsExactly("ROLE_QUALITY");
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime()).isEqualTo(Duration.ofHours(24).toMillis());
    }

    @Test
    void le_jeton_est_signe_en_hs512_avec_la_cle_historique() {
        String enTete = new String(Base64.getUrlDecoder().decode(adapter.generate(MARIE).split("\\.")[0]), StandardCharsets.UTF_8);

        assertThat(enTete).contains("\"alg\":\"HS512\"");
    }

    @Test
    void extractEmail_lit_le_sujet_du_jeton() {
        assertThat(adapter.extractEmail(adapter.generate(MARIE))).isEqualTo("marie@siege.fr");
    }

    @Test
    void isValid_accepte_le_jeton_de_son_proprietaire() {
        assertThat(adapter.isValid(adapter.generate(MARIE), MARIE)).isTrue();
    }

    @Test
    void isValid_refuse_le_jeton_d_un_autre_utilisateur() {
        assertThat(adapter.isValid(adapter.generate(MARIE), PAUL)).isFalse();
    }

    @Test
    void un_jeton_expire_est_refuse_a_la_lecture() {
        JwtTokenAdapter eclair = new JwtTokenAdapter(new JwtProperties(SECRET, Duration.ofSeconds(-60)));

        String perime = eclair.generate(MARIE);

        assertThatThrownBy(() -> adapter.extractEmail(perime)).isInstanceOf(ExpiredJwtException.class);
        assertThatThrownBy(() -> adapter.isValid(perime, MARIE)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void un_jeton_signe_avec_une_autre_cle_est_refuse() {
        JwtTokenAdapter autre = new JwtTokenAdapter(new JwtProperties("A".repeat(64), Duration.ofHours(1)));

        assertThatThrownBy(() -> adapter.extractEmail(autre.generate(MARIE))).isInstanceOf(JwtException.class);
    }

    @Test
    void un_jeton_illisible_ou_sans_signature_est_refuse() {
        String sansSignature = Jwts.builder().subject("marie@siege.fr").expiration(new Date(System.currentTimeMillis() + 60_000)).compact();

        assertThatThrownBy(() -> adapter.extractEmail("pas.un.jeton")).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> adapter.extractEmail(sansSignature)).isInstanceOf(JwtException.class);
    }

    @Test
    void une_cle_trop_courte_est_refusee_des_la_creation() {
        assertThatThrownBy(() -> new JwtTokenAdapter(new JwtProperties("trop-courte", Duration.ofHours(1))))
                .isInstanceOf(io.jsonwebtoken.security.WeakKeyException.class);
    }
}

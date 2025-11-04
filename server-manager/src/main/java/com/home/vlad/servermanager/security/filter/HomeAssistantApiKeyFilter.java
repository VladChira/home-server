package com.home.vlad.servermanager.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HomeAssistantApiKeyFilter extends OncePerRequestFilter {

    @Value("${homeassistant.api_key}")
    private String apiKey;

    private boolean isBypassableEndpoint(HttpServletRequest req) {
        return "POST".equals(req.getMethod())
                && (req.getRequestURI().equals("/manage/api/v1/llm/prompt") || req.getRequestURI().equals("/manage/api/v1/voice/command"));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res,
            @NonNull FilterChain chain)
            throws ServletException, IOException {

        if (isBypassableEndpoint(req)) {

            String auth = req.getHeader("Authorization");
            if (auth != null && auth.startsWith("Manager-Key ")) {
                String key = auth.substring("Manager-Key ".length()).trim();

                if (!key.isEmpty() && key.equals(apiKey)) {
                    // authenticate as Home Assistant service account
                    log.info("Authenticated Home Assistant API key access from {}",
                            req.getRemoteAddr());
                    AbstractAuthenticationToken authToken = new AbstractAuthenticationToken(
                            List.of(new SimpleGrantedAuthority("HOMEASSISTANT"))) {
                        @Override
                        public Object getCredentials() {
                            return "";
                        }

                        @Override
                        public Object getPrincipal() {
                            return "homeassistant";
                        }

                        @Override
                        public boolean isAuthenticated() {
                            return true;
                        }
                    };
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        chain.doFilter(req, res);
    }
}

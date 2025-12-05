package com.home.vlad.servermanager.security.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PrometheusApiKeyFilter extends OncePerRequestFilter {
    @Value("${prometheus.api_key}")
    private String apiKey;

    private boolean isBypassableEndpoint(HttpServletRequest req) {
        return "GET".equals(req.getMethod())
                && (req.getRequestURI().equals("/actuator/prometheus"));
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
                    // authenticate as Prometheus service account
                    AbstractAuthenticationToken authToken = new AbstractAuthenticationToken(
                            List.of(new SimpleGrantedAuthority("PROMETHEUS"))) {
                        @Override
                        public Object getCredentials() {
                            return "";
                        }

                        @Override
                        public Object getPrincipal() {
                            return "prometheus";
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

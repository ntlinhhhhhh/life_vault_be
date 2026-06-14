package com.moon.api_gateway.routes;

import com.moon.api_gateway.exception.GatewayException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class Routes {

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade",
            "host",
            "content-length"
    );

    private final RestTemplate restTemplate;

    @Value("${gateway.routes.auth-service-url:http://localhost:5001}")
    private String authServiceUrl;

    @Value("${gateway.routes.vault-service-url:http://localhost:5002}")
    private String vaultServiceUrl;

    @RequestMapping({"/api/auth", "/api/auth/**"})
    public ResponseEntity<byte[]> routeAuth(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return forward(request, body, authServiceUrl);
    }

    @RequestMapping({"/api/vault", "/api/vault/**"})
    public ResponseEntity<byte[]> routeVault(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return forward(request, body, vaultServiceUrl);
    }

    private ResponseEntity<byte[]> forward(HttpServletRequest request, byte[] body, String upstreamBaseUrl) {
        URI uri = upstreamUri(request, upstreamBaseUrl);
        HttpHeaders headers = requestHeaders(request);
        HttpEntity<byte[]> entity = new HttpEntity<>(body == null ? new byte[0] : body, headers);
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    uri,
                    HttpMethod.valueOf(request.getMethod()),
                    entity,
                    byte[].class
            );
            return ResponseEntity
                    .status(response.getStatusCode())
                    .headers(responseHeaders(response.getHeaders()))
                    .body(response.getBody());
        } catch (RestClientException exception) {
            throw new GatewayException(HttpStatus.SERVICE_UNAVAILABLE, "GW-503", "Upstream service is unavailable");
        }
    }

    private URI upstreamUri(HttpServletRequest request, String upstreamBaseUrl) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String upstreamPath = path.startsWith("/api") ? path.substring("/api".length()) : path;
        return UriComponentsBuilder
                .fromUriString(trimTrailingSlash(upstreamBaseUrl))
                .path(upstreamPath)
                .query(request.getQueryString())
                .build(true)
                .toUri();
    }

    private HttpHeaders requestHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(name -> {
            if (!isHopByHop(name)) {
                headers.put(name, Collections.list(request.getHeaders(name)));
            }
        });
        headers.set("X-Forwarded-For", forwardedFor(request));
        headers.set("X-Forwarded-Host", request.getHeader(HttpHeaders.HOST));
        headers.set("X-Forwarded-Proto", request.getScheme());
        return headers;
    }

    private HttpHeaders responseHeaders(HttpHeaders source) {
        HttpHeaders headers = new HttpHeaders();
        source.forEach((name, values) -> {
            if (!isHopByHop(name)) {
                headers.put(name, values);
            }
        });
        return headers;
    }

    private String forwardedFor(HttpServletRequest request) {
        String existing = request.getHeader("X-Forwarded-For");
        if (existing == null || existing.isBlank()) {
            return request.getRemoteAddr();
        }
        return existing + ", " + request.getRemoteAddr();
    }

    private boolean isHopByHop(String name) {
        return HOP_BY_HOP_HEADERS.contains(name.toLowerCase(Locale.ROOT));
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}

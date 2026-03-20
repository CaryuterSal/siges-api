package dev.spiffocode.sigesapi.common.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        doFilterWrapped(wrapRequest(request), wrapResponse(response), filterChain);
    }

    protected void doFilterWrapped(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
            FilterChain filterChain)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequestResponse(request, response, duration);
            response.copyBodyToResponse();
        }
    }

    private void logRequestResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
            long duration) {
        String queryString = request.getQueryString() == null ? "" : "?" + request.getQueryString();
        String method = request.getMethod();
        String uri = request.getRequestURI() + queryString;
        int status = response.getStatus();

        StringBuilder msg = new StringBuilder();
        msg.append("\n=== INCOMING REQUEST ===\n");
        msg.append(String.format("%s %s %d (%dms)\n", method, uri, status, duration));
        msg.append("Headers: ").append(getHeaders(request)).append("\n");

        String requestBody = getBody(request.getContentAsByteArray(), request.getCharacterEncoding());
        if (!requestBody.isEmpty()) {
            msg.append("Request Body: ").append(requestBody).append("\n");
        }
        String responseBody = getBody(response.getContentAsByteArray(), response.getCharacterEncoding());
        if (!responseBody.isEmpty()) {
            msg.append("Request Body: ").append(responseBody).append("\n");
        }
        msg.append("=========================");
        log.info(msg.toString());
    }

    private String getHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .filter(header -> !header.equalsIgnoreCase("authorization"))
                .map(header -> header + ":" + Collections.list(request.getHeaders(header)))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String getBody(byte[] content, String encoding) {
        if (content == null || content.length == 0) {
            return "";
        }
        try {
            return new String(content, encoding);
        } catch (UnsupportedEncodingException e) {
            return "[unable to parse body]";
        }
    }

    private static ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            return (ContentCachingRequestWrapper) request;
        } else {
            return new ContentCachingRequestWrapper(request, 65536); // Use 64KB limit
        }
    }

    private static ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            return (ContentCachingResponseWrapper) response;
        } else {
            return new ContentCachingResponseWrapper(response);
        }
    }
}

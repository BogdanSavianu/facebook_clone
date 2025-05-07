package com.facebook.post.security.jwt;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    private final RequestMatcher permittedPaths = new OrRequestMatcher(
        new AntPathRequestMatcher("/api/posts/public/**"),
        new AntPathRequestMatcher("/api/posts/upload-image"),
        new AntPathRequestMatcher("/post-images/**"),
        new AntPathRequestMatcher("/api/posts/internal/**")
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (this.permittedPaths.matches(request)) {
            logger.debug("Permitted path accessed: {}, skipping JWT filter", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        
        logger.debug("Processing JWT for path: {}", request.getRequestURI());
        try {
            String jwt = parseJwt(request);
            logger.debug("Request URI: {}, JWT Token present: {}", request.getRequestURI(), jwt != null);
            
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                logger.debug("JWT Token validated for user: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                jwt,
                                userDetails.getAuthorities());
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                logger.debug("Setting authentication in SecurityContext: {}", authentication);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else if (jwt != null) {
                logger.warn("JWT token was present but failed validation");
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        logger.debug("Authorization header: {}", headerAuth != null ? 
                     headerAuth.substring(0, Math.min(20, headerAuth.length())) + "..." : "null");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
} 
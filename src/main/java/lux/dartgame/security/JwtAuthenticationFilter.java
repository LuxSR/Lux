package lux.dartgame.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lux.dartgame.service.JwtService;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public final class JwtAuthenticationFilter implements Filter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            final JwtService jwtServiceParam,
            final UserDetailsService userDetailsServiceParam) {
        this.jwtService = jwtServiceParam;
        this.userDetailsService = userDetailsServiceParam;
    }

    @Override
    public void doFilter(final @NonNull ServletRequest servletRequest,
                         final @NonNull ServletResponse servletResponse,
                         final @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String header = request.getHeader(HEADER);

        // No token — not our business. Let the request through unauthenticated.
        if (header == null || !header.startsWith(PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIX.length());

        try {
            String username = jwtService.extractUsername(token);

            // Already authenticated by an earlier filter? Leave it alone.
            if (username != null && SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                var userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isValid(token, userDetails)) {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,                              // no credentials — already proven
                            userDetails.getAuthorities()
                    );
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // Bad token: leave the context empty and let authorization reject it.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}

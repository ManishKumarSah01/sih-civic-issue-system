package com.sih.civic.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.JwtException;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserDetailsService users;
    public JwtAuthFilter(JwtService jwt, UserDetailsService users){ this.jwt=jwt; this.users=users; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                              FilterChain chain) throws ServletException, IOException {
        String header=request.getHeader("Authorization");
        if(header!=null && header.startsWith("Bearer ") &&
           SecurityContextHolder.getContext().getAuthentication()==null) {
            try {
                String email=jwt.subject(header.substring(7));
                var details=users.loadUserByUsername(email);
                var auth=new UsernamePasswordAuthenticationToken(details,null,details.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException ex) {
                // The request will be treated as unauthenticated by Spring Security.
            } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
                // Account may have been removed after token creation.
            }
        }
        chain.doFilter(request,response);
    }
}

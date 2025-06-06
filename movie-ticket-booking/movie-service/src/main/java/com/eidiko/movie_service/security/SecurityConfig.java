package com.eidiko.movie_service.security;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return
                httpSecurity.csrf(AbstractHttpConfigurer::disable)
                        .authorizeHttpRequests(auth ->auth
                                .requestMatchers("/api/movies/id/{id}" ,"/api/movies/getAllMovies").permitAll()
                                .anyRequest().authenticated())

                        .sessionManagement(session ->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                        .build();
    }

}

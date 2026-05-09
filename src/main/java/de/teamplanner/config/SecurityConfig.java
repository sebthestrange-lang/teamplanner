package de.teamplanner.config;

import de.teamplanner.service.BenutzerDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, BenutzerDetailsService uds) throws Exception {
        http
            .userDetailsService(uds)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/favicon.svg", "/favicon.ico").permitAll()
                .requestMatchers("/benutzer/**").hasRole("ADMIN")
                .requestMatchers("/organisationen/**").hasRole("ADMIN")
                .requestMatchers("/audit/**").hasRole("ADMIN")
                .requestMatchers("/audit/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/dashboard/layout").authenticated()
                .requestMatchers(HttpMethod.POST, "/**").hasAnyRole("ADMIN", "MANAGER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("benutzername")
                .passwordParameter("passwort")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?abgemeldet")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

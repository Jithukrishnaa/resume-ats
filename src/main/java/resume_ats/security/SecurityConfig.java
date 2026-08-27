package resume_ats.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {

                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http

                                .csrf(csrf -> csrf.disable())

                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers(
                                                                "/login.html",
                                                                "/register.html",
                                                                "/forgot-password.html",
                                                                "/reset-password.html")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/api/auth/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/favicon.ico")
                                                .permitAll()

                                                .anyRequest().authenticated())

                                .formLogin(form -> form

                                                .loginPage("/login.html")

                                                .loginProcessingUrl("/login")

                                                .defaultSuccessUrl(
                                                                "/index.html",
                                                                true)

                                                .failureUrl(
                                                                "/login.html?error=true")

                                                .permitAll())

                                .logout(logout -> logout

                                                .logoutUrl("/logout")

                                                .logoutSuccessUrl(
                                                                "/login.html?logout=true")

                                                .invalidateHttpSession(true)

                                                .clearAuthentication(true)

                                                .deleteCookies("JSESSIONID")

                                                .permitAll());

                return http.build();
        }
}
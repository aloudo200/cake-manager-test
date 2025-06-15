package com.waracle.cakemgr.config;

import com.waracle.cakemgr.exception.CustomAccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/error").permitAll()
                            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").authenticated()
                            .requestMatchers(HttpMethod.GET, "/rest/cakes/**").hasAnyRole("USER", "CHEF")
                            .requestMatchers(HttpMethod.GET, "/rest/cakes/getAllCakes").hasAnyRole("USER", "CHEF")
                            .requestMatchers(HttpMethod.GET, "/rest/cakes/getCakeById/**").hasAnyRole("USER", "CHEF")
                            .requestMatchers(HttpMethod.POST, "/rest/cakes/addNewCake").hasAnyRole("USER", "CHEF")
                            .requestMatchers(HttpMethod.PUT, "/rest/cakes/updateCake/**").hasRole("CHEF")
                            .requestMatchers(HttpMethod.DELETE, "/rest/cakes/deleteCake/**").hasRole("CHEF")
                            .anyRequest().authenticated()

                )
                .exceptionHandling( ex -> ex.accessDeniedHandler(accessDeniedHandler))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/login")
                        .permitAll()
                )
                .formLogin(form -> form.defaultSuccessUrl("/swagger-ui/index.html", true))
                .httpBasic(org.springframework.security.config.Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
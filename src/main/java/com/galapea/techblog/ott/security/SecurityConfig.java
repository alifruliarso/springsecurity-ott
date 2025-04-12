package com.galapea.techblog.ott.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      SendLinkOneTimeTokenGenerationSuccessHandler successHandler,
      CustomOneTimeTokenService customOneTimeTokenService)
      throws Exception {
    AuthenticationSuccessHandler ottLoginsuccessHandler =
        (request, response, authentication) -> response.sendRedirect("/");

    http.csrf(Customizer.withDefaults())
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/error", "/", "/images/**", "/js/*.js", "/css/*.css")
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/authentication/login"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/logout"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/webjars/**"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/ott/sent"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/ott/submit"))
                    .permitAll()
                    // .requestMatchers(new AntPathRequestMatcher("/api/users"))
                    // .permitAll()
                    // .requestMatchers(new AntPathRequestMatcher("/users"))
                    // .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginPage("/authentication/login")
                    .loginProcessingUrl("/login")
                    .failureUrl("/authentication/login?failed")
                    .defaultSuccessUrl("/")
                    .permitAll())
        .headers(
            httpSecurityHeaders -> httpSecurityHeaders.frameOptions(FrameOptionsConfig::disable))
        .logout(Customizer.withDefaults())
        .oneTimeTokenLogin(
            configurer ->
                configurer
                    .tokenGenerationSuccessHandler(successHandler)
                    .tokenService(customOneTimeTokenService)
                    .showDefaultSubmitPage(false)
                    .authenticationSuccessHandler(ottLoginsuccessHandler));

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      CustomUserDetailService userDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(provider);
  }
}

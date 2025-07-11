package com.danven.web_library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Security configuration class for the PalaTOS system.
 * Supports both customers and employees for login.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Configures password encoding mechanism using BCrypt.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures authentication provider using the custom user details service.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Registers custom authentication provider.
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    /**
     * Configures HTTP security: login, logout, CSRF, and access rules.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf()
                .ignoringAntMatchers("/h2-console/**")
                //   .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .headers().frameOptions().sameOrigin()
                .and()
                .authorizeRequests()
                // H2 console
                .antMatchers("/h2-console/**").permitAll()
                // public browsing of menu & details
                .antMatchers(
                        "/", "/login",
                        "/menu", "/menu/**",
                        "/product/*/details",
                        "/product/*/details/**",
                        "/static/**", "/css/**", "/js/**", "/images/**"
                ).permitAll()
                // allow only EMPLOYEEs to add/edit/delete products
                .antMatchers("/employee/**").hasAuthority("ROLE_EMPLOYEE")
                .antMatchers("/product/add", "/product/save",
                        "/product/edit/**", "/product/delete/**")
                .hasAuthority("ROLE_EMPLOYEE")
                // protect everything else (orders, cart, profile)
                // Feedback for customers: только авторизованный CUSTOMER
                .antMatchers(HttpMethod.GET,  "/orders/*/feedback").hasAuthority("ROLE_CUSTOMER")
                .antMatchers(HttpMethod.POST, "/orders/*/feedback").hasAuthority("ROLE_CUSTOMER")

                // Feedbacks view for employees
                .antMatchers(HttpMethod.GET, "/employee/order/*/feedbacks")
                .hasAuthority("ROLE_EMPLOYEE")

                .antMatchers(
                        "/profileTab/**",
                        "/order/**", "/orders/**",
                        "/cart/**", "/feedback/**"
                ).authenticated()
                // deny any other request
                .anyRequest().denyAll()
                .and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/menu", true)
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll();
    }

}

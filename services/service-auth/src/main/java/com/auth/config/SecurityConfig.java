package com.auth.config;

//import com.auth.security.JwtAuthenticationEntryPoint;
//import com.auth.security.JwtAuthenticationFilter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//    private final UserDetailsService userDetailsService;
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//
//    @Autowired
//    public SecurityConfig(UserDetailsService userDetailsService,
//                          JwtAuthenticationFilter jwtAuthenticationFilter,
//                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
//        this.userDetailsService = userDetailsService;
//        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
//        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
//    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .csrf().disable() // 禁用CSRF
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 无状态会话
//                .and()
//                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint) // JWT认证入口点
//                .and()
//                .authorizeRequests(authorize -> authorize
//                        .antMatchers("/auth/login", "/auth/register", "/auth/refresh").permitAll()
//                        .antMatchers("/public/**", "/css/**", "/js/**", "/images/**").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // 添加JWT过滤器
//    }
//
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
//    }
//
//    @Bean
//    @Override
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}

import com.auth.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS) // 保持会话
                .and()
                .authorizeRequests()
                .antMatchers("/view/login","/user/**","/auth/**", "/view/**").permitAll()
//                .antMatchers("/view/**","/auth/**","/knowledge/**","/qa/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

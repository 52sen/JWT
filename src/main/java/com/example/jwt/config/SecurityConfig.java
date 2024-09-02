package com.example.jwt.config;

import com.example.jwt.jwt.JWTFilter;
import com.example.jwt.jwt.JWTUtil;
import com.example.jwt.jwt.LoginFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// 이 클래스가 Configuration에 관리되기 위해
@Configuration
@EnableWebSecurity
public class SecurityConfig {


    // AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;

    //JWTUtil 주입
    private final JWTUtil jwtUtil;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil) {

        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    //CROS,로그인방식 등 filter => 메서드들을 Bean으로 설정
   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

       //csrf disable : 세션방식에서는 csrf를 필수적으로 방어 해야함 (jwt방식은 세션을 stateless 방식으로 관리해서 disable안해도됨)
       http.csrf(AbstractHttpConfigurer::disable);

       //From 로그인 방식 disable
       http.formLogin(AbstractHttpConfigurer::disable);

       //http basic 인증 방식 disable
       http.httpBasic(AbstractHttpConfigurer::disable);

       //경로별 인가 작업
       http
               .authorizeHttpRequests((auth) -> auth
                       .requestMatchers("/login", "/", "/join").permitAll()
                       .requestMatchers("/admin").hasRole("ADMIN")
                       .anyRequest().authenticated()); // 다른 접근은 로그인한 사용자만 사용할 수 있도록

       //JWTFilter 등록
       http
               .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);


       // 필터 추가 LoginFilter()는 인자를 받음 (AuthenticationManager() 메소드에 authenticationConfiguration 객체를 넣어야 함)
       // 따라서 등록 필요
       http
               .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration),jwtUtil), UsernamePasswordAuthenticationFilter.class);

       //세션 설정
       http
               .sessionManagement((session) -> session
                       .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

       return http.build();
   }
}
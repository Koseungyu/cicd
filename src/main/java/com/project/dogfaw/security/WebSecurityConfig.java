package com.project.dogfaw.security;

import com.project.dogfaw.security.jwt.JwtAuthenticationFilter;
import com.project.dogfaw.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity // 스프링 Security 지원을 가능하게 함
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public BCryptPasswordEncoder encodePassword() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) {
        // h2-console 사용에 대한 허용 (CSRF, FrameOptions 무시)
        web
                .ignoring()
                .antMatchers("/h2-console/**");
//                .antMatchers("/v2/api-docs","/v3/api-docs", "/swagger-resources/**", "**/swagger-resources/**", "/swagger-ui.html", "/webjars/**", "/swagger/**","/swagger-ui","/swagger-ui/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.cors().configurationSource(corsConfigurationSource());
        http.csrf().disable().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.headers().frameOptions().disable();
        http.authorizeRequests();
        // 회원 관리 처리 API (POST /user/**) 에 대해 CSRF 무시

        http.authorizeRequests()
                // login 없이 허용
                .antMatchers("/user/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/post/**").permitAll()
//                .antMatchers("/api/posts").permitAll()
//                .antMatchers("/api/preview").permitAll()
//                .antMatchers("/post/filter/**").permitAll()
//                .antMatchers("/webSocket/**").permitAll()
//                .antMatchers("/search/**").permitAll()
//                .antMatchers("/subscribe/**").permitAll()
//                .antMatchers("/v2/api-docs","/v3/api-docs", "/swagger-resources/**", "**/swagger-resources/**", "/swagger-ui.html", "/webjars/**", "/swagger*/**","/swagger-ui","/swagger-ui/**").permitAll()


                //추가 - 메인 페이지 접근 허용
                .antMatchers("/").permitAll()

                // 그 외 어떤 요청이든 '인증'과정 필요
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);
    }

    //"http://localhost:3000","http://localhost:8080"
    //"https://amplify.d3ifjxscizr42x.amplifyapp.com",
    //"https://everymohum.shop","https://everymohum.com"
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true) ;
        configuration.addAllowedOriginPattern("http://localhost:3000");
//        configuration.addAllowedOrigin(""); // local 테스트 시
//        configuration.addAllowedOrigin("https://amplify.d3ifjxscizr42x.amplifyapp.com"); // 배포 시
//        configuration.addAllowedOrigin("https://www.everymohum.com");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

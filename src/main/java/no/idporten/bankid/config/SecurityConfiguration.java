package no.idporten.bankid.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/token").authenticated()
                .anyRequest().permitAll()
                .and().httpBasic()
        .and()
            .headers().frameOptions().sameOrigin();
    }


}

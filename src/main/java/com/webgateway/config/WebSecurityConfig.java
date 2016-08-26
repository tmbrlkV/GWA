package com.webgateway.config;

import com.chat.util.entity.User;
import com.chat.util.json.JsonObjectFactory;
import com.chat.util.json.JsonProtocol;
import com.webgateway.entity.CustomUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Optional;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);
    @Autowired
    private ShaPasswordEncoder passwordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/user", "/registration", "/resources/**").anonymous()
                .antMatchers("/", "/topic", "/hello", "/topic/greetings", "/init", "/logout").authenticated()
                .anyRequest().authenticated()
                .and().formLogin().loginPage("/login").defaultSuccessUrl("/init", true).permitAll()
                .and().logout().logoutSuccessUrl("/login")
                .permitAll();
    }


    @Bean
    public ShaPasswordEncoder passwordEncoder() {
        return new ShaPasswordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(username -> {
            User reply = getAuth(username);
            String password = reply.getPassword();
            org.springframework.security.core.userdetails.User user = new org.springframework.security
                    .core.userdetails.User(reply.getLogin(), password, AuthorityUtils.createAuthorityList("USER"));
            return new CustomUser(reply.getId(), user);
        }).passwordEncoder(passwordEncoder);
    }


    private User getAuth(String username) {
        User user = new User(username);
        try {
            DatabaseSocketConfig instance = DatabaseSocketConfig.getInstance();
            String command = "getUserByLogin";
            JsonProtocol<User> protocol = new JsonProtocol<>(command, user);
            protocol.setFrom("");
            protocol.setTo("database");
            String json = JsonObjectFactory.getJsonString(protocol);

            logger.debug(json);
            instance.send(json);
            String reply = instance.receive();
            user = (User) Optional.ofNullable(JsonObjectFactory.getObjectFromJson(reply, JsonProtocol.class))
                    .map(JsonProtocol::getAttachment)
                    .orElseGet(User::new);
            instance.close();
        } catch (Exception e) {
            logger.error("Bad auth", e);
        }
        return user;
    }
}
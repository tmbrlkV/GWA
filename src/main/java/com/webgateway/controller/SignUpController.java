package com.webgateway.controller;

import com.chat.util.entity.User;
import com.chat.util.json.JsonObjectFactory;
import com.chat.util.json.JsonProtocol;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.webgateway.config.DatabaseSocketConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@RestController
public class SignUpController {
    private final ShaPasswordEncoder passwordEncoder;

    @Autowired
    public SignUpController(ShaPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @RequestMapping("/user")
    public ModelAndView registration(@RequestParam String username,
                                     @RequestParam String password,
                                     @RequestParam String repeatPassword) throws Exception {
        if (isEmptyFields(username, password, repeatPassword)) {
            return new ModelAndView("redirect:/error");
        }
        if (!password.equals(repeatPassword)) {
            return new ModelAndView("redirect:/error");
        }

        User user = signUp(username, password);

        if (isAuthenticated(user)) {
            return new ModelAndView("redirect:/login");
        } else {
            return new ModelAndView("redirect:/error");
        }
    }

    private boolean isEmptyFields(@RequestParam String username, @RequestParam String password, @RequestParam String repeatPassword) {
        return username == null || password == null || repeatPassword == null
                || username.equals("") || password.equals("") || repeatPassword.equals("");
    }

    private boolean isAuthenticated(User user) {
        return user.getLogin() != null && user.getPassword() != null;
    }

    private User signUp(String username, String password) throws JsonProcessingException {
        DatabaseSocketConfig instance = DatabaseSocketConfig.getInstance();

        User user = new User(username, passwordEncoder.encodePassword(password, null));
        String command = "newUser";
        JsonProtocol<User> protocol = new JsonProtocol<>(command, user);
        protocol.setFrom("");
        protocol.setTo("database");
        String json = JsonObjectFactory.getJsonString(protocol);
        instance.send(json);

        String reply = instance.receive();
        instance.close();
        user = (User) Optional.ofNullable(JsonObjectFactory.getObjectFromJson(reply, JsonProtocol.class))
                .map(JsonProtocol::getAttachment)
                .orElseGet(User::new);
        return user;
    }
}

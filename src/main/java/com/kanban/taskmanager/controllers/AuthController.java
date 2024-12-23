package com.kanban.taskmanager.controllers;

import com.kanban.taskmanager.model.users.AuthDTO;
import com.kanban.taskmanager.model.users.LoginResDTO;
import com.kanban.taskmanager.model.users.User;
import com.kanban.taskmanager.security.Token;
import com.kanban.taskmanager.model.users.RegisterDTO;
import com.kanban.taskmanager.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository repository;
    @Autowired
    private Token token;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = this.token.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid RegisterDTO data){
        if(this.repository.findByLogin(data.login()) != null) return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(data.login(), encryptedPassword, data.role()) {
            @Override
            public String getPassword() {
                return "";
            }
        };

        this.repository.save(newUser);

        return ResponseEntity.ok().build();
    }
}

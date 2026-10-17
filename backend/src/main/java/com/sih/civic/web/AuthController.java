package com.sih.civic.web;
import com.sih.civic.model.User;
import com.sih.civic.repo.UserRepository;
import com.sih.civic.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users; private final PasswordEncoder encoder;
    private final AuthenticationManager authManager; private final JwtService jwt;
    public AuthController(UserRepository users, PasswordEncoder encoder, AuthenticationManager authManager, JwtService jwt){
        this.users=users; this.encoder=encoder; this.authManager=authManager; this.jwt=jwt;
    }
    public record RegisterRequest(@NotBlank @Size(max=100) String name,
        @NotBlank @Email @Size(max=190) String email,
        @NotBlank @Size(min=8,max=72) String password) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if(users.existsByEmailIgnoreCase(req.email())) return ResponseEntity.status(409).body(Map.of("error","Email is already registered"));
        User u=new User(); u.setName(req.name().trim()); u.setEmail(req.email().trim().toLowerCase());
        u.setPassword(encoder.encode(req.password())); u.setRole("CITIZEN"); users.save(u);
        return ResponseEntity.status(201).body(Map.of("message","Registration successful"));
    }
    @PostMapping("/login")
    public Map<String,Object> login(@Valid @RequestBody LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email().trim().toLowerCase(),req.password()));
        User u=users.findByEmailIgnoreCase(req.email()).orElseThrow();
        return Map.of("token",jwt.generate(u.getEmail()),"id",u.getId(),"name",u.getName(),"email",u.getEmail(),"role",u.getRole());
    }
}

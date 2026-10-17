package com.sih.civic.security;
import com.sih.civic.repo.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public CustomUserDetailsService(UserRepository users){ this.users=users; }
    @Override public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var u=users.findByEmailIgnoreCase(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return User.withUsername(u.getEmail()).password(u.getPassword())
            .authorities("ROLE_"+u.getRole()).build();
    }
}

package pl.tiguarces.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.tiguarces.JwtUtils;
import pl.tiguarces.user.dto.UserPayload;
import pl.tiguarces.user.entity.AppUser;
import pl.tiguarces.user.repository.AppUserRepository;

import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;
import static pl.tiguarces.Constants.MESSAGE_KEY;

@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AppUser getLoggedUser() {
        Object userDetails = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return (userDetails instanceof final User user)
                 ? new AppUser(user.getUsername(), user.getPassword(), user.getAuthorities())
                 : null;
    }

    public Optional<AppUser> getLoggedUserFromDb() {
        var user = getLoggedUser();

        return (user != null) ? repository.findByUsername(user.getUsername())
                              : Optional.empty();
    }

    public ResponseEntity<?> register(final UserPayload payload) {
        if(repository.existsByUsername(payload.username())) {
            return new ResponseEntity<>("User with username '%s' exists".formatted(payload.username()), BAD_REQUEST);
        }

        repository.save(new AppUser(payload.username(), passwordEncoder.encode(payload.password())));
        return new ResponseEntity<>(OK);
    }

    public ResponseEntity<?> generateTokens(final UserPayload payload) {
        var user = repository.findByUsername(payload.username());

        if(user.isEmpty()) {
            return new ResponseEntity<>(Map.of(MESSAGE_KEY, "User not found"), NOT_FOUND);
        }

        String userPassword = user.get()
                                  .getPassword();

        if(!passwordEncoder.matches(payload.password(), userPassword)) {
            throw new BadCredentialsException("Invalid credentials");
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(payload.username(), userPassword));
        return new ResponseEntity<>(jwtUtils.generateTokens(payload.username()), OK);
    }

    public Map<String, String> refreshToken(final String refreshToken) {
        return jwtUtils.refreshTokens(refreshToken);
    }

    @Transactional(readOnly = true)
    public AppUser getById(final long loggedUserId) {
        return repository.findById(loggedUserId)
                         .orElseThrow();
    }
}

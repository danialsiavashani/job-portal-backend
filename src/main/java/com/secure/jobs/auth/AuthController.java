package com.secure.jobs.auth;

import com.secure.jobs.auth.dto.LoginRequest;
import com.secure.jobs.auth.dto.LoginResponse;
import com.secure.jobs.auth.dto.RegisterRequest;
import com.secure.jobs.exceptions.BadRequestException;
import com.secure.jobs.exceptions.ResourceNotFoundException;
import com.secure.jobs.models.auth.AppRole;
import com.secure.jobs.models.auth.Role;
import com.secure.jobs.models.auth.User;
import com.secure.jobs.repositories.RoleRepository;
import com.secure.jobs.repositories.UserRepository;
import com.secure.jobs.security.jwt.JwtUtils;
import com.secure.jobs.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtils jwtUtils;


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){

        boolean username = userRepository.existsByUsername(request.getUsername());

        if(username){
            throw new BadRequestException("Username is already taken");
        }

        boolean email = userRepository.existsByEmail(request.getEmail());

        if (email) {
            throw new BadRequestException("Email is already in use");
        }

        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                .orElseThrow(()-> new ResourceNotFoundException("Error: Role not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .build();

        userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request){

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Collect roles from the UserDetails
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        String jwt = jwtUtils.generateToken(userDetails.getUsername());

        return new LoginResponse(userDetails.getUsername(), roles, jwt);
    }


}

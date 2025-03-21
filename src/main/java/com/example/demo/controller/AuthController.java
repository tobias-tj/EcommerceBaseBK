package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.exception.ResourcesNotFoundException;
import com.example.demo.model.User;
import com.example.demo.service.JwtService;
import com.example.demo.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login( @RequestBody LoginRequest loginRequest ){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        final User userDetails = userService.getUserByEmail(loginRequest.getEmail());
        final String jwt = jwtService.generateToken(userDetails);
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("role", userDetails.getRole());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody User user) throws IllegalAccessException, MessagingException {
        User registeredUser = userService.registerUser(user);

        if (registeredUser == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error al registrar el usuario"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Usuario registrado correctamente"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        userService.changePassword(email, changePasswordRequest);
        return ResponseEntity.ok().body("Password changed");
    }

    @PostMapping("/confirm-email")
    public ResponseEntity<?> confirmEmail( @RequestBody EmailConfirmationRequest request ){
        try{
            userService.confirmEmail(request.getEmail(), request.getConfirmationCode());
            return ResponseEntity.ok().body("Email confirmed successfully");
        }catch (BadCredentialsException e){
            return ResponseEntity.badRequest().body("Invalid confirmation code");
        }catch (ResourcesNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/recoverAccount")
    public ResponseEntity<?> recoverAccount( @RequestBody RecoverAccountRequest request ){
        try{
            final UserDetails userDetails = userService.getUserByEmail(request.getEmail());
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "Email invalid"));
            }
            userService.recoverAccount(request.getEmail());
            return ResponseEntity.ok().body("Hemos enviado correctamente al correo la nueva contraseña.");
        }catch (ResourcesNotFoundException e){
            return ResponseEntity.notFound().build();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/userDetails")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDetailDTO> userDetails(@AuthenticationPrincipal UserDetails userDetails){
        if (!(userDetails instanceof User)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        Long userId = ((User) userDetails).getId();
        final UserDetailDTO userDetailDTO = userService.getUserDetails(userId);
        return ResponseEntity.ok(userDetailDTO);
    }
}

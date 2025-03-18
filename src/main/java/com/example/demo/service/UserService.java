package com.example.demo.service;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.UserDetailDTO;
import com.example.demo.exception.ResourcesNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public User registerUser( User user ) throws IllegalAccessException {
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new IllegalAccessException("Email already taken");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.USER);
        user.setConfirmationCode(generateConfirmationCode());
        user.setEmailConfirmation(false);
        emailService.sendConfirmationCode(user);
        return userRepository.save(user);
    }
    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourcesNotFoundException("User not found"));
    }

    public User getUserById(Long id){
        return userRepository.findById(id).orElseThrow(() -> new ResourcesNotFoundException("User not found"));
    }

    public void changePassword(String email, ChangePasswordRequest request){
        User user = getUserByEmail(email);
        if(!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())){
            throw new BadCredentialsException("Current Password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void confirmEmail(String email, String confirmationCode){
        User user = getUserByEmail(email);
        if(user.getConfirmationCode().equals(confirmationCode)){
            user.setEmailConfirmation(true);
            user.setConfirmationCode(null);
            userRepository.save(user);
        }
        else{
            throw new BadCredentialsException("Confirmation code is incorrect");
        }
    }

    public void recoverAccount(String email) {
        User user = getUserByEmail(email);
        String newPassword = generatePasswordTemporary();
        user.setPassword(passwordEncoder.encode(newPassword));
        emailService.sendRecoverAccount(user, newPassword);
        userRepository.save(user);
    }

    public UserDetailDTO getUserDetails(Long id){
        User user = getUserById(id);
        UserDetailDTO userDetailDTO = new UserDetailDTO();
        userDetailDTO.setEmail(user.getEmail());
        userDetailDTO.setFullName(user.getFullName());
        return userDetailDTO;
    }

    public String generateConfirmationCode(){
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public String generatePasswordTemporary() {
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String password = getPasswordSecurity(lowercase, uppercase);

        // Mezclar los caracteres para mayor aleatoriedad
        List<Character> chars = password.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(chars);

        // Convertir la lista de caracteres a una cadena
        return chars.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    private static String getPasswordSecurity( String lowercase, String uppercase ) {
        String numbers = "012345";
        String specialChars = "!#%^&*";

        String allChars = lowercase + uppercase + numbers + specialChars;
        SecureRandom random = new SecureRandom();

        // Asegurar al menos un carácter de cada tipo
        String password = String.valueOf(lowercase.charAt(random.nextInt(lowercase.length())))
                + uppercase.charAt(random.nextInt(uppercase.length()))
                + numbers.charAt(random.nextInt(numbers.length()))
                + specialChars.charAt(random.nextInt(specialChars.length()));

        // Generar el resto de la contraseña
        for (int i = 0; i < 4; i++) {
            password += allChars.charAt(random.nextInt(allChars.length()));
        }
        return password;
    }

}


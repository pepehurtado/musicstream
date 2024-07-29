package com.musicapp.musicstream.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.musicapp.musicstream.common.EmailService;
import com.musicapp.musicstream.dto.UserDTO;
import com.musicapp.musicstream.entities.Role;
import com.musicapp.musicstream.entities.User;
import com.musicapp.musicstream.jwt.CustomerDetailsService;
import com.musicapp.musicstream.jwt.JwtUtil;
import com.musicapp.musicstream.repository.RoleRepository;
import com.musicapp.musicstream.repository.UserRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "Operations related to User")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private CustomerDetailsService customerDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // Crear un nuevo usuario
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setImage(userDTO.getImage());
        user.setActive(0);
        user.setSoftDelete(0);

        // Generar un token de activación único
        String activationToken = UUID.randomUUID().toString();
        user.setActivationToken(activationToken);

        // Añadir el rol de USER
        List<Role> roles = new ArrayList<>();
        roles.add(roleRepository.findByName("USER"));
        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        // Enviar correo de confirmación
        String activationLink = "http://localhost:9000/api/users/activate?token=" + activationToken;
        String subject = "Confirma tu cuenta";
        String text = "Por favor, haz clic en el siguiente enlace para activar tu cuenta: " + activationLink;
        emailService.sendEmail(user.getEmail(), subject, text);

        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateUser(@RequestParam String token) {
        Optional<User> userOptional = userRepository.findByActivationToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setActivationToken(null);
            user.setActive(1);
            userRepository.save(user);
            return ResponseEntity.ok("Cuenta activada exitosamente");
        } else {
            return ResponseEntity.badRequest().body("Token de activación no válido");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            // Genera el token JWT
            String token = jwtUtil.generateToken(username);

            // Envolver el token en un objeto JSON
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    // Clase para envolver la respuesta JWT
    class JwtResponse {
        private String token;

        public JwtResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @Valid @RequestBody User userDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setPassword(userDetails.getPassword());
        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Iterable<User>> getAllUsers() {
        Iterable<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/active")
    public ResponseEntity<Iterable<User>> getAllActiveUsers() {
        Iterable<User> users = userRepository.findByActive(1);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<User> softDeleteUser(@PathVariable Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setSoftDelete(1);
        user.setActive(0);
        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/activate/{id}")
    public ResponseEntity<User> activateUser(@PathVariable Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(1);
        user.setSoftDelete(0);
        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("path/{id}")
    public ResponseEntity<User> addRole(@PathVariable Integer id, @RequestBody Role role) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        Role existRole = roleRepository.findById(role.getId()).orElseThrow(() -> new RuntimeException("Role not found"));
        user.getRoles().add(existRole);
        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }
}

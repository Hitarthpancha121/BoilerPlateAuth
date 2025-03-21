package com.Dotsquares.BoilerPlateAuth.Controller;

import com.Dotsquares.BoilerPlateAuth.Config.JwtUtil;
import com.Dotsquares.BoilerPlateAuth.Entity.Log;
import com.Dotsquares.BoilerPlateAuth.Entity.User;
import com.Dotsquares.BoilerPlateAuth.Repository.LogRepository;
import com.Dotsquares.BoilerPlateAuth.Repository.UserRepository;
import com.Dotsquares.BoilerPlateAuth.Service.JwtService;
import com.Dotsquares.BoilerPlateAuth.errorHandling.BaseResponse;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final LogRepository logRepository;
    private final JwtUtil jwtUtil;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, LogRepository logRepository, JwtUtil jwtUtil, JwtService jwtService) {
        this.userRepository = userRepository;
        this.logRepository = logRepository;
        this.jwtUtil = jwtUtil;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logRepository.save(new Log(null, "REGISTER_FAILED", request.getEmail(), 400, "Email already exists", LocalDateTime.now(),400));
//            return ResponseEntity.badRequest().body("Email already registered");
            return ResponseEntity.ok(new BaseResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Email already registered"
            ));
        }

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser= userRepository.save(request);
        logRepository.save(new Log(null, "REGISTER_SUCCESS", request.getEmail(), 201, "User registered successfully", LocalDateTime.now(),0));
        System.out.println("User Registered Successfully: ");
        System.out.println("ID: " + savedUser.getId());
        System.out.println("FirstName: " + savedUser.getFirstName());
        System.out.println("LastName: "+savedUser.getLastName());
        System.out.println("Email: " + savedUser.getEmail());
        System.out.println("Registration Time: " + LocalDateTime.now());
        return ResponseEntity.ok(new BaseResponse<>(
                HttpStatus.CREATED.value(),
                "User Registered Successfully!!"
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logRepository.save(new Log(null, "LOGIN_FAILED", request.getEmail(), 401, "Invalid credentials", LocalDateTime.now(),401));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new BaseResponse<>(
                            HttpStatus.UNAUTHORIZED.value(),
                            "Invalid Credentials!!"
                    )
            );
        }

        String token = jwtUtil.generateToken(user.getEmail());
        System.out.println("JWT Token: " + token);

        logRepository.save(new Log(null, "LOGIN_SUCCESS", request.getEmail(), 200, "User logged in successfully", LocalDateTime.now(),0));
        return ResponseEntity.ok(new BaseResponse<>(
                HttpStatus.OK.value(),
                "Success!!",
                token
        ));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String token) {
        try {
            // Extract token from "Bearer <token>" format
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Validate token
            if (!jwtService.isTokenValid(token)) {
                return ResponseEntity.ok(new BaseResponse<>(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Invalid or Expired Token"
                ));
            }

            // Fetch all users from database
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(new BaseResponse<>(
                    HttpStatus.OK.value(),
                    "VERIFIED!!",
                    users
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request.");
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(new BaseResponse<>(
                HttpStatus.OK.value(),
                "Test Successful!!"
        ));
    }
}
package com.Dotsquares.BoilerPlateAuth.Controller;

import com.Dotsquares.BoilerPlateAuth.Entity.Log;
import com.Dotsquares.BoilerPlateAuth.Entity.User;
import com.Dotsquares.BoilerPlateAuth.Repository.LogRepository;
import com.Dotsquares.BoilerPlateAuth.Repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class OauthController {

    private final UserRepository userRepository;
    private final LogRepository logRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public OauthController(UserRepository userRepository, LogRepository logRepository) {
        this.userRepository = userRepository;
        this.logRepository = logRepository;
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null) {
            logRepository.save(new Log(null, "OAUTH_ACCESS_DENIED", "UNKNOWN", 403, "Unauthorized access attempt", LocalDateTime.now(), 403));
            return "redirect:/login";
        }

        // Get user details from OAuth provider
        String userEmail = principal.getAttribute("email");
        String userName = principal.getAttribute("name");

        if (userEmail == null) {
            logRepository.save(new Log(null, "OAUTH_LOGIN_FAILED", "UNKNOWN", 400, "Email not provided by OAuth provider", LocalDateTime.now(), 400));
            return "redirect:/login";
        }

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(userEmail);
        if (existingUser.isEmpty()) {
            // Register the new user
            User newUser = new User();
            newUser.setEmail(userEmail);
            newUser.setFirstName(userName.split(" ")[0]); // Extract first name
            newUser.setLastName(userName.contains(" ") ? userName.substring(userName.indexOf(" ") + 1) : ""); // Extract last name if available
//          newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Set a random password
            userRepository.save(newUser);

            logRepository.save(new Log(null, "OAUTH_REGISTER_SUCCESS", userEmail, 201, "User registered via OAuth", LocalDateTime.now(), 0));
        }

        logRepository.save(new Log(null, "OAUTH_LOGIN_SUCCESS", userEmail, 200, "User logged in via OAuth successfully", LocalDateTime.now(), 0));

        model.addAttribute("name", principal.getAttribute("name"));
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            String userEmail = (String) request.getSession().getAttribute("userEmail");
            request.logout();

            if (userEmail != null) {
                logRepository.save(new Log(null, "OAUTH_LOGOUT_SUCCESS", userEmail, 200, "User logged out successfully", LocalDateTime.now(), 0));
            } else {
                logRepository.save(new Log(null, "OAUTH_LOGOUT_ATTEMPT", "UNKNOWN", 400, "Logout attempted without an active session", LocalDateTime.now(), 400));
            }

        } catch (ServletException e) {
            logRepository.save(new Log(null, "OAUTH_LOGOUT_FAILED", "UNKNOWN", 500, "Logout failed due to server error", LocalDateTime.now(), 500));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logout failed");
        }
        return ResponseEntity.ok("Logout successful");
    }
}
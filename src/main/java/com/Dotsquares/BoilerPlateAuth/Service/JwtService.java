package com.Dotsquares.BoilerPlateAuth.Service;

import com.Dotsquares.BoilerPlateAuth.Config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtUtil jwtUtil;  // Utility class for JWT parsing

    public boolean isTokenValid(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }
}
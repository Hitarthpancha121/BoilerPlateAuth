package com.Dotsquares.BoilerPlateAuth.Service;

import com.Dotsquares.BoilerPlateAuth.Entity.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public User createUser(OAuth2User oAuth2User) {
        User user = new User();
        // Set user attributes from OAuth2User
        user.setFirstName(oAuth2User.getAttribute("name"));
        user.setEmail(oAuth2User.getAttribute("email"));
        return user;
    }
}
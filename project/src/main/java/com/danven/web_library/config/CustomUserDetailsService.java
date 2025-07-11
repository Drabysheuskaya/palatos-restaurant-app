package com.danven.web_library.config;


import com.danven.web_library.domain.user.Customer;
import com.danven.web_library.domain.user.Employee;
import com.danven.web_library.domain.user.User;
import com.danven.web_library.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.danven.web_library.domain.user.User user = userService.getUserByEmail(email);

        String role;
        if (user instanceof Employee) {
            role = "ROLE_EMPLOYEE";
        } else if (user instanceof Customer) {
            role = "ROLE_CUSTOMER";
        } else {
            throw new UsernameNotFoundException("Unknown user type for email: " + email);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(),
                true,
                true,
                true,
                Collections.singleton(new SimpleGrantedAuthority(role))
        );
    }

    public com.danven.web_library.domain.user.User getLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email);
    }

    public User getLoggedInUserOrNull() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            if (email == null || email.equals("anonymousUser")) {
                return null;
            }
            return userService.getUserByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }

}

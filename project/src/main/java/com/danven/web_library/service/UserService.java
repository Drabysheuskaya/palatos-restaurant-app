package com.danven.web_library.service;

import com.danven.web_library.domain.user.User;

public interface UserService {
    User getUserByEmail(String email);
}

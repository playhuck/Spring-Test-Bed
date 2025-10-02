package com.side.springtestbed.monad;

import com.side.springtestbed.monad.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
        setUsers();
    }

    public User getUserByImperative(String name) {

        User user = userRepository.getUserByName(name);
        if (Objects.isNull(user)) {
            throw new RuntimeException("User not found");
        }

        if (Objects.isNull(user.getEmail())) {
            throw new RuntimeException("Email not found");
        }

        return user;

    }

    public User getUserByOptional(String name) {

        Optional<User> user = Optional.ofNullable(userRepository.getUserByName(name));

        user
                .flatMap(User::getEmail)
                .map(String::toUpperCase)
                .orElseThrow(() -> new RuntimeException("Email not found"));


        return user.get();

    }

    public void setUsers() {

        userRepository.save(new User("Anna", "Anna@gmail.com"));
        userRepository.save(new User("Joy", null));

    }

}

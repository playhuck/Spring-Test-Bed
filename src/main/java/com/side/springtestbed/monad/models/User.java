package com.side.springtestbed.monad.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String name;
    private String email;

    public Optional<String> getEmail() {
        return Optional.ofNullable(this.email);
    }

}

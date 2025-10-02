package com.side.springtestbed.monad;

import com.side.springtestbed.monad.models.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {

    public Map<String, User> store = new ConcurrentHashMap<>();

    public void save(User user){
        store.put(user.getName(), user);
    }

    public User getUserByName(String name){
        return store.get(name);
    }

}

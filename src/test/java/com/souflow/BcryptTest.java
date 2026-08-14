package com.souflow;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptTest {
    @Test
    public void generateHash() {
        System.out.println("====== BCRYPT HASH ======");
        System.out.println(new BCryptPasswordEncoder().encode("123456"));
        System.out.println("=========================");
    }
}

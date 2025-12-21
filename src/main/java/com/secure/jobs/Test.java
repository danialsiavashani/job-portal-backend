package com.secure.jobs;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test {

    @GetMapping("/api/test/authenticated")
    public String authenticatedOnly() {
        return "You are authenticated";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/api/test/user")
    public String userOnly() {
        return "Hello USER";
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/api/test/super-admin")
    public String adminOnly() {
        return "Hello SUPER ADMIN";
    }
}

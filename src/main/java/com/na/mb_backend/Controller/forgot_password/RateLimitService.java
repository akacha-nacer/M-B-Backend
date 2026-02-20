package com.na.mb_backend.Controller.forgot_password;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    private final ConcurrentHashMap<String, List<LocalDateTime>> attempts = new ConcurrentHashMap<>();

    public boolean isAllowed(String email, int maxAttempts, int windowMinutes) {
        var now = LocalDateTime.now();
        var cutoff = now.minusMinutes(windowMinutes);

        attempts.compute(email, (k, v) -> {
            if (v == null) v = new ArrayList<>();
            v.removeIf(time -> time.isBefore(cutoff));
            return v;
        });

        var recentAttempts = attempts.get(email);
        if (recentAttempts.size() >= maxAttempts) {
            return false;
        }

        recentAttempts.add(now);
        return true;
    }
}

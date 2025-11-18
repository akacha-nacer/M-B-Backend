package com.na.mb_backend.Security.token;


import com.na.mb_backend.User.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Integer Id;

    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    private  boolean expired;
    private  boolean revoked;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}

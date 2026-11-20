package com.caldova.production.setup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class PresenterAccountSetup {
    private PresenterAccountSetup() { }

    public static void main(String[] args) throws Exception {
        String password = required("PRESENTER_PASSWORD");
        if (password.length() < 12 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Presenter password must be at least 12 characters and at most 72 UTF-8 bytes.");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        try (Connection connection = DriverManager.getConnection(required("SPRING_DATASOURCE_URL"),
                required("ORACLE_SETUP_USER"), required("ORACLE_SETUP_PASSWORD"))) {
            connection.setAutoCommit(false);
            try (PreparedStatement query = connection.prepareStatement(
                    "SELECT password_hash FROM CALDOVA_OWNER.app_user WHERE username='presenter'");
                 ResultSet result = query.executeQuery()) {
                if (result.next()) {
                    if (!encoder.matches(password, result.getString(1))) {
                        throw new IllegalStateException(
                                "Presenter credentials differ from Oracle. Restore the original local presenter password; setup does not overwrite accounts.");
                    }
                    System.out.println("Existing presenter account verified; password hash preserved.");
                    return;
                }
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO CALDOVA_OWNER.app_user (username,password_hash,enabled) VALUES ('presenter',?,1)")) {
                insert.setString(1, encoder.encode(password));
                insert.executeUpdate();
                connection.commit();
            }
        }
        System.out.println("Presenter account created with a BCrypt hash in Oracle.");
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(name + " is required for local account setup.");
        }
        return value;
    }
}

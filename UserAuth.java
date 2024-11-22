package auth;

import java.sql.*;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class UserAuth {
    private Connection conn;
    private Scanner scanner;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public UserAuth(Connection conn) {
        this.conn = conn;
        this.scanner = new Scanner(System.in);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8;
    }

    private boolean isValidUsername(String username) {
        return username.length() >= 3;
    }

    public void registerUser() {
        try {
            System.out.println("\n=== User Registration ===");

            String username;
            do {
                System.out.print("Enter username (minimum 3 characters): ");
                username = scanner.nextLine().trim();
                if (!isValidUsername(username)) {
                    System.out.println("Username must be at least 3 characters long!");
                    continue;
                }

                PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT username FROM users WHERE username = ?"
                );
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    System.out.println("Username already exists!");
                    username = "";
                }
            } while (!isValidUsername(username));

            String password;
            do {
                System.out.print("Enter password (minimum 8 characters): ");
                password = scanner.nextLine();
                if (!isValidPassword(password)) {
                    System.out.println("Password must be at least 8 characters long!");
                }
            } while (!isValidPassword(password));

            String email;
            do {
                System.out.print("Enter email: ");
                email = scanner.nextLine().trim();
                if (!isValidEmail(email)) {
                    System.out.println("Please enter a valid email address!");
                    continue;
                }

                PreparedStatement checkEmailStmt = conn.prepareStatement(
                        "SELECT email FROM users WHERE email = ?"
                );
                checkEmailStmt.setString(1, email);
                ResultSet rs = checkEmailStmt.executeQuery();

                if (rs.next()) {
                    System.out.println("Email already registered!");
                    email = "";
                }
            } while (!isValidEmail(email) || email.isEmpty());

            System.out.print("Enter full name: ");
            String fullName = scanner.nextLine().trim();

            System.out.print("Enter department (press Enter to skip): ");
            String department = scanner.nextLine().trim();

            String hashedPassword = hashPassword(password);

            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, email, full_name, department) " +
                            "VALUES (?, ?, ?, ?, ?)"
            );
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, email);
            pstmt.setString(4, fullName);
            pstmt.setString(5, department.isEmpty() ? null : department);

            pstmt.executeUpdate();
            System.out.println("Registration successful! Please login.");

        } catch (SQLException e) {
            System.out.println("Error during registration: " + e.getMessage());
        }
    }

    public ResultSet loginUser(String username, String password) {
        try {
            String hashedPassword = hashPassword(password);

            PreparedStatement authStmt = conn.prepareStatement(
                    "SELECT user_id, role FROM users WHERE username = ? AND password = ? AND is_active = TRUE",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY
            );
            authStmt.setString(1, username);
            authStmt.setString(2, hashedPassword);
            ResultSet rs = authStmt.executeQuery();

            if (rs.next()) {
                PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE username = ?"
                );
                updateStmt.setString(1, username);
                updateStmt.executeUpdate();

                rs.beforeFirst();
            }
            return rs;

        } catch (SQLException e) {
            System.out.println("Error during login: " + e.getMessage());
            return null;
        }
    }
}
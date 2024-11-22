import java.sql.*;
import java.util.Scanner;
import president.PresidentOperations;
import regularuser.RegularUserOperations;
import auth.UserAuth;

public class Main {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/emat";
    private static final String USER = "root";
    private static final String PASS = "Fhhi@1454";

    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Scanner scanner = new Scanner(System.in);
            UserAuth auth = new UserAuth(conn);

            while (true) {
                System.out.println("\n=== EMAT Event Management System ===");
                System.out.println("1. Login");
                System.out.println("2. Register");
                System.out.println("3. Exit");
                System.out.print("Choice: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (choice == 1) {
                    System.out.print("Username: ");
                    String username = scanner.nextLine();
                    System.out.print("Password: ");
                    String password = scanner.nextLine();

                    ResultSet rs = auth.loginUser(username, password);

                    if (rs != null && rs.next()) {
                        int userId = rs.getInt("user_id");
                        String role = rs.getString("role");

                        if (role.equals("president")) {
                            PresidentOperations presOps = new PresidentOperations(conn);
                            while (true) {
                                System.out.println("\n=== President Menu ===");
                                System.out.println("1. Create Event");
                                System.out.println("2. Update Event");
                                System.out.println("3. Delete Event");
                                System.out.println("4. View Events");
                                System.out.println("5. Logout");
                                System.out.print("Choice: ");

                                int menuChoice = scanner.nextInt();
                                switch (menuChoice) {
                                    case 1: presOps.createEvent(); break;
                                    case 2: presOps.updateEvent(); break;
                                    case 3: presOps.deleteEvent(); break;
                                    case 4: new RegularUserOperations(conn).viewEvents(); break;
                                    case 5: break;
                                }
                                if (menuChoice == 5) break;
                            }
                        } else {
                            RegularUserOperations regOps = new RegularUserOperations(conn);
                            while (true) {
                                System.out.println("\n=== User Menu ===");
                                System.out.println("1. View Events");
                                System.out.println("2. Register for Event");
                                System.out.println("3. Logout");
                                System.out.print("Choice: ");
                                int menuChoice = scanner.nextInt();
                                switch (menuChoice) {
                                    case 1: regOps.viewEvents(); break;
                                    case 2: regOps.registerForEvent(userId); break;
                                    case 3: break;
                                }
                                if (menuChoice == 3) break;
                            }
                        }
                    } else {
                        System.out.println("Invalid credentials!");
                    }
                } else if (choice == 2) {
                    auth.registerUser();
                } else if (choice == 3) {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
package regularuser;

import java.sql.*;
import java.util.Scanner;

public class RegularUserOperations {
    private Connection conn;
    private Scanner scanner;

    public RegularUserOperations(Connection conn) {
        this.conn = conn;
        this.scanner = new Scanner(System.in);
    }

    public void viewEvents() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM public_events");

            System.out.println("\n=== Available Events ===");
            while(rs.next()) {
                System.out.println("ID: " + rs.getInt("event_id"));
                System.out.println("Name: " + rs.getString("event_name"));
                System.out.println("Date: " + rs.getTimestamp("event_date"));
                System.out.println("Location: " + rs.getString("location"));
                System.out.println("------------------");
            }
        } catch (SQLException e) {
            System.out.println("Error viewing events: " + e.getMessage());
        }
    }

    public void registerForEvent(int userId) {
        System.out.print("Enter event ID to register: ");
        int eventId = scanner.nextInt();

        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO attendees (event_id, user_id) VALUES (?, ?)");
            pstmt.setInt(1, eventId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            System.out.println("Successfully registered for the event!");
        } catch (SQLException e) {
            System.out.println("Error registering for event: " + e.getMessage());
        }
    }
}
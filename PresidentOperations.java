package president;

import java.sql.*;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PresidentOperations {
    private Connection conn;
    private Scanner scanner;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PresidentOperations(Connection conn) {
        this.conn = conn;
        this.scanner = new Scanner(System.in);
    }

    public void createEvent() {
        try {
            System.out.println("\n=== Create New Event ===");
            scanner.nextLine(); // Clear buffer

            System.out.print("Event Name: ");
            String name = scanner.nextLine();

            System.out.print("Description: ");
            String description = scanner.nextLine();

            System.out.print("Location: ");
            String location = scanner.nextLine();

            System.out.print("Event Date (YYYY-MM-DD HH:MM:SS): ");
            String eventDateStr = scanner.nextLine();
            Timestamp eventDate = Timestamp.valueOf(eventDateStr);

            System.out.print("Registration Deadline (YYYY-MM-DD HH:MM:SS): ");
            String deadlineStr = scanner.nextLine();
            Timestamp deadline = Timestamp.valueOf(deadlineStr);

            System.out.print("Capacity: ");
            int capacity = scanner.nextInt();

            CallableStatement cstmt = conn.prepareCall(
                    "{CALL create_event(?, ?, ?, ?, ?, ?, 'conference', 1)}");
            cstmt.setString(1, name);
            cstmt.setString(2, description);
            cstmt.setTimestamp(3, eventDate);
            cstmt.setTimestamp(4, deadline);
            cstmt.setString(5, location);
            cstmt.setInt(6, capacity);

            cstmt.executeUpdate();
            System.out.println("Event created successfully!");

        } catch (SQLException e) {
            System.out.println("Error creating event: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD HH:MM:SS format");
        }
    }

    public void updateEvent() {
        try {
            System.out.print("Enter event ID to update: ");
            int eventId = scanner.nextInt();
            scanner.nextLine(); // consume newline

            System.out.print("New event name: ");
            String name = scanner.nextLine();

            System.out.print("New description: ");
            String description = scanner.nextLine();

            System.out.print("New location: ");
            String location = scanner.nextLine();

            System.out.print("New event date (YYYY-MM-DD HH:MM:SS): ");
            String eventDateStr = scanner.nextLine();
            Timestamp eventDate = Timestamp.valueOf(eventDateStr);

            System.out.print("New registration deadline (YYYY-MM-DD HH:MM:SS): ");
            String deadlineStr = scanner.nextLine();
            Timestamp deadline = Timestamp.valueOf(deadlineStr);

            System.out.print("New capacity: ");
            int capacity = scanner.nextInt();

            CallableStatement cstmt = conn.prepareCall(
                    "{CALL update_event(?, ?, ?, ?, ?, ?, ?, 'conference')}");
            cstmt.setInt(1, eventId);
            cstmt.setString(2, name);
            cstmt.setString(3, description);
            cstmt.setTimestamp(4, eventDate);
            cstmt.setTimestamp(5, deadline);
            cstmt.setString(6, location);
            cstmt.setInt(7, capacity);

            cstmt.executeUpdate();
            System.out.println("Event updated successfully!");

        } catch (SQLException e) {
            System.out.println("Error updating event: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD HH:MM:SS format");
        }
    }

    public void deleteEvent() {
        try {
            System.out.print("Enter event ID to delete: ");
            int eventId = scanner.nextInt();

            CallableStatement cstmt = conn.prepareCall("{CALL delete_event(?)}");
            cstmt.setInt(1, eventId);
            cstmt.executeUpdate();

            System.out.println("Event deleted successfully!");
        } catch (SQLException e) {
            System.out.println("Error deleting event: " + e.getMessage());
        }
    }
}
package server;

import database.DBConnection;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import model.Student;

public class IDCardServer {
    public static void main(String[] args) {
        int port = 5000;
        System.out.println("Server started on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("✅ Client connected");

                try (
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())
                ) {
                    Student student = (Student) in.readObject();
                    System.out.println("📦 Received: " + student.getName() + " (" + student.getRollNo() + ")");

                    try (Connection conn = DBConnection.getConnection()) {
                        // New query includes idcard_path
                        String query = "INSERT INTO students (name, roll_no, department, year, email, phone, photo_path, idcard_path, issue_date) " +
                                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement ps = conn.prepareStatement(query);
                        ps.setString(1, student.getName());
                        ps.setString(2, student.getRollNo());
                        ps.setString(3, student.getDepartment());
                        ps.setInt(4, student.getYear());
                        ps.setString(5, student.getEmail());
                        ps.setString(6, student.getPhone());
                        ps.setString(7, student.getPhotoPath());

                        // auto-generate the path to generated card based on roll number
                        String idCardPath = System.getProperty("user.dir") + "/generated_cards/" + student.getRollNo() + "_ID_HD.png";
                        ps.setString(8, idCardPath);

                        // set issue date as current date
                        ps.setDate(9, java.sql.Date.valueOf(LocalDate.now()));

                        ps.executeUpdate();
                        out.writeObject("Student '" + student.getName() + "' saved successfully!\n🪪 ID Card: " + idCardPath);

                        System.out.println("Saved: " + student.getName() + " | Roll: " + student.getRollNo());
                        System.out.println("Photo: " + student.getPhotoPath());
                        System.out.println("ID Card: " + idCardPath);

                    } catch (SQLException e) {
                        out.writeObject("Database error: " + e.getMessage());
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

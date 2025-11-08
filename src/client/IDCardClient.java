package client;

import database.DBConnection;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Student;
import ui.IDCardPanel;

public class IDCardClient extends JFrame {
    private JTextField nameField, rollField, deptField, yearField, emailField, phoneField;
    private JLabel photoLabel;
    private JTextArea outputArea;
    private File selectedPhoto;
    private IDCardPanel idCardPanel;

    public IDCardClient() {
        setTitle("🎓 ID Card Generator");
        setSize(1100, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 🧾 Left-side Form Panel
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Enter Student Details"));

        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Roll No:"));
        rollField = new JTextField();
        formPanel.add(rollField);

        formPanel.add(new JLabel("Department:"));
        deptField = new JTextField();
        formPanel.add(deptField);

        formPanel.add(new JLabel("Year:"));
        yearField = new JTextField();
        formPanel.add(yearField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Phone:"));
        phoneField = new JTextField();
        formPanel.add(phoneField);

        JButton photoButton = new JButton("📸 Choose Photo");
        photoButton.addActionListener(this::choosePhoto);
        formPanel.add(photoButton);

        photoLabel = new JLabel("No photo selected", SwingConstants.CENTER);
        photoLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        formPanel.add(photoLabel);

        JButton sendButton = new JButton("✅ Generate ID");
        sendButton.addActionListener(this::sendData);
        formPanel.add(sendButton);

        JButton viewAllButton = new JButton("📋 View All Students");
        viewAllButton.addActionListener(this::viewAllStudents);
        formPanel.add(viewAllButton);

        add(formPanel, BorderLayout.WEST);

        // 🧠 Right-side Panel (Preview + Save + Logs)
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

        idCardPanel = new IDCardPanel();
        JPanel previewWrap = new JPanel();
        previewWrap.setBorder(BorderFactory.createTitledBorder("ID Card Preview"));
        previewWrap.add(idCardPanel);
        rightPanel.add(previewWrap, BorderLayout.CENTER);

        JButton saveButton = new JButton("💾 Save ID Card as PNG (HD)");
        saveButton.addActionListener(this::saveIDCard);
        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topButtons.add(saveButton);
        rightPanel.add(topButtons, BorderLayout.NORTH);

        outputArea = new JTextArea(6, 40);
        outputArea.setEditable(false);
        outputArea.setBorder(BorderFactory.createTitledBorder("System Messages"));
        rightPanel.add(new JScrollPane(outputArea), BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.CENTER);
    }

    // 🖼️ Choose Photo
    private void choosePhoto(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Student Photo");
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedPhoto = chooser.getSelectedFile();
            photoLabel.setText(selectedPhoto.getName());
        }
    }

    // 🚀 Send Data to Server (and copy photo)
    private void sendData(ActionEvent e) {
        try {
            // Copy selected photo to /photos/
            String photoPath = null;
            if (selectedPhoto != null) {
                File photosDir = new File("photos");
                if (!photosDir.exists()) photosDir.mkdir();

                File copied = new File(photosDir, selectedPhoto.getName());
                Files.copy(selectedPhoto.toPath(), copied.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                photoPath = copied.getAbsolutePath();
            }

            Student student = new Student(
                nameField.getText(),
                rollField.getText(),
                deptField.getText(),
                Integer.parseInt(yearField.getText()),
                emailField.getText(),
                phoneField.getText(),
                photoPath
            );

            Socket socket = new Socket("localhost", 5000);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(student);

            String response = (String) in.readObject();
            outputArea.append(response + "\n");

            idCardPanel.setStudent(student);
            socket.close();

        } catch (Exception ex) {
            outputArea.append("❌ Error: " + ex.getMessage() + "\n");
        }
    }

    // 💾 Save HD ID Card (3× resolution export)
    private void saveIDCard(ActionEvent e) {
        try {
            if (!idCardPanel.hasStudent()) {
                JOptionPane.showMessageDialog(this, "Generate an ID first.", "No ID", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Determine panel size
            int panelW = idCardPanel.getWidth();
            int panelH = idCardPanel.getHeight();
            if (panelW <= 0 || panelH <= 0) {
                Dimension pref = idCardPanel.getPreferredSize();
                panelW = pref.width;
                panelH = pref.height;
            }

            // Render at 3× scale
            double scale = 3.0;
            int imgW = (int) Math.round(panelW * scale);
            int imgH = (int) Math.round(panelH * scale);

            BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();

            // High-quality rendering
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            g2.scale(scale, scale);
            idCardPanel.paint(g2);
            g2.dispose();

            // Ensure /generated_cards/ exists
            File cardsDir = new File("generated_cards");
            if (!cardsDir.exists()) {
                boolean ok = cardsDir.mkdir();
                if (!ok) {
                    outputArea.append("⚠️ Could not create generated_cards/ folder, saving to project root instead.\n");
                }
            }

            // Auto filename
            String roll = (rollField.getText() == null || rollField.getText().trim().isEmpty())
                    ? "Student"
                    : rollField.getText().trim();
            File fileToSave = new File(cardsDir.exists() ? cardsDir : new File("."), roll + "_ID_HD.png");

            ImageIO.write(img, "png", fileToSave);

            outputArea.append("✅ Saved HD ID card to: " + fileToSave.getAbsolutePath() + "\n");
            JOptionPane.showMessageDialog(this, "Saved ID card to:\n" + fileToSave.getAbsolutePath());

        } catch (Exception ex) {
            outputArea.append("❌ Save failed: " + ex.getMessage() + "\n");
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving ID card:\n" + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 📋 View All Students
    private void viewAllStudents(ActionEvent e) {
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name, roll_no, department, year, email, phone FROM students");

            String[] cols = {"Name", "Roll No", "Department", "Year", "Email", "Phone"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getString("roll_no"),
                        rs.getString("department"),
                        rs.getInt("year"),
                        rs.getString("email"),
                        rs.getString("phone")
                });
            }

            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(700, 400));

            JOptionPane.showMessageDialog(this, scrollPane, "All Students", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error fetching students:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new IDCardClient().setVisible(true));
    }
}

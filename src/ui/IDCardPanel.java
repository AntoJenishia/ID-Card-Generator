package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import model.Student;

public class IDCardPanel extends JPanel {
    private Student student;
    private BufferedImage photoSrc;          // original loaded photo (not upscaled)
    private static final int PHOTO_W = 100;  // visual box width
    private static final int PHOTO_H = 120;  // visual box height

    public IDCardPanel() {
        setPreferredSize(new Dimension(420, 260));
        setBackground(new Color(230, 240, 250));
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
    }

    public void setStudent(Student student) {
        this.student = student;
        this.photoSrc = null;
        if (student != null && student.getPhotoPath() != null && !student.getPhotoPath().isEmpty()) {
            try {
                File f = new File(student.getPhotoPath());
                if (f.exists()) {
                    photoSrc = ImageIO.read(f);
                } else {
                    photoSrc = null;
                }
            } catch (Exception ex) {
                System.err.println("Could not load photo: " + ex.getMessage());
                photoSrc = null;
            }
        }
        repaint();
    }

    public boolean hasStudent() {
        return student != null;
    }

    @Override
    protected void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (student == null) {
            g.setColor(Color.GRAY);
            g.drawString("No ID generated yet", 150, 125);
            g.dispose();
            return;
        }

        // Header
        g.setColor(new Color(25, 55, 109));
        g.fillRect(0, 0, getWidth(), 70);

        // College name centered
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        FontMetrics fm = g.getFontMetrics();
        String college = "RAJALAKSHMI INSTITUTE OF TECHNOLOGY";
        int tx = (getWidth() - fm.stringWidth(college)) / 2;
        g.drawString(college, tx, 25);

        // Subtitle
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        String sub = "COLLEGE ID CARD";
        int sx = (getWidth() - g.getFontMetrics().stringWidth(sub)) / 2;
        g.drawString(sub, sx, 45);

        // Photo box coordinates
        int photoX = 20;
        int photoY = 90;

        // Draw photo with center-crop; avoid upscaling for preview to prevent blur
        if (photoSrc != null) {
            // Determine crop region (same aspect ratio as box)
            BufferedImage cropped = centerCrop(photoSrc, PHOTO_W, PHOTO_H);

            // Calculate scale factor but DO NOT upscale beyond 1.0 for on-screen preview
            double sxFactor = Math.min(1.0, (double) cropped.getWidth() / PHOTO_W);
            double syFactor = Math.min(1.0, (double) cropped.getHeight() / PHOTO_H);
            // Here we prefer not to upscale; instead we scale down if needed.
            int drawW = Math.min(PHOTO_W, cropped.getWidth());
            int drawH = Math.min(PHOTO_H, cropped.getHeight());

            // For crispness, use high-quality rendering for downscale
            BufferedImage drawImg = getScaledInstance(cropped, drawW, drawH);

            int offsetX = photoX + (PHOTO_W - drawW) / 2;
            int offsetY = photoY + (PHOTO_H - drawH) / 2;
            // photo background
            g.setColor(Color.WHITE);
            g.fillRect(photoX - 2, photoY - 2, PHOTO_W + 4, PHOTO_H + 4);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(photoX, photoY, PHOTO_W, PHOTO_H);

            g.drawImage(drawImg, offsetX, offsetY, null);
            // border
            g.setColor(Color.DARK_GRAY);
            g.drawRect(photoX, photoY, PHOTO_W, PHOTO_H);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(photoX, photoY, PHOTO_W, PHOTO_H);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(photoX, photoY, PHOTO_W, PHOTO_H);
            g.drawString("No Photo", photoX + 20, photoY + PHOTO_H / 2);
        }

        // Student info
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        int textX = 140;
        int y = 100;
        g.drawString("Name: " + student.getName(), textX, y);
        g.drawString("Roll No: " + student.getRollNo(), textX, y + 25);
        g.drawString("Department: " + student.getDepartment(), textX, y + 50);
        g.drawString("Year: " + student.getYear(), textX, y + 75);
        if (student.getEmail() != null && !student.getEmail().isEmpty()) {
            g.drawString("Email: " + student.getEmail(), textX, y + 100);
        }

        // Footer
        g.setColor(new Color(25, 55, 109));
        g.fillRect(0, getHeight() - 30, getWidth(), 30);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("Valid for Academic Year 2025", 120, getHeight() - 10);

        g.dispose();
    }

    // Center-crop to target aspect ratio but keep full resolution of cropped area
    private static BufferedImage centerCrop(BufferedImage src, int targetW, int targetH) {
        double targetRatio = (double) targetW / targetH;
        int sw = src.getWidth();
        int sh = src.getHeight();
        double srcRatio = (double) sw / sh;

        int cropW = sw, cropH = sh;
        if (srcRatio > targetRatio) {
            // crop width
            cropW = (int) (sh * targetRatio);
        } else {
            // crop height
            cropH = (int) (sw / targetRatio);
        }
        int cropX = (sw - cropW) / 2;
        int cropY = (sh - cropH) / 2;
        return src.getSubimage(cropX, cropY, cropW, cropH);
    }

    // High-quality scale (only for downscaling and small adjustments). Returns TYPE_INT_RGB image.
    private static BufferedImage getScaledInstance(BufferedImage src, int targetW, int targetH) {
        if (src.getWidth() == targetW && src.getHeight() == targetH) {
            // return copy with guaranteed TYPE_INT_RGB
            BufferedImage copy = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = copy.createGraphics();
            g.drawImage(src, 0, 0, null);
            g.dispose();
            return copy;
        }

        BufferedImage scaled = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src, 0, 0, targetW, targetH, null);
        g2.dispose();
        return scaled;
    }
}

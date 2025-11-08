package model;

import java.io.Serializable;

public class Student implements Serializable {
    private String name;
    private String rollNo;
    private String department;
    private int year;
    private String email;
    private String phone;
    private String photoPath;

    public Student(String name, String rollNo, String department, int year,
                   String email, String phone, String photoPath) {
        this.name = name;
        this.rollNo = rollNo;
        this.department = department;
        this.year = year;
        this.email = email;
        this.phone = phone;
        this.photoPath = photoPath;
    }

    public String getName() { return name; }
    public String getRollNo() { return rollNo; }
    public String getDepartment() { return department; }
    public int getYear() { return year; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPhotoPath() { return photoPath; }

    @Override
    public String toString() {
        return name + " (" + rollNo + ") - " + department + " - Year " + year;
    }
}

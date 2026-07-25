public boolean updateStudent(Student student) {
        String sql = "UPDATE students SET name = ?, age = ?, course = ?, email = ?, marks = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getName());
            ps.setInt(2, student.getAge());
            ps.setString(3, student.getCourse());
            ps.setString(4, student.getEmail());
            ps.setDouble(5, student.getMarks());
            ps.setInt(6, student.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
            return false;
        }
    }

    // ---------------- DELETE ----------------
    public boolean deleteStudent(int id) {
        String sql = "DELETE FROM students WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            return false;
        }
    }

    // ---------------- Helper: map a ResultSet row to a Student object ----------------
    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("course"),
                rs.getString("email"),
                rs.getDouble("marks")
        );
    }
}



StudentDBMS APP

import java.util.List;
import java.util.Scanner;

/**
 * StudentDBMSApp.java
 * Console front-end for the Student Database Management System.
 * Presents a menu and delegates all data operations to StudentDAO.
 */
public class StudentDBMSApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final StudentDAO studentDAO = new StudentDAO();

    public static void main(String[] args) {
        boolean running = true;

        System.out.println("=========================================");
        System.out.println(" STUDENT DATABASE MANAGEMENT SYSTEM (JDBC)");
        System.out.println("=========================================");

        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> addStudent();
                case 2 -> viewAllStudents();
                case 3 -> viewStudentById();
                case 4 -> searchStudentsByName();
                case 5 -> updateStudent();
                case 6 -> deleteStudent();
                case 0 -> {
                    running = false;
                    System.out.println("Exiting. Goodbye!");
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
            System.out.println();
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("---------------------------------------");
        System.out.println("1. Add Student");
        System.out.println("2. View All Students");
        System.out.println("3. View Student by ID");
        System.out.println("4. Search Students by Name");
        System.out.println("5. Update Student");
        System.out.println("6. Delete Student");
        System.out.println("0. Exit");
        System.out.println("---------------------------------------");
    }

    // ---------------- Menu actions ----------------

    private static void addStudent() {
        System.out.println("\n-- Add New Student --");
        String name = readString("Name: ");
        int age = readInt("Age: ");
        String course = readString("Course: ");
        String email = readString("Email: ");
        double marks = readDouble("Marks: ");

        Student student = new Student(name, age, course, email, marks);
        boolean success = studentDAO.addStudent(student);

        if (success) {
            System.out.println("Student added successfully with ID: " + student.getId());
        } else {
            System.out.println("Failed to add student.");
        }
    }

    private static void viewAllStudents() {
        System.out.println("\n-- All Students --");
        List<Student> students = studentDAO.getAllStudents();

        if (students.isEmpty()) {
            System.out.println("No student records found.");
        } else {
            for (Student s : students) {
                System.out.println(s);
            }
        }
    }

    private static void viewStudentById() {
        System.out.println("\n-- View Student by ID --");
        int id = readInt("Enter student ID: ");
        Student student = studentDAO.getStudentById(id);

        if (student != null) {
            System.out.println(student);
        } else {
            System.out.println("No student found with ID: " + id);
        }
    }

    private static void searchStudentsByName() {
        System.out.println("\n-- Search Students by Name --");
        String name = readString("Enter name (or part of it): ");
        List<Student> results = studentDAO.searchByName(name);

        if (results.isEmpty()) {
            System.out.println("No matching students found.");
        } else {
            for (Student s : results) {
                System.out.println(s);
            }
        }
    }

    private static void updateStudent() {
        System.out.println("\n-- Update Student --");
        int id = readInt("Enter ID of student to update: ");
        Student existing = studentDAO.getStudentById(id);

        if (existing == null) {
            System.out.println("No student found with ID: " + id);
            return;
        }

        System.out.println("Current record: " + existing);
        System.out.println("Enter new values (leave blank to keep current value):");

        String name = readOptionalString("Name [" + existing.getName() + "]: ", existing.getName());
        int age = readOptionalInt("Age [" + existing.getAge() + "]: ", existing.getAge());
        String course = readOptionalString("Course [" + existing.getCourse() + "]: ", existing.getCourse());
        String email = readOptionalString("Email [" + existing.getEmail() + "]: ", existing.getEmail());
        double marks = readOptionalDouble("Marks [" + existing.getMarks() + "]: ", existing.getMarks());

        existing.setName(name);
        existing.setAge(age);
        existing.setCourse(course);
        existing.setEmail(email);
        existing.setMarks(marks);

        boolean success = studentDAO.updateStudent(existing);
        System.out.println(success ? "Student updated successfully." : "Failed to update student.");
    }

    private static void deleteStudent() {
        System.out.println("\n-- Delete Student --");
        int id = readInt("Enter ID of student to delete: ");

        Student existing = studentDAO.getStudentById(id);
        if (existing == null) {
            System.out.println("No student found with ID: " + id);
            return;
        }

        System.out.println("About to delete: " + existing);
        String confirm = readString("Are you sure? (y/n): ");

        if (confirm.equalsIgnoreCase("y")) {
            boolean success = studentDAO.deleteStudent(id);
            System.out.println(success ? "Student deleted successfully." : "Failed to delete student.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // ---------------- Input helpers (with basic validation) ----------------

    private static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static String readOptionalString(String prompt, String currentValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? currentValue : input;
    }

    private static int readOptionalInt(String prompt, int currentValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return currentValue;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number, keeping current value.");
            return currentValue;
        }
    }

    private static double readOptionalDouble(String prompt, double currentValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return currentValue;
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number, keeping current value.");
            return currentValue;
        }
    }
}


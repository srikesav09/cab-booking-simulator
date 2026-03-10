package cabsimulator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

class User {
    String username, password, name, phone;

    public User(String username, String password, String name, String phone) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.phone = phone;
    }
}


class Driver implements Runnable {
    String name, phone;
    volatile boolean available = true;

    public Driver(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    @Override
    public void run() {
        try {
            while (true) Thread.sleep(5000);
        } catch (Exception ignored) {}
    }
}

class DB {
    static Connection getConnection() throws Exception {
        String url = "jdbc:mysql://localhost:3306/cabsimulator";
        String user = "root";
        String pass = "your_password";
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, pass);
    }
}

class DriverPool {
    private BlockingQueue<Driver> queue = new LinkedBlockingQueue<>();

    public DriverPool(ArrayList<Driver> drivers) {
        queue.addAll(drivers);

        for (Driver d : drivers) {
            Thread t = new Thread(d);
            t.setDaemon(true);
            t.start();
        }
    }

    public Driver assignDriver() throws Exception {
        Driver d = queue.poll(30, TimeUnit.SECONDS);
        if (d == null) throw new Exception("No Drivers Available");

        d.available = false;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE drivers SET available=FALSE WHERE name=?")) {
            ps.setString(1, d.name);
            ps.executeUpdate();
        }

        return d;
    }

    public void releaseDriver(Driver d) {
        d.available = true;
        queue.offer(d);

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE drivers SET available=TRUE WHERE name=?")) {
            ps.setString(1, d.name);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }
}

class AuthManager {
    public User login(String u, String p) throws Exception {
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM users WHERE username=? AND password=?")) {

            ps.setString(1, u);
            ps.setString(2, p);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("phone")
                );
            }
            throw new Exception("Invalid Username or Password!");
        }
    }

    public void registerUser(User u) throws Exception {
        Connection con = DB.getConnection();

        
        PreparedStatement c = con.prepareStatement(
                "SELECT * FROM users WHERE username=?");
        c.setString(1, u.username);
        ResultSet rs = c.executeQuery();
        if (rs.next()) throw new Exception("Username Exists!");

        
        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users(username, password, name, phone) VALUES(?,?,?,?)");
        ps.setString(1, u.username);
        ps.setString(2, u.password);
        ps.setString(3, u.name);
        ps.setString(4, u.phone);
        ps.executeUpdate();
    }
}

class LoginFrame extends JFrame {
    AuthManager auth;
    DriverPool pool;

    JTextField tfUser;
    JPasswordField tfPass;

    public LoginFrame(AuthManager auth, DriverPool pool) {
        this.auth = auth;
        this.pool = pool;

        setTitle("Login");
        setSize(400, 300);
        setLayout(new GridLayout(4, 2, 10, 10));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        add(new JLabel("Username"));
        tfUser = new JTextField();
        add(tfUser);

        add(new JLabel("Password"));
        tfPass = new JPasswordField();
        add(tfPass);

        JButton login = new JButton("Login");
        JButton signup = new JButton("Signup");
        add(login);
        add(signup);

        login.addActionListener(e -> {
            try {
                User u = auth.login(tfUser.getText().trim(),
                        new String(tfPass.getPassword()));
                JOptionPane.showMessageDialog(this, "Login Success!");
                new BookingFrame(u, pool).setVisible(true);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        signup.addActionListener(e -> {
            new SignupFrame(auth, pool).setVisible(true);
            dispose();
        });
    }
}

class SignupFrame extends JFrame {
    AuthManager auth;
    DriverPool pool;

    JTextField tfUser, tfName, tfPhone;
    JPasswordField tfPass;

    public SignupFrame(AuthManager auth, DriverPool pool) {
        this.auth = auth;
        this.pool = pool;

        setTitle("Signup");
        setSize(400, 300);
        setLayout(new GridLayout(5, 2, 10, 10));
        setLocationRelativeTo(null);

        tfName = new JTextField();
        tfPhone = new JTextField();
        tfUser = new JTextField();
        tfPass = new JPasswordField();

        add(new JLabel("Full Name"));
        add(tfName);
        add(new JLabel("Phone"));
        add(tfPhone);
        add(new JLabel("Username"));
        add(tfUser);
        add(new JLabel("Password"));
        add(tfPass);

        JButton register = new JButton("Register");
        add(register);
        add(new JLabel());

        register.addActionListener(e -> {
            try {
                User u = new User(
                        tfUser.getText().trim(),
                        new String(tfPass.getPassword()),
                        tfName.getText().trim(),
                        tfPhone.getText().trim()
                );

                auth.registerUser(u);
                JOptionPane.showMessageDialog(this, "Registered Successfully!");

                new LoginFrame(auth, pool).setVisible(true);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
    }
}

class BookingFrame extends JFrame {
    User user;
    DriverPool pool;

    JComboBox<String> cbPickup, cbDrop;
    JLabel lblFare;

    double fare = 0;

    public BookingFrame(User user, DriverPool pool) {
        this.user = user;
        this.pool = pool;

        setTitle("Welcome " + user.name);
        setSize(500, 350);
        setLayout(new GridLayout(6, 2, 10, 10));
        setLocationRelativeTo(null);

        cbPickup = new JComboBox<>(new String[]{"Gandhipuram", "Peelamedu", "Airport", "RailwayStation"});
        cbDrop = new JComboBox<>(new String[]{"Ukkadam", "Hopes", "Neelambur", "Saibaba Colony"});

        lblFare = new JLabel("₹0");

        JButton calc = new JButton("Calculate Fare");
        JButton book = new JButton("Book Cab");
        JButton profile = new JButton("Profile");

        add(new JLabel("Pickup")); add(cbPickup);
        add(new JLabel("Drop")); add(cbDrop);
        add(new JLabel("Fare")); add(lblFare);

        add(calc); add(book);
        add(profile); add(new JLabel());

        calc.addActionListener(e -> {
            String p = cbPickup.getSelectedItem().toString();
            String d = cbDrop.getSelectedItem().toString();
            fare = (Math.abs(p.length() - d.length()) + 4) * 12;
            lblFare.setText("₹" + fare);
        });

        book.addActionListener(e -> {
            try {
                Driver d = pool.assignDriver();
                JOptionPane.showMessageDialog(this, "Driver Assigned: " + d.name);

                LocalDateTime start = LocalDateTime.now();

                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        LocalDateTime end = LocalDateTime.now();

                        pool.releaseDriver(d);
                        saveTrip(user, d, start, end);

                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(this,
                                        "Trip Completed! Receipt Saved."));
                        SwingUtilities.invokeLater(() -> dispose() );
                    } catch (Exception ignored) {}
                }).start();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        profile.addActionListener(e -> new ProfileFrame(user, pool).setVisible(true));
    }

    private void saveTrip(User u, Driver d, LocalDateTime s, LocalDateTime e) {
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO trips(username, drivername, pickup, dropLoc, fare, startTime, endTime) VALUES(?,?,?,?,?,?,?)")) {

            ps.setString(1, u.username);
            ps.setString(2, d.name);
            ps.setString(3, cbPickup.getSelectedItem().toString());
            ps.setString(4, cbDrop.getSelectedItem().toString());
            ps.setDouble(5, fare);
            ps.setTimestamp(6, Timestamp.valueOf(s));
            ps.setTimestamp(7, Timestamp.valueOf(e));

            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

class ProfileFrame extends JFrame {
    public ProfileFrame(User u, DriverPool pool) {
        setTitle("Profile");
        setSize(400, 250);
        setLayout(new GridLayout(4, 1));
        setLocationRelativeTo(null);

        add(new JLabel("Username: " + u.username));
        add(new JLabel("Name: " + u.name));
        add(new JLabel("Phone: " + u.phone));

        JButton history = new JButton("Trip History");
        add(history);

        history.addActionListener(e ->
                new HistoryFrame(u).setVisible(true));
    }
}

class HistoryFrame extends JFrame {
    public HistoryFrame(User u) {
        setTitle("Trip History");
        setSize(700, 400);
        setLocationRelativeTo(null);

        String[] cols = {"ID", "Driver", "Pickup", "Drop", "Fare", "Start", "End"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);

        loadTrips(u, model);

        add(new JScrollPane(table));
    }

    private void loadTrips(User u, DefaultTableModel model) {
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM trips WHERE username=? ORDER BY startTime DESC")) {

            ps.setString(1, u.username);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("drivername"),
                        rs.getString("pickup"),
                        rs.getString("dropLoc"),
                        rs.getDouble("fare"),
                        rs.getTimestamp("startTime"),
                        rs.getTimestamp("endTime")
                });
            }
        } catch (Exception ignored) {}
    }
}

public class CabSimulator {
    public static void main(String[] args) {

        AuthManager auth = new AuthManager();
        ArrayList<Driver> drivers = new ArrayList<>();

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM drivers");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                drivers.add(new Driver(rs.getString("name"),
                        rs.getString("phone")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        DriverPool pool = new DriverPool(drivers);

        SwingUtilities.invokeLater(() ->
                new LoginFrame(auth, pool).setVisible(true));
    }
}


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;

/*
 * PLATRA - Railway Reservation System
 * Connected with MySQL Database
 *
 * Default Login:
 * Username: admin
 * Password: 1234
 */

public class RailwayReservationSystem {

    // =========================================================
    // COLORS
    // =========================================================

    private static final Color NAVY = new Color(15, 23, 42);
    private static final Color NAVY_LIGHT = new Color(30, 41, 59);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color BLUE_LIGHT = new Color(59, 130, 246);
    private static final Color BACKGROUND = new Color(241, 245, 249);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(15, 23, 42);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color RED = new Color(220, 38, 38);
    private static final Color ORANGE = new Color(234, 88, 12);
    private static final Color BORDER = new Color(226, 232, 240);

    // =========================================================
    // DATA & STATE
    // =========================================================

    private ArrayList<Train> trains = new ArrayList<Train>();
    private ArrayList<Booking> bookings = new ArrayList<Booking>();

    private JFrame frame;
    private JPanel contentPanel;

    private String loggedInUser = "admin";
    private final Random random = new Random();

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Initialize database schema and seed trains if not yet present
                DatabaseConnection.initializeDatabase();
                new RailwayReservationSystem().showLogin();
            }
        });
    }

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public RailwayReservationSystem() {
        loadDataFromDatabase();
    }

    // =========================================================
    // DATABASE DATA LOADING
    // =========================================================

    public void loadDataFromDatabase() {
        loadTrainData();
        loadBookingData();
    }

    private void loadTrainData() {
        trains.clear();
        String sql = "SELECT train_number, train_name, source, destination, departure_time, arrival_time, duration, total_seats, available_seats, base_fare, status FROM trains ORDER BY train_number";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                trains.add(new Train(
                        rs.getString("train_number"),
                        rs.getString("train_name"),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getString("departure_time"),
                        rs.getString("arrival_time"),
                        rs.getString("duration"),
                        rs.getInt("total_seats"),
                        rs.getInt("available_seats"),
                        rs.getInt("base_fare"),
                        rs.getString("status")
                ));
            }
        } catch (Exception e) {
            System.err.println("Database Error: Could not load trains from MySQL: " + e.getMessage());
        }
    }

    private void loadBookingData() {
        bookings.clear();
        String sql = "SELECT b.pnr, b.train_number, b.passenger_name, b.seats, b.fare_class, b.total_fare, " +
                     "t.train_name, t.source, t.destination, t.departure_time, t.arrival_time, t.duration, t.total_seats, t.available_seats, t.base_fare, t.status " +
                     "FROM bookings b JOIN trains t ON b.train_number = t.train_number ORDER BY b.booking_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Train t = findTrainByNumber(rs.getString("train_number"));
                if (t == null) {
                    t = new Train(
                            rs.getString("train_number"),
                            rs.getString("train_name"),
                            rs.getString("source"),
                            rs.getString("destination"),
                            rs.getString("departure_time"),
                            rs.getString("arrival_time"),
                            rs.getString("duration"),
                            rs.getInt("total_seats"),
                            rs.getInt("available_seats"),
                            rs.getInt("base_fare"),
                            rs.getString("status")
                    );
                }
                bookings.add(new Booking(
                        rs.getString("pnr"),
                        t,
                        rs.getString("passenger_name"),
                        rs.getInt("seats"),
                        rs.getString("fare_class"),
                        rs.getDouble("total_fare")
                ));
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load bookings from DB: " + e.getMessage());
        }
    }

    private Train findTrainByNumber(String number) {
        for (Train t : trains) {
            if (t.number.equalsIgnoreCase(number)) {
                return t;
            }
        }
        return null;
    }

    // =========================================================
    // LOGIN PAGE
    // =========================================================

    private void showLogin() {

        final JFrame loginFrame = new JFrame("PLATRA - Railway Reservation (MySQL Connected)");
        loginFrame.setSize(1100, 720);
        loginFrame.setMinimumSize(new Dimension(900, 600));
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);

        // LEFT SIDE
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(NAVY);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Abstract rail lines
                g2.setStroke(new BasicStroke(3));
                g2.setColor(new Color(30, 58, 95));
                for (int i = 0; i < 7; i++) {
                    int y = 400 + i * 45;
                    g2.drawLine(0, y, getWidth(), y - 170);
                }

                // Horizon
                g2.setColor(new Color(51, 65, 85));
                g2.fillRect(0, getHeight() - 140, getWidth(), 140);

                // Train shape
                g2.setColor(BLUE);
                g2.fillRoundRect(80, getHeight() - 220, 250, 75, 20, 20);

                g2.setColor(Color.WHITE);
                g2.fillOval(115, getHeight() - 175, 25, 25);
                g2.fillOval(270, getHeight() - 175, 25, 25);

                g2.dispose();
            }
        };

        leftPanel.setPreferredSize(new Dimension(560, 700));
        leftPanel.setLayout(new BorderLayout());

        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(new EmptyBorder(70, 60, 30, 40));

        JLabel logo = new JLabel("PLATRA");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 38));

        JLabel line = new JLabel("SMART RAILWAY RESERVATION");
        line.setForeground(new Color(147, 197, 253));
        line.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel heading = new JLabel(
                "<html>Travel smarter.<br>Travel farther.</html>"
        );
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font("SansSerif", Font.BOLD, 42));
        heading.setBorder(new EmptyBorder(80, 0, 20, 0));

        JLabel description = new JLabel(
                "<html>Book trains, manage reservations and<br>" +
                        "track your journey from one simple platform.</html>"
        );
        description.setForeground(new Color(203, 213, 225));
        description.setFont(new Font("SansSerif", Font.PLAIN, 16));

        brandPanel.add(logo);
        brandPanel.add(Box.createVerticalStrut(6));
        brandPanel.add(line);
        brandPanel.add(heading);
        brandPanel.add(description);

        leftPanel.add(brandPanel, BorderLayout.NORTH);

        // RIGHT SIDE
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(BACKGROUND);

        JPanel card = new RoundedPanel(25, WHITE);
        card.setPreferredSize(new Dimension(410, 540));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(35, 42, 35, 42));

        JLabel welcome = new JLabel("Welcome back");
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 28));
        welcome.setForeground(TEXT);

        JLabel subtitle = new JLabel("Sign in to your PLATRA account");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        card.add(welcome);
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(30));

        JLabel usernameLabel = createLabel("Username");
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JTextField usernameField = createTextField();
        usernameField.setText("admin");

        JLabel passwordLabel = createLabel("Password");
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JPasswordField passwordField = createPasswordField();
        passwordField.setText("1234");

        card.add(usernameLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(16));
        card.add(passwordLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(passwordField);

        JPanel options = new JPanel(new BorderLayout());
        options.setOpaque(false);
        options.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JCheckBox remember = new JCheckBox("Remember me");
        remember.setOpaque(false);
        remember.setForeground(MUTED);
        remember.setFont(new Font("SansSerif", Font.PLAIN, 12));
        remember.setSelected(true);

        JButton dbSettingsBtn = new JButton("MySQL Config");
        dbSettingsBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        dbSettingsBtn.setForeground(BLUE);
        dbSettingsBtn.setBorderPainted(false);
        dbSettingsBtn.setContentAreaFilled(false);
        dbSettingsBtn.setFocusPainted(false);
        dbSettingsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dbSettingsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDatabaseConfigDialog(loginFrame);
            }
        });

        options.add(remember, BorderLayout.WEST);
        options.add(dbSettingsBtn, BorderLayout.EAST);

        card.add(Box.createVerticalStrut(10));
        card.add(options);
        card.add(Box.createVerticalStrut(20));

        final JButton loginButton = createPrimaryButton("SIGN IN");
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(loginButton);

        JLabel hint = new JLabel(
                "<html><center>Database Connected<br>" +
                        "Default: <b>admin</b> / <b>1234</b></center></html>"
        );
        hint.setForeground(MUTED);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(20));
        card.add(hint);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            loginFrame,
                            "Please enter both username and password.",
                            "Missing Fields",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                // Verify with MySQL Database
                boolean authenticated = false;
                String authError = null;

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement("SELECT username, full_name FROM users WHERE username = ? AND password = ?")) {

                    ps.setString(1, username);
                    ps.setString(2, password);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            authenticated = true;
                            loggedInUser = rs.getString("username");
                        }
                    }
                } catch (Exception ex) {
                    authError = ex.getMessage();
                }

                // If database connection error occurs, notify user immediately
                if (!authenticated && authError != null) {
                    int opt = JOptionPane.showConfirmDialog(
                            loginFrame,
                            "Database Error: Could not connect to MySQL database.\n" +
                            authError + "\n\n" +
                            "Would you like to check and configure MySQL settings now?",
                            "MySQL Connection Error",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE
                    );
                    if (opt == JOptionPane.YES_OPTION) {
                        showDatabaseConfigDialog(loginFrame);
                    }
                    return;
                }

                if (authenticated) {
                    loginFrame.dispose();
                    loadDataFromDatabase();
                    showMainApplication();
                } else {
                    JOptionPane.showMessageDialog(
                            loginFrame,
                            "Invalid username or password.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginButton.doClick();
            }
        });

        rightPanel.add(card);

        root.add(leftPanel, BorderLayout.WEST);
        root.add(rightPanel, BorderLayout.CENTER);

        loginFrame.setContentPane(root);
        loginFrame.setVisible(true);
    }

    // =========================================================
    // DATABASE CONFIGURATION DIALOG
    // =========================================================

    private void showDatabaseConfigDialog(JFrame parent) {
        final JDialog dialog = new JDialog(parent, "MySQL Database Settings", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = new JPanel();
        panel.setBackground(WHITE);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("MySQL Connection Setup");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(TEXT);

        JLabel desc = new JLabel("<html>Configure credentials stored in <b>db.properties</b>.</html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(MUTED);

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(desc);
        panel.add(Box.createVerticalStrut(20));

        JLabel urlLabel = createLabel("JDBC URL");
        final JTextField urlField = createTextField();
        urlField.setText(DatabaseConnection.getUrl());

        JLabel userLabel = createLabel("MySQL User");
        final JTextField userField = createTextField();
        userField.setText(DatabaseConnection.getUser());

        JLabel passLabel = createLabel("MySQL Password");
        final JPasswordField passField = createPasswordField();
        passField.setText(DatabaseConnection.getPassword());

        panel.add(urlLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(urlField);
        panel.add(Box.createVerticalStrut(14));

        panel.add(userLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(userField);
        panel.add(Box.createVerticalStrut(14));

        panel.add(passLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(passField);
        panel.add(Box.createVerticalStrut(22));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton testBtn = createSecondaryButton("Test & Save");
        JButton cancelBtn = createDangerButton("Close");

        testBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newUrl = urlField.getText().trim();
                String newUser = userField.getText().trim();
                String newPass = new String(passField.getPassword());

                DatabaseConnection.updateCredentials(newUrl, newUser, newPass);

                boolean success = DatabaseConnection.initializeDatabase();
                if (success) {
                    JOptionPane.showMessageDialog(
                            dialog,
                            "Successfully connected to MySQL database!\nTables verified and initialized.",
                            "Connection Successful",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    loadDataFromDatabase();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(
                            dialog,
                            "Failed to connect to MySQL with provided settings.\nPlease verify MySQL service is running and password is correct.",
                            "Connection Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(testBtn);
        panel.add(btnRow);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // =========================================================
    // MAIN APPLICATION
    // =========================================================

    private void showMainApplication() {

        frame = new JFrame("PLATRA - Railway Reservation System (MySQL Connected)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 850);
        frame.setMinimumSize(new Dimension(1100, 700));
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);

        root.add(createSidebar(), BorderLayout.WEST);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        root.add(contentPanel, BorderLayout.CENTER);

        frame.setContentPane(root);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        showDashboard();
    }

    // =========================================================
    // SIDEBAR
    // =========================================================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setBackground(NAVY);
        sidebar.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(35, 25, 20, 25));

        JLabel logo = new JLabel("PLATRA");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 28));

        JLabel small = new JLabel("RAILWAY SYSTEM");
        small.setForeground(new Color(148, 163, 184));
        small.setFont(new Font("SansSerif", Font.BOLD, 10));

        top.add(logo);
        top.add(Box.createVerticalStrut(4));
        top.add(small);

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(20, 12, 20, 12));

        addMenuButton(menu, "Dashboard", new Runnable() {
            @Override
            public void run() {
                showDashboard();
            }
        });

        addMenuButton(menu, "Search Trains", new Runnable() {
            @Override
            public void run() {
                showSearchTrains();
            }
        });

        addMenuButton(menu, "My Bookings", new Runnable() {
            @Override
            public void run() {
                showMyBookings();
            }
        });

        addMenuButton(menu, "PNR Search", new Runnable() {
            @Override
            public void run() {
                showPNRSearch();
            }
        });

        addMenuButton(menu, "Train Status", new Runnable() {
            @Override
            public void run() {
                showTrainStatus();
            }
        });

        addMenuButton(menu, "Profile", new Runnable() {
            @Override
            public void run() {
                showProfile();
            }
        });

        sidebar.add(top, BorderLayout.NORTH);
        sidebar.add(menu, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(new EmptyBorder(15, 20, 25, 20));

        JLabel userLabel = new JLabel("Signed in as " + loggedInUser);
        userLabel.setForeground(new Color(148, 163, 184));
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton dbButton = createSidebarButton("MySQL Settings");
        dbButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDatabaseConfigDialog(frame);
            }
        });

        JButton logout = createSidebarButton("Logout");
        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int choice = JOptionPane.showConfirmDialog(
                        frame,
                        "Are you sure you want to logout?",
                        "Logout",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    frame.dispose();
                    showLogin();
                }
            }
        });

        bottom.add(userLabel);
        bottom.add(Box.createVerticalStrut(10));
        bottom.add(dbButton);
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(logout);

        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addMenuButton(JPanel panel, String text, final Runnable action) {
        JButton button = createSidebarButton(text);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
        panel.add(button);
        panel.add(Box.createVerticalStrut(6));
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    private void showDashboard() {

        loadDataFromDatabase();

        contentPanel.removeAll();

        JPanel main = new JPanel(new BorderLayout(0, 25));
        main.setBackground(BACKGROUND);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Manage your railway journeys directly with MySQL database."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton refresh = createSecondaryButton("Refresh Data");
        refresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDashboard();
            }
        });

        header.add(titlePanel, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);

        main.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 25));
        center.setOpaque(false);

        // Statistics
        JPanel stats = new JPanel(new GridLayout(1, 4, 18, 0));
        stats.setOpaque(false);

        stats.add(createStatCard(
                "TOTAL TRAINS",
                String.valueOf(trains.size()),
                "Available routes in DB",
                BLUE
        ));

        stats.add(createStatCard(
                "AVAILABLE SEATS",
                String.valueOf(getTotalAvailableSeats()),
                "Across all trains",
                GREEN
        ));

        stats.add(createStatCard(
                "MY BOOKINGS",
                String.valueOf(bookings.size()),
                "Stored in MySQL",
                ORANGE
        ));

        stats.add(createStatCard(
                "ON-TIME TRAINS",
                String.valueOf(getOnTimeCount()),
                "Current status",
                new Color(124, 58, 237)
        ));

        center.add(stats, BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridLayout(1, 2, 20, 0));
        lower.setOpaque(false);

        // Quick actions
        JPanel quick = createWhitePanel();
        quick.setLayout(new BorderLayout());
        quick.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel quickTitle = new JLabel("Quick Actions");
        quickTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        quickTitle.setForeground(TEXT);

        JPanel actionGrid = new JPanel(new GridLayout(2, 2, 12, 12));
        actionGrid.setOpaque(false);
        actionGrid.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton search = createActionButton("Search Trains");
        JButton bookingsButton = createActionButton("My Bookings");
        JButton pnr = createActionButton("PNR Search");
        JButton status = createActionButton("Train Status");

        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSearchTrains();
            }
        });

        bookingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMyBookings();
            }
        });

        pnr.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPNRSearch();
            }
        });

        status.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTrainStatus();
            }
        });

        actionGrid.add(search);
        actionGrid.add(bookingsButton);
        actionGrid.add(pnr);
        actionGrid.add(status);

        quick.add(quickTitle, BorderLayout.NORTH);
        quick.add(actionGrid, BorderLayout.CENTER);

        // Recent bookings
        JPanel recent = createWhitePanel();
        recent.setLayout(new BorderLayout());
        recent.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel recentTitle = new JLabel("Recent Bookings (MySQL)");
        recentTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        recentTitle.setForeground(TEXT);

        JPanel recentList = new JPanel();
        recentList.setOpaque(false);
        recentList.setLayout(new BoxLayout(recentList, BoxLayout.Y_AXIS));
        recentList.setBorder(new EmptyBorder(15, 0, 0, 0));

        if (bookings.isEmpty()) {
            JLabel empty = new JLabel("No bookings yet.");
            empty.setForeground(MUTED);
            empty.setFont(new Font("SansSerif", Font.PLAIN, 14));
            recentList.add(empty);
        } else {
            int max = Math.min(4, bookings.size());
            for (int i = 0; i < max; i++) {
                Booking b = bookings.get(i);
                JLabel item = new JLabel(
                        b.pnr + "  •  " +
                                b.train.name +
                                "  •  " +
                                b.train.source +
                                " → " +
                                b.train.destination
                );
                item.setFont(new Font("SansSerif", Font.PLAIN, 13));
                item.setForeground(TEXT);

                recentList.add(item);
                recentList.add(Box.createVerticalStrut(12));
            }
        }

        recent.add(recentTitle, BorderLayout.NORTH);
        recent.add(recentList, BorderLayout.CENTER);

        lower.add(quick);
        lower.add(recent);

        center.add(lower, BorderLayout.CENTER);
        main.add(center, BorderLayout.CENTER);

        contentPanel.add(main, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================================================
    // SEARCH TRAINS
    // =========================================================

    private void showSearchTrains() {

        loadTrainData();

        contentPanel.removeAll();

        JPanel main = new JPanel(new BorderLayout(0, 20));
        main.setBackground(BACKGROUND);

        JLabel title = new JLabel("Search Trains");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Find trains between your preferred stations."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);

        main.add(heading, BorderLayout.NORTH);

        JPanel searchBox = createWhitePanel();
        searchBox.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 15));
        searchBox.setBorder(new EmptyBorder(10, 15, 10, 15));

        final JTextField from = createSmallTextField();
        final JTextField to = createSmallTextField();

        from.setToolTipText("Enter source station");
        to.setToolTipText("Enter destination station");

        JButton searchButton = createPrimaryButton("SEARCH");

        searchBox.add(createInlineLabel("From"));
        searchBox.add(from);
        searchBox.add(createInlineLabel("To"));
        searchBox.add(to);
        searchBox.add(searchButton);

        final JPanel results = new JPanel();
        results.setBackground(BACKGROUND);
        results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
        results.setBorder(new EmptyBorder(0, 0, 0, 0));

        JScrollPane scroll = new JScrollPane(results);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel fullCenter = new JPanel(new BorderLayout(0, 20));
        fullCenter.setOpaque(false);

        fullCenter.add(searchBox, BorderLayout.NORTH);
        fullCenter.add(scroll, BorderLayout.CENTER);

        main.add(fullCenter, BorderLayout.CENTER);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String source = from.getText().trim().toLowerCase();
                String destination = to.getText().trim().toLowerCase();

                results.removeAll();
                int count = 0;

                for (Train train : trains) {
                    boolean sourceMatch =
                            source.isEmpty() ||
                                    train.source.toLowerCase().contains(source);

                    boolean destinationMatch =
                            destination.isEmpty() ||
                                    train.destination.toLowerCase().contains(destination);

                    if (sourceMatch && destinationMatch) {
                        results.add(createTrainCard(train));
                        results.add(Box.createVerticalStrut(12));
                        count++;
                    }
                }

                if (count == 0) {
                    JPanel empty = createWhitePanel();
                    empty.setLayout(new GridBagLayout());
                    empty.setPreferredSize(new Dimension(500, 120));

                    JLabel noResult = new JLabel("No trains found.");
                    noResult.setForeground(MUTED);
                    noResult.setFont(new Font("SansSerif", Font.PLAIN, 15));

                    empty.add(noResult);
                    results.add(empty);
                }

                results.revalidate();
                results.repaint();
            }
        });

        // Initial results
        for (Train train : trains) {
            results.add(createTrainCard(train));
            results.add(Box.createVerticalStrut(12));
        }

        contentPanel.add(main, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================================================
    // TRAIN CARD
    // =========================================================

    private JPanel createTrainCard(final Train train) {

        JPanel card = createWhitePanel();

        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel number = new JLabel(train.number);
        number.setFont(new Font("SansSerif", Font.BOLD, 15));
        number.setForeground(BLUE);

        JLabel name = new JLabel(train.name);
        name.setFont(new Font("SansSerif", Font.BOLD, 19));
        name.setForeground(TEXT);

        JLabel route = new JLabel(
                train.source + "  →  " + train.destination
        );
        route.setFont(new Font("SansSerif", Font.PLAIN, 13));
        route.setForeground(MUTED);

        left.add(number);
        left.add(Box.createVerticalStrut(5));
        left.add(name);
        left.add(Box.createVerticalStrut(8));
        left.add(route);

        JPanel middle = new JPanel(new GridLayout(1, 3, 30, 0));
        middle.setOpaque(false);

        middle.add(createInfoBlock("DEPARTURE", train.departure));
        middle.add(createInfoBlock("ARRIVAL", train.arrival));
        middle.add(createInfoBlock("DURATION", train.duration));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel seats = new JLabel(train.availableSeats + " seats available");
        seats.setFont(new Font("SansSerif", Font.BOLD, 13));
        seats.setForeground(train.availableSeats > 20 ? GREEN : ORANGE);
        seats.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel fare = new JLabel("From ₹" + train.baseFare);
        fare.setFont(new Font("SansSerif", Font.BOLD, 17));
        fare.setForeground(TEXT);
        fare.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton book = createPrimaryButton("BOOK");
        book.setAlignmentX(Component.CENTER_ALIGNMENT);

        book.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showBookingDialog(train);
            }
        });

        right.add(seats);
        right.add(Box.createVerticalStrut(8));
        right.add(fare);
        right.add(Box.createVerticalStrut(10));
        right.add(book);

        card.add(left, BorderLayout.WEST);
        card.add(middle, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);

        return card;
    }

    // =========================================================
    // BOOKING DIALOG (Saves to MySQL)
    // =========================================================

    private void showBookingDialog(final Train train) {

        if (train.availableSeats <= 0) {
            JOptionPane.showMessageDialog(
                    frame,
                    "No seats are currently available on this train.",
                    "Sold Out",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        final JDialog dialog = new JDialog(frame, "Book Ticket - " + train.name, true);
        dialog.setSize(520, 580);
        dialog.setLocationRelativeTo(frame);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);

        JPanel top = new JPanel();
        top.setBackground(NAVY);
        top.setBorder(new EmptyBorder(20, 25, 20, 25));
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel trainName = new JLabel(train.name);
        trainName.setForeground(Color.WHITE);
        trainName.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel route = new JLabel(
                train.source + " → " + train.destination + "   |   " + train.number
        );
        route.setForeground(new Color(203, 213, 225));
        route.setFont(new Font("SansSerif", Font.PLAIN, 13));

        top.add(trainName);
        top.add(Box.createVerticalStrut(5));
        top.add(route);

        root.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(25, 30, 25, 30));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel passengerLabel = createLabel("Passenger Name");
        final JTextField passenger = createTextField();

        JLabel seatsLabel = createLabel("Number of Seats");
        final JComboBox<String> seatBox = new JComboBox<String>(
                new String[]{"1", "2", "3", "4", "5", "6"}
        );
        seatBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel classLabel = createLabel("Fare Class");
        final JComboBox<String> classBox = new JComboBox<String>(
                new String[]{"General", "Sleeper", "3A", "2A", "1A"}
        );
        classBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        final JLabel priceLabel = new JLabel("Estimated Fare: ₹0");
        priceLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        priceLabel.setForeground(BLUE);

        JButton confirm = createPrimaryButton("CONFIRM & SAVE TO MYSQL");

        form.add(passengerLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(passenger);
        form.add(Box.createVerticalStrut(18));
        form.add(seatsLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(seatBox);
        form.add(Box.createVerticalStrut(18));
        form.add(classLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(classBox);
        form.add(Box.createVerticalStrut(25));
        form.add(priceLabel);
        form.add(Box.createVerticalStrut(20));
        form.add(confirm);

        root.add(form, BorderLayout.CENTER);

        ActionListener updatePrice = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int seats = Integer.parseInt(seatBox.getSelectedItem().toString());
                double multiplier = getClassMultiplier(classBox.getSelectedItem().toString());
                double fare = train.baseFare * multiplier * seats + (25 * seats);
                priceLabel.setText("Estimated Fare: ₹" + String.format("%.0f", fare));
            }
        };

        seatBox.addActionListener(updatePrice);
        classBox.addActionListener(updatePrice);
        updatePrice.actionPerformed(null);

        confirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String name = passenger.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            dialog,
                            "Please enter passenger name.",
                            "Missing Information",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                int seats = Integer.parseInt(seatBox.getSelectedItem().toString());
                if (seats > train.availableSeats) {
                    JOptionPane.showMessageDialog(
                            dialog,
                            "Only " + train.availableSeats + " seats are available.",
                            "Not Enough Seats",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                String fareClass = classBox.getSelectedItem().toString();
                double multiplier = getClassMultiplier(fareClass);
                double totalFare = train.baseFare * multiplier * seats + (25 * seats);
                String pnr = generatePNR();

                // Save to MySQL with Transaction
                boolean savedToDB = false;
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    try {
                        // 1. Insert Booking
                        String insertBooking = "INSERT INTO bookings (pnr, train_number, passenger_name, seats, fare_class, total_fare) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement ps = conn.prepareStatement(insertBooking)) {
                            ps.setString(1, pnr);
                            ps.setString(2, train.number);
                            ps.setString(3, name);
                            ps.setInt(4, seats);
                            ps.setString(5, fareClass);
                            ps.setDouble(6, totalFare);
                            ps.executeUpdate();
                        }

                        // 2. Decrement seats in trains table
                        String updateTrain = "UPDATE trains SET available_seats = available_seats - ? WHERE train_number = ?";
                        try (PreparedStatement ps2 = conn.prepareStatement(updateTrain)) {
                            ps2.setInt(1, seats);
                            ps2.setString(2, train.number);
                            ps2.executeUpdate();
                        }

                        conn.commit();
                        savedToDB = true;
                    } catch (Exception ex) {
                        conn.rollback();
                        throw ex;
                    } finally {
                        conn.setAutoCommit(true);
                    }
                } catch (Exception ex) {
                    System.err.println("Database booking error: " + ex.getMessage());
                    JOptionPane.showMessageDialog(
                            dialog,
                            "Database error while saving booking: " + ex.getMessage() + "\nSaving locally for now.",
                            "Database Notice",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

                train.availableSeats -= seats;

                Booking booking = new Booking(
                        pnr,
                        train,
                        name,
                        seats,
                        fareClass,
                        totalFare
                );

                bookings.add(0, booking);

                dialog.dispose();
                showBookingConfirmation(booking);
            }
        });

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // =========================================================
    // BOOKING CONFIRMATION
    // =========================================================

    private void showBookingConfirmation(Booking booking) {

        String message =
                "Booking confirmed and stored in MySQL database!\n\n" +
                        "PNR: " + booking.pnr + "\n" +
                        "Passenger: " + booking.passengerName + "\n" +
                        "Train: " + booking.train.name + "\n" +
                        "Route: " + booking.train.source + " → " + booking.train.destination + "\n" +
                        "Seats: " + booking.seats + "\n" +
                        "Class: " + booking.fareClass + "\n" +
                        "Total Fare: ₹" + String.format("%.0f", booking.totalFare);

        JOptionPane.showMessageDialog(
                frame,
                message,
                "Booking Confirmed",
                JOptionPane.INFORMATION_MESSAGE
        );

        showMyBookings();
    }

    // =========================================================
    // MY BOOKINGS (Loaded from MySQL)
    // =========================================================

    private void showMyBookings() {

        loadBookingData();

        contentPanel.removeAll();

        JPanel main = new JPanel(new BorderLayout(0, 20));
        main.setBackground(BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("My Bookings");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "View, manage and cancel your reservations directly from MySQL."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(5));
        titles.add(subtitle);

        header.add(titles, BorderLayout.WEST);
        main.add(header, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setBackground(BACKGROUND);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        if (bookings.isEmpty()) {
            JPanel empty = createWhitePanel();
            empty.setLayout(new GridBagLayout());
            empty.setPreferredSize(new Dimension(500, 180));

            JLabel label = new JLabel("You don't have any bookings yet.");
            label.setFont(new Font("SansSerif", Font.PLAIN, 16));
            label.setForeground(MUTED);

            empty.add(label);
            list.add(empty);
        } else {
            for (Booking b : bookings) {
                list.add(createBookingCard(b));
                list.add(Box.createVerticalStrut(12));
            }
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        main.add(scroll, BorderLayout.CENTER);
        contentPanel.add(main, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================================================
    // BOOKING CARD (With MySQL Cancellation)
    // =========================================================

    private JPanel createBookingCard(final Booking booking) {

        JPanel card = createWhitePanel();
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(new EmptyBorder(22, 25, 22, 25));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel pnr = new JLabel("PNR  " + booking.pnr);
        pnr.setForeground(BLUE);
        pnr.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel train = new JLabel(booking.train.name);
        train.setForeground(TEXT);
        train.setFont(new Font("SansSerif", Font.BOLD, 19));

        JLabel route = new JLabel(
                booking.train.source + " → " + booking.train.destination
        );
        route.setForeground(MUTED);
        route.setFont(new Font("SansSerif", Font.PLAIN, 13));

        left.add(pnr);
        left.add(Box.createVerticalStrut(6));
        left.add(train);
        left.add(Box.createVerticalStrut(7));
        left.add(route);

        JPanel middle = new JPanel(new GridLayout(2, 2, 30, 10));
        middle.setOpaque(false);

        middle.add(createInfoBlock("PASSENGER", booking.passengerName));
        middle.add(createInfoBlock("SEATS", String.valueOf(booking.seats)));
        middle.add(createInfoBlock("CLASS", booking.fareClass));
        middle.add(createInfoBlock("FARE", "₹" + String.format("%.0f", booking.totalFare)));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JButton ticket = createSecondaryButton("VIEW TICKET");
        JButton cancel = createDangerButton("CANCEL");

        ticket.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancel.setAlignmentX(Component.CENTER_ALIGNMENT);

        ticket.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTicket(booking);
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int choice = JOptionPane.showConfirmDialog(
                        frame,
                        "Are you sure you want to cancel booking " + booking.pnr + "?\nThis will remove it from the MySQL database and restore seats.",
                        "Cancel Booking",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {

                    // Cancel from MySQL
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        conn.setAutoCommit(false);
                        try {
                            // 1. Delete booking
                            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM bookings WHERE pnr = ?")) {
                                ps.setString(1, booking.pnr);
                                ps.executeUpdate();
                            }

                            // 2. Restore available seats
                            try (PreparedStatement ps2 = conn.prepareStatement("UPDATE trains SET available_seats = available_seats + ? WHERE train_number = ?")) {
                                ps2.setInt(1, booking.seats);
                                ps2.setString(2, booking.train.number);
                                ps2.executeUpdate();
                            }

                            conn.commit();
                        } catch (Exception ex) {
                            conn.rollback();
                            throw ex;
                        } finally {
                            conn.setAutoCommit(true);
                        }
                    } catch (Exception ex) {
                        System.err.println("Database cancellation error: " + ex.getMessage());
                    }

                    booking.train.availableSeats += booking.seats;
                    bookings.remove(booking);

                    JOptionPane.showMessageDialog(
                            frame,
                            "Booking cancelled successfully.",
                            "Cancelled",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    showMyBookings();
                }
            }
        });

        right.add(ticket);
        right.add(Box.createVerticalStrut(10));
        right.add(cancel);

        card.add(left, BorderLayout.WEST);
        card.add(middle, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);

        return card;
    }

    // =========================================================
    // TICKET
    // =========================================================

    private void showTicket(Booking booking) {

        JDialog dialog = new JDialog(frame, "Railway Ticket", true);
        dialog.setSize(550, 600);
        dialog.setLocationRelativeTo(frame);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);

        JPanel ticket = new RoundedPanel(20, WHITE);
        ticket.setBorder(new EmptyBorder(35, 35, 35, 35));
        ticket.setLayout(new BoxLayout(ticket, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("PLATRA");
        logo.setForeground(BLUE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 28));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heading = new JLabel("E-RAILWAY TICKET (VERIFIED)");
        heading.setForeground(MUTED);
        heading.setFont(new Font("SansSerif", Font.BOLD, 11));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel pnr = new JLabel("PNR  " + booking.pnr);
        pnr.setForeground(TEXT);
        pnr.setFont(new Font("SansSerif", Font.BOLD, 22));
        pnr.setAlignmentX(Component.CENTER_ALIGNMENT);

        ticket.add(logo);
        ticket.add(Box.createVerticalStrut(5));
        ticket.add(heading);
        ticket.add(Box.createVerticalStrut(20));
        ticket.add(pnr);
        ticket.add(Box.createVerticalStrut(25));

        ticket.add(createTicketRow("Train", booking.train.number + " - " + booking.train.name));
        ticket.add(createTicketRow("Route", booking.train.source + " → " + booking.train.destination));
        ticket.add(createTicketRow("Departure", booking.train.departure));
        ticket.add(createTicketRow("Arrival", booking.train.arrival));
        ticket.add(createTicketRow("Passenger", booking.passengerName));
        ticket.add(createTicketRow("Seats", String.valueOf(booking.seats)));
        ticket.add(createTicketRow("Class", booking.fareClass));
        ticket.add(createTicketRow("Total Fare", "₹" + String.format("%.0f", booking.totalFare)));

        ticket.add(Box.createVerticalStrut(25));

        JLabel status = new JLabel("CONFIRMED (IN MYSQL)");
        status.setForeground(GREEN);
        status.setFont(new Font("SansSerif", Font.BOLD, 15));
        status.setAlignmentX(Component.CENTER_ALIGNMENT);

        ticket.add(status);

        root.add(ticket, BorderLayout.CENTER);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private JPanel createTicketRow(String label, String value) {

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(7, 0, 7, 0));

        JLabel l = new JLabel(label);
        l.setForeground(MUTED);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JLabel v = new JLabel(value);
        v.setForeground(TEXT);
        v.setFont(new Font("SansSerif", Font.BOLD, 13));

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);

        return row;
    }

    // =========================================================
    // PNR SEARCH (Queries MySQL Directly)
    // =========================================================

    private void showPNRSearch() {

        contentPanel.removeAll();

        JPanel main = new JPanel(new BorderLayout(0, 25));
        main.setBackground(BACKGROUND);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("PNR Search");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Check your reservation status directly from the MySQL database."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);

        main.add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel card = createWhitePanel();
        card.setPreferredSize(new Dimension(540, 320));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(35, 40, 35, 40));

        JLabel label = createLabel("Enter 9-digit PNR Number");
        final JTextField pnrField = createTextField();

        JButton search = createPrimaryButton("SEARCH MYSQL PNR");

        final JLabel result = new JLabel(" ");
        result.setFont(new Font("SansSerif", Font.PLAIN, 14));
        result.setForeground(TEXT);

        card.add(label);
        card.add(Box.createVerticalStrut(8));
        card.add(pnrField);
        card.add(Box.createVerticalStrut(20));
        card.add(search);
        card.add(Box.createVerticalStrut(25));
        card.add(result);

        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String entered = pnrField.getText().trim();
                if (entered.isEmpty()) {
                    result.setForeground(RED);
                    result.setText("Please enter a PNR number.");
                    return;
                }

                // Query MySQL
                String sql = "SELECT b.pnr, b.passenger_name, b.seats, b.fare_class, b.total_fare, " +
                             "t.train_name, t.source, t.destination, t.departure_time, t.status " +
                             "FROM bookings b JOIN trains t ON b.train_number = t.train_number " +
                             "WHERE b.pnr = ?";

                boolean found = false;

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setString(1, entered);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            found = true;
                            result.setForeground(GREEN);
                            result.setText(
                                    "<html><b>Booking Found in Database:</b><br>" +
                                            "Train: " + rs.getString("train_name") + " (" + rs.getString("departure_time") + ")<br>" +
                                            "Route: " + rs.getString("source") + " → " + rs.getString("destination") + "<br>" +
                                            "Passenger: " + rs.getString("passenger_name") + " (" + rs.getInt("seats") + " seats, " + rs.getString("fare_class") + ")<br>" +
                                            "Total Fare: ₹" + rs.getDouble("total_fare") + " &nbsp;|&nbsp; Status: <b>" + rs.getString("status") + "</b>" +
                                            "</html>"
                            );
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("PNR Query error: " + ex.getMessage());
                }

                if (!found) {
                    // Check local memory as fallback
                    Booking b = findBookingByPNR(entered);
                    if (b != null) {
                        result.setForeground(GREEN);
                        result.setText(
                                "<html><b>Booking Found:</b><br>" +
                                        b.train.name + "<br>" +
                                        b.train.source + " → " + b.train.destination + "<br>" +
                                        "Passenger: " + b.passengerName +
                                        "</html>"
                        );
                    } else {
                        result.setForeground(RED);
                        result.setText("No booking found for PNR: " + entered);
                    }
                }
            }
        });

        center.add(card);
        main.add(center, BorderLayout.CENTER);

        contentPanel.add(main, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================================================
    // TRAIN STATUS (From MySQL)
    // =========================================================

    private void showTrainStatus() {

        loadTrainData();

        contentPanel.removeAll();

        JPanel main = new JPanel(new BorderLayout(0, 20));
        main.setBackground(BACKGROUND);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Train Status");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Current operational status of available trains (Live MySQL records)."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);

        main.add(heading, BorderLayout.NORTH);

        JPanel tablePanel = createWhitePanel();
        tablePanel.setLayout(new BorderLayout());

        String[] columns = {
                "Train",
                "Train Name",
                "Route",
                "Departure",
                "Arrival",
                "Available",
                "Status"
        };

        Object[][] data = new Object[trains.size()][7];

        for (int i = 0; i < trains.size(); i++) {
            Train t = trains.get(i);
            data[i][0] = t.number;
            data[i][1] = t.name;
            data[i][2] = t.source + " → " + t.destination;
            data[i][3] = t.departure;
            data[i][4] = t.arrival;
            data[i][5] = t.availableSeats + " / " + t.totalSeats;
            data[i][6] = t.status;
        }

        JTable table = new JTable(data, columns);
        table.setRowHeight(42);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);

        tablePanel.add(scroll, BorderLayout.CENTER);
        main.add(tablePanel, BorderLayout.CENTER);

        contentPanel.add(main, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================================================
    // PROFILE
    // =========================================================

    private void showProfile() {

        contentPanel.removeAll();

        JPanel main = new JPanel(new BorderLayout(0, 25));
        main.setBackground(BACKGROUND);

        JLabel title = new JLabel("Profile");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(TEXT);

        main.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel card = createWhitePanel();
        card.setPreferredSize(new Dimension(600, 480));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(35, 50, 35, 50));

        JLabel avatar = new JLabel("P");
        avatar.setOpaque(true);
        avatar.setBackground(BLUE);
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 34));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(80, 80));
        avatar.setMaximumSize(new Dimension(80, 80));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel("PLATRA Administrator");
        name.setFont(new Font("SansSerif", Font.BOLD, 22));
        name.setForeground(TEXT);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel username = new JLabel("@" + loggedInUser);
        username.setFont(new Font("SansSerif", Font.PLAIN, 14));
        username.setForeground(MUTED);
        username.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(avatar);
        card.add(Box.createVerticalStrut(16));
        card.add(name);
        card.add(Box.createVerticalStrut(4));
        card.add(username);
        card.add(Box.createVerticalStrut(25));

        card.add(createProfileRow("Account Type", "Administrator"));
        card.add(createProfileRow("Total Bookings", String.valueOf(bookings.size())));
        card.add(createProfileRow("Available Trains", String.valueOf(trains.size())));
        card.add(createProfileRow("Database", "MySQL (railway_reservation)"));
        card.add(createProfileRow("DB Host / Port", "localhost:3306"));

        JButton configBtn = createSecondaryButton("Configure Database Connection");
        configBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        configBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDatabaseConfigDialog(frame);
            }
        });

        card.add(Box.createVerticalStrut(20));
        card.add(configBtn);

        center.add(card);
        main.add(center, BorderLayout.CENTER);

        contentPanel.add(main, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createProfileRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 0, 8, 0));

        JLabel left = new JLabel(label);
        left.setFont(new Font("SansSerif", Font.PLAIN, 13));
        left.setForeground(MUTED);

        JLabel right = new JLabel(value);
        right.setFont(new Font("SansSerif", Font.BOLD, 13));
        right.setForeground(TEXT);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    // =========================================================
    // STATISTICS
    // =========================================================

    private JPanel createStatCard(String title, String value, String description, Color accent) {

        JPanel card = createWhitePanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        titleLabel.setForeground(MUTED);

        JLabel dot = new JLabel("●");
        dot.setForeground(accent);
        dot.setFont(new Font("SansSerif", Font.BOLD, 14));

        top.add(titleLabel, BorderLayout.WEST);
        top.add(dot, BorderLayout.EAST);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        valueLabel.setForeground(TEXT);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descLabel.setForeground(MUTED);

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(descLabel, BorderLayout.SOUTH);

        return card;
    }

    // =========================================================
    // UI HELPERS
    // =========================================================

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(TEXT);
        return label;
    }

    private JLabel createInlineLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(TEXT);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(WHITE);
        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(0, 12, 0, 12)
                )
        );
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        return field;
    }

    private JTextField createSmallTextField() {
        JTextField field = createTextField();
        field.setPreferredSize(new Dimension(190, 42));
        field.setMaximumSize(new Dimension(190, 42));
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(WHITE);
        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(0, 12, 0, 12)
                )
        );
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        return field;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(BLUE);
        button.setBorder(new EmptyBorder(12, 20, 12, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setForeground(BLUE);
        button.setBackground(WHITE);
        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(9, 16, 9, 16)
                )
        );
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setForeground(RED);
        button.setBackground(WHITE);
        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(254, 202, 202)),
                        new EmptyBorder(9, 16, 9, 16)
                )
        );
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(new Color(203, 213, 225));
        button.setBackground(NAVY);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(13, 15, 13, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        return button;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(TEXT);
        button.setBackground(new Color(248, 250, 252));
        button.setBorder(BorderFactory.createLineBorder(BORDER));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createWhitePanel() {
        JPanel panel = new RoundedPanel(18, WHITE);
        panel.setOpaque(false);
        return panel;
    }

    private JPanel createInfoBlock(String title, String value) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel top = new JLabel(title);
        top.setFont(new Font("SansSerif", Font.BOLD, 10));
        top.setForeground(MUTED);

        JLabel bottom = new JLabel(value);
        bottom.setFont(new Font("SansSerif", Font.BOLD, 14));
        bottom.setForeground(TEXT);

        panel.add(top);
        panel.add(Box.createVerticalStrut(5));
        panel.add(bottom);

        return panel;
    }

    // =========================================================
    // CALCULATIONS & HELPERS
    // =========================================================

    private double getClassMultiplier(String fareClass) {
        if (fareClass.equals("General")) return 1.0;
        if (fareClass.equals("Sleeper")) return 1.5;
        if (fareClass.equals("3A")) return 2.2;
        if (fareClass.equals("2A")) return 3.0;
        if (fareClass.equals("1A")) return 4.0;
        return 1.0;
    }

    private int getTotalAvailableSeats() {
        int total = 0;
        for (Train train : trains) {
            total += train.availableSeats;
        }
        return total;
    }

    private int getOnTimeCount() {
        int count = 0;
        for (Train train : trains) {
            if (train.status.equalsIgnoreCase("On Time")) {
                count++;
            }
        }
        return count;
    }

    private String generatePNR() {
        String pnr;
        do {
            int number = 100000000 + random.nextInt(900000000);
            pnr = String.valueOf(number);
        } while (findBookingByPNR(pnr) != null);
        return pnr;
    }

    private Booking findBookingByPNR(String pnr) {
        for (Booking booking : bookings) {
            if (booking.pnr.equals(pnr)) {
                return booking;
            }
        }
        return null;
    }

    // =========================================================
    // MODELS
    // =========================================================

    public static class Train {
        public String number;
        public String name;
        public String source;
        public String destination;
        public String departure;
        public String arrival;
        public String duration;
        public int totalSeats;
        public int availableSeats;
        public int baseFare;
        public String status;

        public Train(
                String number,
                String name,
                String source,
                String destination,
                String departure,
                String arrival,
                String duration,
                int totalSeats,
                int availableSeats,
                int baseFare,
                String status
        ) {
            this.number = number;
            this.name = name;
            this.source = source;
            this.destination = destination;
            this.departure = departure;
            this.arrival = arrival;
            this.duration = duration;
            this.totalSeats = totalSeats;
            this.availableSeats = availableSeats;
            this.baseFare = baseFare;
            this.status = status;
        }
    }

    public static class Booking {
        public String pnr;
        public Train train;
        public String passengerName;
        public int seats;
        public String fareClass;
        public double totalFare;

        public Booking(
                String pnr,
                Train train,
                String passengerName,
                int seats,
                String fareClass,
                double totalFare
        ) {
            this.pnr = pnr;
            this.train = train;
            this.passengerName = passengerName;
            this.seats = seats;
            this.fareClass = fareClass;
            this.totalFare = totalFare;
        }
    }

    public static class RoundedPanel extends JPanel {
        private int radius;
        private Color backgroundColor;

        public RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdvancedQueriesGUI extends JFrame {

    // Looks for the SQL file from the project folder or from the java folder
    private static final String[] QUERY_FILE_CANDIDATES = {
        "queries/AdvancedQueries.sql",
        "../queries/AdvancedQueries.sql"
    };

    // Button labels, in the same order as the queries in AdvancedQueries.sql
    private static final String[] BUTTON_TITLES = {
        "Pending Friend Requests",
        "Public Posts",
        "Comments with Usernames",
        "Reactions by RealMojis",
        "Tagged Users"
    };

    // SQL statements loaded from the file, in file order
    private final List<String> queries = new ArrayList<>();

    private JTable resultTable;
    private DefaultTableModel tableModel;

    public AdvancedQueriesGUI() {

        loadQueries();

        setTitle("BeReal - Advanced Queries");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ---- Title and query buttons (top) ----
        JLabel title = new JLabel("Advanced Queries");
        title.setFont(new Font("Arial", Font.BOLD, 22));

        JPanel buttonPanel = new JPanel(new GridLayout(1, BUTTON_TITLES.length, 10, 10));

        for (int i = 0; i < BUTTON_TITLES.length; i++) {
            JButton button = new JButton(BUTTON_TITLES[i]);
            final int index = i;
            button.addActionListener(e -> runQuery(index));
            buttonPanel.add(button);
        }

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        // ---- Result table (center) ----
        tableModel = new DefaultTableModel();
        resultTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultTable);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    // ---------- Loading queries from the .sql file ----------

    private void loadQueries() {
        Path file = findQueryFile();
        if (file == null) {
            JOptionPane.showMessageDialog(this,
                "Could not find queries/AdvancedQueries.sql.\n" +
                "Run the program from the project folder.");
            return;
        }

        try {
            // Remove a possible BOM added by some editors
            String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8)
                                 .replace("\uFEFF", "");

            // Each statement ends with ';' in the file
            for (String chunk : content.split(";")) {
                StringBuilder sql = new StringBuilder();

                for (String line : chunk.split("\\r?\\n")) {
                    String trimmed = line.trim();
                    // Skip blank lines and comment lines
                    if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                        continue;
                    }
                    sql.append(trimmed).append(' ');
                }

                String statement = sql.toString().trim();
                if (!statement.isEmpty()) {
                    queries.add(statement);
                }
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Could not read the query file: " + ex.getMessage());
        }
    }

    private Path findQueryFile() {
        for (String candidate : QUERY_FILE_CANDIDATES) {
            Path path = Paths.get(candidate);
            if (Files.exists(path)) {
                return path;
            }
        }
        return null;
    }

    // ---------- Running queries ----------

    // Runs the query at the given index and shows the result in the table
    private void runQuery(int index) {
        if (index >= queries.size()) {
            JOptionPane.showMessageDialog(this,
                "Query #" + (index + 1) + " was not found in the file.");
            return;
        }

        String sql = queries.get(index);

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            fillTable(rs);

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No results found.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    // Copies a ResultSet into the JTable model
    private void fillTable(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columns = metaData.getColumnCount();

        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        for (int i = 1; i <= columns; i++) {
            tableModel.addColumn(metaData.getColumnName(i));
        }

        while (rs.next()) {
            Object[] row = new Object[columns];
            for (int i = 1; i <= columns; i++) {
                row[i - 1] = rs.getObject(i);
            }
            tableModel.addRow(row);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AdvancedQueriesGUI().setVisible(true);
        });
    }
}
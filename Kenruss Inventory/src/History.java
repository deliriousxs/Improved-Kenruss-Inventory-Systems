import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class History implements ActionListener {

    JFrame historyWindowFrame = new JFrame();
    JPanel backPanel = new JPanel();
    JPanel upPanel = new JPanel();
    JPanel lowPanel = new JPanel();
    JTable historyTable = new JTable();

    JLabel titleLabel = new JLabel("Transaction History");
    JTextField searchField = new JTextField();
    JButton clearButton = new JButton("Clear Search");

    JButton backButton = new RoundedButton("Back");

    History() {
        historyWindowFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        historyWindowFrame.setSize(1100, 750);
        historyWindowFrame.setLayout(null);
        historyWindowFrame.setResizable(false);
        historyWindowFrame.setLocationRelativeTo(null);
        historyWindowFrame.setVisible(true);

        backPanel.setLayout(null);
        backPanel.setBackground(new Color(4, 76, 172));
        backPanel.setBounds(0, 0, 1100, 750);

        upPanel.setLayout(null);
        upPanel.setBackground(new Color(84, 116, 251));
        upPanel.setBounds(15, 0, 1058, 95);

        lowPanel.setLayout(null);
        lowPanel.setBackground(new Color(141, 189, 255));
        lowPanel.setBounds(15, 95, 1058, 605);

        titleLabel.setFont(new Font("Verdana", Font.BOLD, 46));
        titleLabel.setBounds(300, 25, 800, 46);
        titleLabel.setForeground(Color.white);

        searchField.setBounds(10, 10, 300, 30);
        clearButton.setBounds(320, 10, 120, 30);
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchField.setText("");
                TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) historyTable.getRowSorter();
                if (sorter != null) {
                    sorter.setRowFilter(null);
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                search(searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                search(searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                search(searchField.getText());
            }
        });

        backButton.setBounds(950, 640, 100, 40);
        backButton.setBackground(new Color(4, 76, 172));
        backButton.setFont(new Font("Verdana", Font.BOLD, 20));
        backButton.addActionListener(this);

        historyWindowFrame.add(backButton);
        historyWindowFrame.add(titleLabel);
        historyWindowFrame.add(upPanel);
        historyWindowFrame.add(lowPanel);
        historyWindowFrame.add(backPanel);

        DefaultTableModel historyModel = new DefaultTableModel();
        historyTable.setModel(historyModel);
        historyTable.setRowSorter(new TableRowSorter<>(historyModel));
        fetchHistory(historyModel);

        JScrollPane historyScrollPane = new JScrollPane(historyTable);
        historyScrollPane.setBounds(10, 50, 1038, 490);

        lowPanel.add(searchField);
        lowPanel.add(clearButton);
        lowPanel.add(historyScrollPane);
    }

    private void fetchHistory(DefaultTableModel historyModel) {
        String serverName = "LAPTOP-VFJUTU85\\SQLEXPRESS";
        String databaseName = "Kenruss";
        String url = "jdbc:sqlserver://" + serverName + ";databaseName=" + databaseName + ";integratedSecurity=true;encrypt=true;trustServerCertificate=true";

        String query = "SELECT s.sales_id, s.prod_id, s.quantity, p.price, s.quantity * p.price AS total_amount, s.bought_date, p.item_name, p.item_desc, p.category " +
                       "FROM Sales s " +
                       "JOIN Products p ON s.prod_id = p.prod_id " +
                       "ORDER BY s.bought_date DESC";

        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (historyModel.getColumnCount() == 0) {
                historyModel.addColumn("Sale ID");
                historyModel.addColumn("Product ID");
                historyModel.addColumn("Item Name");
                historyModel.addColumn("Item Description");
                historyModel.addColumn("Category"); 
                historyModel.addColumn("Quantity");
                historyModel.addColumn("Price");
                historyModel.addColumn("Total Amount");
                historyModel.addColumn("Date and Time");
            }

            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            while (resultSet.next()) {
                Object[] row = new Object[9]; 
                row[0] = resultSet.getInt("sales_id");
                row[1] = resultSet.getString("prod_id");
                row[2] = resultSet.getString("item_name");
                row[3] = resultSet.getString("item_desc");
                row[4] = resultSet.getString("category"); 
                row[5] = resultSet.getInt("quantity");
                row[6] = resultSet.getDouble("price");
                row[7] = resultSet.getDouble("total_amount");
                row[8] = dateTimeFormat.format(resultSet.getTimestamp("bought_date"));
                historyModel.addRow(row);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to fetch history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void search(String str) {
        DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        historyTable.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + str));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton) {
            historyWindowFrame.dispose();
            Sales sales = new Sales();
        }
    }
}
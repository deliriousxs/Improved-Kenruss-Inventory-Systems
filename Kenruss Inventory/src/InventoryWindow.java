import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;

public class InventoryWindow implements ActionListener {
 
    JFrame inventoryWindowFrame = new JFrame();
    JPanel backPanel = new JPanel();
    JPanel upPanel = new JPanel();
    JPanel lowPanel = new JPanel();
    JPanel bestSellerPanel = new JPanel();
    JPanel lowStockPanel = new JPanel();
 
    JTable bestSellerTableWeekly = new JTable();
    DefaultTableModel bestSellerModelWeekly;
 
    JTable bestSellerTableMonthly = new JTable();
    DefaultTableModel bestSellerModelMonthly;
 
    JTable lowStockTable = new JTable();
    DefaultTableModel lowStockModel;
 
    JLabel titleLabel = new JLabel("INVENTORY");
    JTable table = new JTable();
    DefaultTableModel model;
    JTextField searchField = new JTextField();
    JButton backButton = new RoundedButton("Back");
    JButton viewSupplierButton = new RoundedButton("View Supplier");
    JButton clearButton = new JButton("Clear Search");
 
    JComboBox<String> timePeriodDropdown = new JComboBox<>(new String[]{"Weekly", "Monthly"});
 
    Set<String> bestSellerProductIDsWeekly = new HashSet<>();
    Set<String> bestSellerProductIDsMonthly = new HashSet<>();
 
    InventoryWindow() {
        inventoryWindowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        inventoryWindowFrame.setSize(1100, 750);
        inventoryWindowFrame.setLayout(null);
        inventoryWindowFrame.setResizable(false);
        inventoryWindowFrame.setLocationRelativeTo(null);
        inventoryWindowFrame.setVisible(true);
 
        backPanel.setLayout(null);
        backPanel.setBackground(new Color(4, 76, 172));
        backPanel.setBounds(0, 0, 1100, 750);
 
        upPanel.setLayout(null);
        upPanel.setBackground(new Color(84, 116, 251));
        upPanel.setBounds(15, 0, 1050, 95);
 
        lowPanel.setLayout(null);
        lowPanel.setBackground(new Color(141, 189, 255));
        lowPanel.setBounds(15, 95, 525, 605);
 
        bestSellerPanel.setLayout(null);
        bestSellerPanel.setBackground(new Color(141, 189, 255));
        bestSellerPanel.setBounds(540, 95, 525, 290);
 
        lowStockPanel.setLayout(null);
        lowStockPanel.setBackground(new Color(141, 189, 255));
        lowStockPanel.setBounds(540, 390, 525, 310);
 
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 46));
        titleLabel.setBounds(380, 25, 800, 46);
        titleLabel.setForeground(Color.white);
 
        backButton.setBounds(38, 650, 100, 40);
        backButton.setBackground(new Color(4, 76, 172));
        backButton.setFont(new Font("Verdana", Font.BOLD, 20));
        backButton.addActionListener(this);
 
        viewSupplierButton.setBounds(310, 650, 200, 40);
        viewSupplierButton.setBackground(new Color(4, 76, 172));
        viewSupplierButton.setFont(new Font("Verdana", Font.BOLD, 20));
        viewSupplierButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == backButton) {
                    inventoryWindowFrame.dispose();
                    HomePage homePage = new HomePage();
                } else if (e.getSource() == viewSupplierButton) {
                    inventoryWindowFrame.dispose();
                    ViewSupplier viewSupplierWindow = new ViewSupplier();
                }
            }
        });
 
        table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(10, 50, 505, 490);
        lowPanel.add(scrollPane);
 
        searchField.setBounds(10, 10, 300, 30);
        lowPanel.add(searchField);
 
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
 
        clearButton.setBounds(320, 10, 120, 30);
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchField.setText("");
                table.setRowSorter(null);
            }
        });
        lowPanel.add(clearButton);
 
        bestSellerTableWeekly = new JTable();
        bestSellerTableMonthly = new JTable();
 
        JScrollPane bestSellerScrollPaneWeekly = new JScrollPane(bestSellerTableWeekly);
        bestSellerScrollPaneWeekly.setBounds(10, 50, 505, 225);
        bestSellerPanel.add(bestSellerScrollPaneWeekly);
 
        JScrollPane bestSellerScrollPaneMonthly = new JScrollPane(bestSellerTableMonthly);
        bestSellerScrollPaneMonthly.setBounds(10, 50, 505, 225);
 
        bestSellerPanel.add(bestSellerScrollPaneWeekly);
        bestSellerPanel.add(bestSellerScrollPaneMonthly);
 
        bestSellerScrollPaneMonthly.setVisible(false);
 
        JLabel bestSellerLabel = new JLabel("Best Sellers:");
        bestSellerLabel.setFont(new Font("Verdana", Font.BOLD, 20));
        bestSellerLabel.setBounds(10, 10, 200, 30);
        bestSellerLabel.setForeground(Color.black);
        bestSellerPanel.add(bestSellerLabel);
 
        timePeriodDropdown.setBounds(160, 10, 120, 30);
        bestSellerPanel.add(timePeriodDropdown);
 
        timePeriodDropdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (timePeriodDropdown.getSelectedItem().equals("Weekly")) {
                    bestSellerScrollPaneWeekly.setVisible(true);
                    bestSellerScrollPaneMonthly.setVisible(false);
                } else {
                    bestSellerScrollPaneWeekly.setVisible(false);
                    bestSellerScrollPaneMonthly.setVisible(true);
                }
            }
        });
 
        lowStockTable = new JTable();
        JScrollPane lowStockScrollPane = new JScrollPane(lowStockTable);
        lowStockScrollPane.setBounds(10, 50, 505, 250);
        lowStockPanel.add(lowStockScrollPane);
 
        JLabel lowStockLabel = new JLabel("Low on Stocks:");
        lowStockLabel.setFont(new Font("Verdana", Font.BOLD, 20));
        lowStockLabel.setBounds(10, 10, 200, 30);
        lowStockLabel.setForeground(Color.black);
        lowStockPanel.add(lowStockLabel);
 
        inventoryWindowFrame.add(viewSupplierButton);
        inventoryWindowFrame.add(backButton);
        inventoryWindowFrame.add(titleLabel);
        inventoryWindowFrame.add(upPanel);
        inventoryWindowFrame.add(lowPanel);
        inventoryWindowFrame.add(bestSellerPanel);
        inventoryWindowFrame.add(lowStockPanel);
        inventoryWindowFrame.add(backPanel);
 
        populateBestSellers();
        Database();
    }
 
    public void populateBestSellers() {
        DefaultTableModel bestSellerTableModelWeekly = new DefaultTableModel();
        DefaultTableModel bestSellerTableModelMonthly = new DefaultTableModel();
        DefaultTableModel lowStockModel = new DefaultTableModel();
   
        Vector<String> bestSellerColumnNames = new Vector<>();
        bestSellerColumnNames.add("Product ID");
        bestSellerColumnNames.add("Item Name");
        bestSellerColumnNames.add("Number of Sales");
        bestSellerColumnNames.add("Revenue");
        bestSellerTableModelWeekly.setColumnIdentifiers(bestSellerColumnNames);
        bestSellerTableModelMonthly.setColumnIdentifiers(bestSellerColumnNames);
   
        Vector<String> lowStockColumnNames = new Vector<>();
        lowStockColumnNames.add("Product ID");
        lowStockColumnNames.add("Quantity");
        lowStockModel.setColumnIdentifiers(lowStockColumnNames);
   
        String serverName = "LAPTOP-VFJUTU85\\SQLEXPRESS";
        String databaseName = "Kenruss";
        String url = "jdbc:sqlserver://" + serverName + ";databaseName=" + databaseName + ";integratedSecurity=true;encrypt=true;trustServerCertificate=true";
   
        final TableCellRenderer defaultRenderer = lowStockTable.getDefaultRenderer(Object.class);
   
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {
   
        bestSellerTableModelWeekly.setRowCount(0);

        // calculate date range for the current week
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // start from Monday of current week
        Date startDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String startDateStr = dateFormat.format(startDate);

        // weekly best seller query dito
        String bestSellerQueryWeekly = "SELECT prod_id, item_name, total_sales, revenue " +
                               "FROM (" +
                                   "SELECT s.prod_id, p.item_name, " +
                                          "SUM(s.quantity) AS total_sales, " +
                                          "SUM(s.quantity * s.price) AS revenue, " +
                                          "DENSE_RANK() OVER (ORDER BY SUM(s.quantity) DESC) AS sales_rank " +
                                   "FROM Sales s " +
                                   "JOIN Products p ON s.prod_id = p.prod_id " +
                                   "WHERE s.bought_date >= '" + startDateStr + "' " +
                                   "GROUP BY s.prod_id, p.item_name " +
                               ") AS ranked_sales " +
                               "WHERE sales_rank <= 10 " +
                               "ORDER BY total_sales DESC";

        ResultSet bestSellerResultSetWeekly = statement.executeQuery(bestSellerQueryWeekly);
        while (bestSellerResultSetWeekly.next()) {
            String prodId = bestSellerResultSetWeekly.getString("prod_id");
            String itemName = bestSellerResultSetWeekly.getString("item_name");
            int totalSales = bestSellerResultSetWeekly.getInt("total_sales");
            double revenue = bestSellerResultSetWeekly.getDouble("revenue");
            bestSellerProductIDsWeekly.add(prodId);

            Vector<Object> row = new Vector<>();
            row.add(prodId);
            row.add(itemName);
            row.add(totalSales);
            row.add(revenue);
            bestSellerTableModelWeekly.addRow(row);
        }
   
            // monthly best seller query dito
            String bestSellerQueryMonthly = "SELECT prod_id, item_name, total_sales, revenue " +
                                "FROM (" +
                                    "SELECT s.prod_id, p.item_name, " +
                                           "SUM(s.quantity) AS total_sales, " +
                                           "SUM(s.quantity * s.price) AS revenue, " +
                                           "DENSE_RANK() OVER (ORDER BY SUM(s.quantity) DESC) AS sales_rank " +
                                    "FROM Sales s " +
                                    "JOIN Products p ON s.prod_id = p.prod_id " +
                                    "WHERE s.bought_date >= DATEADD(MONTH, -1, GETDATE()) " +
                                    "GROUP BY s.prod_id, p.item_name " +
                                ") AS ranked_sales " +
                                "WHERE sales_rank <= 10 " +
                                "ORDER BY total_sales DESC";

            ResultSet bestSellerResultSetMonthly = statement.executeQuery(bestSellerQueryMonthly);
            while (bestSellerResultSetMonthly.next()) {
                String prodId = bestSellerResultSetMonthly.getString("prod_id");
                String itemName = bestSellerResultSetMonthly.getString("item_name");
                int totalSales = bestSellerResultSetMonthly.getInt("total_sales");
                double revenue = bestSellerResultSetMonthly.getDouble("revenue");
                bestSellerProductIDsMonthly.add(prodId);
   
                Vector<Object> row = new Vector<>();
                row.add(prodId);
                row.add(itemName);
                row.add(totalSales);
                row.add(revenue);
                bestSellerTableModelMonthly.addRow(row);
            }
   
            // low on stock query dito pati yung nagddetermine sa categories ng low on stock
            String lowStockQuery = "SELECT p.prod_id, p.quantity " +
                                   "FROM Products p " +
                                   "WHERE (p.quantity <= 20 AND p.prod_id LIKE 'm%') OR " +
                                   "(p.quantity <= 15 AND p.prod_id LIKE 'o%') OR " +
                                   "(p.quantity <= 10 AND p.prod_id LIKE 't%') " +
                                   "ORDER BY p.quantity ASC";
            ResultSet lowStockResultSet = statement.executeQuery(lowStockQuery);
            while (lowStockResultSet.next()) {
                String prodId = lowStockResultSet.getString("prod_id");
                int quantity = lowStockResultSet.getInt("quantity");
   
                Vector<Object> row = new Vector<>();
                row.add(prodId);
                row.add(quantity);
                lowStockModel.addRow(row);
            }
   
            lowStockTable.setDefaultRenderer(Object.class, new TableCellRenderer() {
                // color indicator in low on stocks  
                private final Color WEEKLY_BEST_SELLER_COLOR = Color.RED;
                private final Color MONTHLY_BEST_SELLER_COLOR = Color.ORANGE;
   
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component component = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
   
                    String productId = (String) table.getValueAt(row, 0);
                    if (bestSellerProductIDsWeekly.contains(productId)) {
                        component.setBackground(WEEKLY_BEST_SELLER_COLOR);
                    } else if (bestSellerProductIDsMonthly.contains(productId)) {
                        component.setBackground(MONTHLY_BEST_SELLER_COLOR);
                    } else {
                        component.setBackground(Color.WHITE);
                    }
   
                    return component;
                }
            });
   
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database");
            e.printStackTrace();
        }
   
        bestSellerTableWeekly.setModel(bestSellerTableModelWeekly);
        bestSellerTableMonthly.setModel(bestSellerTableModelMonthly);
        lowStockTable.setModel(lowStockModel);
    }        
 
    private void Database() {
        String serverName = "LAPTOP-VFJUTU85\\SQLEXPRESS";
        String databaseName = "Kenruss";
        String url = "jdbc:sqlserver://" + serverName + ";databaseName=" + databaseName + ";integratedSecurity=true;encrypt=true;trustServerCertificate=true";
 
        try (Connection connection = DriverManager.getConnection(url)) {
            System.out.println("Connected to the database");
 
            String sql = "SELECT Products.*, Supplier.supp_company AS SupplierName FROM Products " +
            "LEFT JOIN Supplier ON Products.supp_id = Supplier.supp_id "+
            "ORDER BY " +
            "ISNUMERIC(Products.prod_id), " +
            "Products.prod_id";
 
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            model = new DefaultTableModel();
 
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                String columnName = metaData.getColumnName(columnIndex);
                model.addColumn(columnName);
            }
 
            while (resultSet.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    rowData[i] = resultSet.getObject(i + 1);
                }
                model.addRow(rowData);
            }
           
            table.setModel(model);
 
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database");
            e.printStackTrace();
        }
    }
 
    private void search(String text) {
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(model);
        table.setRowSorter(rowSorter);
        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
 
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(inventoryWindowFrame, "No matching results found.");
        }
    }
 
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton) {
            inventoryWindowFrame.dispose();
            HomePage homePage = new HomePage();
        }
    }
}
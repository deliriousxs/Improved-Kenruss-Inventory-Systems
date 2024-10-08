import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class Sales implements ActionListener {

    JFrame salesWindowFrame = new JFrame();
    JPanel backPanel = new JPanel();
    JPanel upPanel = new JPanel();
    JPanel lowPanel = new JPanel();
    JTable salesTable = new JTable();

    JLabel titleLabel = new JLabel("Sales");

    JButton backButton = new RoundedButton("Back");
    JButton historyButton = new RoundedButton("History");
    JButton avgSalesButton = new RoundedButton("Average Revenue");
    JTextField searchField = new JTextField();
    JButton clearButton = new JButton("Clear Search");

    JComboBox<String> salesTypeDropdown = new JComboBox<>(new String[]{"Weekly", "Monthly"});
    JComboBox<String> categoryDropdown = new JComboBox<>(new String[]{"By Product", "By Category"});
    JComboBox<String> timeRangeDropdown = new JComboBox<>();

    Sales() {
        salesWindowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        salesWindowFrame.setSize(1100, 750);
        salesWindowFrame.setLayout(null);
        salesWindowFrame.setResizable(false);
        salesWindowFrame.setLocationRelativeTo(null);
        salesWindowFrame.setVisible(true);

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
        titleLabel.setBounds(460, 25, 800, 46);
        titleLabel.setForeground(Color.white);

        backButton.setBounds(950, 640, 100, 40);
        backButton.setBackground(new Color(4, 76, 172));
        backButton.setFont(new Font("Verdana", Font.BOLD, 20));
        backButton.addActionListener(this);

        historyButton.setBounds(780, 640, 150, 40);
        historyButton.setBackground(new Color(4, 76, 172));
        historyButton.setFont(new Font("Verdana", Font.BOLD, 20));
        historyButton.addActionListener(this);

        avgSalesButton.setBounds(510, 640, 250, 40);
        avgSalesButton.setBackground(new Color(4, 76, 172));
        avgSalesButton.setFont(new Font("Verdana", Font.BOLD, 20));
        avgSalesButton.addActionListener(this);

        salesTypeDropdown.setBounds(10, 10, 150, 30);
        categoryDropdown.setBounds(180, 10, 150, 30);
        timeRangeDropdown.setBounds(350, 10, 150, 30);

        searchField.setBounds(600, 10, 300, 30);
        clearButton.setBounds(910, 10, 120, 30);
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchField.setText("");
                TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) salesTable.getRowSorter();
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

        salesTypeDropdown.addActionListener(this);
        categoryDropdown.addActionListener(this);
        timeRangeDropdown.addActionListener(this);

        upPanel.add(salesTypeDropdown);
        upPanel.add(categoryDropdown);
        upPanel.add(timeRangeDropdown);

        salesWindowFrame.add(backButton);
        salesWindowFrame.add(historyButton);
        salesWindowFrame.add(avgSalesButton);
        salesWindowFrame.add(titleLabel);
        salesWindowFrame.add(upPanel);
        salesWindowFrame.add(lowPanel);
        salesWindowFrame.add(backPanel);

        DefaultTableModel salesModel = new DefaultTableModel();
        salesTable.setModel(salesModel);
        updateTimeRangeOptions(); 

        JScrollPane salesScrollPane = new JScrollPane(salesTable);
        salesScrollPane.setBounds(10, 48, 1038, 490);
        lowPanel.add(salesScrollPane);

        
        lowPanel.add(salesTypeDropdown);
        lowPanel.add(categoryDropdown);
        lowPanel.add(timeRangeDropdown);
        lowPanel.add(searchField);
        lowPanel.add(clearButton);

        updateTable(salesModel); 
    }

    private void updateTimeRangeOptions() {
        timeRangeDropdown.removeAllItems();
        if (salesTypeDropdown.getSelectedItem().equals("Weekly")) {
            timeRangeDropdown.addItem("This Week");
            timeRangeDropdown.addItem("Last Week");
            timeRangeDropdown.addItem("Last 2 Weeks");
            timeRangeDropdown.addItem("Last 3 Weeks");
        } else {
            timeRangeDropdown.addItem("This Month");
            timeRangeDropdown.addItem("Last Month");
            timeRangeDropdown.addItem("Last 6 Months");
            timeRangeDropdown.addItem("Last 12 Months");
        }
        updateTable((DefaultTableModel) salesTable.getModel()); 
    }

    private void updateTable(DefaultTableModel salesModel) {
        String serverName = "LAPTOP-VFJUTU85\\SQLEXPRESS";
        String databaseName = "Kenruss";
        String url = "jdbc:sqlserver://" + serverName + ";databaseName=" + databaseName + ";integratedSecurity=true;encrypt=true;trustServerCertificate=true";
    
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {
    
            String query = "SELECT s.sales_id, p.prod_id, p.category, p.item_name, SUM(s.total_amount) AS total_amount, SUM(s.quantity) AS quantity, s.bought_date " +
                    "FROM Sales s " +
                    "INNER JOIN Products p ON s.prod_id = p.prod_id " +
                    "GROUP BY s.sales_id, p.prod_id, p.category, p.item_name, s.bought_date";
    
            ResultSet resultSet = statement.executeQuery(query);
    
            Calendar calendar = Calendar.getInstance();
            long currentTime = calendar.getTimeInMillis();
            String selectedTimeRange = (String) timeRangeDropdown.getSelectedItem();
    
            long timeLimit = currentTime;
    
            if (selectedTimeRange != null) {
                if (salesTypeDropdown.getSelectedItem().equals("Weekly")) {
                    switch (selectedTimeRange) {
                        case "This Week":
                            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                            calendar.add(Calendar.DAY_OF_WEEK, -1); 
                            timeLimit = calendar.getTimeInMillis();
                            break;
                        case "Last Week":
                            calendar.add(Calendar.WEEK_OF_YEAR, -1);
                            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                            calendar.add(Calendar.DAY_OF_WEEK, -1); 
                            timeLimit = calendar.getTimeInMillis();
                            calendar.add(Calendar.WEEK_OF_YEAR, 1);
                            currentTime = calendar.getTimeInMillis();
                            break;
                        case "Last 2 Weeks":
                            calendar.add(Calendar.WEEK_OF_YEAR, -2);
                            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                            calendar.add(Calendar.DAY_OF_WEEK, -1);  
                            timeLimit = calendar.getTimeInMillis();
                            calendar.add(Calendar.WEEK_OF_YEAR, 2);
                            currentTime = calendar.getTimeInMillis();
                            break;
                        case "Last 3 Weeks":
                            calendar.add(Calendar.WEEK_OF_YEAR, -3);
                            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                            calendar.add(Calendar.DAY_OF_WEEK, -1);  
                            timeLimit = calendar.getTimeInMillis();
                            calendar.add(Calendar.WEEK_OF_YEAR, 3);
                            currentTime = calendar.getTimeInMillis();
                            break;
                    }
                } else {
                    switch (selectedTimeRange) {
                        case "This Month":
                            calendar.set(Calendar.DAY_OF_MONTH, 1);
                            timeLimit = calendar.getTimeInMillis();
                            break;
                        case "Last Month":
                            calendar.add(Calendar.MONTH, -1);
                            calendar.set(Calendar.DAY_OF_MONTH, 1);
                            timeLimit = calendar.getTimeInMillis();
                            calendar.add(Calendar.MONTH, 1);
                            calendar.set(Calendar.DAY_OF_MONTH, 1);
                            calendar.add(Calendar.DAY_OF_MONTH, -1);
                            currentTime = calendar.getTimeInMillis();
                            break;
                        case "Last 6 Months":
                            calendar.add(Calendar.MONTH, -6);
                            calendar.set(Calendar.DAY_OF_MONTH, 1);
                            timeLimit = calendar.getTimeInMillis();
                            calendar.add(Calendar.MONTH, 6);
                            calendar.set(Calendar.DAY_OF_MONTH, 1);
                            calendar.add(Calendar.DAY_OF_MONTH, -1);
                            currentTime = calendar.getTimeInMillis();
                            break;
                        case "Last 12 Months":
                            calendar.add(Calendar.YEAR, -1);
                            calendar.set(Calendar.DAY_OF_MONTH, 1);
                            timeLimit = calendar.getTimeInMillis();
                            calendar.add(Calendar.YEAR, 1);
                            calendar.set(Calendar.DAY_OF_MONTH, 1);
                            calendar.add(Calendar.DAY_OF_MONTH, -1);
                            currentTime = calendar.getTimeInMillis();
                            break;
                    }
                }
            }
    
            if (categoryDropdown.getSelectedItem().equals("By Product")) {
                salesModel.setColumnIdentifiers(new String[]{"Product ID", "Category", "Item Name", "Revenue", "Number of Sales"});
            } else if (categoryDropdown.getSelectedItem().equals("By Category")) {
                salesModel.setColumnIdentifiers(new String[]{"Category", "Revenue"});
            }
    
            Map<String, Vector<Object>> salesData = new HashMap<>();
    
            while (resultSet.next()) {
                String prodId = resultSet.getString("prod_id");
                String category = resultSet.getString("category");
                String itemName = resultSet.getString("item_name");
                double totalAmount = resultSet.getDouble("total_amount");
                int quantity = resultSet.getInt("quantity");
                java.sql.Date boughtDate = resultSet.getDate("bought_date");
                long boughtTime = boughtDate.getTime();
                double revenue = totalAmount;
    
                if (boughtTime >= timeLimit && boughtTime <= currentTime) {
                    if (categoryDropdown.getSelectedItem().equals("By Product")) {
                        salesData.merge(prodId, createRow(prodId, category, itemName, revenue, quantity),
                                (oldRow, newRow) -> {
                                    oldRow.set(3, (double) oldRow.get(3) + (double) newRow.get(3));
                                    oldRow.set(4, (int) oldRow.get(4) + (int) newRow.get(4));
                                    return oldRow;
                                });
                    } else if (categoryDropdown.getSelectedItem().equals("By Category")) {
                        salesData.merge(category, createCategoryRow(category, revenue),
                                (oldRow, newRow) -> {
                                    oldRow.set(1, (double) oldRow.get(1) + (double) newRow.get(1));
                                    return oldRow;
                                });
                    }
                }
            }
    
            List<Vector<Object>> rows = new ArrayList<>(salesData.values());
    
            rows.sort((row1, row2) -> {
                if (categoryDropdown.getSelectedItem().equals("By Product")) {
                    return Double.compare((double) row2.get(3), (double) row1.get(3));
                } else {
                    return Double.compare((double) row2.get(1), (double) row1.get(1));
                }
            });
    
            addRowsToModel(rows, salesModel);
    
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database");
            e.printStackTrace();
        }
    }
    
    private Vector<Object> createRow(String prodId, String category, String itemName, double revenue, int quantity) {
        Vector<Object> row = new Vector<>();
        row.add(prodId);
        row.add(category);
        row.add(itemName);
        row.add(revenue);
        row.add(quantity);
        return row;
    }
    
    private Vector<Object> createCategoryRow(String category, double revenue) {
        Vector<Object> row = new Vector<>();
        row.add(category);
        row.add(revenue);
        return row;
    }
    
    private void addRowsToModel(List<Vector<Object>> rows, DefaultTableModel model) {
        model.setRowCount(0);
        for (Vector<Object> row : rows) {
            model.addRow(row);
        }
    }

    private void search(String str) {
        DefaultTableModel model = (DefaultTableModel) salesTable.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        salesTable.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + str));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton) {
            salesWindowFrame.dispose();
            new HomePage();
        } else if (e.getSource() == historyButton) {
            salesWindowFrame.dispose();
            new History();
        } else if (e.getSource() == avgSalesButton) {
            salesWindowFrame.dispose();
            new AvgRevenue();
        } else if (e.getSource() == salesTypeDropdown) {
            updateTimeRangeOptions();
        } else if (e.getSource() == timeRangeDropdown || e.getSource() == categoryDropdown) {
            DefaultTableModel salesModel = (DefaultTableModel) salesTable.getModel();
            updateTable(salesModel);
        }
    }
}
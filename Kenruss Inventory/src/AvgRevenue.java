import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class AvgRevenue implements ActionListener {

    JFrame avgRevenueWindowFrame = new JFrame();
    JPanel backPanel = new JPanel();
    JPanel upPanel = new JPanel();
    JPanel lowPanel = new JPanel();

    JLabel titleLabel = new JLabel("Average Revenue");

    JButton backButton = new RoundedButton("Back");
    JComboBox<String> viewDropdown = new JComboBox<>(new String[]{"By Product", "By Category"});
    JComboBox<String> timeDropdown = new JComboBox<>(new String[]{"Weekly", "Monthly"});
    JComboBox<String> rangeDropdown = new JComboBox<>();
    ChartPanel chartPanel;

    AvgRevenue() {
        avgRevenueWindowFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        avgRevenueWindowFrame.setSize(1100, 750);
        avgRevenueWindowFrame.setLayout(null);
        avgRevenueWindowFrame.setResizable(false);
        avgRevenueWindowFrame.setLocationRelativeTo(null);
        avgRevenueWindowFrame.setVisible(true);

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
        titleLabel.setBounds(320, 25, 800, 46);
        titleLabel.setForeground(Color.white);

        backButton.setBounds(950, 640, 100, 40);
        backButton.setBackground(new Color(4, 76, 172));
        backButton.setFont(new Font("Verdana", Font.BOLD, 20));
        backButton.addActionListener(this);

        viewDropdown.setBounds(20, 10, 150, 30);
        viewDropdown.addActionListener(this);

        timeDropdown.setBounds(180, 10, 150, 30);
        timeDropdown.addActionListener(this);

        rangeDropdown.setBounds(340, 10, 150, 30);
        updateRangeDropdown();
        rangeDropdown.addActionListener(this);

        upPanel.add(titleLabel);
        lowPanel.add(viewDropdown);
        lowPanel.add(timeDropdown);
        lowPanel.add(rangeDropdown);

        avgRevenueWindowFrame.add(backButton);
        avgRevenueWindowFrame.add(upPanel);
        avgRevenueWindowFrame.add(lowPanel);
        avgRevenueWindowFrame.add(backPanel);

        chartPanel = new ChartPanel(null);
        chartPanel.setBounds(10, 50, 1038, 490);
        lowPanel.add(chartPanel);

        updateChart();
    }

    private void updateRangeDropdown() {
        rangeDropdown.removeAllItems();
        if (timeDropdown.getSelectedItem().equals("Weekly")) {
            rangeDropdown.addItem("This Week");
            rangeDropdown.addItem("Last Week");
            rangeDropdown.addItem("Last 2 Weeks");
            rangeDropdown.addItem("Last 3 Weeks");
        } else {
            rangeDropdown.addItem("This Month");
            rangeDropdown.addItem("Last Month");
            rangeDropdown.addItem("Last 6 Months");
            rangeDropdown.addItem("Last 12 Months");
        }
    }

    private void updateChart() {
        String selectedView = (String) viewDropdown.getSelectedItem();
        String selectedTime = (String) timeDropdown.getSelectedItem();
        String selectedRange = (String) rangeDropdown.getSelectedItem();

        if (selectedRange == null) {
            return;
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String serverName = "LAPTOP-VFJUTU85\\SQLEXPRESS";
        String databaseName = "Kenruss";
        String url = "jdbc:sqlserver://" + serverName + ";databaseName=" + databaseName + ";integratedSecurity=true;encrypt=true;trustServerCertificate=true";

        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {

            String query;
            if ("By Product".equals(selectedView)) {
                query = "SELECT s.prod_id, AVG(s.total_amount / s.quantity) AS avg_revenue " +
                        "FROM Sales s " +
                        "JOIN Products p ON s.prod_id = p.prod_id " +
                        "WHERE " + getDateCondition(selectedTime, selectedRange) +
                        " GROUP BY s.prod_id";
            } else {
                query = "SELECT p.category, AVG(s.total_amount / s.quantity) AS avg_revenue " +
                        "FROM Sales s " +
                        "JOIN Products p ON s.prod_id = p.prod_id " +
                        "WHERE " + getDateCondition(selectedTime, selectedRange) +
                        " GROUP BY p.category";
            }

            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String label = resultSet.getString(1);
                double avgRevenue = resultSet.getDouble(2);
                dataset.addValue(avgRevenue, "Average Revenue", label);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to fetch data: " + e.getMessage());
            e.printStackTrace();
        }

        JFreeChart chart;
        if ("By Product".equals(selectedView)) {
            chart = ChartFactory.createBarChart(
                    "Average Revenue by Product",
                    "Product",
                    "Average Revenue",
                    dataset,
                    PlotOrientation.VERTICAL,
                    false, true, false);
        } else {
            chart = ChartFactory.createBarChart(
                    "Average Revenue by Category",
                    "Category",
                    "Average Revenue",
                    dataset,
                    PlotOrientation.VERTICAL,
                    false, true, false);
        }

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.black);
        plot.setRangeGridlinePaint(Color.black);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);

        chart.getTitle().setFont(new Font("Verdana", Font.BOLD, 18));

        plot.getDomainAxis().setLabelFont(new Font("Verdana", Font.PLAIN, 14));
        plot.getRangeAxis().setLabelFont(new Font("Verdana", Font.PLAIN, 14));
        plot.getDomainAxis().setTickLabelFont(new Font("Verdana", Font.PLAIN, 12));
        plot.getRangeAxis().setTickLabelFont(new Font("Verdana", Font.PLAIN, 12));

        chartPanel.setChart(chart);
    }

    private String getDateCondition(String selectedTime, String selectedRange) {
        String dateCondition = "";
        String dateColumn = "s.bought_date"; 
    
        if ("Weekly".equals(selectedTime)) {
            switch (selectedRange) {
                case "This Week":
                    dateCondition = dateColumn + " >= DATEADD(DAY, 1 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE))";
                    break;
                case "Last Week":
                    dateCondition = dateColumn + " >= DATEADD(DAY, -6 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)) AND " + dateColumn + " < DATEADD(DAY, 1 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE))";
                    break;
                case "Last 2 Weeks":
                    dateCondition = dateColumn + " >= DATEADD(DAY, -13 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)) AND " + dateColumn + " < DATEADD(DAY, 1 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE))";
                    break;
                case "Last 3 Weeks":
                    dateCondition = dateColumn + " >= DATEADD(DAY, -20 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)) AND " + dateColumn + " < DATEADD(DAY, 1 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE))";
                    break;
            }
        } else {
            switch (selectedRange) {
                case "This Month":
                    dateCondition = dateColumn + " >= DATEADD(month, DATEDIFF(month, 0, GETDATE()), 0) AND " + dateColumn + " < DATEADD(month, DATEDIFF(month, 0, GETDATE()) + 1, 0)";
                    break;
                case "Last Month":
                    dateCondition = dateColumn + " >= DATEADD(month, DATEDIFF(month, 0, GETDATE()) - 1, 0) AND " + dateColumn + " < DATEADD(month, DATEDIFF(month, 0, GETDATE()), 0)";
                    break;
                case "Last 6 Months":
                    dateCondition = dateColumn + " >= DATEADD(month, DATEDIFF(month, 0, GETDATE()) - 6, 0) AND " + dateColumn + " < DATEADD(month, DATEDIFF(month, 0, GETDATE()), 0)";
                    break;
                case "Last 12 Months":
                    dateCondition = dateColumn + " >= DATEADD(month, DATEDIFF(month, 0, GETDATE()) - 12, 0) AND " + dateColumn + " < DATEADD(month, DATEDIFF(month, 0, GETDATE()), 0)";
                    break;
                }
            }
        
        return dateCondition;
    }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == backButton) {
                avgRevenueWindowFrame.dispose();
                new Sales();
        
            } else if (e.getSource() == viewDropdown || e.getSource() == timeDropdown || e.getSource() == rangeDropdown) {
                if (e.getSource() == timeDropdown) {
                    updateRangeDropdown();
                }
                updateChart();
            }
        }
    }
        

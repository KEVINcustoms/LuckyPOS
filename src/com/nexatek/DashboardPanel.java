package com.nexatek;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import raven.chart.bar.HorizontalBarChart;
import raven.chart.data.category.DefaultCategoryDataset;
import raven.chart.data.pie.DefaultPieDataset;
import raven.chart.line.LineChart;
import raven.chart.pie.PieChart;

public class DashboardPanel extends JPanel {

    private static final Color PAGE_BG = new Color(244, 247, 250);
    private static final Color CARD_BORDER = new Color(224, 230, 238);
    private static final Color INK = new Color(25, 42, 65);
    private static final Color MUTED = new Color(92, 104, 118);
    private static final int LOW_STOCK_LIMIT = 50;

    private final Connection conn;
    private final StatisticsService statisticsService;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
    private final NumberFormat integerFormat = NumberFormat.getIntegerInstance(Locale.US);

    private final JLabel salesValue = new JLabel("0");
    private final JLabel profitValue = new JLabel("0");
    private final JLabel marginValue = new JLabel("0%");
    private final JLabel stockRiskValue = new JLabel("0");
    private final JLabel noProfitValue = new JLabel("0");
    private final List<JPanel> metricCards = new ArrayList<>();

    private JPanel cardsPanel;
    private JPanel contentPanel;
    private JPanel summaryPanel;
    private JPanel insightPanel;
    private JPanel moneyPanel;
    private JPanel profitStatusPanel;
    private JPanel stockHealthPanel;
    private JPanel comparisonPanel;
    private JPanel lowStockPanel;
    private JPanel profitLeadersPanel;
    private JPanel attentionPanel;

    private PieChart moneyChart;
    private PieChart profitStatusChart;
    private PieChart stockHealthChart;
    private LineChart productComparisonChart;
    private HorizontalBarChart lowStockChart;
    private HorizontalBarChart profitLeadersChart;
    private JTable attentionTable;
    private int layoutMode = -1;

    public DashboardPanel() {
        conn = connection.connect();
        statisticsService = new StatisticsService(conn);
        numberFormat.setMaximumFractionDigits(2);
        buildLayout();
        loadDashboardData();
    }

    private void buildLayout() {
        setLayout(new MigLayout("insets 12, gap 10, fill, wrap 1", "[grow,fill]", "[][top][grow,fill]"));
        setBackground(PAGE_BG);

        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][]", "[]"));
        header.setOpaque(false);

        JLabel title = new JLabel("LuckyPOS Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(INK);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> loadDashboardData());

        header.add(title);
        header.add(refreshButton, "h 34!, w 110!");
        add(header, "growx");

        cardsPanel = new JPanel();
        cardsPanel.setOpaque(false);
        metricCards.add(createMetricCard("Sales", salesValue, "Total cash collected", new Color(0, 102, 204)));
        metricCards.add(createMetricCard("Net Profit", profitValue, "Sales less cost", new Color(0, 128, 64)));
        metricCards.add(createMetricCard("Margin", marginValue, "Profit as sales percentage", new Color(117, 76, 184)));
        metricCards.add(createMetricCard("Stock Risk", stockRiskValue, "Low / out of stock", new Color(196, 92, 0)));
        metricCards.add(createMetricCard("No Profit", noProfitValue, "Products to review", new Color(190, 50, 45)));
        add(cardsPanel, "growx");

        createDashboardSections();
        contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        add(contentPanel, "grow");

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyResponsiveLayout();
            }
        });
        SwingUtilities.invokeLater(this::applyResponsiveLayout);
    }

    private void createDashboardSections() {
        moneyChart = createPieChart("Money Health", true);
        profitStatusChart = createPieChart("Profit Status", true);
        stockHealthChart = createPieChart("Stock Health", true);
        productComparisonChart = createLineChart("Top Products: Sales, Cost, Profit");
        lowStockChart = createBarChart("Lowest Stock Items", new Color(56, 189, 248));
        profitLeadersChart = createBarChart("Profit Leaders", new Color(16, 185, 129));

        moneyPanel = wrapChart(moneyChart);
        profitStatusPanel = wrapChart(profitStatusChart);
        stockHealthPanel = wrapChart(stockHealthChart);
        comparisonPanel = wrapChart(productComparisonChart);
        lowStockPanel = wrapChart(lowStockChart);
        profitLeadersPanel = wrapChart(profitLeadersChart);
        attentionPanel = createAttentionPanel();

        summaryPanel = new JPanel();
        summaryPanel.setOpaque(false);
        insightPanel = new JPanel();
        insightPanel.setOpaque(false);
    }

    private void applyResponsiveLayout() {
        int width = Math.max(getWidth(), 900);
        int mode = width >= 1280 ? 3 : width >= 980 ? 2 : 1;
        if (mode == layoutMode) {
            return;
        }
        layoutMode = mode;

        rebuildMetricCards(mode == 3 ? 5 : mode == 2 ? 3 : 2);
        rebuildSummaryPanel(mode);
        rebuildInsightPanel(mode);
        rebuildContentPanel(mode);

        revalidate();
        repaint();
    }

    private void rebuildMetricCards(int columns) {
        cardsPanel.removeAll();
        cardsPanel.setLayout(new MigLayout("insets 0, gap 10, fillx, wrap " + columns, repeatColumns(columns), "[82!]"));
        for (JPanel card : metricCards) {
            cardsPanel.add(card, "grow, h 82!");
        }
    }

    private void rebuildSummaryPanel(int mode) {
        summaryPanel.removeAll();
        if (mode == 3) {
            summaryPanel.setLayout(new MigLayout("insets 0, gapy 10, fill, wrap 1", "[grow,fill]", "[grow,fill][grow,fill][grow,fill]"));
            summaryPanel.add(moneyPanel, "grow");
            summaryPanel.add(profitStatusPanel, "grow");
            summaryPanel.add(stockHealthPanel, "grow");
        } else {
            summaryPanel.setLayout(new MigLayout("insets 0, gap 10, fill, wrap " + (mode == 2 ? 3 : 1),
                    repeatColumns(mode == 2 ? 3 : 1), "[220!,fill]"));
            summaryPanel.add(moneyPanel, "grow");
            summaryPanel.add(profitStatusPanel, "grow");
            summaryPanel.add(stockHealthPanel, "grow");
        }
    }

    private void rebuildInsightPanel(int mode) {
        insightPanel.removeAll();
        int columns = mode == 1 ? 1 : 3;
        insightPanel.setLayout(new MigLayout("insets 0, gap 10, fill, wrap " + columns,
                repeatColumns(columns),
                mode == 1 ? "[210!,fill][210!,fill][210!,fill]" : "[230!,fill]"));
        insightPanel.add(lowStockPanel, "grow");
        insightPanel.add(profitLeadersPanel, "grow");
        insightPanel.add(attentionPanel, "grow");
    }

    private void rebuildContentPanel(int mode) {
        contentPanel.removeAll();
        if (mode == 3) {
            contentPanel.setLayout(new MigLayout("insets 0, gap 10, fill", "[grow 64,fill][360:420:480,fill]", "[grow,fill][230!,fill]"));
            contentPanel.add(comparisonPanel, "grow");
            contentPanel.add(summaryPanel, "grow");
            contentPanel.add(insightPanel, "span 2, grow");
        } else {
            contentPanel.setLayout(new MigLayout("insets 0, gapy 10, fill, wrap 1", "[grow,fill]", "[300!,fill][220!,fill][grow,fill]"));
            contentPanel.add(comparisonPanel, "grow");
            contentPanel.add(summaryPanel, "grow");
            contentPanel.add(insightPanel, "grow");
        }
    }

    private String repeatColumns(int columns) {
        StringBuilder spec = new StringBuilder();
        for (int i = 0; i < columns; i++) {
            spec.append("[160:220,grow,fill]");
        }
        return spec.toString();
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String helper, Color accent) {
        JPanel card = new JPanel(new MigLayout("insets 11, fill, wrap 1", "[grow,fill]", "[][grow][]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:8");

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accent);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel helperLabel = new JLabel(helper);
        helperLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        helperLabel.setForeground(new Color(118, 132, 148));

        card.add(titleLabel);
        card.add(valueLabel, "grow");
        card.add(helperLabel);
        return card;
    }

    private PieChart createPieChart(String title, boolean donut) {
        PieChart chart = new PieChart();
        chart.setHeader(createChartHeader(title));
        chart.getChartColor().addColor(
                Color.decode("#38bdf8"),
                Color.decode("#34d399"),
                Color.decode("#fbbf24"),
                Color.decode("#fb7185"),
                Color.decode("#818cf8"));
        if (donut) {
            chart.setChartType(PieChart.ChartType.DONUT_CHART);
        }
        chart.putClientProperty(FlatClientProperties.STYLE, "border:6,6,6,6,$Component.borderColor,,8");
        return chart;
    }

    private LineChart createLineChart(String title) {
        LineChart chart = new LineChart();
        chart.setChartType(LineChart.ChartType.CURVE);
        chart.setHeader(createChartHeader(title));
        chart.getChartColor().addColor(
                Color.decode("#38bdf8"),
                Color.decode("#fb7185"),
                Color.decode("#34d399"));
        return chart;
    }

    private HorizontalBarChart createBarChart(String title, Color color) {
        HorizontalBarChart chart = new HorizontalBarChart();
        chart.setHeader(createChartHeader(title));
        chart.setBarColor(color);
        return chart;
    }

    private JPanel wrapChart(JPanel chart) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:8");
        panel.add(chart, BorderLayout.CENTER);
        panel.setMinimumSize(new Dimension(180, 180));
        return panel;
    }

    private JLabel createChartHeader(String title) {
        JLabel header = new JLabel(title);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(INK);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return header;
    }

    private JPanel createAttentionPanel() {
        attentionTable = new JTable();
        attentionTable.setRowHeight(24);
        attentionTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        attentionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        attentionTable.setFillsViewportHeight(true);
        attentionTable.setAutoCreateRowSorter(true);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:8");
        panel.add(createChartHeader("Products Needing Attention"), BorderLayout.NORTH);
        panel.add(new JScrollPane(attentionTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadDashboardData() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed. Dashboard data cannot be loaded.");
            return;
        }

        new SwingWorker<List<StatisticsService.ProfitRow>, Void>() {
            @Override
            protected List<StatisticsService.ProfitRow> doInBackground() throws Exception {
                return statisticsService.loadProfitRows();
            }

            @Override
            protected void done() {
                try {
                    updateDashboard(get());
                } catch (Exception ex) {
                    Logger.getLogger(DashboardPanel.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(DashboardPanel.this, "Unable to load dashboard data: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateDashboard(List<StatisticsService.ProfitRow> rows) {
        double totalSales = rows.stream().mapToDouble(StatisticsService.ProfitRow::getTotalSales).sum();
        double totalCost = rows.stream().mapToDouble(StatisticsService.ProfitRow::getTotalCostPrices).sum();
        double netProfit = rows.stream().mapToDouble(StatisticsService.ProfitRow::getProfit).sum();
        double margin = totalSales == 0 ? 0 : (netProfit / totalSales) * 100;
        long noProfitCount = rows.stream().filter(row -> row.getProfit() <= 0).count();
        long lowStockCount = rows.stream().filter(row -> row.getStockQuantity() > 0 && row.getStockQuantity() <= LOW_STOCK_LIMIT).count();
        long outOfStockCount = rows.stream().filter(row -> row.getStockQuantity() <= 0).count();

        salesValue.setText(numberFormat.format(totalSales));
        profitValue.setText(numberFormat.format(netProfit));
        profitValue.setForeground(netProfit >= 0 ? new Color(0, 128, 64) : new Color(190, 50, 45));
        marginValue.setText(String.format(Locale.US, "%.1f%%", margin));
        stockRiskValue.setText(integerFormat.format(lowStockCount) + " / " + integerFormat.format(outOfStockCount));
        noProfitValue.setText(integerFormat.format(noProfitCount) + " of " + integerFormat.format(rows.size()));

        moneyChart.setDataset(createMoneyDataset(totalSales, totalCost, netProfit));
        profitStatusChart.setDataset(createProfitStatusDataset(rows));
        stockHealthChart.setDataset(createStockHealthDataset(rows));
        productComparisonChart.setCategoryDataset(createComparisonDataset(topSalesRows(rows)));
        lowStockChart.setDataset(createBarDataset(lowStockRows(rows), ValueType.STOCK));
        profitLeadersChart.setDataset(createBarDataset(profitLeaderRows(rows), ValueType.PROFIT));
        attentionTable.setModel(createAttentionTableModel(attentionRows(rows)));

        SwingUtilities.invokeLater(() -> {
            moneyChart.startAnimation();
            profitStatusChart.startAnimation();
            stockHealthChart.startAnimation();
            productComparisonChart.startAnimation();
            lowStockChart.startAnimation();
            profitLeadersChart.startAnimation();
        });
    }

    private DefaultPieDataset<String> createMoneyDataset(double totalSales, double totalCost, double netProfit) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        if (netProfit >= 0) {
            addPieValue(dataset, "Cost", Math.min(totalCost, totalSales));
            addPieValue(dataset, "Net Profit", netProfit);
        } else {
            addPieValue(dataset, "Sales Covered", totalSales);
            addPieValue(dataset, "Loss Gap", Math.abs(netProfit));
        }
        ensureDatasetHasData(dataset);
        return dataset;
    }

    private DefaultPieDataset<String> createProfitStatusDataset(List<StatisticsService.ProfitRow> rows) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        long profitable = rows.stream().filter(row -> row.getProfit() > 0).count();
        long notProfitable = rows.size() - profitable;
        addPieValue(dataset, "Making Profit", profitable);
        addPieValue(dataset, "No Profit Yet", notProfitable);
        ensureDatasetHasData(dataset);
        return dataset;
    }

    private DefaultPieDataset<String> createStockHealthDataset(List<StatisticsService.ProfitRow> rows) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        long healthy = rows.stream().filter(row -> row.getStockQuantity() > LOW_STOCK_LIMIT).count();
        long low = rows.stream().filter(row -> row.getStockQuantity() > 0 && row.getStockQuantity() <= LOW_STOCK_LIMIT).count();
        long out = rows.stream().filter(row -> row.getStockQuantity() <= 0).count();
        addPieValue(dataset, "Healthy", healthy);
        addPieValue(dataset, "Low", low);
        addPieValue(dataset, "Out", out);
        ensureDatasetHasData(dataset);
        return dataset;
    }

    private DefaultCategoryDataset<String, String> createComparisonDataset(List<StatisticsService.ProfitRow> rows) {
        DefaultCategoryDataset<String, String> dataset = new DefaultCategoryDataset<>();
        for (StatisticsService.ProfitRow row : rows) {
            String label = label(row);
            dataset.addValue(row.getTotalSales(), "Sales", label);
            dataset.addValue(row.getTotalCostPrices(), "Cost", label);
            dataset.addValue(row.getProfit(), "Profit", label);
        }
        return dataset;
    }

    private DefaultPieDataset<String> createBarDataset(List<StatisticsService.ProfitRow> rows, ValueType valueType) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (StatisticsService.ProfitRow row : rows) {
            dataset.addValue(label(row), valueFor(row, valueType));
        }
        ensureDatasetHasData(dataset);
        return dataset;
    }

    private DefaultTableModel createAttentionTableModel(List<StatisticsService.ProfitRow> rows) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Product", "Issue", "Sales", "Profit", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (StatisticsService.ProfitRow row : rows) {
            model.addRow(new Object[]{
                row.getProductName(),
                issueFor(row),
                numberFormat.format(row.getTotalSales()),
                numberFormat.format(row.getProfit()),
                row.getStockQuantity()
            });
        }
        if (rows.isEmpty()) {
            model.addRow(new Object[]{"No urgent products", "-", "-", "-", "-"});
        }
        return model;
    }

    private List<StatisticsService.ProfitRow> topSalesRows(List<StatisticsService.ProfitRow> rows) {
        List<StatisticsService.ProfitRow> sorted = new ArrayList<>(rows);
        sorted.sort(Comparator.comparingDouble(StatisticsService.ProfitRow::getTotalSales).reversed());
        return sorted.subList(0, Math.min(sorted.size(), 6));
    }

    private List<StatisticsService.ProfitRow> lowStockRows(List<StatisticsService.ProfitRow> rows) {
        List<StatisticsService.ProfitRow> sorted = new ArrayList<>(rows);
        sorted.removeIf(row -> row.getStockQuantity() > LOW_STOCK_LIMIT);
        sorted.sort(Comparator.comparingInt(StatisticsService.ProfitRow::getStockQuantity));
        return sorted.subList(0, Math.min(sorted.size(), 6));
    }

    private List<StatisticsService.ProfitRow> profitLeaderRows(List<StatisticsService.ProfitRow> rows) {
        List<StatisticsService.ProfitRow> sorted = new ArrayList<>(rows);
        sorted.removeIf(row -> row.getProfit() <= 0);
        sorted.sort(Comparator.comparingDouble(StatisticsService.ProfitRow::getProfit).reversed());
        return sorted.subList(0, Math.min(sorted.size(), 6));
    }

    private List<StatisticsService.ProfitRow> attentionRows(List<StatisticsService.ProfitRow> rows) {
        List<StatisticsService.ProfitRow> sorted = new ArrayList<>(rows);
        sorted.removeIf(row -> row.getProfit() > 0 && row.getStockQuantity() > LOW_STOCK_LIMIT);
        sorted.sort(Comparator
                .comparingInt((StatisticsService.ProfitRow row) -> row.getStockQuantity() <= 0 ? 0 : 1)
                .thenComparingDouble(StatisticsService.ProfitRow::getProfit)
                .thenComparingInt(StatisticsService.ProfitRow::getStockQuantity));
        return sorted.subList(0, Math.min(sorted.size(), 8));
    }

    private String issueFor(StatisticsService.ProfitRow row) {
        if (row.getStockQuantity() <= 0) {
            return "Out of stock";
        }
        if (row.getProfit() <= 0 && row.getStockQuantity() <= LOW_STOCK_LIMIT) {
            return "No profit, low stock";
        }
        if (row.getProfit() <= 0) {
            return "No profit";
        }
        return "Low stock";
    }

    private void addPieValue(DefaultPieDataset<String> dataset, String label, double value) {
        if (value > 0) {
            dataset.addValue(label, value);
        }
    }

    private void ensureDatasetHasData(DefaultPieDataset<String> dataset) {
        if (dataset.getItemCount() == 0) {
            dataset.addValue("No data", 1);
        }
    }

    private String label(StatisticsService.ProfitRow row) {
        String name = row.getProductName();
        if (name == null || name.isBlank()) {
            return "Unnamed";
        }
        return name.length() <= 18 ? name : name.substring(0, 18) + "...";
    }

    private double valueFor(StatisticsService.ProfitRow row, ValueType valueType) {
        switch (valueType) {
            case PROFIT:
                return Math.max(row.getProfit(), 0);
            case STOCK:
            default:
                return Math.max(row.getStockQuantity(), 0);
        }
    }

    private enum ValueType {
        PROFIT,
        STOCK
    }
}

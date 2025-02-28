package view;

import org.json.JSONObject;
import org.json.JSONTokener;

import config.ConfigInterface;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.Stack;

public class DataViewer {

    private JTabbedPane tabbedPane;
    private Stack<Integer> tabHistory;

    public void createAndShowGUI(ConfigInterface config) {
        JFrame frame = new JFrame("Data Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);

        tabbedPane = new JTabbedPane();
        tabHistory = new Stack<>();

        JPanel mainPanel = createMainPanel(config);
        tabbedPane.addTab("Main", mainPanel);

        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close Tab");
        closeButton.addActionListener(e -> closeCurrentTab());
        navigationPanel.add(closeButton);

        frame.add(navigationPanel, BorderLayout.SOUTH);
        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private JPanel createMainPanel(ConfigInterface config) {
        JPanel panel = new JPanel(new BorderLayout());

        JTable table = new JTable();
        DefaultTableModel tableModel = createTableModel();
        table.setModel(tableModel);
        configureTableAppearance(table);

        JScrollPane scrollPane = new JScrollPane(table);
        loadDataFromFile(tableModel, config.getResultFilePath());

        configureColumnRenderers(table);
        addTableMouseListener(table, config);

        JPanel controlsPanel = createControlsPanel(tableModel, table);

        panel.add(controlsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void configureTableAppearance(JTable table) {
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
    }

    private void configureColumnRenderers(JTable table) {
        table.getColumnModel().getColumn(table.getColumnModel().getColumnIndex("NodeID")).setCellRenderer(new GreenBackgroundRenderer());
        table.getColumnModel().getColumn(table.getColumnModel().getColumnIndex("Rank")).setCellRenderer(new BlueBackgroundRenderer());
        table.getColumnModel().getColumn(table.getColumnModel().getColumnIndex("Score")).setCellRenderer(new RedBackgroundRenderer());
    }

    private void addTableMouseListener(JTable table, ConfigInterface config) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    int col = table.getSelectedColumn();

                    if (row != -1 && col != -1 && "NodeID".equals(table.getColumnName(col))) {
                        String nodeId = table.getValueAt(row, col).toString();
                        showNodeDetails(nodeId, config);
                    }
                }
            }
        });
    }

    private JPanel createControlsPanel(DefaultTableModel tableModel, JTable table) {
        JPanel controlsPanel = new JPanel(new BorderLayout());

        JPanel searchPanel = createSearchPanel(tableModel, table);
        JPanel sortPanel = createSortPanel(table);

        controlsPanel.add(searchPanel, BorderLayout.WEST);
        controlsPanel.add(sortPanel, BorderLayout.EAST);

        return controlsPanel;
    }

    private JPanel createSearchPanel(DefaultTableModel tableModel, JTable table) {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel searchLabel = new JLabel("Search NodeID: ");
        JTextField searchField = new JTextField(15);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTable(searchField.getText(), tableModel, table);
            }
        });

        JLabel userNote = new JLabel("Double click on NodeID to see more details.", SwingConstants.LEFT);
        userNote.setFont(new Font("Arial", Font.ITALIC, 15));
        userNote.setForeground(Color.GRAY);

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(userNote);

        return searchPanel;
    }

    private JPanel createSortPanel(JTable table) {
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JLabel sortLabel = new JLabel("Sort By: ");
        JComboBox<String> sortComboBox = new JComboBox<>(new String[]{"NodeID", "Rank"});
        JCheckBox ascendingCheckBox = new JCheckBox("Ascending", true);
        JButton sortButton = new JButton("Sort");

        sortButton.addActionListener(e -> {
            String columnName = (String) sortComboBox.getSelectedItem();
            boolean ascending = ascendingCheckBox.isSelected();
            sortTable(columnName, table, ascending);
        });

        sortPanel.add(sortLabel);
        sortPanel.add(sortComboBox);
        sortPanel.add(ascendingCheckBox);
        sortPanel.add(sortButton);

        return sortPanel;
    }

    private void loadDataFromFile(DefaultTableModel tableModel, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            if ((line = br.readLine()) != null) {
                String[] columns = line.trim().split("\\s{2,}");
                for (String column : columns) {
                    tableModel.addColumn(column);
                }
            }

            while ((line = br.readLine()) != null) {
                String[] data = line.trim().split("\\s{2,}");
                tableModel.addRow(data);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error loading file: " + ex.getMessage());
        }
    }

    private void showNodeDetails(String nodeId, ConfigInterface config) {
        String jsonFilePath = config.getLocalManager().getDatabasefilepath();
        try (FileReader reader = new FileReader(jsonFilePath)) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);

            if (jsonObject.has(nodeId)) {
                JSONObject nodeDetails = jsonObject.getJSONArray(nodeId).getJSONObject(0);

                JTextArea textArea = new JTextArea(nodeDetails.toString(4));
                textArea.setEditable(false);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
                JScrollPane scrollPane = new JScrollPane(textArea);

                JPanel detailPanel = new JPanel(new BorderLayout());
                detailPanel.add(scrollPane, BorderLayout.CENTER);

                int tabIndex = tabbedPane.getTabCount();
                tabbedPane.addTab("Details: " + nodeId, detailPanel);
                tabHistory.push(tabbedPane.getSelectedIndex());
                tabbedPane.setSelectedIndex(tabIndex);
            } else {
                JOptionPane.showMessageDialog(null, "No details found for NodeID: " + nodeId);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error reading JSON file: " + ex.getMessage());
        }
    }

    private void closeCurrentTab() {
        int currentIndex = tabbedPane.getSelectedIndex();

        if (currentIndex > 0) {
            tabHistory.removeIf(index -> index == currentIndex);
            tabbedPane.remove(currentIndex);
            if (currentIndex - 1 >= 0) {
                tabbedPane.setSelectedIndex(currentIndex - 1);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Cannot close the main tab.");
        }
    }

    private void filterTable(String query, DefaultTableModel tableModel, JTable table) {
        TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        rowSorter.setRowFilter(query.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + query));
    }

    private void sortTable(String columnName, JTable table, boolean ascending) {
        if (columnName == null) {
            JOptionPane.showMessageDialog(null, "Please select a column to sort.");
            return;
        }

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);

        int columnIndex = table.getColumnModel().getColumnIndex(columnName);
        sorter.setComparator(columnIndex, Comparator.comparing(Object::toString));

        sorter.setSortKeys(java.util.Collections.singletonList(
                new RowSorter.SortKey(columnIndex, ascending ? SortOrder.ASCENDING : SortOrder.DESCENDING)
        ));
        sorter.sort();
    }

    // Custom Renderers
    static class GreenBackgroundRenderer extends DefaultTableCellRenderer {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cellComponent.setBackground("NodeID".equals(table.getColumnName(column)) ? new Color(204, 255, 204) : Color.WHITE);
            return cellComponent;
        }
    }

    static class BlueBackgroundRenderer extends DefaultTableCellRenderer {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cellComponent.setBackground("Rank".equals(table.getColumnName(column)) ? new Color(173, 216, 230) : Color.WHITE);
            return cellComponent;
        }
    }

    static class RedBackgroundRenderer extends DefaultTableCellRenderer {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cellComponent.setBackground("Score".equals(table.getColumnName(column)) ? new Color(255, 182, 193) : Color.WHITE);
            return cellComponent;
        }
    }
}
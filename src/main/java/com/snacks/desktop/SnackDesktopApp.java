package com.snacks.desktop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SnackDesktopApp extends JFrame {
    private JTable snackTable;
    private DefaultTableModel tableModel;
    private JTextField titleField, japaneseField, englishField, descriptionField, imageField;
    private JButton addButton, updateButton, deleteButton, refreshButton;
    private Connection connection;
    private String imageBaseUrl = "http://www.xxx.com/";

    public SnackDesktopApp() {
        initializeDatabase();
        initializeUI();
        loadSnacks();
    }

    private void initializeDatabase() {
        try {
            Properties prop = new Properties();
            prop.load(getClass().getClassLoader().getResourceAsStream("config.properties"));

            String url = prop.getProperty("db.url");
            String username = prop.getProperty("db.username");
            String password = prop.getProperty("db.password");

            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "数据库连接失败: " + e.getMessage());
            System.exit(1);
        }
    }

    private void initializeUI() {
        setTitle("日本零食学习 - 桌面版");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 表格面板
        String[] columnNames = {"ID", "标题", "日语", "英语", "描述", "图片"};
        tableModel = new DefaultTableModel(columnNames, 0);
        snackTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(snackTable);

        // 表单面板
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("零食信息"));

        formPanel.add(new JLabel("标题:"));
        titleField = new JTextField();
        formPanel.add(titleField);

        formPanel.add(new JLabel("日语:"));
        japaneseField = new JTextField();
        formPanel.add(japaneseField);

        formPanel.add(new JLabel("英语:"));
        englishField = new JTextField();
        formPanel.add(englishField);

        formPanel.add(new JLabel("描述:"));
        descriptionField = new JTextField();
        formPanel.add(descriptionField);

        formPanel.add(new JLabel("图片名:"));
        imageField = new JTextField();
        formPanel.add(imageField);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        addButton = new JButton("添加");
        updateButton = new JButton("更新");
        deleteButton = new JButton("删除");
        refreshButton = new JButton("刷新");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        formPanel.add(buttonPanel);

        // 添加事件监听器
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addSnack();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateSnack();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteSnack();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadSnacks();
            }
        });

        // 表格选择监听
        snackTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = snackTable.getSelectedRow();
                if (selectedRow != -1) {
                    titleField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    japaneseField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    englishField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                    descriptionField.setText(tableModel.getValueAt(selectedRow, 4).toString());
                    imageField.setText(tableModel.getValueAt(selectedRow, 5).toString());
                }
            }
        });

        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(formPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadSnacks() {
        try {
            tableModel.setRowCount(0);
            String sql = "SELECT * FROM japanese_snacks ORDER BY id";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("japanese"),
                        rs.getString("english"),
                        rs.getString("description"),
                        rs.getString("image_name")
                };
                tableModel.addRow(row);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "加载数据失败: " + e.getMessage());
        }
    }

    private void addSnack() {
        try {
            String sql = "INSERT INTO japanese_snacks (title, japanese, english, description, image_name) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, titleField.getText());
            stmt.setString(2, japaneseField.getText());
            stmt.setString(3, englishField.getText());
            stmt.setString(4, descriptionField.getText());
            stmt.setString(5, imageField.getText());

            stmt.executeUpdate();
            stmt.close();

            clearForm();
            loadSnacks();
            JOptionPane.showMessageDialog(this, "添加成功!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "添加失败: " + e.getMessage());
        }
    }

    private void updateSnack() {
        int selectedRow = snackTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要更新的记录");
            return;
        }

        try {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String sql = "UPDATE japanese_snacks SET title=?, japanese=?, english=?, description=?, image_name=? WHERE id=?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, titleField.getText());
            stmt.setString(2, japaneseField.getText());
            stmt.setString(3, englishField.getText());
            stmt.setString(4, descriptionField.getText());
            stmt.setString(5, imageField.getText());
            stmt.setInt(6, id);

            stmt.executeUpdate();
            stmt.close();

            clearForm();
            loadSnacks();
            JOptionPane.showMessageDialog(this, "更新成功!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "更新失败: " + e.getMessage());
        }
    }

    private void deleteSnack() {
        int selectedRow = snackTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的记录");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "确定要删除这条记录吗?", "确认删除", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                String sql = "DELETE FROM japanese_snacks WHERE id=?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, id);

                stmt.executeUpdate();
                stmt.close();

                clearForm();
                loadSnacks();
                JOptionPane.showMessageDialog(this, "删除成功!");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "删除失败: " + e.getMessage());
            }
        }
    }

    private void clearForm() {
        titleField.setText("");
        japaneseField.setText("");
        englishField.setText("");
        descriptionField.setText("");
        imageField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SnackDesktopApp().setVisible(true);
        });
        System.out.println("test git");
    }
}
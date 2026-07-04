package com.nexatek;

import java.awt.Component;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;
import net.proteanit.sql.DbUtils;

public final class UiDataLoader {

    private UiDataLoader() {
    }

    public interface StatementBinder {
        void bind(PreparedStatement statement) throws Exception;
    }

    public static void loadTable(Component parent, Connection conn, JTable table, String sql) {
        loadTable(parent, conn, table, sql, null, null);
    }

    public static void loadTable(Component parent, Connection conn, JTable table, String sql, StatementBinder binder) {
        loadTable(parent, conn, table, sql, binder, null);
    }

    public static void loadTable(Component parent, Connection conn, JTable table, String sql,
            StatementBinder binder, Consumer<TableModel> afterLoad) {
        if (conn == null) {
            JOptionPane.showMessageDialog(parent, "Database connection failed.", "LuckyPOS", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new SwingWorker<TableModel, Void>() {
            @Override
            protected TableModel doInBackground() throws Exception {
                try (PreparedStatement statement = conn.prepareStatement(sql)) {
                    if (binder != null) {
                        binder.bind(statement);
                    }
                    try (ResultSet result = statement.executeQuery()) {
                        return DbUtils.resultSetToTableModel(result);
                    }
                }
            }

            @Override
            protected void done() {
                try {
                    TableModel model = get();
                    table.setModel(model);
                    if (afterLoad != null) {
                        afterLoad.accept(model);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parent, ex.getMessage(), "LuckyPOS", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}

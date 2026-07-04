package com.nexatek;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

public final class JasperReportHelper {

    private JasperReportHelper() {
    }

    public static File resolveReportFile(Class<?> anchor, String reportFileName) {
        String resourcePath = "/reports/" + reportFileName;
        java.net.URL resource = anchor.getResource(resourcePath);
        if (resource != null) {
            try {
                File resourceFile = new File(resource.toURI());
                if (resourceFile.exists()) {
                    return resourceFile;
                }
            } catch (Exception ignore) {
            }
        }

        resource = anchor.getResource("/" + reportFileName);
        if (resource != null) {
            try {
                File resourceFile = new File(resource.toURI());
                if (resourceFile.exists()) {
                    return resourceFile;
                }
            } catch (Exception ignore) {
            }
        }

        File current = new File(".").getAbsoluteFile();
        File[] searchRoots = new File[]{current, current.getParentFile()};
        for (File root : searchRoots) {
            if (root == null) {
                continue;
            }
            for (String relative : new String[]{
                "src/reports/" + reportFileName,
                "reports/" + reportFileName,
                "build/classes/reports/" + reportFileName
            }) {
                File candidate = new File(root, relative);
                if (candidate.exists()) {
                    return candidate;
                }
            }
        }

        File walk = current;
        while (walk != null) {
            for (String relative : new String[]{
                "src/reports/" + reportFileName,
                "reports/" + reportFileName,
                "build/classes/reports/" + reportFileName
            }) {
                File candidate = new File(walk, relative);
                if (candidate.exists()) {
                    return candidate;
                }
            }
            walk = walk.getParentFile();
        }

        return null;
    }

    public static void showReport(Connection conn, Class<?> anchor, String reportFileName,
            Map<String, Object> params, String windowTitle) {
        try {
            if (conn == null || conn.isClosed()) {
                JOptionPane.showMessageDialog(null,
                        "Database connection is not available.",
                        "Report Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            File reportFile = resolveReportFile(anchor, reportFileName);
            if (reportFile == null) {
                JOptionPane.showMessageDialog(null,
                        "Report file not found:\n" + reportFileName,
                        "Report Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            JasperDesign design = JRXmlLoader.load(reportFile);
            JasperReport report = JasperCompileManager.compileReport(design);
            Map<String, Object> fillParams = params != null ? params : new HashMap<>();
            JasperPrint print = JasperFillManager.fillReport(report, fillParams, conn);

            if (print.getPages().isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Report generated but contains no data.",
                        "Report",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JFrame frame = new JFrame(windowTitle != null ? windowTitle : "Report Viewer");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JasperViewer viewer = new JasperViewer(print, false);
            frame.getContentPane().add(viewer.getContentPane());
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception ex) {
            Logger.getLogger(anchor.getName()).log(Level.SEVERE, null, ex);
            Throwable root = ex;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            JOptionPane.showMessageDialog(null,
                    "Error generating report.\n\nCause: " + root.getMessage(),
                    "Report Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showReport(Connection conn, Class<?> anchor, String reportFileName, String windowTitle) {
        showReport(conn, anchor, reportFileName, null, windowTitle);
    }
}

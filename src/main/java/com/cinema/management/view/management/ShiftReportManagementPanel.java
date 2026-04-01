package com.cinema.management.view.management;

import com.cinema.management.controller.ShiftReportController;
import com.cinema.management.model.entity.ShiftReport;
import com.cinema.management.model.entity.User;
import com.cinema.management.util.UserSessionContext;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShiftReportManagementPanel extends JPanel {

    // Modern color palette (Tailwind CSS inspired)
    private static final Color BG_COLOR = new Color(248, 250, 252); // slate-50
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_MAIN = new Color(15, 23, 42);   // slate-900
    private static final Color TEXT_MUTED = new Color(100, 116, 139); // slate-500
    private static final Color BORDER_COLOR = new Color(226, 232, 240); // slate-200
    
    private static final Color PRIMARY_COLOR = new Color(14, 165, 233); // sky-500
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);  // green-500
    private static final Color WARNING_COLOR = new Color(245, 158, 11); // amber-500
    private static final Color DANGER_COLOR = new Color(239, 68, 68);   // red-500

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final ShiftReportController shiftReportController = new ShiftReportController();
    private final List<ShiftReport> currentReports = new ArrayList<>();

    // UI Components
    private final JSpinner spFromDate = createDateSpinner();
    private final JSpinner spToDate = createDateSpinner();
    private final JComboBox<String> cbStatus = new JComboBox<>(new String[]{"ALL", "PENDING", "APPROVED", "LOCKED"});
    private final JTextField txtStaffSearch = new JTextField(15);

    // Dashboard Cards UI References
    private final JLabel lblTotalRevenue = new JLabel("0 ₫");
    private final JLabel lblPendingCount = new JLabel("0 báo cáo");
    private final JLabel lblProcessedCount = new JLabel("0 báo cáo");
    private final JLabel lblTotalDiscrepancy = new JLabel("0 ₫");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Mã BCC", "Nhân viên", "Thời gian ca", "Doanh thu", "Tiền mặt", "Chênh lệch", "Trạng thái", "Duyệt bởi"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> rowSorter;

    // Detail Section
    private final JLabel lblDetailId = new JLabel("-");
    private final JLabel lblDetailStaff = new JLabel("-");
    private final JLabel lblDetailTime = new JLabel("-");
    private final JLabel lblDetailRevenue = new JLabel("0 ₫");
    private final JLabel lblDetailActualCash = new JLabel("0 ₫");
    private final JLabel lblDetailDiscrepancy = new JLabel("0 ₫");
    private final JLabel lblDetailRemitted = new JLabel("0 ₫");
    private final JLabel lblDetailCarryOver = new JLabel("0 ₫");
    private final JLabel lblDetailStatus = new JLabel("-");

    private ShiftReport selectedReport = null;

    public ShiftReportManagementPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);

        // Styling FlatLaf properties explicitly for this panel context
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);

        configureTable();
        refreshData();
    }

    private JPanel buildHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("QUẢN LÝ BÁO CÁO CA");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Theo dõi dòng tiền thu được từ nhân viên qua từng ca làm việc, kiểm tra chênh lệch.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(148, 163, 184));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        headerPanel.add(left, BorderLayout.WEST);

        // Action Buttons Top Right
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setOpaque(false);
        
        JButton btnExport = new JButton("  Xuất Báo Cáo  ");
        styleButton(btnExport, SUCCESS_COLOR, Color.WHITE);
        btnExport.addActionListener(e -> exportExcel());
        actionPanel.add(btnExport);

        headerPanel.add(actionPanel, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel buildMainContent() {
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setOpaque(false);

        mainContent.add(buildTopDashboardArea(), BorderLayout.NORTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTablePanel(), buildRightAnalyticsPanel());
        splitPane.setResizeWeight(0.70);
        splitPane.setDividerSize(15);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setBackground(BG_COLOR); // FlatLaf transparency fix
        
        mainContent.add(splitPane, BorderLayout.CENTER);
        
        return mainContent;
    }

    private JPanel buildTopDashboardArea() {
        JPanel area = new JPanel(new BorderLayout(0, 20));
        area.setOpaque(false);
        
        area.add(buildFilterPanel(), BorderLayout.NORTH);
        area.add(buildSummaryCards(), BorderLayout.CENTER);
        
        return area;
    }

    private JPanel buildFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBackground(CARD_BG);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));

        filterPanel.add(createFilterGroup("Từ ngày:", spFromDate));
        filterPanel.add(createFilterGroup("Đến ngày:", spToDate));
        filterPanel.add(createFilterGroup("Trạng thái:", cbStatus));
        filterPanel.add(createFilterGroup("Nhân viên:", txtStaffSearch));

        JButton btnApply = new JButton("  Lọc Dữ Liệu  ");
        styleButton(btnApply, PRIMARY_COLOR, Color.WHITE);
        btnApply.addActionListener(e -> refreshData());
        
        JButton btnRefresh = new JButton("  Làm Mới  ");
        styleButton(btnRefresh, TEXT_MUTED, Color.WHITE);
        btnRefresh.addActionListener(e -> {
            txtStaffSearch.setText("");
            cbStatus.setSelectedIndex(0);
            spFromDate.setValue(new Date());
            spToDate.setValue(new Date());
            refreshData();
        });

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnWrapper.setOpaque(false);
        btnWrapper.setBorder(new EmptyBorder(22, 10, 0, 0));
        btnWrapper.add(btnApply);
        btnWrapper.add(btnRefresh);
        
        filterPanel.add(btnWrapper);

        return filterPanel;
    }

    private JPanel createFilterGroup(String labelText, JComponent inputComp) {
        JPanel group = new JPanel(new BorderLayout(0, 5));
        group.setOpaque(false);
        
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MUTED);
        
        if(inputComp instanceof JComboBox) {
            inputComp.setPreferredSize(new Dimension(140, 36));
        } else if(inputComp instanceof JTextField) {
            inputComp.setPreferredSize(new Dimension(160, 36));
        } else {
            inputComp.setPreferredSize(new Dimension(120, 36));
        }
        inputComp.setFont(FONT_REGULAR);
        
        group.add(lbl, BorderLayout.NORTH);
        group.add(inputComp, BorderLayout.CENTER);
        return group;
    }

    private JPanel buildSummaryCards() {
        JPanel cardsContainer = new JPanel(new GridLayout(1, 4, 20, 0));
        cardsContainer.setOpaque(false);

        cardsContainer.add(createMetricCard("Tổng Doanh Thu Ca", lblTotalRevenue, null, PRIMARY_COLOR, "💰"));
        cardsContainer.add(createMetricCard("Ca Chờ Duyệt", lblPendingCount, null, WARNING_COLOR, "⏳"));
        cardsContainer.add(createMetricCard("Ca Đã Xử Lý", lblProcessedCount, null, SUCCESS_COLOR, "✅"));
        cardsContainer.add(createMetricCard("Tổng Chênh Lệch", lblTotalDiscrepancy, null, DANGER_COLOR, "⚠️"));

        return cardsContainer;
    }

    private JPanel createMetricCard(String title, JLabel lblVal, JLabel lblCount, Color accentColor, String iconSymbol) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        
        JPanel topBorder = new JPanel();
        topBorder.setBackground(accentColor);
        topBorder.setPreferredSize(new Dimension(0, 4));
        card.add(topBorder, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 5));
        content.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_BOLD);
        lblTitle.setForeground(TEXT_MUTED);
        
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(accentColor);
        
        JPanel centerInfo = new JPanel(new GridLayout(lblCount != null ? 2 : 1, 1, 0, 4));
        centerInfo.setOpaque(false);
        centerInfo.add(lblVal);
        if(lblCount != null) {
            lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblCount.setForeground(TEXT_MUTED);
            centerInfo.add(lblCount);
        }

        content.add(lblTitle, BorderLayout.NORTH);
        content.add(centerInfo, BorderLayout.CENTER);
        
        JLabel lblIcon = new JLabel(iconSymbol);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        lblIcon.setForeground(accentColor);

        card.add(content, BorderLayout.CENTER);
        card.add(lblIcon, BorderLayout.EAST);
        
        return card;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));

        table.setRowHeight(44);
        table.setFont(FONT_REGULAR);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(241, 245, 249)); // slate-100
        table.setSelectionForeground(TEXT_MAIN);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(header.getWidth(), 44));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());

        table.getSelectionModel().addListSelectionListener(e -> onTableSelectionChanged());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.putClientProperty("JScrollPane.smoothScrolling", true);

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel title = new JLabel("Lịch Sử Cập Nhật Ca");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_MAIN);
        tableHeader.add(title, BorderLayout.WEST);

        panel.add(tableHeader, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRightAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(25, 25, 25, 25)
        ));

        JLabel title = new JLabel("Chi Tiết Xét Duyệt Nhanh");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);
        panel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(9, 1, 0, 12));
        formPanel.setOpaque(false);

        formPanel.add(buildDetailRow("Mã Báo Cáo Ca:", lblDetailId));
        formPanel.add(buildDetailRow("Nhân viên:", lblDetailStaff));
        formPanel.add(buildDetailRow("Thời gian ca:", lblDetailTime));
        formPanel.add(buildDetailRow("Tổng Doanh thu:", lblDetailRevenue));
        formPanel.add(buildDetailRow("Tiền mặt thực tế:", lblDetailActualCash));
        formPanel.add(buildDetailRow("Chênh lệch:", lblDetailDiscrepancy));
        formPanel.add(buildDetailRow("Nộp về công ty:", lblDetailRemitted));
        formPanel.add(buildDetailRow("Giữ lại tại quầy:", lblDetailCarryOver));
        formPanel.add(buildDetailRow("Trạng thái:", lblDetailStatus));

        panel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        buttonPanel.setOpaque(false);
        
        JButton btnApprove = new JButton("Duyệt Báo Cáo");
        styleButton(btnApprove, SUCCESS_COLOR, Color.WHITE);
        btnApprove.addActionListener(e -> approveShiftReport());

        JButton btnLock = new JButton("Khóa Báo Cáo");
        styleButton(btnLock, TEXT_MAIN, Color.WHITE);
        btnLock.addActionListener(e -> lockShiftReport());

        JButton btnViewDetails = new JButton("Chi Tiết / In Ấn");
        styleButton(btnViewDetails, BG_COLOR, TEXT_MAIN);
        btnViewDetails.setBorderPainted(true);
        btnViewDetails.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        btnViewDetails.addActionListener(e -> viewFullDetails());

        buttonPanel.add(btnApprove);
        buttonPanel.add(btnLock);
        buttonPanel.add(btnViewDetails);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildDetailRow(String labelText, JLabel valLabel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createMatteBorder(0,0,1,0, BORDER_COLOR));
        
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_REGULAR);
        lbl.setForeground(TEXT_MUTED);
        
        valLabel.setFont(FONT_BOLD);
        valLabel.setForeground(TEXT_MAIN);
        
        p.add(lbl, BorderLayout.WEST);
        p.add(valLabel, BorderLayout.EAST);
        return p;
    }

    private void configureTable() {
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
    }

    private void onTableSelectionChanged() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = rowSorter.convertRowIndexToModel(selectedRow);
            if (modelRow >= 0 && modelRow < currentReports.size()) {
                selectedReport = currentReports.get(modelRow);
                updateDetailPanel();
            }
        }
    }

    private void updateDetailPanel() {
        if (selectedReport == null) {
            lblDetailId.setText("-");
            lblDetailStaff.setText("-");
            lblDetailTime.setText("-");
            lblDetailRevenue.setText("0 ₫");
            lblDetailActualCash.setText("0 ₫");
            lblDetailDiscrepancy.setText("0 ₫");
            lblDetailRemitted.setText("0 ₫");
            lblDetailCarryOver.setText("0 ₫");
            lblDetailStatus.setText("-");
            lblDetailDiscrepancy.setForeground(TEXT_MAIN);
            return;
        }

        lblDetailId.setText(selectedReport.getShiftReportId() != null ? 
            selectedReport.getShiftReportId().substring(0, Math.min(8, selectedReport.getShiftReportId().length())) : "-");
        
        String staffInfo = "-";
        if (selectedReport.getUser() != null) {
            staffInfo = (selectedReport.getUser().getUserId() != null ? selectedReport.getUser().getUserId() : "") + 
                       " - " + (selectedReport.getUser().getFullName() != null ? selectedReport.getUser().getFullName() : "");
        }
        lblDetailStaff.setText(staffInfo);

        String shiftTime = "-";
        if (selectedReport.getShiftStart() != null && selectedReport.getShiftEnd() != null) {
            shiftTime = selectedReport.getShiftStart().format(DateTimeFormatter.ofPattern("HH:mm dd/MM")) +
                    " - " + selectedReport.getShiftEnd().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        lblDetailTime.setText(shiftTime);

        lblDetailRevenue.setText(fmt(selectedReport.getTotalRevenue()));
        lblDetailActualCash.setText(fmt(selectedReport.getActualCash()));
        
        lblDetailDiscrepancy.setText(fmt(selectedReport.getDiscrepancy()));
        if (selectedReport.getDiscrepancy() != null && selectedReport.getDiscrepancy().compareTo(BigDecimal.ZERO) < 0) {
            lblDetailDiscrepancy.setForeground(DANGER_COLOR);
        } else if (selectedReport.getDiscrepancy() != null && selectedReport.getDiscrepancy().compareTo(BigDecimal.ZERO) > 0) {
            lblDetailDiscrepancy.setForeground(SUCCESS_COLOR);
        } else {
            lblDetailDiscrepancy.setForeground(TEXT_MAIN);
        }

        lblDetailRemitted.setText(fmt(selectedReport.getRemittedCash()));
        lblDetailCarryOver.setText(fmt(selectedReport.getCarryOverCash()));
        lblDetailStatus.setText(selectedReport.getStatus() != null ? selectedReport.getStatus().toUpperCase() : "-");
    }

    private void refreshData() {
        try {
            LocalDate from = asLocalDate((Date) spFromDate.getValue());
            LocalDate to = asLocalDate((Date) spToDate.getValue());
            if (from.isAfter(to)) {
                JOptionPane.showMessageDialog(this, "Từ ngày không được lớn hơn đến ngày.",
                        "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<ShiftReport> allReports = shiftReportController.getAllShiftReports();
            List<ShiftReport> filtered = filterReports(allReports, from, to);
            
            currentReports.clear();
            currentReports.addAll(filtered);
            renderTable(filtered);
            renderDashboard(filtered);
            
            // clear selection
            table.clearSelection();
            selectedReport = null;
            updateDetailPanel();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu báo cáo ca:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private List<ShiftReport> filterReports(List<ShiftReport> reports, LocalDate from, LocalDate to) {
        List<ShiftReport> result = new ArrayList<>();
        String statusFilter = (String) cbStatus.getSelectedItem();
        String staffSearch = txtStaffSearch.getText().trim().toLowerCase();

        for (ShiftReport r : reports) {
            if (r.getShiftStart() == null) continue;

            LocalDate shiftDate = r.getShiftStart().toLocalDate();
            if (shiftDate.isBefore(from) || shiftDate.isAfter(to)) continue;

            if (!"ALL".equals(statusFilter) && !statusFilter.equals(r.getStatus())) continue;

            if (!staffSearch.isEmpty()) {
                String staffInfo = (r.getUser() != null ? r.getUser().getUserId() + " " + r.getUser().getFullName() : "").toLowerCase();
                if (!staffInfo.contains(staffSearch)) continue;
            }

            result.add(r);
        }
        return result;
    }

    private void renderTable(List<ShiftReport> reports) {
        tableModel.setRowCount(0);
        for (ShiftReport r : reports) {
            String shiftTime = "";
            if (r.getShiftStart() != null && r.getShiftEnd() != null) {
                shiftTime = r.getShiftStart().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) +
                        " - " + r.getShiftEnd().format(DateTimeFormatter.ofPattern("HH:mm"));
            }

            String staffInfo = "";
            if (r.getUser() != null) {
                staffInfo = (r.getUser().getUserId() != null ? r.getUser().getUserId() : "") +
                        " - " + (r.getUser().getFullName() != null ? r.getUser().getFullName() : "");
            }

            BigDecimal discrepancy = r.getDiscrepancy() != null ? r.getDiscrepancy() : BigDecimal.ZERO;
            String discrepancyStr = fmt(discrepancy);

            String approvedBy = "";
            if (r.getApprovedBy() != null && r.getApprovedAt() != null) {
                approvedBy = r.getApprovedBy().getUserId() + " (" + r.getApprovedAt().format(DT_FMT) + ")";
            }

            tableModel.addRow(new Object[]{
                    r.getShiftReportId() != null ? r.getShiftReportId().substring(0, Math.min(8, r.getShiftReportId().length())) : "",
                    staffInfo,
                    shiftTime,
                    fmt(r.getTotalRevenue()),
                    fmt(r.getActualCash()),
                    discrepancyStr,
                    r.getStatus() != null ? r.getStatus() : "PENDING",
                    approvedBy
            });
        }
    }

    private void renderDashboard(List<ShiftReport> reports) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int pendingCount = 0;
        int processedCount = 0;
        BigDecimal totalDiscrepancy = BigDecimal.ZERO;

        for (ShiftReport r : reports) {
            if (r.getTotalRevenue() != null) {
                totalRevenue = totalRevenue.add(r.getTotalRevenue());
            }
            if (r.getDiscrepancy() != null) {
                totalDiscrepancy = totalDiscrepancy.add(r.getDiscrepancy());
            }

            if ("PENDING".equalsIgnoreCase(r.getStatus())) {
                pendingCount++;
            } else if ("APPROVED".equalsIgnoreCase(r.getStatus()) || "LOCKED".equalsIgnoreCase(r.getStatus())) {
                processedCount++;
            }
        }

        lblTotalRevenue.setText(fmt(totalRevenue));
        lblPendingCount.setText(pendingCount + " ca làm việc");
        lblProcessedCount.setText(processedCount + " ca làm việc");
        
        lblTotalDiscrepancy.setText(fmt(totalDiscrepancy));
        if (totalDiscrepancy.compareTo(BigDecimal.ZERO) < 0) {
            lblTotalDiscrepancy.setForeground(DANGER_COLOR);
        } else if (totalDiscrepancy.compareTo(BigDecimal.ZERO) > 0) {
            lblTotalDiscrepancy.setForeground(SUCCESS_COLOR);
        } else {
            lblTotalDiscrepancy.setForeground(DANGER_COLOR);
        }
    }

    private void approveShiftReport() {
        if (selectedReport == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một báo cáo để duyệt.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (!"PENDING".equalsIgnoreCase(selectedReport.getStatus())) {
            JOptionPane.showMessageDialog(this, "Chỉ duyệt những ca đang ở trạng thái PENDING.", "Lệnh bị từ chối", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resp = JOptionPane.showConfirmDialog(this,"Xác nhận ca làm việc không có vấn đề để duyệt?", "Xét duyệt nhanh", JOptionPane.YES_NO_OPTION);
        if (resp != JOptionPane.YES_OPTION) return;

        try {
            User currentUser = UserSessionContext.getCurrentUser();
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this, "Hết phiên đăng nhập. Vui lòng đăng nhập lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            shiftReportController.approveShiftReport(selectedReport.getShiftReportId(), currentUser.getUserId(), "Đã kiểm tra nhanh qua Dashboard");
            JOptionPane.showMessageDialog(this, "Duyệt báo cáo thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Duyệt thất bại:\n" + ex.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void lockShiftReport() {
        if (selectedReport == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một báo cáo để khóa.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (!"APPROVED".equalsIgnoreCase(selectedReport.getStatus())) {
            JOptionPane.showMessageDialog(this, "Chỉ khóa các ca đã APPROVED (nhằm phong tỏa dữ liệu).", "Lệnh bị từ chối", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Khóa dữ liệu này sẽ ngăn mọi sửa đổi sau này. Xác nhận khóa vĩnh viễn báo cáo ca?",
                "Cảnh báo hệ thống", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            User currentUser = UserSessionContext.getCurrentUser();
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this, "Hết phiên đăng nhập. Vui lòng đăng nhập lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            shiftReportController.lockShiftReport(selectedReport.getShiftReportId(), currentUser.getUserId());
            JOptionPane.showMessageDialog(this, "Đã khóa thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Khóa thất bại:\n" + ex.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewFullDetails() {
        if (selectedReport == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một báo cáo để xem chi tiết.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("==== BÁO CÁO CHỐT CA CHI TIẾT ====\n\n");
        details.append("Mã Báo Cáo: ").append(selectedReport.getShiftReportId()).append("\n");
        
        if (selectedReport.getUser() != null) {
            details.append("Nhân viên: ").append(selectedReport.getUser().getUserId()).append(" - ")
                    .append(selectedReport.getUser().getFullName()).append("\n");
        }

        details.append("\n[THỜI GIAN CA]\n");
        details.append("Bắt đầu: ").append(selectedReport.getShiftStart() != null ? selectedReport.getShiftStart().format(DT_FMT) : "").append("\n");
        details.append("Kết thúc: ").append(selectedReport.getShiftEnd() != null ? selectedReport.getShiftEnd().format(DT_FMT) : "").append("\n");

        details.append("\n[SỐ LIỆU TÀI CHÍNH]\n");
        details.append("- Tiền tồn đầu ca: \t\t").append(fmt(selectedReport.getOpeningCash())).append("\n");
        details.append("- Doanh thu tiền mặt (CASH): \t").append(fmt(selectedReport.getCashRevenue())).append("\n");
        details.append("- Doanh thu chuyển khoản (QR): \t").append(fmt(selectedReport.getTransferRevenue())).append("\n");
        details.append("- Doanh thu thẻ (CARD): \t").append(fmt(selectedReport.getCardRevenue())).append("\n");
        details.append("- TỔNG DOANH THU: \t\t").append(fmt(selectedReport.getTotalRevenue())).append("\n");
        
        details.append("\n[ĐỐI SOÁT KIỂM KÊ]\n");
        details.append("- Tiền mặt máy tính tính: \t").append(fmt(selectedReport.getExpectedCash())).append("\n");
        details.append("- Tiền mặt kiểm đếm thực tế: \t").append(fmt(selectedReport.getActualCash())).append("\n");
        details.append("- Lệch so với hệ thống: \t").append(fmt(selectedReport.getDiscrepancy())).append("\n");
        details.append("- Nộp về tài quỹ công ty: \t").append(fmt(selectedReport.getRemittedCash())).append("\n");
        details.append("- Giữ lại gối đầu ca sau: \t").append(fmt(selectedReport.getCarryOverCash())).append("\n");

        details.append("\n[TÌNH TRẠNG DUYỆT]\n");
        details.append("Trạng thái: ").append(selectedReport.getStatus()).append("\n");
        if (selectedReport.getApprovedBy() != null) {
            details.append("Người duyệt: ").append(selectedReport.getApprovedBy().getUserId()).append("\n");
            details.append("Thời gian: ").append(selectedReport.getApprovedAt() != null ? selectedReport.getApprovedAt().format(DT_FMT) : "").append("\n");
        }
        if (selectedReport.getNotes() != null && !selectedReport.getNotes().isEmpty()) {
            details.append("Ghi chú: ").append(selectedReport.getNotes()).append("\n");
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setMargin(new Insets(15, 15, 15, 15));
        textArea.setBackground(new Color(250, 250, 250));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(550, 480));
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        JOptionPane.showMessageDialog(this, scrollPane, "Hồ Sơ Chốt Ca", JOptionPane.PLAIN_MESSAGE);
    }

    private void exportExcel() {
        if (currentReports.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có báo cáo nào ở bộ lọc hiện tại.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu file Excel");
        chooser.setSelectedFile(new java.io.File("ThongKeBaoCaoCa.xlsx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new java.io.File(file.getAbsolutePath() + ".xlsx");
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet("LichSuCa");
            
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font f = workbook.createFont();
            f.setBold(true);
            headerStyle.setFont(f);

            String[] headers = {"Mã Ca", "Mã NV", "Tên NV", "Bắt đầu", "Kết thúc", 
                    "Két đầu ca", "Tiền mặt phát sinh", "QR/CK", "Máy quẹt thẻ", "Tổng cộng", 
                    "Dự kiến mặt", "Đếm thực tế", "Lệch", "Đã nộp", "Tồn két để lại", "Trạng thái", "Người duyệt"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rIdx = 1;
            for (ShiftReport r : currentReports) {
                Row row = sheet.createRow(rIdx++);
                row.createCell(0).setCellValue(nonNull(r.getShiftReportId()));
                row.createCell(1).setCellValue(r.getUser() != null ? nonNull(r.getUser().getUserId()) : "");
                row.createCell(2).setCellValue(r.getUser() != null ? nonNull(r.getUser().getFullName()) : "");
                row.createCell(3).setCellValue(r.getShiftStart() != null ? r.getShiftStart().format(DT_FMT) : "");
                row.createCell(4).setCellValue(r.getShiftEnd() != null ? r.getShiftEnd().format(DT_FMT) : "");
                row.createCell(5).setCellValue(r.getOpeningCash() != null ? r.getOpeningCash().doubleValue() : 0);
                row.createCell(6).setCellValue(r.getCashRevenue() != null ? r.getCashRevenue().doubleValue() : 0);
                row.createCell(7).setCellValue(r.getTransferRevenue() != null ? r.getTransferRevenue().doubleValue() : 0);
                row.createCell(8).setCellValue(r.getCardRevenue() != null ? r.getCardRevenue().doubleValue() : 0);
                row.createCell(9).setCellValue(r.getTotalRevenue() != null ? r.getTotalRevenue().doubleValue() : 0);
                row.createCell(10).setCellValue(r.getExpectedCash() != null ? r.getExpectedCash().doubleValue() : 0);
                row.createCell(11).setCellValue(r.getActualCash() != null ? r.getActualCash().doubleValue() : 0);
                row.createCell(12).setCellValue(r.getDiscrepancy() != null ? r.getDiscrepancy().doubleValue() : 0);
                row.createCell(13).setCellValue(r.getRemittedCash() != null ? r.getRemittedCash().doubleValue() : 0);
                row.createCell(14).setCellValue(r.getCarryOverCash() != null ? r.getCarryOverCash().doubleValue() : 0);
                row.createCell(15).setCellValue(nonNull(r.getStatus()));
                row.createCell(16).setCellValue(r.getApprovedBy() != null ? nonNull(r.getApprovedBy().getUserId()) : "");
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            workbook.write(fos);
            JOptionPane.showMessageDialog(this, "Xuất dữ liệu Excel thành công:\n" + file.getAbsolutePath(), "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu tệp:\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String fmt(BigDecimal value) {
        return String.format("%,.0f ₫", value != null ? value : BigDecimal.ZERO);
    }

    private String nonNull(String s) {
        if(s == null || "null".equals(s)) return "";
        return s;
    }

    private LocalDate asLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        return spinner;
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 38));
    }

    // -- Custom Renderers --

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);
            
            String status = value == null ? "" : value.toString();
            if(!isSelected) {
                if ("APPROVED".equalsIgnoreCase(status) || "LOCKED".equalsIgnoreCase(status)) {
                    label.setForeground(new Color(21, 128, 61)); 
                    label.setBackground(new Color(220, 252, 231));
                } else if ("PENDING".equalsIgnoreCase(status)) {
                    label.setForeground(new Color(180, 83, 9)); 
                    label.setBackground(new Color(254, 243, 199)); 
                } else if ("FAILED".equalsIgnoreCase(status)) {
                    label.setForeground(new Color(185, 28, 28)); 
                    label.setBackground(new Color(254, 226, 226)); 
                } else {
                    label.setForeground(TEXT_MAIN);
                    label.setBackground(CARD_BG);
                }
            } else {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            }
            label.setBorder(new EmptyBorder(0, 10, 0, 10));
            return label;
        }
    }
}

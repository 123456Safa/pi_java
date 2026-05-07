package services;

import models.User;
import models.AdminLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExportService {

    public static void exportToPdf(List<User> users, File file) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("PharmaX Users Export", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 3f, 2f, 2f, 2f, 2f});

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        String[] headers = {"ID", "Email", "Role", "Status", "First Name", "Last Name"};
        
        for (String headerTitle : headers) {
            PdfPCell header = new PdfPCell(new Phrase(headerTitle, headerFont));
            header.setBackgroundColor(new BaseColor(44, 62, 80)); // Dark Blue/Gray
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setPadding(8);
            table.addCell(header);
        }

        Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        boolean alternate = false;

        for (User user : users) {
            BaseColor bgColor = alternate ? new BaseColor(240, 240, 240) : BaseColor.WHITE;

            addPdfCell(table, String.valueOf(user.getId()), rowFont, bgColor);
            addPdfCell(table, user.getEmail(), rowFont, bgColor);
            addPdfCell(table, cleanRole(user.getRoles()), rowFont, bgColor);
            
            // Highlight status in PDF
            Font statusFont = new Font(rowFont);
            if ("BLOCKED".equalsIgnoreCase(user.getStatus())) {
                statusFont.setColor(BaseColor.RED);
            } else {
                statusFont.setColor(new BaseColor(46, 204, 113)); // Green
            }
            addPdfCell(table, user.getStatus() != null ? user.getStatus() : "UNBLOCKED", statusFont, bgColor);
            
            addPdfCell(table, user.getFirstName(), rowFont, bgColor);
            addPdfCell(table, user.getLastName() != null ? user.getLastName() : "", rowFont, bgColor);

            alternate = !alternate;
        }

        document.add(table);
        document.close();
    }

    private static void addPdfCell(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    public static void exportToExcel(List<User> users, File file) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 8000);
        sheet.setColumnWidth(2, 5000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 5000);
        sheet.setColumnWidth(5, 5000);

        Row headerRow = sheet.createRow(0);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        headerStyle.setFont(font);

        String[] headers = {"ID", "Email", "Role", "Status", "First Name", "Last Name"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIndex = 1;
        for (User user : users) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getEmail());
            row.createCell(2).setCellValue(cleanRole(user.getRoles()));
            row.createCell(3).setCellValue(user.getStatus() != null ? user.getStatus() : "UNBLOCKED");
            row.createCell(4).setCellValue(user.getFirstName());
            row.createCell(5).setCellValue(user.getLastName() != null ? user.getLastName() : "");
        }

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    public static void exportToJson(List<User> users, File file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(users, writer);
        }
    }

    // --- Admin Logs Export ---

    public static void exportLogsToPdf(List<AdminLog> logs, File file) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("PharmaX Administrative Audit Logs", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 3f, 2f, 3f, 4f, 3f});

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        String[] headers = {"ID", "Admin Email", "Action", "Target", "Details", "Timestamp"};
        
        for (String headerTitle : headers) {
            PdfPCell header = new PdfPCell(new Phrase(headerTitle, headerFont));
            header.setBackgroundColor(new BaseColor(44, 62, 80));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setPadding(8);
            table.addCell(header);
        }

        Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        boolean alternate = false;

        for (AdminLog log : logs) {
            BaseColor bgColor = alternate ? new BaseColor(240, 240, 240) : BaseColor.WHITE;

            addPdfCell(table, String.valueOf(log.getId()), rowFont, bgColor);
            addPdfCell(table, log.getAdminEmail(), rowFont, bgColor);
            addPdfCell(table, log.getActionType(), rowFont, bgColor);
            addPdfCell(table, log.getTargetEmail() != null ? log.getTargetEmail() : "N/A", rowFont, bgColor);
            addPdfCell(table, log.getDetails(), rowFont, bgColor);
            addPdfCell(table, log.getCreatedAt().toString(), rowFont, bgColor);

            alternate = !alternate;
        }

        document.add(table);
        document.close();
    }

    public static void exportLogsToExcel(List<AdminLog> logs, File file) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Audit Logs");

        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 8000);
        sheet.setColumnWidth(2, 5000);
        sheet.setColumnWidth(3, 8000);
        sheet.setColumnWidth(4, 12000);
        sheet.setColumnWidth(5, 6000);

        Row headerRow = sheet.createRow(0);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        String[] headers = {"ID", "Admin Email", "Action", "Target", "Details", "Timestamp"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIndex = 1;
        for (AdminLog log : logs) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(log.getId());
            row.createCell(1).setCellValue(log.getAdminEmail());
            row.createCell(2).setCellValue(log.getActionType());
            row.createCell(3).setCellValue(log.getTargetEmail() != null ? log.getTargetEmail() : "N/A");
            row.createCell(4).setCellValue(log.getDetails());
            row.createCell(5).setCellValue(log.getCreatedAt().toString());
        }

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    public static void exportLogsToJson(List<AdminLog> logs, File file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(logs, writer);
        }
    }

    private static String cleanRole(String rolesStr) {
        if (rolesStr == null) return "";
        if (rolesStr.contains("ROLE_ADMIN")) return "Admin";
        if (rolesStr.contains("ROLE_USER")) return "Normal User";
        return rolesStr;
    }
}

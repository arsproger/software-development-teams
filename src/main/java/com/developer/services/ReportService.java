package com.developer.services;

import com.developer.exceptions.DeveloperNotFoundException;
import com.developer.exceptions.ReportNotFoundException;
import com.developer.models.Developer;
import com.developer.models.Report;
import com.developer.models.Task;
import com.developer.repositories.DeveloperRepository;
import com.developer.repositories.ReportRepository;
import com.developer.repositories.TaskRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final DeveloperRepository developerRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository, DeveloperRepository developerRepository, TaskRepository taskRepository) {
        this.reportRepository = reportRepository;
        this.developerRepository = developerRepository;
        this.taskRepository = taskRepository;
    }

    public List<Report> getAllReport() {
        return reportRepository.findAll();
    }

    public Report getById(Long id) {
        return reportRepository.findById(id).orElseThrow(ReportNotFoundException::new);
    }

    public Long delete(Long id) {
        reportRepository.findById(id).orElseThrow(ReportNotFoundException::new);
        reportRepository.deleteById(id);
        return id;
    }

    public List<Task> createReport(Report report) {
        return taskRepository.createReport(report.getStartDate(), report.getEndDate());
    }

    public List<Task> createReportByDevId(Long id, Report report) {
        Developer developer = developerRepository.findById(id).orElseThrow(DeveloperNotFoundException::new);
        report.setTasks(taskRepository.createReportByDevId(developer.getId(), report.getStartDate(), report.getEndDate()));
        report.setDeveloper(developer);
        return reportRepository.save(report).getTasks();
    }

    public List<Task> reportForExportByDevId(Long id, String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        return taskRepository.createReportByDevId(id, start, end);
    }

    public List<Task> reportForExportAllDevs(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        return taskRepository.createReport(start, end);
    }

    public ResponseEntity<byte[]> exportToExcel(List<Task> tasks) throws IOException {
        // Создаем книгу Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reports");

        // Задаем стиль заголовков
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        // Создаем заголовки столбцов
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Title");
        headerRow.createCell(2).setCellValue("Description");
        headerRow.createCell(3).setCellValue("Start_date");
        headerRow.createCell(4).setCellValue("End_date");
        headerRow.createCell(5).setCellValue("Status");
        headerRow.createCell(6).setCellValue("Owner");

        // Устанавливаем стиль заголовков
        for (Cell cell : headerRow) {
            cell.setCellStyle(headerStyle);
        }

        // Заполняем таблицу данными
        int rowNum = 1;
        for (Task task : tasks) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(task.getId());
            row.createCell(1).setCellValue(task.getTitle());
            row.createCell(2).setCellValue(task.getDescription());
            row.createCell(3).setCellValue(task.getStartDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
            row.createCell(4).setCellValue(task.getEndDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
            row.createCell(5).setCellValue(String.valueOf(task.getStatus()));
            row.createCell(6).setCellValue(task.getDeveloper().getFullName());
        }

        // Авторазмер столбцов
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        // Переводим книгу Excel в массив байтов
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);

        String fileName = "report on tasks " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        // Формируем ответ с файлом Excel
        byte[] bytes = outputStream.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName + ".xlsx");
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes, headers, 200);
    }

    public ResponseEntity<byte[]> exportToPDF(List<Task> tasks) throws DocumentException {
        // Создаем документ PDF
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // Создаем таблицу и задаем заголовки столбцов
        PdfPTable table = new PdfPTable(7);
        table.setWidths(new int[]{1, 2, 3, 2, 2, 1, 2});
        table.addCell("ID");
        table.addCell("Title");
        table.addCell("Description");
        table.addCell("Start_date");
        table.addCell("End_date");
        table.addCell("Status");
        table.addCell("Owner");

        // Заполняем таблицу данными
        for (Task task : tasks) {
            table.addCell(String.valueOf(task.getId()));
            table.addCell(task.getTitle());
            table.addCell(task.getDescription());
            table.addCell(task.getStartDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
            table.addCell(task.getEndDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
            table.addCell(String.valueOf(task.getStatus()));
            table.addCell(task.getDeveloper().getFullName());
        }

        // Добавляем таблицу в документ PDF
        document.add(table);
        document.close();

        String fileName = "report on tasks " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        // Формируем ответ с файлом PDF
        byte[] bytes = outputStream.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", fileName + ".pdf");
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes, headers, 200);
    }
}

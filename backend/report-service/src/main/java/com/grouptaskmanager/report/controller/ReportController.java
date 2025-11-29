package com.grouptaskmanager.report.controller;

import com.grouptaskmanager.report.model.DailyTaskStats;
import com.grouptaskmanager.report.service.PdfReportService;
import com.grouptaskmanager.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final PdfReportService pdfReportService;

    @GetMapping("/stats/current-user")
    public ResponseEntity<DailyTaskStats> getCurrentUserStats(
            @RequestHeader("X-User-Login") String userLogin,
            @RequestParam(defaultValue = "7") int daysBack) {
        log.info("Get current user stats request from user: {} for last {} days", userLogin, daysBack);
        DailyTaskStats stats = reportService.getCurrentUserStats(userLogin, daysBack);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/all-users")
    public ResponseEntity<List<DailyTaskStats>> getAllUsersStats(
            @RequestHeader("X-User-Login") String userLogin,
            @RequestParam(defaultValue = "7") int daysBack) {
        log.info("Get all users stats request from user: {} for last {} days", userLogin, daysBack);
        List<DailyTaskStats> stats = reportService.getAllUsersStats(userLogin, daysBack);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> generatePdfReport(
            @RequestHeader("X-User-Login") String userLogin,
            @RequestParam(defaultValue = "7") int daysBack) {
        log.info("Generate PDF report request from user: {} for last {} days", userLogin, daysBack);
        
        try {
            byte[] pdfBytes = pdfReportService.generateDailyTaskReport(userLogin, daysBack);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "daily-tasks-report.pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            log.error("Error generating PDF report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


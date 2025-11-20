package com.grouptaskmanager.controller;

import java.util.List;

import com.grouptaskmanager.model.DailyTask;
import com.grouptaskmanager.rest.dailytask.RestCreateDailyTask;
import com.grouptaskmanager.rest.dailytask.RestDailyTask;
import com.grouptaskmanager.rest.dailytask.RestDailyTaskStats;
import com.grouptaskmanager.service.DailyTaskService;
import com.grouptaskmanager.service.PdfReportService;
import org.apache.catalina.connector.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/daily-tasks")
@AllArgsConstructor
public class DailyTaskController {

    private final DailyTaskService dailyTaskService;
    private final PdfReportService pdfReportService;

    @PostMapping
    public ResponseEntity<DailyTask> createDailyTask(@RequestBody RestCreateDailyTask restCreateDailyTask) {
        log.info("Received create daily task request {}", restCreateDailyTask.getDescription());
        DailyTask createdDailyTask = dailyTaskService.createDailyTask(restCreateDailyTask);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(createdDailyTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteDailyTask(@PathVariable Long id) {
        log.info("Received delete daily task request {}", id);
        dailyTaskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<RestDailyTask>> getAllDailyTasksForGroup() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dailyTaskService.getAllTasksForGroup());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> toggleIsDone(@PathVariable Long id) {
        log.info("Received toggle done status for daily task {}", id);
        dailyTaskService.toggleIsDone(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/stats/current-user")
    public ResponseEntity<RestDailyTaskStats> getCurrentUserStats(
            @RequestParam(defaultValue = "7") int daysBack) {
        log.info("Received get current user stats request for last {} days", daysBack);
        RestDailyTaskStats stats = dailyTaskService.getCurrentUserStats(daysBack);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/stats/all-users")
    public ResponseEntity<List<RestDailyTaskStats>> getAllUsersStats(
            @RequestParam(defaultValue = "7") int daysBack) {
        log.info("Received get all users stats request for last {} days", daysBack);
        List<RestDailyTaskStats> stats = dailyTaskService.getAllUsersStats(daysBack);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> generatePdfReport(
            @RequestParam(defaultValue = "7") int daysBack) {
        log.info("Received generate PDF report request for last {} days", daysBack);
        
        try {
            byte[] pdfBytes = pdfReportService.generateDailyTaskReport(daysBack);
            
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

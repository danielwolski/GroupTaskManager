package com.grouptaskmanager.service;

import com.grouptaskmanager.model.DailyTaskStats;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.element.AreaBreak;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class PdfReportService {

    private final DailyTaskArchiveService dailyTaskArchiveService;
    private final UserService userService;

    public byte[] generateDailyTaskReport(int daysBack) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            addTitlePage(document, daysBack);

            addGroupOverviewPage(document, daysBack);

            addUserDetailPages(document, daysBack);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF report", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private void addTitlePage(Document document, int daysBack) {
        Paragraph title = new Paragraph("Daily Tasks Report")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("Group Performance Analysis")
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(subtitle);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysBack - 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        
        Paragraph dateRange = new Paragraph(String.format("Period: %s - %s", 
                startDate.format(formatter), endDate.format(formatter)))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(dateRange);

        Paragraph generatedDate = new Paragraph(String.format("Generated on: %s", 
                LocalDate.now().format(formatter)))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(generatedDate);

        document.add(new Paragraph("").setMarginTop(50));
    }

    private void addGroupOverviewPage(Document document, int daysBack) {
        Paragraph pageTitle = new Paragraph("Group Overview")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(pageTitle);

        List<DailyTaskStats> allStats = dailyTaskArchiveService.getAllUsersStats(daysBack);
        
        if (allStats.isEmpty()) {
            Paragraph noData = new Paragraph("No data available for this period.")
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(noData);
            return;
        }

        long totalCompleted = allStats.stream().mapToLong(DailyTaskStats::getCompletedTasks).sum();
        long totalTasks = allStats.stream().mapToLong(DailyTaskStats::getTotalTasks).sum();
        double averageCompletionRate = allStats.stream()
                .mapToDouble(DailyTaskStats::getCompletionRate)
                .average()
                .orElse(0.0);


        Table summaryTable = new Table(2).useAllAvailableWidth();
        summaryTable.addCell(new Cell().add(new Paragraph("Metric")).setBold());
        summaryTable.addCell(new Cell().add(new Paragraph("Value")).setBold());
        
        summaryTable.addCell(new Cell().add(new Paragraph("Total Users")));
        summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(allStats.size()))));
        
        summaryTable.addCell(new Cell().add(new Paragraph("Total Tasks Completed")));
        summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(totalCompleted))));
        
        summaryTable.addCell(new Cell().add(new Paragraph("Total Tasks Assigned")));
        summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(totalTasks))));
        
        summaryTable.addCell(new Cell().add(new Paragraph("Average Completion Rate")));
        summaryTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", averageCompletionRate))));

        document.add(summaryTable);

        document.add(new Paragraph("Individual Performance").setFontSize(16).setBold().setMarginTop(20));

        Table userTable = new Table(4).useAllAvailableWidth();
        userTable.addCell(new Cell().add(new Paragraph("User")).setBold());
        userTable.addCell(new Cell().add(new Paragraph("Completed")).setBold());
        userTable.addCell(new Cell().add(new Paragraph("Total")).setBold());
        userTable.addCell(new Cell().add(new Paragraph("Rate")).setBold());

        for (DailyTaskStats stat : allStats) {
            userTable.addCell(new Cell().add(new Paragraph(stat.getUsername())));
            userTable.addCell(new Cell().add(new Paragraph(String.valueOf(stat.getCompletedTasks()))));
            userTable.addCell(new Cell().add(new Paragraph(String.valueOf(stat.getTotalTasks()))));
            userTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", stat.getCompletionRate()))));
        }

        document.add(userTable);
    }

    private void addUserDetailPages(Document document, int daysBack) {
        List<DailyTaskStats> allStats = dailyTaskArchiveService.getAllUsersStats(daysBack);
        for (DailyTaskStats stat : allStats) {
            document.add(new AreaBreak());

            Paragraph userTitle = new Paragraph("User Details: " + stat.getUsername())
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(userTitle);

            Table userSummaryTable = new Table(2).useAllAvailableWidth();
            userSummaryTable.addCell(new Cell().add(new Paragraph("Metric")).setBold());
            userSummaryTable.addCell(new Cell().add(new Paragraph("Value")).setBold());
            
            userSummaryTable.addCell(new Cell().add(new Paragraph("Username")));
            userSummaryTable.addCell(new Cell().add(new Paragraph(stat.getUsername())));
            
            userSummaryTable.addCell(new Cell().add(new Paragraph("Tasks Completed")));
            userSummaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(stat.getCompletedTasks()))));
            
            userSummaryTable.addCell(new Cell().add(new Paragraph("Total Tasks")));
            userSummaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(stat.getTotalTasks()))));
            
            userSummaryTable.addCell(new Cell().add(new Paragraph("Completion Rate")));
            userSummaryTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", stat.getCompletionRate()))));
            
            userSummaryTable.addCell(new Cell().add(new Paragraph("Analysis Period")));
            userSummaryTable.addCell(new Cell().add(new Paragraph("Last " + stat.getPeriodDays() + " days")));
            
            userSummaryTable.addCell(new Cell().add(new Paragraph("Regular Tasks Done")));
            userSummaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(stat.getRegularTasksDone()))));
            
            userSummaryTable.addCell(new Cell().add(new Paragraph("Regular Tasks Not Done")));
            userSummaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(stat.getRegularTasksNotDone()))));

            document.add(userSummaryTable);

            // Zwykłe taski - Done
            if (stat.getRegularTasksDone() != null && stat.getRegularTasksDone() > 0) {
                document.add(new Paragraph("Regular Tasks - Completed (" + stat.getRegularTasksDone() + ")")
                        .setFontSize(16).setBold().setMarginTop(20));
                if (stat.getRegularTasksDoneNames() != null && !stat.getRegularTasksDoneNames().isEmpty()) {
                    for (String taskName : stat.getRegularTasksDoneNames()) {
                        Paragraph taskParagraph = new Paragraph("• " + taskName);
                        document.add(taskParagraph);
                    }
                }
            }
            
            // Zwykłe taski - Not Done
            if (stat.getRegularTasksNotDone() != null && stat.getRegularTasksNotDone() > 0) {
                document.add(new Paragraph("Regular Tasks - Not Completed (" + stat.getRegularTasksNotDone() + ")")
                        .setFontSize(16).setBold().setMarginTop(20));
                if (stat.getRegularTasksNotDoneNames() != null && !stat.getRegularTasksNotDoneNames().isEmpty()) {
                    for (String taskName : stat.getRegularTasksNotDoneNames()) {
                        Paragraph taskParagraph = new Paragraph("• " + taskName);
                        document.add(taskParagraph);
                    }
                }
            }

            document.add(new Paragraph("Daily Tasks Performance Analysis").setFontSize(16).setBold().setMarginTop(20));
            
            String performanceComment = getPerformanceComment(stat.getCompletionRate());
            Paragraph comment = new Paragraph(performanceComment);
            document.add(comment);


            document.add(new Paragraph("Recommendations").setFontSize(16).setBold().setMarginTop(20));
            String recommendation = getRecommendation(stat.getCompletionRate());
            Paragraph rec = new Paragraph(recommendation);
            document.add(rec);
        }
    }

    private String getPerformanceComment(double completionRate) {
        if (completionRate >= 90) {
            return "Excellent performance! This user consistently completes their daily tasks and demonstrates strong commitment to the group's goals.";
        } else if (completionRate >= 80) {
            return "Very good performance. The user shows good consistency in completing daily tasks with room for minor improvements.";
        } else if (completionRate >= 70) {
            return "Good performance. The user completes most tasks but could benefit from improved consistency.";
        } else if (completionRate >= 60) {
            return "Average performance. There's significant room for improvement in task completion consistency.";
        } else if (completionRate >= 40) {
            return "Below average performance. The user struggles with completing daily tasks regularly.";
        } else {
            return "Poor performance. Immediate attention is needed to improve task completion rates.";
        }
    }

    private String getRecommendation(double completionRate) {
        if (completionRate >= 90) {
            return "• Consider taking on additional responsibilities\n• Mentor other team members\n• Share best practices with the group";
        } else if (completionRate >= 80) {
            return "• Focus on maintaining current performance\n• Set slightly more challenging goals\n• Help others improve their performance";
        } else if (completionRate >= 70) {
            return "• Identify obstacles to task completion\n• Set up daily reminders\n• Break down complex tasks into smaller steps";
        } else if (completionRate >= 60) {
            return "• Review task priorities and time management\n• Consider if tasks are too complex\n• Seek support from team members";
        } else if (completionRate >= 40) {
            return "• Conduct a detailed review of daily routine\n• Simplify task descriptions\n• Set up accountability check-ins";
        } else {
            return "• Immediate intervention required\n• Review task assignments and complexity\n• Consider additional training or support";
        }
    }
}

import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DailyTaskStats } from '../models/daily-task-stats.model';
import { DailyTaskService } from '../services/daily-task.service';

@Component({
  selector: 'app-daily-task-reports',
  imports: [CommonModule, FormsModule],
  templateUrl: './daily-task-reports.component.html',
  styleUrl: './daily-task-reports.component.css'
})
export class DailyTaskReportsComponent implements OnInit {

  allUsersStats: DailyTaskStats[] = [];
  currentUserStats: DailyTaskStats | null = null;
  selectedPeriod: number = 7;
  loading: boolean = false;
  error: string | null = null;

  periodOptions = [
    { value: 3, label: 'Last 3 days' },
    { value: 7, label: 'Last 7 days' },
    { value: 14, label: 'Last 14 days' },
    { value: 30, label: 'Last 30 days' }
  ];

  constructor(private dailyTaskService: DailyTaskService) {}

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.loading = true;
    this.error = null;

    // Load current user stats
    this.dailyTaskService.getCurrentUserStats(this.selectedPeriod).subscribe({
      next: (stats) => {
        this.currentUserStats = stats;
      },
      error: (err) => {
        console.error('Error loading current user stats:', err);
        this.error = 'Failed to load your statistics';
      }
    });

    // Load all users stats
    this.dailyTaskService.getAllUsersStats(this.selectedPeriod).subscribe({
      next: (stats) => {
        this.allUsersStats = stats;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading all users stats:', err);
        this.error = 'Failed to load group statistics';
        this.loading = false;
      }
    });
  }

  onPeriodChange(): void {
    this.loadStats();
  }

  getCompletionRateColor(rate: number): string {
    if (rate >= 80) return 'green';
    if (rate >= 60) return 'orange';
    return 'red';
  }

  getCompletionRateClass(rate: number): string {
    if (rate >= 80) return 'completion-high';
    if (rate >= 60) return 'completion-medium';
    return 'completion-low';
  }

  getAverageCompletionRate(): number {
    if (this.allUsersStats.length === 0) return 0;
    const totalRate = this.allUsersStats.reduce((sum, stat) => sum + stat.completionRate, 0);
    return totalRate / this.allUsersStats.length;
  }

  getTotalCompletedTasks(): number {
    return this.allUsersStats.reduce((sum, stat) => sum + stat.completedTasks, 0);
  }

  getRegularTasksCompletionRate(stat: DailyTaskStats): number {
    if (!stat.regularTasksDone && !stat.regularTasksNotDone) return 0;
    const total = (stat.regularTasksDone || 0) + (stat.regularTasksNotDone || 0);
    if (total === 0) return 0;
    return ((stat.regularTasksDone || 0) / total) * 100;
  }

  getTotalRegularTasksDone(): number {
    return this.allUsersStats.reduce((sum, stat) => sum + (stat.regularTasksDone || 0), 0);
  }

  getTotalRegularTasksNotDone(): number {
    return this.allUsersStats.reduce((sum, stat) => sum + (stat.regularTasksNotDone || 0), 0);
  }

  getTotalRegularTasks(): number {
    return this.getTotalRegularTasksDone() + this.getTotalRegularTasksNotDone();
  }

  getOverallRegularTasksCompletionRate(): number {
    const total = this.getTotalRegularTasks();
    if (total === 0) return 0;
    return (this.getTotalRegularTasksDone() / total) * 100;
  }

  hasRegularTasks(stat: DailyTaskStats): boolean {
    return (stat.regularTasksDone || 0) > 0 || (stat.regularTasksNotDone || 0) > 0;
  }

  getAllRegularTasksDone(): string[] {
    const allDoneTasks: string[] = [];
    this.allUsersStats.forEach(stat => {
      if (stat.regularTasksDoneNames) {
        allDoneTasks.push(...stat.regularTasksDoneNames);
      }
    });
    return allDoneTasks;
  }

  getAllRegularTasksNotDone(): string[] {
    const allNotDoneTasks: string[] = [];
    this.allUsersStats.forEach(stat => {
      if (stat.regularTasksNotDoneNames) {
        allNotDoneTasks.push(...stat.regularTasksNotDoneNames);
      }
    });
    return allNotDoneTasks;
  }

  generateReport(): void {
    this.loading = true;
    this.error = null;

    this.dailyTaskService.generatePdfReport(this.selectedPeriod).subscribe({
      next: (blob) => {
        // Create download link
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `daily-tasks-report-${this.selectedPeriod}days.pdf`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        this.loading = false;
      },
      error: (err) => {
        console.error('Error generating PDF report:', err);
        this.error = 'Failed to generate PDF report';
        this.loading = false;
      }
    });
  }
}

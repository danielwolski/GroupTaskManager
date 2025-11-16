import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CreateDailyTaskRequest, DailyTask } from '../models/daily-task.model';
import { DailyTaskService } from '../services/daily-task.service';
import { UserService } from '../services/user.service';
import { User } from '../models/user.model';

@Component({
  selector: 'app-daily-task',
  imports: [CommonModule, FormsModule],
  templateUrl: './daily-task.component.html',
  styleUrl: './daily-task.component.css'
})
export class DailyTaskComponent implements OnInit {

  dailyTasks: DailyTask[] = [];
  users: User[] = [];

  dailyTaskRequest: CreateDailyTaskRequest = {
    description: '',
    assigneeUserId: undefined
  };

  constructor(
    private dailyTaskService: DailyTaskService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.loadDailyTasks();
    this.loadUsers();

    this.dailyTaskService.dailyTasksUpdated$.subscribe(() => {
      this.loadDailyTasks();
    });
  }

  loadUsers(): void {
    this.userService.getUsersByGroup().subscribe((data: User[]) => {
      this.users = data;
    });
  }

  removeDailyTask(dailyTaskId: number) {
    this.dailyTaskService.removeDailyTask(dailyTaskId).subscribe(() => {});
  }

  toggleIsDone(dailyTaskId: number) {
    this.dailyTaskService.toggleIsDone(dailyTaskId).subscribe(() => {});
  }

  loadDailyTasks(): void {
    this.dailyTaskService.getDailyTasks().subscribe((data: DailyTask[]) => {
      this.dailyTasks = data;
    });
  }
  
  confirmCreateDailyTask(): void {
    this.dailyTaskService.addDailyTask(this.dailyTaskRequest).subscribe({
      next: (response) => {
        console.log('DailyTask created:', response);
      },
      error: (err) => {
        console.error('Error during creating daily task:', err);
      },
    });
    this.clearAddDailyTaskFormInput();
  }

  clearAddDailyTaskFormInput(): void {
    this.dailyTaskRequest = {
      description: '',
      assigneeUserId: undefined
    };
  }  
}

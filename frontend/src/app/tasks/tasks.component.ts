import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CreateTaskRequest, Task } from '../models/task.model';
import { TaskService } from '../services/task.service';
import { UserService } from '../services/user.service';
import { User } from '../models/user.model';

@Component({
  selector: 'app-tasks',
  imports: [CommonModule, FormsModule],
  templateUrl: './tasks.component.html',
  styleUrl: './tasks.component.css'
})
export class TasksComponent implements OnInit {

  todoTasks: Task[] = [];
  doneTasks: Task[] = [];
  users: User[] = [];

  taskRequest: CreateTaskRequest = {
    description: '',
    assigneeUserId: undefined
  };

  constructor(
    private taskService: TaskService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.loadTasks();
    this.loadUsers();

    this.taskService.tasksUpdated$.subscribe(() => {
      this.loadTasks();
    });
  }

  loadUsers(): void {
    this.userService.getUsersByGroup().subscribe((data: User[]) => {
      this.users = data;
    });
  }

  removeTask(taskId: number) {
    this.taskService.removeTask(taskId).subscribe(() => {});
  }

  toggleIsDone(taskId: number) {
    this.taskService.toggleIsDone(taskId).subscribe(() => {});
  }

  loadTasks(): void {
    this.taskService.getTasks().subscribe((data: Task[]) => {
      this.todoTasks = data.filter(task => !task.done);
      this.doneTasks = data.filter(task => task.done);
    });
  }
  
  confirmCreateTask(): void {
    console.log(this.taskRequest)
    this.taskService.addTask(this.taskRequest).subscribe({
      next: (response) => {
        console.log('Task created:', response);
      },
      error: (err) => {
        console.error('Error during creating task:', err);
      },
    });
    this.clearAddTaskFormInput();
  }

  clearAddTaskFormInput(): void {
    this.taskRequest = {
      description: '',
      assigneeUserId: undefined
    };
  }
  
  isTasksListEmpty(): boolean {
    return this.todoTasks.length === 0 && this.doneTasks.length === 0;
  }

  isTodoListEmpty(): boolean {
    return this.todoTasks.length === 0;
  }
}

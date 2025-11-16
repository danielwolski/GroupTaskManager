import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from './authorization/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  standalone: true,
  imports: [
    RouterOutlet, CommonModule]
})
export class AppComponent {
  constructor(private router: Router, private authService: AuthService) {}

  goToCalendar() {
    this.router.navigate(['/tasks']);
  }

  goToTasks() {
    this.router.navigate(['/tasks']);
  }

  goToDailyTask() {
    this.router.navigate(['/daily-task']);
  }

  goToDailyTaskReports() {
    this.router.navigate(['/daily-task-reports']);
  }

  getUsername() {
    return localStorage.getItem('username');
  }

  isLoggedIn() {
    return this.authService.isAuthenticated();
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => console.log('Logged out on the server'),
      error: (err) => console.error('Server logout error:', err),
    });
    this.router.navigate(['/login']);
  }

  isActive(path: string): boolean {
    return this.router.url === path;
  }
}

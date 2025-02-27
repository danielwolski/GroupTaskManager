import { Component } from '@angular/core';
import { AuthService } from '../authorization/auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  login = '';
  password = '';
  errorMessage = '';

  constructor(private authService: AuthService, private router: Router) {}

  onLogin() {
    this.authService.login(this.login, this.password).subscribe({
      next: response => {
        this.authService.saveToken(response.access_token);
        this.authService.saveUsername(response.username);
        this.router.navigate(['/events-list']);
      },
      error: err => {
        this.login = '';
        this.password = '';
        if (err.status === 500) {
          this.errorMessage = 'Invalid credentials. Please try again.';
        } else {
          this.errorMessage = 'An unexpected error occurred. Please try again later.';
        }
      }
    });
  }

  goToRegister() {
    this.router.navigate(['/register']);
  }
}

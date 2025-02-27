import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private authApiUrl = environment.apiUrl + '/api/auth';
  
  constructor(private http: HttpClient) {}

  login(login: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.authApiUrl}/login`, { login, password });
  }

  register(username: string, login: string, password: string, groupPasscode: string): Observable<any> {
    return this.http.post<any>(`${this.authApiUrl}/register`, { username, login, password, groupPasscode });
  }

  saveUsername(username: string): void {
    console.log("Hello", username)
    localStorage.setItem('username', username);
  }

  saveToken(token: string): void {
    localStorage.setItem('jwtToken', token);
  }

  getToken(): string | null {
    return localStorage.getItem('jwtToken');
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  logout(): Observable<any> {
    localStorage.removeItem('username')
    localStorage.removeItem('jwtToken')
    return this.http.post(`${this.authApiUrl}/logout`, {});
  }
}

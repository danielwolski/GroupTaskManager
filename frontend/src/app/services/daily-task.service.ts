import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject, tap } from 'rxjs';
import { DailyTask, CreateDailyTaskRequest } from '../models/daily-task.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class DailyTaskService {
  private apiUrl = `${environment.apiUrl}/api/daily-tasks`;  

  private dailyTasksUpdatedSubject = new Subject<void>();

  dailyTasksUpdated$ = this.dailyTasksUpdatedSubject.asObservable();

  constructor(private http: HttpClient) {}

  getDailyTasks(): Observable<DailyTask[]> {
    return this.http.get<DailyTask[]>(this.apiUrl);
  }

  addDailyTask(request: CreateDailyTaskRequest): Observable<DailyTask> {
    return this.http.post<DailyTask>(this.apiUrl, request).pipe(
      tap(() => this.dailyTasksUpdatedSubject.next())
    );
  }

  removeDailyTask(dailyTaskId: number): Observable<void> {
    const url = `${this.apiUrl}/${dailyTaskId}`;
    return this.http.delete<void>(url).pipe(
      tap(() => this.dailyTasksUpdatedSubject.next())
    );
  }

  toggleIsDone(dailyTaskId: number): Observable<void> {
    const url = `${this.apiUrl}/${dailyTaskId}`;
    return this.http.patch<void>(url, {}).pipe(
      tap(() => this.dailyTasksUpdatedSubject.next())
    );
  }  
}

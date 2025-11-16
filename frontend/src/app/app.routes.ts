import { Routes } from '@angular/router';
import { TasksComponent } from './tasks/tasks.component';
import { DailyTaskComponent } from './daily-task/daily-task.component';
import { DailyTaskReportsComponent } from './daily-task-reports/daily-task-reports.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { AuthGuard } from './authorization/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'tasks', component: TasksComponent, canActivate: [AuthGuard] },
  { path: 'daily-task', component: DailyTaskComponent, canActivate: [AuthGuard] },
  { path: 'daily-task-reports', component: DailyTaskReportsComponent, canActivate: [AuthGuard] },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' },
];

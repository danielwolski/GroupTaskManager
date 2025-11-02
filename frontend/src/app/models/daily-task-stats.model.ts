export interface DailyTaskStats {
    userId: number;
    username: string;
    completedTasks: number;
    totalTasks: number;
    completionRate: number;
    periodDays: number;
}

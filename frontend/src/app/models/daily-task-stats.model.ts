export interface DailyTaskStats {
    userId: number;
    username: string;
    completedTasks: number;
    totalTasks: number;
    completionRate: number;
    periodDays: number;
    
    // Zwykłe taski
    regularTasksDone?: number;
    regularTasksNotDone?: number;
    regularTasksDoneNames?: string[];
    regularTasksNotDoneNames?: string[];
}

export interface DailyTask {
    id: number;
    done: boolean;
    description: string;
    assigneeUsername: string;
}

export interface CreateDailyTaskRequest {
    description: string;
    assigneeUserId?: number;
}
  
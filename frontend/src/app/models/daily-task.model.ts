export interface DailyTask {
    id: number;
    done: boolean;
    description: string;
    assignee: string;
}

export interface CreateDailyTaskRequest {
    description: string;
    assigneeUserId?: number;
}
  
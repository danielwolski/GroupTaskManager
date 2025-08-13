export interface DailyTask {
    id: number;
    done: boolean;
    description: string;
    createdBy: string;
}

export interface CreateDailyTaskRequest {
    description: string;
}
  
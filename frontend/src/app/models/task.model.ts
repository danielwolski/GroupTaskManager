export interface Task {
    id: number;
    done: boolean;
    description: string;
    assignee: string;
}

export interface CreateTaskRequest {
    description: string;
    assigneeUserId?: number;
}
  
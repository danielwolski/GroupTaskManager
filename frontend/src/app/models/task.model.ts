export interface Task {
    id: number;
    done: boolean;
    description: string;
    createdBy: string;
}

export interface CreateTaskRequest {
    description: string;
}
  
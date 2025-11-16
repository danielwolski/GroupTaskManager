# TaskManager Backend

This repository contains the backend for the TaskManager application, used to manage tasks and events. It's built using Java and Spring Boot.


# Local installation and setup

1. **Start PostgreSQL in Docker:**

To do it, PostgreSQL docker image is needed
```
docker pull postgres
```
```bash
 cd ./postgresql
 docker-compose -f docker-compose-postgresql.yml up -d
```
You may need to change this part of /postgresql/docker-compose-postgresql.yml file:
```
image: bitnami/postgresql
```
to your PostgreSQL docker image name.

2. **Build and run the project:**
```bash
 cd ..
./gradlew build
./gradlew bootRun
```

# Additional resources:
**Gradle installation**
```
https://gradle.org/install/
```

**Getting started with Docker**
```
https://www.docker.com/get-started/
```

**DockerHub PostgreSQL**
```
https://hub.docker.com/_/postgres
```

flowchart TB
subgraph client["Frontend (Angular)"]
Web[Web App]
end

Web -->|HTTP (REST) /tasks, /events, /groups, /auth| Gateway[API Gateway]

subgraph gateway["API Gateway"]
Gateway
end

Gateway --> Auth[Auth / User Service]
Gateway --> Group[Group Service]
Gateway --> Task[Task Service]
Gateway --> Event[Event Service]

%% Databases
Auth --> AuthDB[(auth_db)]
Group --> GroupDB[(group_db)]
Task --> TaskDB[(task_db)]
Event --> EventDB[(event_db)]

%% Inter-service sync calls (examples)
Task -->|GET /groups/{id}/members or GET /groups/{id}| Group
Task -->|GET /users/{id} (optional)| Auth
Event -->|GET /groups/{id}/members| Group

%% Async message bus (optional)
Gateway ---|publish events| MsgBroker[(Message Broker)]
Task ---|publish task.created| MsgBroker
Event ---|publish event.created| MsgBroker
MsgBroker ---|consume| Task
MsgBroker ---|consume| Event

style AuthDB fill:#FFF7E6,stroke:#E6A800
style GroupDB fill:#FFF7E6,stroke:#E6A800
style TaskDB fill:#FFF7E6,stroke:#E6A800
style EventDB fill:#FFF7E6,stroke:#E6A800

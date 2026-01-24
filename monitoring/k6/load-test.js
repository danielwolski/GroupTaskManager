
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const taskCreationTime = new Trend('task_creation_time');
const taskListTime = new Trend('task_list_time');
const dailyTaskCreationTime = new Trend('daily_task_creation_time');

// Test configuration
export const options = {
  stages: [
    { duration: '30s', target: 10 },   // Ramp up to 10 users
    { duration: '1m', target: 10 },    // Stay at 10 users
    { duration: '30s', target: 20 },   // Ramp up to 20 users
    { duration: '1m', target: 20 },    // Stay at 20 users
    { duration: '30s', target: 30 },   // Ramp up to 30 users
    { duration: '1m', target: 30 },    // Stay at 30 users
    { duration: '30s', target: 0 },    // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'], // 95% of requests should be below 500ms, 99% below 1000ms
    http_req_failed: ['rate<0.05'],                  // Error rate should be less than 5%
    errors: ['rate<0.1'],                            // Custom error rate should be less than 10%
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://backend:8080';
const API_BASE = `${BASE_URL}/api`;

// Test data
const testUsers = [
  { login: 'test@wp.pl', password: 'test@wp.pl', username: 'test@wp.pl' },
 // { login: 'testuser2@example.com', password: 'password123', username: 'testuser@example.com2' },
 // { login: 'testuser3@example.com', password: 'password123', username: 'testuser@example.com3' },
];

// Helper function to get or create user and return token
function authenticateUser(user) {
  // Try to login first
  const loginPayload = JSON.stringify({
    login: user.login,
    password: user.password,
  });

  let loginRes = http.post(`${API_BASE}/auth/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  if (loginRes.status === 200) {
    const loginData = JSON.parse(loginRes.body);
    return loginData.token;
  }

  // If login fails, try to register
  const registerPayload = JSON.stringify({
    login: user.login,
    password: user.password,
    username: user.username,
    groupPasscode: 'TESTGROUP123',
  });

  const registerRes = http.post(`${API_BASE}/auth/register`, registerPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  if (registerRes.status === 200) {
    const registerData = JSON.parse(registerRes.body);
    return registerData.token;
  }

  return null;
}

export default function () {
  const user = testUsers[__VU % testUsers.length];
  
  // Authenticate
  const token = authenticateUser(user);
  if (!token) {
    errorRate.add(1);
    return;
  }

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };

  // Test 1: Get all tasks
  let startTime = Date.now();
  let res = http.get(`${API_BASE}/tasks`, { headers });
  let duration = Date.now() - startTime;
  taskListTime.add(duration);
  
  check(res, {
    'get tasks status is 200': (r) => r.status === 200,
    'get tasks has response body': (r) => r.body.length > 0,
  }) || errorRate.add(1);
  sleep(1);

  // Test 2: Create a task
  const taskPayload = JSON.stringify({
    description: `Load test task ${Date.now()}`,
    dueDate: new Date(Date.now() + 86400000).toISOString(), // Tomorrow
  });

  startTime = Date.now();
  res = http.post(`${API_BASE}/tasks`, taskPayload, { headers });
  duration = Date.now() - startTime;
  taskCreationTime.add(duration);

  check(res, {
    'create task status is 202': (r) => r.status === 202,
    'create task has task id': (r) => {
      if (r.status === 202) {
        const task = JSON.parse(r.body);
        return task.id !== undefined;
      }
      return false;
    },
  }) || errorRate.add(1);
  sleep(1);

  // Test 3: Get all daily tasks
  res = http.get(`${API_BASE}/daily-tasks`, { headers });
  check(res, {
    'get daily tasks status is 200': (r) => r.status === 200,
  }) || errorRate.add(1);
  sleep(1);

  // Test 4: Create a daily task
  const dailyTaskPayload = JSON.stringify({
    description: `Load test daily task ${Date.now()}`,
  });

  startTime = Date.now();
  res = http.post(`${API_BASE}/daily-tasks`, dailyTaskPayload, { headers });
  duration = Date.now() - startTime;
  dailyTaskCreationTime.add(duration);

  check(res, {
    'create daily task status is 202': (r) => r.status === 202,
  }) || errorRate.add(1);
  sleep(1);

  // Test 5: Get users in group
  res = http.get(`${API_BASE}/users/group`, { headers });
  check(res, {
    'get users status is 200': (r) => r.status === 200,
  }) || errorRate.add(1);
  sleep(1);

  // Test 6: Get daily task stats
  res = http.get(`${API_BASE}/daily-tasks/stats/current-user?daysBack=7`, { headers });
  check(res, {
    'get stats status is 200': (r) => r.status === 200,
  }) || errorRate.add(1);
  sleep(1);
}

export function handleSummary(data) {
  return {
    'stdout': JSON.stringify(data, null, 2),
  };
}


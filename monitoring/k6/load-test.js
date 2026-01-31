
import http from 'k6/http';
import { check, sleep } from 'k6';

// Load test - gradual increase in load
export const options = {
  stages: [
    { duration: '1m', target: 10 },   // Ramp up to 10 users
    { duration: '2m', target: 10 },   // Stay at 10 users
    { duration: '1m', target: 20 },   // Ramp up to 20 users
    { duration: '2m', target: 20 },   // Stay at 20 users
    { duration: '1m', target: 30 },   // Ramp up to 30 users
    { duration: '2m', target: 30 },   // Stay at 30 users
    { duration: '30s', target: 0 },   // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
    http_req_failed: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://backend:8080';
const API_BASE = `${BASE_URL}/api`;

// Each VU registers once and reuses the token
let userToken = null;
let userRegistered = false;

export function setup() {
  // Verify backend is available
  const healthRes = http.get(`${BASE_URL}/actuator/health`);
  if (healthRes.status !== 200) {
    throw new Error('Backend is not available');
  }
  console.log('Backend health check passed');
}

function registerAndLogin() {
  const uniqueId = `${Date.now()}_${__VU}_${__ITER}`;
  const login = `loadtest${uniqueId}@test.com`;
  const password = 'password123';

  // Register
  const registerPayload = JSON.stringify({
    username: login,
    password: password,
    login: login,
    groupPasscode: 'LOADTEST',
  });

  const registerRes = http.post(`${API_BASE}/auth/register`, registerPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  if (registerRes.status !== 200) {
    console.log(`[REG] Failed for ${login}: ${registerRes.status} - ${registerRes.body}`);
    return null;
  }

  // Login to get token
  const loginPayload = JSON.stringify({
    login: login,
    password: password,
  });

  const loginRes = http.post(`${API_BASE}/auth/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  if (loginRes.status !== 200) {
    console.log(`[LOGIN] Failed for ${login}: ${loginRes.status} - ${loginRes.body}`);
    return null;
  }

  const responseBody = JSON.parse(loginRes.body);
  return responseBody.access_token;
}

export default function () {
  // Register and login for each iteration (creates new user each time)
  const token = registerAndLogin();

  if (!token) {
    console.log('[AUTH] Failed to get token, skipping iteration');
    sleep(1);
    return;
  }

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };

  // Test getting tasks
  let res = http.get(`${API_BASE}/tasks`, { headers });
  check(res, {
    'get tasks works': (r) => r.status === 200 || r.status === 202,
  });

  // Test creating a task
  const taskPayload = JSON.stringify({
    description: `Load test task ${Date.now()}`,
    dueDate: new Date(Date.now() + 86400000).toISOString(),
  });

  res = http.post(`${API_BASE}/tasks`, taskPayload, { headers });
  check(res, {
    'create task works': (r) => r.status === 200 || r.status === 202,
  });

  // Test getting daily tasks
  res = http.get(`${API_BASE}/daily-tasks`, { headers });
  check(res, {
    'get daily tasks works': (r) => r.status === 200 || r.status === 202,
  });

  // Test creating a daily task
  const dailyTaskPayload = JSON.stringify({
    description: `Load test daily task ${Date.now()}`,
  });

  res = http.post(`${API_BASE}/daily-tasks`, dailyTaskPayload, { headers });
  check(res, {
    'create daily task works': (r) => r.status === 200 || r.status === 202,
  });

  // Test getting users in group
  res = http.get(`${API_BASE}/users/group`, { headers });
  check(res, {
    'get users in group works': (r) => r.status === 200 || r.status === 202,
  });

  // Test getting daily task stats
  res = http.get(`${API_BASE}/daily-tasks/stats/current-user?daysBack=7`, { headers });
  check(res, {
    'get daily task stats works': (r) => r.status === 200 || r.status === 202,
  });

  sleep(1);
}


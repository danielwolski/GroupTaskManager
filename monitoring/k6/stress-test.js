import http from 'k6/http';
import { check, sleep } from 'k6';

// Stress test - gradually increase load to find breaking point
export const options = {
  stages: [
    { duration: '1m', target: 10 },   // Ramp up to 10 users
    { duration: '2m', target: 10 },   // Stay at 10 users
    { duration: '1m', target: 20 },   // Ramp up to 20 users
    { duration: '2m', target: 20 },   // Stay at 20 users
    { duration: '1m', target: 30 },   // Ramp up to 30 users
    { duration: '2m', target: 30 },   // Stay at 30 users
    { duration: '1m', target: 40 },   // Ramp up to 40 users
    { duration: '2m', target: 40 },   // Stay at 40 users
    { duration: '1m', target: 50 },   // Ramp up to 50 users
    { duration: '2m', target: 50 },   // Stay at 50 users
    { duration: '5m', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // More lenient for stress test
    http_req_failed: ['rate<0.1'],     // Allow up to 10% errors
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://backend:8080';
const API_BASE = `${BASE_URL}/api`;

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
  const login = `stresstest${uniqueId}@test.com`;
  const password = 'password123';

  // Register
  const registerPayload = JSON.stringify({
    username: login,
    password: password,
    login: login,
    groupPasscode: 'STRESSTEST',
  });

  const registerRes = http.post(`${API_BASE}/auth/register`, registerPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  if (registerRes.status !== 200) {
    console.log(`[REG] Failed for ${login}: ${registerRes.status}`);
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
    console.log(`[LOGIN] Failed for ${login}: ${loginRes.status}`);
    return null;
  }

  const responseBody = JSON.parse(loginRes.body);
  return responseBody.access_token;
}

export default function () {
  // Register and login for each iteration
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

  // Mix of operations
  const operations = [
    () => {
      const res = http.get(`${API_BASE}/tasks`, { headers });
      check(res, { 'get tasks': (r) => r.status >= 200 && r.status < 300 });
      return res;
    },
    () => {
      const res = http.get(`${API_BASE}/daily-tasks`, { headers });
      check(res, { 'get daily tasks': (r) => r.status >= 200 && r.status < 300 });
      return res;
    },
    () => {
      const payload = JSON.stringify({
        description: `Stress test task ${Date.now()}`,
        dueDate: new Date(Date.now() + 86400000).toISOString(),
      });
      const res = http.post(`${API_BASE}/tasks`, payload, { headers });
      check(res, { 'create task': (r) => r.status >= 200 && r.status < 300 });
      return res;
    },
    () => {
      const payload = JSON.stringify({
        description: `Stress test daily task ${Date.now()}`,
      });
      const res = http.post(`${API_BASE}/daily-tasks`, payload, { headers });
      check(res, { 'create daily task': (r) => r.status >= 200 && r.status < 300 });
      return res;
    },
    () => {
      const res = http.get(`${API_BASE}/users/group`, { headers });
      check(res, { 'get users in group': (r) => r.status >= 200 && r.status < 300 });
      return res;
    },
    () => {
      const res = http.get(`${API_BASE}/daily-tasks/stats/current-user?daysBack=7`, { headers });
      check(res, { 'get daily task stats': (r) => r.status >= 200 && r.status < 300 });
      return res;
    },
  ];

  // Execute 3 random operations per iteration
  for (let i = 0; i < 3; i++) {
    const operation = operations[Math.floor(Math.random() * operations.length)];
    operation();
    sleep(0.5);
  }
}


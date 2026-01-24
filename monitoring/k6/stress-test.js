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

const testUsers = [
  { email: 'stresstest1@example.com', password: 'password123', username: 'stresstest1' },
  { email: 'stresstest2@example.com', password: 'password123', username: 'stresstest2' },
  { email: 'stresstest3@example.com', password: 'password123', username: 'stresstest3' },
];

function authenticateUser(user) {
  const loginPayload = JSON.stringify({
    email: user.email,
    password: user.password,
  });

  let loginRes = http.post(`${API_BASE}/auth/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  if (loginRes.status === 200) {
    return JSON.parse(loginRes.body).token;
  }

  const registerPayload = JSON.stringify({
    email: user.email,
    password: user.password,
    username: user.username,
    groupPasscode: 'STRESSTEST',
  });

  const registerRes = http.post(`${API_BASE}/auth/register`, registerPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  if (registerRes.status === 200) {
    return JSON.parse(registerRes.body).token;
  }

  return null;
}

export default function () {
  const user = testUsers[__VU % testUsers.length];
  const token = authenticateUser(user);
  
  if (!token) {
    sleep(1);
    return;
  }

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };

  // Mix of operations
  const operations = [
    () => http.get(`${API_BASE}/tasks`, { headers }),
    () => http.get(`${API_BASE}/daily-tasks`, { headers }),
    () => {
      const payload = JSON.stringify({
        description: `Stress test task ${Date.now()}`,
        dueDate: new Date(Date.now() + 86400000).toISOString(),
      });
      return http.post(`${API_BASE}/tasks`, payload, { headers });
    },
    () => {
      const payload = JSON.stringify({
        description: `Stress test daily task ${Date.now()}`,
      });
      return http.post(`${API_BASE}/daily-tasks`, payload, { headers });
    },
    () => http.get(`${API_BASE}/users/group`, { headers }),
    () => http.get(`${API_BASE}/daily-tasks/stats/current-user?daysBack=7`, { headers }),
  ];

  // Execute random operations
  for (let i = 0; i < 3; i++) {
    const operation = operations[Math.floor(Math.random() * operations.length)];
    const res = operation();
    check(res, {
      'request successful': (r) => r.status >= 200 && r.status < 300,
    });
    sleep(0.5);
  }
}


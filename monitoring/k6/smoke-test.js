import http from 'k6/http';
import { check } from 'k6';

// Smoke test - minimal load to verify system is working
export const options = {
  vus: 1,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://backend:8080';
const API_BASE = `${BASE_URL}/api`;

export default function () {
  // Test health endpoint
  let res = http.get(`${BASE_URL}/actuator/health`);
  check(res, {
    'health check status is 200': (r) => r.status === 200,
  });

  // Test registration
  const registerPayload = JSON.stringify({
    email: `smoketest${Date.now()}@example.com`,
    password: 'password123',
    username: `smoketest${Date.now()}`,
    groupPasscode: 'SMOKETEST',
  });

  res = http.post(`${API_BASE}/auth/register`, registerPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  const token = res.status === 200 ? JSON.parse(res.body).token : null;

  if (token) {
    const headers = {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    };

    // Test getting tasks
    res = http.get(`${API_BASE}/tasks`, { headers });
    check(res, {
      'get tasks works': (r) => r.status === 200,
    });

    // Test creating a task
    const taskPayload = JSON.stringify({
      description: 'Smoke test task',
      dueDate: new Date(Date.now() + 86400000).toISOString(),
    });

    res = http.post(`${API_BASE}/tasks`, taskPayload, { headers });
    check(res, {
      'create task works': (r) => r.status === 202,
    });
  }
}


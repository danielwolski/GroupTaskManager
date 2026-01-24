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
  const uniqueId = `${Date.now()}_${__VU}_${__ITER}`;
  const login = `smoketest${uniqueId}`;
  const password = 'password123';

  const registerPayload = JSON.stringify({
    username: login,
    password: password,
    login: login,
    groupPasscode: 'SMOKETEST',
  });

  console.log(`[REG] Attempting registration for: ${login}`);

  res = http.post(`${API_BASE}/auth/register`, registerPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  console.log(`[REG] Status: ${res.status}, Body: ${res.body}`);

  check(res, {
    'registration status is 200': (r) => r.status === 200,
  });

  // Login to get token
  let token = null;
  if (res.status === 200) {
    const loginPayload = JSON.stringify({
      login: login,
      password: password,
    });

    console.log(`[LOGIN] Attempting login for: ${login}`);

    res = http.post(`${API_BASE}/auth/login`, loginPayload, {
      headers: { 'Content-Type': 'application/json' },
    });

    console.log(`[LOGIN] Status: ${res.status}, Body: ${res.body}`);

    if (res.status === 200) {
      const responseBody = JSON.parse(res.body);
      // Poprawka: używamy access_token zamiast token
      token = responseBody.access_token;
      console.log(`[LOGIN] Token received: ${token ? 'YES' : 'NO'}`);
      if (token) {
        console.log(`[LOGIN] Token (first 50 chars): ${token.substring(0, 50)}...`);
      }
    }

    check(res, {
      'login status is 200': (r) => r.status === 200,
    });
  } else {
    console.log(`[REG] Registration failed, skipping login`);
  }

  if (token) {
    console.log(`[AUTH] Entering authenticated section`);
    
    const headers = {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    };

    // Test getting tasks
    res = http.get(`${API_BASE}/tasks`, { headers });
    console.log(`[TASKS] GET status: ${res.status}`);
    check(res, {
      'get tasks works': (r) => r.status === 200,
    });

    // Test creating a task
    const taskPayload = JSON.stringify({
      description: 'Smoke test task',
      dueDate: new Date(Date.now() + 86400000).toISOString(),
    });

    res = http.post(`${API_BASE}/tasks`, taskPayload, { headers });
    console.log(`[TASKS] POST status: ${res.status}`);
    check(res, {
      'create task works': (r) => r.status === 202,
    });
  } else {
    console.log(`[AUTH] No token available, skipping authenticated tests`);
  }
}


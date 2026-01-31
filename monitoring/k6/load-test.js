import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

// Custom metrics for response time tracking per endpoint
const authRegisterDuration = new Trend('auth_register_duration');
const authLoginDuration = new Trend('auth_login_duration');
const taskGetDuration = new Trend('task_get_duration');
const taskCreateDuration = new Trend('task_create_duration');
const dailyTaskGetDuration = new Trend('daily_task_get_duration');
const dailyTaskCreateDuration = new Trend('daily_task_create_duration');
const reportStatsDuration = new Trend('report_stats_duration');

// Success/Error rates
const authRegisterSuccess = new Rate('auth_register_success');
const authLoginSuccess = new Rate('auth_login_success');
const taskGetSuccess = new Rate('task_get_success');
const taskCreateSuccess = new Rate('task_create_success');
const dailyTaskGetSuccess = new Rate('daily_task_get_success');
const dailyTaskCreateSuccess = new Rate('daily_task_create_success');
const reportStatsSuccess = new Rate('report_stats_success');

// Error counters
const authRegisterErrors = new Counter('auth_register_errors');
const authLoginErrors = new Counter('auth_login_errors');
const taskGetErrors = new Counter('task_get_errors');
const taskCreateErrors = new Counter('task_create_errors');
const dailyTaskGetErrors = new Counter('daily_task_get_errors');
const dailyTaskCreateErrors = new Counter('daily_task_create_errors');
const reportStatsErrors = new Counter('report_stats_errors');

// Load test - gradual increase in load
// Tests microservices architecture (api-gateway, auth-service, task-service, report-service) under normal load conditions
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
    // Global thresholds
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
    http_req_failed: ['rate<0.05'], // Less than 5% errors
    
    // Per endpoint response time thresholds
    'http_req_duration{name:POST /api/auth/register}': ['p(95)<1000', 'p(99)<2000'],
    'http_req_duration{name:POST /api/auth/login}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:GET /api/tasks}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:POST /api/tasks}': ['p(95)<800', 'p(99)<1500'],
    'http_req_duration{name:GET /api/daily-tasks}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:POST /api/daily-tasks}': ['p(95)<800', 'p(99)<1500'],
    'http_req_duration{name:GET /api/reports/stats/current-user}': ['p(95)<1000', 'p(99)<2000'],
    
    // Custom metrics thresholds
    'auth_register_duration': ['p(95)<1000'],
    'auth_login_duration': ['p(95)<500'],
    'task_get_duration': ['p(95)<500'],
    'task_create_duration': ['p(95)<800'],
    'daily_task_get_duration': ['p(95)<500'],
    'daily_task_create_duration': ['p(95)<800'],
    'report_stats_duration': ['p(95)<1000'],
    
    // Success rates
    'auth_register_success': ['rate>0.95'],
    'auth_login_success': ['rate>0.95'],
    'task_get_success': ['rate>0.95'],
    'task_create_success': ['rate>0.90'],
    'daily_task_get_success': ['rate>0.95'],
    'daily_task_create_success': ['rate>0.90'],
    'report_stats_success': ['rate>0.95'],
    
    // Checks - content validation
    checks: ['rate>0.95'],
    'checks{name:auth register has access_token}': ['rate>0.95'],
    'checks{name:auth login has access_token}': ['rate>0.95'],
    'checks{name:get tasks returns array}': ['rate>0.95'],
    'checks{name:create task returns task with id}': ['rate>0.90'],
    'checks{name:get daily tasks returns array}': ['rate>0.95'],
    'checks{name:create daily task returns task with id}': ['rate>0.90'],
    'checks{name:report stats returns valid data}': ['rate>0.95'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://api-gateway:8080';
const API_BASE = `${BASE_URL}/api`;

export function setup() {
  // Verify API Gateway is available
  const healthRes = http.get(`${BASE_URL}/actuator/health`);
  if (healthRes.status !== 200) {
    throw new Error('API Gateway is not available');
  }
  console.log('API Gateway health check passed');
}

// Helper function to validate JSON response structure
function validateJsonResponse(res, expectedFields) {
  if (res.status < 200 || res.status >= 300) {
    return false;
  }
  
  try {
    const body = JSON.parse(res.body);
    if (typeof body !== 'object' || body === null) {
      return false;
    }
    
    // Check if all expected fields exist
    for (const field of expectedFields) {
      if (!(field in body)) {
        return false;
      }
    }
    
    return true;
  } catch (e) {
    return false;
  }
}

// Helper function to validate array response
function validateArrayResponse(res, itemValidator) {
  if (res.status < 200 || res.status >= 300) {
    return false;
  }
  
  try {
    const body = JSON.parse(res.body);
    if (!Array.isArray(body)) {
      return false;
    }
    
    // If itemValidator provided, validate first item
    if (itemValidator && body.length > 0) {
      return itemValidator(body[0]);
    }
    
    return true;
  } catch (e) {
    return false;
  }
}

// Helper function to log failed requests
function logFailure(operation, res, details = '') {
  if (res.status < 200 || res.status >= 300) {
    console.log(`[FAILED] ${operation}: Status=${res.status}, Body=${res.body.substring(0, 200)}${details ? ', ' + details : ''}`);
  }
}

function registerAndLogin() {
  const uniqueId = `${Date.now()}_${__VU}_${__ITER}`;
  const login = `loadtest${uniqueId}@test.com`;
  const password = 'password123';

  // ==================== AUTH: Registration ====================
  const registerPayload = JSON.stringify({
    username: login,
    password: password,
    login: login,
    groupPasscode: 'LOADTEST',
  });

  const registerStartTime = Date.now();
  const registerRes = http.post(`${API_BASE}/auth/register`, registerPayload, {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'POST /api/auth/register' },
  });
  const registerDuration = Date.now() - registerStartTime;
  authRegisterDuration.add(registerDuration);

  const isRegisterSuccess = registerRes.status === 200 && 
    validateJsonResponse(registerRes, ['access_token', 'refresh_token']);
  
  if (isRegisterSuccess) {
    authRegisterSuccess.add(1);
  } else {
    authRegisterErrors.add(1);
    logFailure('AUTH: Registration', registerRes, `Login=${login}`);
    return null;
  }

  check(registerRes, {
    'auth register status is 200': (r) => r.status === 200,
    'auth register has access_token': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.access_token !== undefined && body.access_token !== null;
      } catch (e) {
        return false;
      }
    },
    'auth register has refresh_token': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.refresh_token !== undefined && body.refresh_token !== null;
      } catch (e) {
        return false;
      }
    },
  });

  // ==================== AUTH: Login ====================
  const loginPayload = JSON.stringify({
    login: login,
    password: password,
  });

  const loginStartTime = Date.now();
  const loginRes = http.post(`${API_BASE}/auth/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'POST /api/auth/login' },
  });
  const loginDuration = Date.now() - loginStartTime;
  authLoginDuration.add(loginDuration);

  const isLoginSuccess = loginRes.status === 200 && 
    validateJsonResponse(loginRes, ['access_token', 'refresh_token']);
  
  if (isLoginSuccess) {
    authLoginSuccess.add(1);
  } else {
    authLoginErrors.add(1);
    logFailure('AUTH: Login', loginRes, `Login=${login}`);
    return null;
  }

  check(loginRes, {
    'auth login status is 200': (r) => r.status === 200,
    'auth login has access_token': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.access_token !== undefined && body.access_token !== null && body.access_token.length > 0;
      } catch (e) {
        return false;
      }
    },
    'auth login has refresh_token': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.refresh_token !== undefined && body.refresh_token !== null;
      } catch (e) {
        return false;
      }
    },
  });

  const responseBody = JSON.parse(loginRes.body);
  return responseBody.access_token;
}

export default function () {
  // Register and login
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

  // ==================== TASK: Get Tasks ====================
  const taskGetStartTime = Date.now();
  let res = http.get(`${API_BASE}/tasks`, { 
    headers,
    tags: { name: 'GET /api/tasks' },
  });
  const taskGetDurationMs = Date.now() - taskGetStartTime;
  taskGetDuration.add(taskGetDurationMs);

  const isTaskGetSuccess = (res.status === 200 || res.status === 202) && 
    validateArrayResponse(res, (item) => {
      return item.id !== undefined && 
             item.description !== undefined && 
             typeof item.done === 'boolean';
    });

  if (isTaskGetSuccess) {
    taskGetSuccess.add(1);
  } else {
    taskGetErrors.add(1);
    logFailure('TASK: GET /tasks', res);
  }

  check(res, {
    'get tasks status is 200 or 202': (r) => r.status === 200 || r.status === 202,
    'get tasks returns array': (r) => {
      try {
        const body = JSON.parse(r.body);
        return Array.isArray(body);
      } catch (e) {
        return false;
      }
    },
    'get tasks has valid task structure': (r) => {
      try {
        const body = JSON.parse(r.body);
        if (!Array.isArray(body) || body.length === 0) return true; // Empty array is OK
        const firstTask = body[0];
        return firstTask.id !== undefined && 
               firstTask.description !== undefined && 
               typeof firstTask.done === 'boolean';
      } catch (e) {
        return false;
      }
    },
  });

  // ==================== TASK: Create Task ====================
  const taskPayload = JSON.stringify({
    description: `Load test task ${Date.now()}`,
    dueDate: new Date(Date.now() + 86400000).toISOString(),
  });

  const taskCreateStartTime = Date.now();
  res = http.post(`${API_BASE}/tasks`, taskPayload, { 
    headers,
    tags: { name: 'POST /api/tasks' },
  });
  const taskCreateDurationMs = Date.now() - taskCreateStartTime;
  taskCreateDuration.add(taskCreateDurationMs);

  const isTaskCreateSuccess = (res.status === 200 || res.status === 202) && 
    validateJsonResponse(res, ['id']);

  if (isTaskCreateSuccess) {
    taskCreateSuccess.add(1);
  } else {
    taskCreateErrors.add(1);
    logFailure('TASK: POST /tasks', res, `Payload=${taskPayload}`);
  }

  check(res, {
    'create task status is 200 or 202': (r) => r.status === 200 || r.status === 202,
    'create task returns task with id': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.id !== undefined && body.id !== null;
      } catch (e) {
        return false;
      }
    },
    'create task has description': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.description !== undefined && body.description !== null;
      } catch (e) {
        return false;
      }
    },
  });

  // ==================== TASK: Get Daily Tasks ====================
  const dailyTaskGetStartTime = Date.now();
  res = http.get(`${API_BASE}/daily-tasks`, { 
    headers,
    tags: { name: 'GET /api/daily-tasks' },
  });
  const dailyTaskGetDurationMs = Date.now() - dailyTaskGetStartTime;
  dailyTaskGetDuration.add(dailyTaskGetDurationMs);

  const isDailyTaskGetSuccess = res.status === 200 && 
    validateArrayResponse(res, (item) => {
      return item.id !== undefined && 
             item.description !== undefined && 
             typeof item.done === 'boolean' &&
             item.currentDay !== undefined;
    });

  if (isDailyTaskGetSuccess) {
    dailyTaskGetSuccess.add(1);
  } else {
    dailyTaskGetErrors.add(1);
    logFailure('TASK: GET /daily-tasks', res);
  }

  check(res, {
    'get daily tasks status is 200 or 202': (r) => r.status === 200 || r.status === 202,
    'get daily tasks returns array': (r) => {
      try {
        const body = JSON.parse(r.body);
        return Array.isArray(body);
      } catch (e) {
        return false;
      }
    },
    'get daily tasks has valid structure': (r) => {
      try {
        const body = JSON.parse(r.body);
        if (!Array.isArray(body) || body.length === 0) return true;
        const firstTask = body[0];
        return firstTask.id !== undefined && 
               firstTask.description !== undefined && 
               typeof firstTask.done === 'boolean' &&
               firstTask.currentDay !== undefined;
      } catch (e) {
        return false;
      }
    },
  });

  // ==================== TASK: Create Daily Task ====================
  const dailyTaskPayload = JSON.stringify({
    description: `Load test daily task ${Date.now()}`,
  });

  const dailyTaskCreateStartTime = Date.now();
  res = http.post(`${API_BASE}/daily-tasks`, dailyTaskPayload, { 
    headers,
    tags: { name: 'POST /api/daily-tasks' },
  });
  const dailyTaskCreateDurationMs = Date.now() - dailyTaskCreateStartTime;
  dailyTaskCreateDuration.add(dailyTaskCreateDurationMs);

  const isDailyTaskCreateSuccess = (res.status === 200 || res.status === 202) && 
    validateJsonResponse(res, ['id']);

  if (isDailyTaskCreateSuccess) {
    dailyTaskCreateSuccess.add(1);
  } else {
    dailyTaskCreateErrors.add(1);
    logFailure('TASK: POST /daily-tasks', res, `Payload=${dailyTaskPayload}`);
  }

  check(res, {
    'create daily task status is 200 or 202': (r) => r.status === 200 || r.status === 202,
    'create daily task returns task with id': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.id !== undefined && body.id !== null;
      } catch (e) {
        return false;
      }
    },
    'create daily task has description': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.description !== undefined && body.description !== null;
      } catch (e) {
        return false;
      }
    },
  });

  // ==================== REPORT-SERVICE: Get Stats ====================
  const reportStatsStartTime = Date.now();
  res = http.get(`${API_BASE}/reports/stats/current-user?daysBack=7`, { 
    headers,
    tags: { name: 'GET /api/reports/stats/current-user' },
  });
  const reportStatsDurationMs = Date.now() - reportStatsStartTime;
  reportStatsDuration.add(reportStatsDurationMs);

  const isReportStatsSuccess = res.status === 200 && 
    validateJsonResponse(res, []); // DailyTaskStats - just check it's valid JSON

  if (isReportStatsSuccess) {
    reportStatsSuccess.add(1);
  } else {
    reportStatsErrors.add(1);
    logFailure('REPORT-SERVICE: GET /reports/stats/current-user', res);
  }

  check(res, {
    'report stats status is 200': (r) => r.status === 200,
    'report stats returns valid data': (r) => {
      try {
        const body = JSON.parse(r.body);
        return typeof body === 'object' && body !== null;
      } catch (e) {
        return false;
      }
    },
  });

  sleep(1);
}

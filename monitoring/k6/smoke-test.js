import http from 'k6/http';
import { check } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

// Custom metrics for response time tracking per endpoint
const authRegisterDuration = new Trend('auth_register_duration');
const authLoginDuration = new Trend('auth_login_duration');
const taskGetDuration = new Trend('task_get_duration');
const taskCreateDuration = new Trend('task_create_duration');
const dailyTaskGetDuration = new Trend('daily_task_get_duration');
const dailyTaskCreateDuration = new Trend('daily_task_create_duration');
const reportPdfDuration = new Trend('report_pdf_duration');
const reportStatsDuration = new Trend('report_stats_duration');

// Success/Error rates
const authRegisterSuccess = new Rate('auth_register_success');
const authLoginSuccess = new Rate('auth_login_success');
const taskGetSuccess = new Rate('task_get_success');
const taskCreateSuccess = new Rate('task_create_success');
const dailyTaskGetSuccess = new Rate('daily_task_get_success');
const dailyTaskCreateSuccess = new Rate('daily_task_create_success');
const reportPdfSuccess = new Rate('report_pdf_success');
const reportStatsSuccess = new Rate('report_stats_success');

// Error counters
const authRegisterErrors = new Counter('auth_register_errors');
const authLoginErrors = new Counter('auth_login_errors');
const taskGetErrors = new Counter('task_get_errors');
const taskCreateErrors = new Counter('task_create_errors');
const dailyTaskGetErrors = new Counter('daily_task_get_errors');
const dailyTaskCreateErrors = new Counter('daily_task_create_errors');
const reportPdfErrors = new Counter('report_pdf_errors');
const reportStatsErrors = new Counter('report_stats_errors');

// Smoke test - minimal load to verify system is working
// Tests monolithic backend application endpoints: auth, tasks, daily-tasks, reports
export const options = {
  vus: 1,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
    
    // Per endpoint response time thresholds
    'http_req_duration{name:POST /api/auth/register}': ['p(95)<1000', 'p(99)<2000'],
    'http_req_duration{name:POST /api/auth/login}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:GET /api/tasks}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:POST /api/tasks}': ['p(95)<800', 'p(99)<1500'],
    'http_req_duration{name:GET /api/daily-tasks}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:POST /api/daily-tasks}': ['p(95)<800', 'p(99)<1500'],
    'http_req_duration{name:GET /api/daily-tasks/report/pdf}': ['p(95)<3000', 'p(99)<5000'],
    'http_req_duration{name:GET /api/daily-tasks/stats/current-user}': ['p(95)<1000', 'p(99)<2000'],
    
    // Custom metrics thresholds
    'auth_register_duration': ['p(95)<1000'],
    'auth_login_duration': ['p(95)<500'],
    'task_get_duration': ['p(95)<500'],
    'task_create_duration': ['p(95)<800'],
    'daily_task_get_duration': ['p(95)<500'],
    'daily_task_create_duration': ['p(95)<800'],
    'report_pdf_duration': ['p(95)<3000'],
    'report_stats_duration': ['p(95)<1000'],
    
    // Success rates
    'auth_register_success': ['rate>0.90'],
    'auth_login_success': ['rate>0.95'],
    'task_get_success': ['rate>0.95'],
    'task_create_success': ['rate>0.90'],
    'daily_task_get_success': ['rate>0.95'],
    'daily_task_create_success': ['rate>0.90'],
    'report_pdf_success': ['rate>0.85'],
    'report_stats_success': ['rate>0.95'],
    
    // Checks - content validation
    checks: ['rate>0.90'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://backend:8080';
const API_BASE = `${BASE_URL}/api`;

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

export default function () {
  // ==================== HEALTH CHECK ====================
  let res = http.get(`${BASE_URL}/actuator/health`);
  if (res.status !== 200) {
    logFailure('Health Check', res);
  }
  check(res, {
    'health check status is 200': (r) => r.status === 200,
  });

  // ==================== AUTH-SERVICE: Registration ====================
  const uniqueId = `${Date.now()}_${__VU}_${__ITER}`;
  const login = `smoketest${uniqueId}@test.com`;
  const password = 'password123';

  const registerPayload = JSON.stringify({
    username: login,
    password: password,
    login: login,
    groupPasscode: 'SMOKETEST',
  });

  console.log(`[AUTH-SERVICE] Attempting registration for: ${login}`);

  const registerStartTime = Date.now();
  res = http.post(`${API_BASE}/auth/register`, registerPayload, {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'POST /api/auth/register' },
  });
  const registerDuration = Date.now() - registerStartTime;
  authRegisterDuration.add(registerDuration);

  const isRegisterSuccess = res.status === 200 && 
    validateJsonResponse(res, ['access_token', 'refresh_token']);
  
  if (isRegisterSuccess) {
    authRegisterSuccess.add(1);
  } else {
    authRegisterErrors.add(1);
    logFailure('AUTH-SERVICE: Registration', res, `Login=${login}`);
  }

  check(res, {
    'registration status is 200': (r) => r.status === 200,
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

  // ==================== AUTH-SERVICE: Login ====================
  let token = null;
  let userLogin = null;
  
  if (res.status === 200) {
    const loginPayload = JSON.stringify({
      login: login,
      password: password,
    });

    console.log(`[AUTH-SERVICE] Attempting login for: ${login}`);

    const loginStartTime = Date.now();
    res = http.post(`${API_BASE}/auth/login`, loginPayload, {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'POST /api/auth/login' },
    });
    const loginDuration = Date.now() - loginStartTime;
    authLoginDuration.add(loginDuration);

    const isLoginSuccess = res.status === 200 && 
      validateJsonResponse(res, ['access_token', 'refresh_token']);
    
    if (isLoginSuccess) {
      authLoginSuccess.add(1);
    } else {
      authLoginErrors.add(1);
      logFailure('AUTH-SERVICE: Login', res, `Login=${login}`);
    }

    if (res.status === 200) {
      const responseBody = JSON.parse(res.body);
      token = responseBody.access_token;
      userLogin = login; // Store login for X-User-Login header
      console.log(`[AUTH-SERVICE] Login successful, token received: ${token ? 'YES' : 'NO'}`);
    } else {
      console.log(`[AUTH-SERVICE] Login failed, skipping remaining tests`);
    }

    check(res, {
      'login status is 200': (r) => r.status === 200,
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
  } else {
    console.log(`[AUTH-SERVICE] Registration failed, skipping login and remaining tests`);
  }

  if (!token || !userLogin) {
    console.log(`[SKIP] No token available, skipping authenticated tests`);
    return;
  }

  console.log(`[AUTH] Entering authenticated section for user: ${userLogin}`);
  
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };

  // ==================== TASK-SERVICE: Get Tasks ====================
  console.log(`[TASK-SERVICE] Getting tasks for user: ${userLogin}`);
  const taskGetStartTime = Date.now();
  res = http.get(`${API_BASE}/tasks`, { 
    headers,
    tags: { name: 'GET /api/tasks' },
  });
  const taskGetDurationMs = Date.now() - taskGetStartTime;
  taskGetDuration.add(taskGetDurationMs);

  const isTaskGetSuccess = res.status === 200 && 
    validateArrayResponse(res, (item) => {
      return item.id !== undefined && 
             item.description !== undefined && 
             typeof item.done === 'boolean';
    });

  if (isTaskGetSuccess) {
    taskGetSuccess.add(1);
  } else {
    taskGetErrors.add(1);
    logFailure('TASK-SERVICE: GET /tasks', res, `User=${userLogin}`);
  }

  check(res, {
    'get tasks works': (r) => r.status === 200 || r.status === 202,
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

  // ==================== TASK-SERVICE: Create Task ====================
  const taskPayload = JSON.stringify({
    description: 'Smoke test task',
    dueDate: new Date(Date.now() + 86400000).toISOString(),
  });

  console.log(`[TASK-SERVICE] Creating task for user: ${userLogin}`);
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
    logFailure('TASK-SERVICE: POST /tasks', res, `User=${userLogin}, Payload=${taskPayload}`);
  }

  check(res, {
    'create task works': (r) => r.status === 200 || r.status === 202,
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

  // ==================== TASK-SERVICE: Get Daily Tasks ====================
  console.log(`[TASK-SERVICE] Getting daily tasks for user: ${userLogin}`);
  const dailyTaskGetStartTime = Date.now();
  res = http.get(`${API_BASE}/daily-tasks`, { 
    headers,
    tags: { name: 'GET /api/daily-tasks' },
  });
  const dailyTaskGetDurationMs = Date.now() - dailyTaskGetStartTime;
  dailyTaskGetDuration.add(dailyTaskGetDurationMs);

  const isDailyTaskGetSuccess = (res.status === 200 || res.status === 202) && 
    validateArrayResponse(res, (item) => {
      return item.id !== undefined && 
             item.description !== undefined && 
             typeof item.done === 'boolean';
    });

  if (isDailyTaskGetSuccess) {
    dailyTaskGetSuccess.add(1);
  } else {
    dailyTaskGetErrors.add(1);
    logFailure('TASK-SERVICE: GET /daily-tasks', res, `User=${userLogin}`);
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
               typeof firstTask.done === 'boolean';
      } catch (e) {
        return false;
      }
    },
  });

  // ==================== TASK-SERVICE: Create Daily Task ====================
  const dailyTaskPayload = JSON.stringify({
    description: 'Smoke test daily task',
  });

  console.log(`[TASK-SERVICE] Creating daily task for user: ${userLogin}`);
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
    logFailure('TASK-SERVICE: POST /daily-tasks', res, `User=${userLogin}, Payload=${dailyTaskPayload}`);
  }

  check(res, {
    'create daily task works': (r) => r.status === 200 || r.status === 202,
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
  console.log(`[REPORT-SERVICE] Getting stats for user: ${userLogin}`);
  const reportStatsStartTime = Date.now();
  res = http.get(`${API_BASE}/daily-tasks/stats/current-user?daysBack=7`, { 
    headers,
    tags: { name: 'GET /api/daily-tasks/stats/current-user' },
  });
  const reportStatsDurationMs = Date.now() - reportStatsStartTime;
  reportStatsDuration.add(reportStatsDurationMs);

  const isReportStatsSuccess = res.status === 200 && 
    validateJsonResponse(res, []); // DailyTaskStats - just check it's valid JSON

  if (isReportStatsSuccess) {
    reportStatsSuccess.add(1);
  } else {
    reportStatsErrors.add(1);
    logFailure('REPORT-SERVICE: GET /reports/stats/current-user', res, `User=${userLogin}`);
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

  // ==================== REPORT-SERVICE: Generate PDF Report ====================
  console.log(`[REPORT-SERVICE] Generating PDF report for user: ${userLogin}`);
  const reportPdfStartTime = Date.now();
  res = http.get(`${API_BASE}/daily-tasks/report/pdf?daysBack=7`, { 
    headers,
    tags: { name: 'GET /api/daily-tasks/report/pdf' },
  });
  const reportPdfDurationMs = Date.now() - reportPdfStartTime;
  reportPdfDuration.add(reportPdfDurationMs);

  const isReportPdfSuccess = res.status === 200 && 
    res.headers['Content-Type'] && 
    res.headers['Content-Type'].includes('application/pdf') &&
    res.body.length > 0;

  if (isReportPdfSuccess) {
    reportPdfSuccess.add(1);
  } else {
    reportPdfErrors.add(1);
    logFailure('REPORT-SERVICE: GET /reports/pdf', res, `User=${userLogin}, daysBack=7`);
  }

  check(res, {
    'generate PDF report works': (r) => r.status === 200,
    'report pdf has correct content type': (r) => {
      const contentType = r.headers['Content-Type'];
      return contentType && contentType.includes('application/pdf');
    },
    'report pdf has content': (r) => r.body && r.body.length > 0,
  });

  if (res.status === 200) {
    console.log(`[REPORT-SERVICE] PDF report generated successfully, size: ${res.body.length} bytes`);
  }

  console.log(`[SUCCESS] All smoke tests completed for user: ${userLogin}`);
}

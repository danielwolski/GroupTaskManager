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

// Stress test - gradually increase load to find breaking point
// Tests monolithic backend application under increasing load
// Measures response time per endpoint and validates response content
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
    // Global thresholds
    http_req_duration: ['p(95)<2000', 'p(99)<5000'],
    http_req_failed: ['rate<0.1'], // Allow up to 10% errors for stress test
    
    // Per endpoint response time thresholds
    'http_req_duration{name:POST /api/auth/register}': ['p(95)<1000', 'p(99)<2000'],
    'http_req_duration{name:POST /api/auth/login}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:GET /api/tasks}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:POST /api/tasks}': ['p(95)<800', 'p(99)<1500'],
    'http_req_duration{name:GET /api/daily-tasks}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:POST /api/daily-tasks}': ['p(95)<800', 'p(99)<1500'],
    'http_req_duration{name:GET /api/daily-tasks/report/pdf}': ['p(95)<3000', 'p(99)<5000'], // PDF generation is slower
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
    'auth_register_success': ['rate>0.90'], // 90% success rate
    'auth_login_success': ['rate>0.95'],
    'task_get_success': ['rate>0.95'],
    'task_create_success': ['rate>0.90'],
    'daily_task_get_success': ['rate>0.95'],
    'daily_task_create_success': ['rate>0.90'],
    'report_pdf_success': ['rate>0.85'], // PDF can be slower, more tolerant
    'report_stats_success': ['rate>0.95'],
    
    // Checks - content validation
    checks: ['rate>0.90'],
    'checks{name:auth register has access_token}': ['rate>0.90'],
    'checks{name:auth login has access_token}': ['rate>0.95'],
    'checks{name:get tasks returns array}': ['rate>0.95'],
    'checks{name:create task returns task with id}': ['rate>0.90'],
    'checks{name:get daily tasks returns array}': ['rate>0.95'],
    'checks{name:create daily task returns task with id}': ['rate>0.90'],
    'checks{name:report pdf has correct content type}': ['rate>0.85'],
    'checks{name:report stats returns valid data}': ['rate>0.95'],
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

function registerAndLogin() {
  const uniqueId = `${Date.now()}_${__VU}_${__ITER}`;
  const login = `stresstest${uniqueId}@test.com`;
  const password = 'password123';

  // ==================== AUTH-SERVICE: Registration ====================
  const registerPayload = JSON.stringify({
    username: login,
    password: password,
    login: login,
    groupPasscode: 'STRESSTEST',
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
    console.log(`[FAILED] AUTH-SERVICE: Registration failed - Status=${registerRes.status}, Body=${registerRes.body.substring(0, 200)}`);
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

  // ==================== AUTH-SERVICE: Login ====================
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
    console.log(`[FAILED] AUTH-SERVICE: Login failed - Status=${loginRes.status}, Body=${loginRes.body.substring(0, 200)}`);
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
  return {
    token: responseBody.access_token,
    login: login,
  };
}

// Configuration for request distribution
// These weights control how often each endpoint is called
const REQUEST_WEIGHTS = {
  taskGet: 100,           // Always call (100% probability)
  taskCreate: 80,         // 80% probability
  dailyTaskGet: 100,      // Always call (100% probability)
  dailyTaskCreate: 70,    // 70% probability
  reportStats: 60,        // 60% probability
  reportPdf: 15,          // Only 15% probability - PDF is very resource-intensive
};

// Helper function to generate random integer between min and max (inclusive)
function randomIntBetween(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

// Helper function to decide if an action should be performed based on weight
function shouldPerform(weight) {
  return randomIntBetween(1, 100) <= weight;
}

// Helper function to get random number of task operations (1-3)
function getTaskOperationCount() {
  return randomIntBetween(1, 3);
}

export default function () {
  // Register and login - always performed
  const authResult = registerAndLogin();

  if (!authResult || !authResult.token) {
    console.log('[AUTH] Failed to get token, skipping iteration');
    sleep(1);
    return;
  }

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${authResult.token}`,
  };

  // ==================== TASK-SERVICE OPERATIONS ====================
  // Task operations are performed with varying frequency
  const taskOperations = getTaskOperationCount();
  
  for (let i = 0; i < taskOperations; i++) {
    // Randomly choose between regular tasks and daily tasks
    const useDailyTasks = randomIntBetween(1, 100) <= 50; // 50% chance for daily tasks
    
    if (useDailyTasks) {
      // ==================== TASK-SERVICE: Get Daily Tasks ====================
      if (shouldPerform(REQUEST_WEIGHTS.dailyTaskGet)) {
        const dailyTaskGetStartTime = Date.now();
        let res = http.get(`${API_BASE}/daily-tasks`, { 
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
          console.log(`[FAILED] TASK-SERVICE: GET /daily-tasks - Status=${res.status}, Body=${res.body.substring(0, 200)}`);
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
        
        sleep(randomIntBetween(100, 500) / 1000); // Random sleep 0.1-0.5s
      }

      // ==================== TASK-SERVICE: Create Daily Task ====================
      if (shouldPerform(REQUEST_WEIGHTS.dailyTaskCreate)) {
        const dailyTaskPayload = JSON.stringify({
          description: `Stress test daily task ${Date.now()}_${__VU}_${__ITER}`,
        });

        const dailyTaskCreateStartTime = Date.now();
        let res = http.post(`${API_BASE}/daily-tasks`, dailyTaskPayload, { 
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
          console.log(`[FAILED] TASK-SERVICE: POST /daily-tasks - Status=${res.status}, Body=${res.body.substring(0, 200)}`);
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
        
        sleep(randomIntBetween(100, 500) / 1000); // Random sleep 0.1-0.5s
      }
    } else {
      // ==================== TASK-SERVICE: Get Tasks ====================
      if (shouldPerform(REQUEST_WEIGHTS.taskGet)) {
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
          console.log(`[FAILED] TASK-SERVICE: GET /tasks - Status=${res.status}, Body=${res.body.substring(0, 200)}`);
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
        
        sleep(randomIntBetween(100, 500) / 1000); // Random sleep 0.1-0.5s
      }

      // ==================== TASK-SERVICE: Create Task ====================
      if (shouldPerform(REQUEST_WEIGHTS.taskCreate)) {
        const taskPayload = JSON.stringify({
          description: `Stress test task ${Date.now()}_${__VU}_${__ITER}`,
          dueDate: new Date(Date.now() + 86400000).toISOString(),
        });

        const taskCreateStartTime = Date.now();
        let res = http.post(`${API_BASE}/tasks`, taskPayload, { 
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
          console.log(`[FAILED] TASK-SERVICE: POST /tasks - Status=${res.status}, Body=${res.body.substring(0, 200)}`);
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
        
        sleep(randomIntBetween(100, 500) / 1000); // Random sleep 0.1-0.5s
      }
    }
  }

  // ==================== REPORT-SERVICE: Get Stats ====================
  // Stats endpoint is called more frequently than PDF
  if (shouldPerform(REQUEST_WEIGHTS.reportStats)) {
    const reportStatsStartTime = Date.now();
    let res = http.get(`${API_BASE}/daily-tasks/stats/current-user?daysBack=7`, { 
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
      console.log(`[FAILED] REPORT-SERVICE: GET /reports/stats/current-user - Status=${res.status}, Body=${res.body.substring(0, 200)}`);
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
    
    sleep(randomIntBetween(200, 800) / 1000); // Random sleep 0.2-0.8s
  }

  // ==================== REPORT-SERVICE: Generate PDF ====================
  // PDF generation is very resource-intensive, so only 15% of users will generate it
  if (shouldPerform(REQUEST_WEIGHTS.reportPdf)) {
    const reportPdfStartTime = Date.now();
    let res = http.get(`${API_BASE}/daily-tasks/report/pdf?daysBack=7`, { 
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
      console.log(`[FAILED] REPORT-SERVICE: GET /reports/pdf - Status=${res.status}, Content-Type=${res.headers['Content-Type']}, Body length=${res.body.length}`);
    }

    check(res, {
      'report pdf status is 200': (r) => r.status === 200,
      'report pdf has correct content type': (r) => {
        const contentType = r.headers['Content-Type'];
        return contentType && contentType.includes('application/pdf');
      },
      'report pdf has content': (r) => r.body && r.body.length > 0,
    });
    
    sleep(randomIntBetween(500, 1500) / 1000); // Longer sleep after PDF generation (0.5-1.5s)
  }

  // Random sleep at the end to simulate user thinking time
  sleep(randomIntBetween(200, 1000) / 1000); // 0.2-1.0s
}

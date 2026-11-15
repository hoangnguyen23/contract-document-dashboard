/**
 * E2E Workflow Test Suite - Contract Governance Dashboard
 * Conforms strictly to API Contract B1.3.0 and RFC 9457
 * Tests full end-to-end integration against Spring Boot API at http://127.0.0.1:8081
 */

import assert from 'node:assert/strict';

const BASE_URL = process.env.API_BASE_URL || 'http://127.0.0.1:8081';
const DEMO_PASSWORD = process.env.CG_DEMO_PASSWORD || 'TfYnSqjKLV70F9oSVCuCiPf0KFqHm7f_M7tAnYRMWrM';

// Minimal valid PDF binary conforming to application/pdf magic bytes
const MINIMAL_PDF_BYTES = Buffer.from(
  '%PDF-1.4\n' +
  '1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n' +
  '2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n' +
  '3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\n' +
  'xref\n0 4\n0000000000 65535 f \n0000000010 00000 n \n0000000060 00000 n \n0000000117 00000 n \n' +
  'trailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n187\n%%EOF'
);

const colors = {
  reset: '\x1b[0m',
  bold: '\x1b[1m',
  green: '\x1b[32m',
  red: '\x1b[31m',
  yellow: '\x1b[33m',
  cyan: '\x1b[36m',
  dim: '\x1b[2m',
};

let testsPassed = 0;
let testsFailed = 0;

async function runStep(name, fn) {
  process.stdout.write(`  ${colors.cyan}▶${colors.reset} ${name}... `);
  try {
    const start = performance.now();
    await fn();
    const duration = Math.round(performance.now() - start);
    console.log(`${colors.green}✓ PASS${colors.reset} ${colors.dim}(${duration}ms)${colors.reset}`);
    testsPassed++;
  } catch (err) {
    console.log(`${colors.red}✗ FAIL${colors.reset}`);
    console.error(`    ${colors.red}${err.message}${colors.reset}`);
    if (err.stack) {
      console.error(`    ${colors.dim}${err.stack.split('\n').slice(1, 4).join('\n    ')}${colors.reset}`);
    }
    testsFailed++;
  }
}

// Client helper managing cookies and CSRF
class ApiSession {
  constructor(username, password) {
    this.username = username;
    this.password = password;
    this.cookie = null;
    this.csrfToken = null;
    this.user = null;
  }

  async init() {
    const res = await fetch(`${BASE_URL}/api/v1/session`);
    assert.equal(res.status, 200, `Expected 200 from /api/v1/session, got ${res.status}`);
    this.cookie = res.headers.get('set-cookie');
    const data = await res.json();
    assert.ok(data.csrfToken, 'Missing csrfToken in session response');
    this.csrfToken = data.csrfToken;
  }

  async login() {
    await this.init();
    const res = await fetch(`${BASE_URL}/api/v1/session/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-CSRF-Token': this.csrfToken,
        'Cookie': this.cookie,
      },
      body: JSON.stringify({
        username: this.username,
        password: this.password,
      }),
    });

    assert.equal(res.status, 200, `Login failed for ${this.username}: status ${res.status}`);
    const newCookie = res.headers.get('set-cookie');
    if (newCookie) this.cookie = newCookie;

    const data = await res.json();
    assert.equal(data.authenticated, true, 'Expected authenticated = true');
    assert.ok(data.user, 'Missing user object in login response');
    this.csrfToken = data.csrfToken;
    this.user = data.user;
    return data;
  }

  async request(path, options = {}) {
    const headers = {
      ...(options.headers || {}),
      'Cookie': this.cookie,
    };

    if (['POST', 'PUT', 'DELETE'].includes(options.method?.toUpperCase())) {
      headers['X-CSRF-Token'] = this.csrfToken;
      if (!headers['Content-Type'] && !(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
      }
    }

    const res = await fetch(`${BASE_URL}${path}`, {
      ...options,
      headers,
    });

    const newCookie = res.headers.get('set-cookie');
    if (newCookie) this.cookie = newCookie;

    return res;
  }
}

// Global test variables
let makerSession;
let checkerSession;
let adminSession;
let sampleRelationshipId;
let createdDossierId;
let createdDossierEtag;

async function runE2E() {
  console.log(`\n${colors.bold}${colors.cyan}=======================================================`);
  console.log(` 🚀 STARTING FULL E2E WORKFLOW TEST (API B1.3.0)`);
  console.log(` Target API: ${BASE_URL}`);
  console.log(`=======================================================${colors.reset}\n`);

  // --- 1. AUTHENTICATION & CSRF PROTECTION ---
  console.log(`${colors.bold}[1/7] Authentication & Security Protocol${colors.reset}`);

  await runStep('Reject unsafe requests without CSRF token (403 CSRF_INVALID)', async () => {
    const res = await fetch(`${BASE_URL}/api/v1/session/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: 'demo.maker', password: DEMO_PASSWORD }),
    });
    assert.equal(res.status, 403, 'Should reject with 403 without CSRF token');
    const problem = await res.json();
    assert.equal(problem.code, 'CSRF_INVALID');
    assert.ok(problem.traceId, 'Should include RFC9457 traceId');
  });

  await runStep('Maker login (demo.maker with MAKER role)', async () => {
    makerSession = new ApiSession('demo.maker', DEMO_PASSWORD);
    const data = await makerSession.login();
    assert.equal(data.user.username, 'demo.maker');
    assert.ok(data.user.roles.includes('MAKER'), 'Expected role MAKER');
  });

  await runStep('Checker login (demo.checker with CHECKER role)', async () => {
    checkerSession = new ApiSession('demo.checker', DEMO_PASSWORD);
    const data = await checkerSession.login();
    assert.equal(data.user.username, 'demo.checker');
    assert.ok(data.user.roles.includes('CHECKER'), 'Expected role CHECKER');
  });

  await runStep('Admin login (demo.admin with BANK_ADMIN role)', async () => {
    adminSession = new ApiSession('demo.admin', DEMO_PASSWORD);
    const data = await adminSession.login();
    assert.equal(data.user.username, 'demo.admin');
    assert.ok(data.user.roles.includes('BANK_ADMIN'), 'Expected role BANK_ADMIN');
  });

  // --- 2. RELATIONSHIPS & DOSSIERS WORKFLOW ---
  console.log(`\n${colors.bold}[2/7] Relationships & Dossier Lifecycle (B1.3.0)${colors.reset}`);

  await runStep('Query relationships with cursor-based pagination', async () => {
    const res = await makerSession.request('/api/v1/relationships?limit=10');
    assert.equal(res.status, 200);
    const data = await res.json();
    assert.ok(Array.isArray(data.items), 'Expected items array');
    assert.ok('nextCursor' in data, 'Expected nextCursor property');
    assert.ok('asOf' in data, 'Expected asOf ISO timestamp');
    assert.ok('timelineProcessingLagSeconds' in data, 'Expected timelineProcessingLagSeconds');
    assert.ok(data.items.length > 0, 'Expected at least 1 seeded relationship');
    sampleRelationshipId = data.items[0].id;
  });

  await runStep('Create Draft Dossier conforming strictly to B1.3.0 schema', async () => {
    const payload = {
      relationshipId: sampleRelationshipId,
      linkedFromDossierId: null,
      documents: [],
      externalAccountReference: 'ACC-E2E-TEST',
      terms: {
        clauseVersion: 'NEGOTIATED-VA-1',
        currency: 'VND',
        endExclusive: '2027-12-31',
        feePayer: 'BENEFICIARY',
        feeVnd: '750000',
        pricingMode: 'NEGOTIATED_FIXED',
        startOn: '2026-11-15',
        termKind: 'FIXED',
      },
      representation: {
        basisType: 'LEGAL_REPRESENTATIVE',
        personName: 'Nguyễn Văn Kiểm Thử',
        scopeText: 'Toàn quyền ký kết theo Giấy phép kinh doanh',
        validFrom: '2026-01-01',
        validTo: null,
        sourceFileId: null,
        issuerReference: 'GPKD-999999',
        checkedAt: null,
        checkEvidenceFileId: null,
        title: 'Giám Đốc Điều Hành',
        knownRevocationAt: null,
      },
    };

    const idempotencyKey = crypto.randomUUID();
    const res = await makerSession.request('/api/v1/dossiers', {
      method: 'POST',
      headers: {
        'Idempotency-Key': idempotencyKey,
      },
      body: JSON.stringify(payload),
    });

    assert.equal(res.status, 201, `Failed to create dossier: ${res.status}`);
    createdDossierEtag = res.headers.get('etag');
    assert.match(createdDossierEtag, /^"r-[1-9][0-9]*"$/, 'ETag must match pattern ^"r-[1-9][0-9]*"$');

    const dossier = await res.json();
    createdDossierId = dossier.id;
    assert.equal(dossier.state, 'DRAFT');
    assert.equal(dossier.version, 1);
    assert.ok(Array.isArray(dossier.allowedActions), 'Must return allowedActions');
    assert.ok(Array.isArray(dossier.blockedActions), 'Must return blockedActions');
    assert.ok(dossier.allowedActions.includes('UPDATE'));
    assert.ok(dossier.allowedActions.includes('CANCEL'));
  });

  await runStep('Query Reviewer Options dynamically for created Dossier', async () => {
    const res = await makerSession.request(`/api/v1/reviewer-options?targetKind=DOSSIER&targetId=${createdDossierId}`);
    assert.equal(res.status, 200, `Reviewer options query failed: ${res.status}`);
    const data = await res.json();
    assert.ok('asOf' in data, 'Expected asOf in ReviewerOptions');
    assert.ok(Array.isArray(data.items), 'Expected items array in ReviewerOptions');
    for (const item of data.items) {
      assert.ok(item.userId, 'ReviewerOption missing userId');
      assert.ok(item.displayName, 'ReviewerOption missing displayName');
      assert.ok(['L1', 'L2'].includes(item.level), `Invalid reviewer level: ${item.level}`);
    }
  });

  // --- 3. CONCURRENCY CONTROL & ERROR HANDLING (RFC 9457) ---
  console.log(`\n${colors.bold}[3/7] Concurrency Control & RFC 9457 Problem Details${colors.reset}`);

  await runStep('Stale If-Match returns 412 PRECONDITION_FAILED with currentVersion', async () => {
    const updatePayload = {
      documents: [],
      externalAccountReference: 'ACC-E2E-UPDATED',
      terms: {
        clauseVersion: 'NEGOTIATED-VA-1',
        currency: 'VND',
        endExclusive: '2027-12-31',
        feePayer: 'BENEFICIARY',
        feeVnd: '800000',
        pricingMode: 'NEGOTIATED_FIXED',
        startOn: '2026-11-15',
        termKind: 'FIXED',
      },
      representation: {
        basisType: 'LEGAL_REPRESENTATIVE',
        personName: 'Nguyễn Văn Kiểm Thử',
        scopeText: 'Toàn quyền ký kết theo Giấy phép kinh doanh',
        validFrom: '2026-01-01',
        validTo: null,
        sourceFileId: null,
        issuerReference: 'GPKD-999999',
        checkedAt: null,
        checkEvidenceFileId: null,
        title: 'Giám Đốc Điều Hành',
        knownRevocationAt: null,
      },
    };

    // Pass outdated revision "r-999"
    const res = await makerSession.request(`/api/v1/dossiers/${createdDossierId}`, {
      method: 'PUT',
      headers: {
        'If-Match': '"r-999"',
        'Idempotency-Key': crypto.randomUUID(),
      },
      body: JSON.stringify(updatePayload),
    });

    assert.equal(res.status, 412, `Expected 412 PRECONDITION_FAILED, got ${res.status}`);
    const problem = await res.json();
    assert.equal(problem.code, 'PRECONDITION_FAILED');
    assert.equal(problem.currentVersion, 1, 'Problem must expose currentVersion = 1');
  });

  await runStep('Invalid schema payload returns 422 VALIDATION_FAILED with field violations', async () => {
    const invalidPayload = {
      documents: [],
      externalAccountReference: 'ACC',
      terms: {
        clauseVersion: 'INVALID_CLAUSE', // invalid enum
        currency: 'USD',                 // must be VND
        endExclusive: 'invalid-date',
        feePayer: 'INVALID_PAYER',
        feeVnd: '-1000',                // invalid VND pattern
        pricingMode: 'UNKNOWN',
        startOn: 'invalid-date',
        termKind: 'UNKNOWN',
      },
      representation: {},
      extraFieldThatIsNotAllowed: true,  // additionalProperties: false
    };

    const res = await makerSession.request(`/api/v1/dossiers/${createdDossierId}`, {
      method: 'PUT',
      headers: {
        'If-Match': createdDossierEtag,
        'Idempotency-Key': crypto.randomUUID(),
      },
      body: JSON.stringify(invalidPayload),
    });

    assert.equal(res.status, 422, 'Expected 422 VALIDATION_FAILED');
    const problem = await res.json();
    assert.equal(problem.code, 'VALIDATION_FAILED');
    assert.ok(Array.isArray(problem.violations), 'Expected violations array');
    assert.ok(problem.violations.length > 0, 'Expected at least one violation');
  });

  await runStep('Successful Dossier Update with valid If-Match', async () => {
    const updatePayload = {
      documents: [],
      externalAccountReference: 'ACC-E2E-SUCCESS-UPDATE',
      terms: {
        clauseVersion: 'NEGOTIATED-VA-1',
        currency: 'VND',
        endExclusive: '2027-12-31',
        feePayer: 'BENEFICIARY',
        feeVnd: '850000',
        pricingMode: 'NEGOTIATED_FIXED',
        startOn: '2026-11-15',
        termKind: 'FIXED',
      },
      representation: {
        basisType: 'LEGAL_REPRESENTATIVE',
        personName: 'Nguyễn Văn Kiểm Thử',
        scopeText: 'Toàn quyền ký kết theo Giấy phép kinh doanh',
        validFrom: '2026-01-01',
        validTo: null,
        sourceFileId: null,
        issuerReference: 'GPKD-999999',
        checkedAt: null,
        checkEvidenceFileId: null,
        title: 'Giám Đốc Điều Hành',
        knownRevocationAt: null,
      },
    };

    const res = await makerSession.request(`/api/v1/dossiers/${createdDossierId}`, {
      method: 'PUT',
      headers: {
        'If-Match': createdDossierEtag,
        'Idempotency-Key': crypto.randomUUID(),
      },
      body: JSON.stringify(updatePayload),
    });

    assert.equal(res.status, 200, `Expected 200 from PUT dossier, got ${res.status}`);
    createdDossierEtag = res.headers.get('etag');
    assert.equal(createdDossierEtag, '"r-2"', 'ETag should increment to "r-2"');
    const updated = await res.json();
    assert.equal(updated.version, 2);
    assert.equal(updated.externalAccountReference, 'ACC-E2E-SUCCESS-UPDATE');
  });

  // --- 4. DOCUMENTS & SCANNER INTEGRATION ---
  console.log(`\n${colors.bold}[4/7] Document Management & ClamAV Antivirus Scanner${colors.reset}`);

  let uploadedFileId;
  await runStep('Upload clean PDF document via /api/v1/files and scan via ClamAV', async () => {
    const formData = new FormData();
    const pdfBlob = new Blob([MINIMAL_PDF_BYTES], { type: 'application/pdf' });
    formData.append('file', pdfBlob, 'e2e-evidence.pdf');
    formData.append('purpose', 'SERVICE_APPLICATION');
    formData.append('targetKind', 'DOSSIER');
    formData.append('targetId', createdDossierId);
    formData.append('branchId', '00000000-0000-4000-8000-000000000001');

    const res = await makerSession.request('/api/v1/files', {
      method: 'POST',
      headers: {
        'Idempotency-Key': crypto.randomUUID(),
      },
      body: formData,
    });

    assert.equal(res.status, 201, `Expected 201 from file upload, got ${res.status}`);
    const doc = await res.json();
    assert.ok(doc.id, 'Expected document ID');
    assert.equal(doc.mediaType, 'application/pdf');
    uploadedFileId = doc.id;
  });

  await runStep('Wait for ClamAV scan worker to mark document as CLEAN', async () => {
    let state = 'SCANNING';
    for (let i = 0; i < 6; i++) {
      await new Promise((resolve) => setTimeout(resolve, 800));
      const res = await makerSession.request(`/api/v1/files/${uploadedFileId}`);
      assert.equal(res.status, 200);
      const fileData = await res.json();
      state = fileData.state;
      if (state === 'CLEAN') break;
    }
    assert.equal(state, 'CLEAN', 'Document should be marked CLEAN by ClamAV scanner worker');
  });

  await runStep('Download uploaded file stream via /api/v1/files/{id}/content?representation=FULL', async () => {
    const res = await makerSession.request(`/api/v1/files/${uploadedFileId}/content?representation=FULL`);
    assert.equal(res.status, 200, `Expected 200 from download, got ${res.status}`);
    const buffer = await res.arrayBuffer();
    assert.equal(buffer.byteLength, MINIMAL_PDF_BYTES.length, 'Downloaded byte size matches uploaded PDF');
  });

  // --- 5. APPROVAL ROUNDS & SNAPSHOT VERIFICATION ---
  console.log(`\n${colors.bold}[5/7] Approval Rounds, Snapshots & Checker Governance${colors.reset}`);

  let sampleRoundId;
  await runStep('Query approval rounds as Checker with cursor pagination', async () => {
    const res = await checkerSession.request('/api/v1/rounds?limit=10');
    assert.equal(res.status, 200, `Expected 200 from /rounds, got ${res.status}`);
    const data = await res.json();
    assert.ok(Array.isArray(data.items), 'Expected rounds items array');
    assert.ok('asOf' in data, 'Expected asOf field');
    if (data.items.length > 0) {
      sampleRoundId = data.items[0].id;
    }
  });

  if (sampleRoundId) {
    await runStep(`Query Round Snapshot for round ${sampleRoundId.slice(0, 8)}...`, async () => {
      const res = await checkerSession.request(`/api/v1/rounds/${sampleRoundId}/snapshot`);
      assert.equal(res.status, 200, `Expected 200 from round snapshot, got ${res.status}`);
      const snapshot = await res.json();
      assert.ok(snapshot.roundId, 'Snapshot must have roundId');
      assert.ok(snapshot.targetKind, 'Snapshot must have targetKind');
    });
  }

  // --- 6. NOTICES & CONTRACTS WORKFLOW ---
  console.log(`\n${colors.bold}[6/7] Notices Full Workflow & Contract Readiness${colors.reset}`);

  let sampleNoticeId;
  await runStep('Query notices with cursor pagination', async () => {
    const res = await checkerSession.request('/api/v1/notices?limit=10');
    assert.equal(res.status, 200, `Expected 200 from /notices, got ${res.status}`);
    const data = await res.json();
    assert.ok(Array.isArray(data.items), 'Expected notices items array');
    if (data.items.length > 0) {
      sampleNoticeId = data.items[0].id;
    }
  });

  if (sampleNoticeId) {
    await runStep(`Inspect Local Mailbox Capture for notice ${sampleNoticeId.slice(0, 8)}...`, async () => {
      const res = await checkerSession.request(`/api/v1/notices/${sampleNoticeId}/capture`);
      assert.ok([200, 404].includes(res.status), `Expected 200 or 404 from mailbox capture, got ${res.status}`);
    });
  }

  await runStep('Query contracts and inspect readiness governance', async () => {
    const res = await makerSession.request('/api/v1/contracts?limit=10');
    assert.equal(res.status, 200, `Expected 200 from /contracts, got ${res.status}`);
    const data = await res.json();
    assert.ok(Array.isArray(data.items), 'Expected contracts items array');
  });

  // --- 7. TEARDOWN & DOSSIER COMMAND ---
  console.log(`\n${colors.bold}[7/7] Lifecycle Completion & Teardown${colors.reset}`);

  await runStep('Fetch latest Dossier ETag before command execution', async () => {
    const res = await makerSession.request(`/api/v1/dossiers/${createdDossierId}`);
    assert.equal(res.status, 200, `Expected 200 fetching dossier, got ${res.status}`);
    createdDossierEtag = res.headers.get('etag');
    assert.ok(createdDossierEtag, 'Must receive valid ETag');
  });

  await runStep('Execute CANCEL command on test dossier', async () => {
    const cancelPayload = {
      action: 'CANCEL',
      reason: 'Dọn dẹp hồ sơ sau khi hoàn thành E2E Workflow Test',
    };

    const res = await makerSession.request(`/api/v1/dossiers/${createdDossierId}/commands`, {
      method: 'POST',
      headers: {
        'If-Match': createdDossierEtag,
        'Idempotency-Key': crypto.randomUUID(),
      },
      body: JSON.stringify(cancelPayload),
    });

    assert.equal(res.status, 200, `Expected 200 from CANCEL command, got ${res.status}`);
    const cancelled = await res.json();
    assert.equal(cancelled.state, 'CANCELLED');
  });

  // Summary
  console.log(`\n${colors.bold}${colors.cyan}=======================================================`);
  console.log(` 📊 E2E WORKFLOW TEST SUMMARY`);
  console.log(` Tests Passed: ${colors.green}${testsPassed}${colors.cyan}`);
  console.log(` Tests Failed: ${testsFailed === 0 ? colors.green + '0' : colors.red + testsFailed}${colors.cyan}`);
  console.log(` Total Checks: ${testsPassed + testsFailed}`);
  console.log(`=======================================================${colors.reset}\n`);

  if (testsFailed > 0) {
    process.exit(1);
  }
}

runE2E().catch((err) => {
  console.error('Fatal test error:', err);
  process.exit(1);
});

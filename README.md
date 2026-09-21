# payment-idempotency-poc

POC built after interview question on preventing double charges.

### Problem
User double-clicks checkout -> don't double-charge Stripe.

### Solution
**React (TS)** generates `Idempotency-Key: crypto.randomUUID()` and sends in header.

**Spring Boot + Postgres:**
- Table `idempotency_keys (key PK, status, payment_id)`
- On request: `INSERT  ... status=PENDING ON CONFLICT DO NOTHING`
- If key exists and status=PENDING -> return `409 Conflict` (already processing)
- If key exists and status=SUCCESS -> return `200` + derive from payment_id
- Else call FakeGateway (sleep 2s), save result, return 200

**Spring Boot Payment API**
- `FakeGatewayClient.charge()` with `Thread.sleep(2000)` to simulate Stripe latency, returns `fake_` + UUID
- No live Stripe call in this POC. Prod would be `stripe.PaymentIntent.create(..., idempotency_key=key)

**Reconciliation:**
- Cron job checks idempotency_keys table; if status=PENDING > 5min, mark FAILED or re-check gateway by gatewayId. In prod, lookup by idemptoency_key in Stripe.

### How to run
- `docker-compose up` (postgres), `./gradlew bootRun`, `cd frontend && npm run dev`

### Why 409 vs 200?
PENDING = 409 tells client to wait/poll. Completed = 200 derived from payment_id prevents second Stripe charge. 

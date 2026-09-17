# payment-idempotency-poc

POC built after interview question on preventing double charges.

### Problem
User double-clicks checkout -> don't double-charge Stripe.

### Solution
**React (TS)** generates `Idempotency-Key: crypto.randomUUID()` and sends in header.

**Spring Boot + Postgres:**
- Table `idempotency_keys (key PK, status, response_json)`
- On request: `INSERT  ... status=PENDING ON CONFLICT DO NOTHING`
- If key exists and status=PENDING -> return `409 Conflict` (already processing)
- If key exists and status=SUCCESS -> return `200` + cached `response_json`
- Else call Lambda, save result, return 200

**Python Lambda (AWS):**
- Recieves amount + idempotency_key
- Calls
- `stripe.PaymentIntent.create(..., idempotency_key=key)

**Reconciliation:**
- Cron job checks Stripe by idempotency_key if server crashed while PENDING

### How to run
- `docker-compose up` (postgres), `./gradlew bootRun`, `cd frontend && npm run dev`

### How to run
`docker-compose up` (postgres), `./gradlew bootRun`, `cd frontend && npm run dev`

### Why 409 vs 200?
PENDING = 409 tells client to wait/poll. Completed = 200 with cached result prevents second Stripe charge. 

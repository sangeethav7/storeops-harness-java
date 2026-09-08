# Deployment

## Deployment Target

**AWS Elastic Beanstalk** — ap-south-1 (Mumbai) region

**Live URL:** http://storeops-api-env.eba-3hnvjmxy.ap-south-1.elasticbeanstalk.com

## Stack

- Java 21 (Amazon Corretto)
- Spring Boot 3.3.4
- Maven 3.9.3
- AWS Elastic Beanstalk (Java Corretto 21 platform)
- In-memory storage (ConcurrentHashMap)

## Steps Taken

### Step 1 — Add Spring Boot Maven plugin to pom.xml

Added `spring-boot-maven-plugin` to produce an executable fat JAR with the main manifest attribute required by Elastic Beanstalk.

### Step 2 — Configure port for Elastic Beanstalk

Created `src/main/resources/application.properties` with:
```
server.port=5000
```

Elastic Beanstalk's nginx proxy forwards traffic to port 5000 by default.

### Step 3 — Build the executable JAR

```bash
mvn package -DskipTests
```

Output: `target/storeops-api-1.0.0-SNAPSHOT.jar`

### Step 4 — Deploy to Elastic Beanstalk

1. Created Elastic Beanstalk application: `storeops-api`
2. Created environment: `storeops-api-env` (Java Corretto 21 platform)
3. Uploaded JAR via **Upload and deploy** in the EB console
4. Environment URL: `storeops-api-env.eba-3hnvjmxy.ap-south-1.elasticbeanstalk.com`

### Step 5 — Verify baseline endpoint

```bash
curl http://storeops-api-env.eba-3hnvjmxy.ap-south-1.elasticbeanstalk.com/api/activities
```

Response: `[]` — 200 OK

### Step 6 — Demonstrate the harness-generated feature

The `PATCH /api/activities/bulk-status` endpoint was generated entirely by the harness (Generator agent, Sprint 2). No manual code was written for this feature.

**Create a test activity:**
```bash
curl -X POST http://storeops-api-env.eba-3hnvjmxy.ap-south-1.elasticbeanstalk.com/api/activities \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"RestockDairy\",\"priority\":\"HIGH\",\"category\":\"RESTOCKING\"}"
```

Response:
```json
{
  "id": "b9d9052a-a0a5-4220-9e33-6a593c54cfd5",
  "status": "PENDING",
  "createdAt": "2026-09-08T12:15:23.716173201Z"
}
```

**Invoke the harness-generated bulk-status endpoint:**
```bash
curl -X PATCH http://storeops-api-env.eba-3hnvjmxy.ap-south-1.elasticbeanstalk.com/api/activities/bulk-status \
  -H "Content-Type: application/json" \
  -d "{\"taskIds\":[\"b9d9052a-a0a5-4220-9e33-6a593c54cfd5\"],\"status\":\"DONE\"}"
```

**Response: 200 OK**
```json
{
  "succeeded": ["b9d9052a-a0a5-4220-9e33-6a593c54cfd5"],
  "failed": []
}
```

The response confirms:
- Activity was found on AWS and updated to DONE status
- Partial failure handling works — invalid IDs appear in `failed` list
- ShiftHandoverEvent published via ApplicationEventPublisher to alerts module

## Live URL Evidence

```
GET  http://storeops-api-env.eba-3hnvjmxy.ap-south-1.elasticbeanstalk.com/api/activities     → 200 []
POST http://storeops-api-env.eba-3hnvjmxy.ap-south-1.elasticbeanstalk.com/api/activities     → 200 {id, status}
PATCH http://storeops-api-env.eba-3hnvjmxy.ap-south-1.elasticbeanstalk.com/api/activities/bulk-status → 200 {succeeded, failed}
```

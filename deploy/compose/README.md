# alexandra-job-tracker (compose)

## 📜 Summary

---

This environment is based on `docker-compose` and is designed for development purposes.

It runs the job-tracker server with a full observability stack (OpenTelemetry collector, Prometheus, Grafana, Tempo, Loki) and container monitoring (cadvisor). The server can be built in **JVM** (default) or **native** mode.

## 🏗️ Architecture

---

Docker Compose orchestrates containerized services, making it ideal for local development and testing.

![High Throughput Docker](./images/ajt-docker-compose-diagram.svg)

## 🚀 Usage

---

### Using Scripts

```bash
cd deploy/compose

# Start with JVM build (default)
./start.sh

# Start with native-image build
./start.sh native

# Stop and clean up
./stop.sh

# Stop and also remove built images
./stop.sh removeImages=true
```

## 🔗 Links

---

* **Job Tracker (API):**
  * [GraphQL API](http://localhost:8880/api/graphql)
  * [GraphiQL IDE](http://localhost:8880/api/graphiql)
  * [H2 Console](http://localhost:8880/api/h2-console) (JDBC URL: `jdbc:h2:file:./deploy/data/jobtracker;AUTO_SERVER=TRUE`, user: `sa`, password: _blank_)
  * [Actuator health](http://localhost:8880/api/actuator/health)
  * [Prometheus metrics](http://localhost:8880/api/actuator/prometheus)
* **Prometheus (Metrics Storage):**
  * [Prometheus dashboard](http://localhost:9090)
* **Tempo (Distributed Tracing):**
  * [Tempo search](http://localhost:3200/status)
* **Loki (Log Aggregation):**
  * [Loki dashboard](http://localhost:3100/services/loki)
* **Grafana (Visualization):**
  * [Grafana dashboard](http://localhost:3000) (admin / admin)
* **Cadvisor (Container Monitoring):**
  * [Cadvisor dashboard](http://localhost:8080)

## 🧪 How to Validate the Changes

---

```bash
# Check server health
curl -s http://localhost:8880/api/actuator/health | jq .

# Verify all services are running
docker compose ps

# Query GraphQL API
curl -s -X POST http://localhost:8880/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "{ __schema { queryType { name } } }"}'

# Check Prometheus targets
curl -s http://localhost:9090/api/v1/targets | jq '.data.activeTargets[].labels.job'

# Verify traces reach Tempo
curl -s http://localhost:3200/api/echo
```

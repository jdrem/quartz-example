## Quartz Microservice Example

### Building and Running
Start a docker instance of mysql:
```bash
docker run --name mysql-quartz \
-p 3306:3306 \
-e MYSQL_ROOT_PASSWORD=Welcome1 \
-e MYSQL_DATABASE=quartz \
-e MYSQL_USER=quser \
-e MYSQL_PASSWORD=Welcome1 \
-d mysql:latest
```
Build everything:
```bash
./gradlew clean build
```
Untar the result:
```bash
cd app/build/distrubtions
tar xf app.tar
cd app
```
Run an instance on port 8084:
```bash
java -cp "lib/*" -Dserver.port=8084 net.remgant.quartz.Application
```
### Creating, Viewing and Deleting Events.
Create an event. Specify a **startDateTime** for the event to be scheduled in the future. If absent, the event will be triggered immediately. Everything else will be passed to the Quartz job.
```bash
curl -verbose -H "Content-Type: application/json" \
http://localhost:8084/api/schedule/event \
-d '{"startDateTime":"2023-12-03T08:30:00-05:00", "a":"xyz","b":3.14159, "c":true}'
```
Gives the result:
```json
{"group":"DEFAULT","name":"J5e245237-8304-498f-a256-3f67ba43b600"}
```
See what's scheduled:
```bash
curl http://localhost:8084/api/schedules
```
Gives the result:
```json lines
{"results":[{"jobName":"J5e245237-8304-498f-a256-3f67ba43b600","nextFireTime":"2023-12-03T12:20:00Z"}]}
```
Delete a scheduled event:
```bash
curl -X DELETE http://localhost:8084/schedule/event/J5e245237-8304-498f-a256-3f67ba43b600
```
Returns a 204 and no data on success.
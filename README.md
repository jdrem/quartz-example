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
Create an event. Specify a **triggerTime** for the event to be scheduled in the future. If absent, the event will be triggered immediately. Everything else will be passed to the Quartz job.
```bash
curl -H "Content-Type: application/json" \
http://localhost:8084/api/schedule/deactivate/device \
-d '{"triggerTime":"2023-12-18T19:54:00-05:00","deviceId":"123456"}'
```
Gives the result:
```json
{"id":"f3d0b3ed-0d5f-4d43-8695-4c475d926b8f"}
```
See what's scheduled:
```bash
curl http://localhost:8084/api/schedule/events
```
Gives the result:
```json lines
[{"id":"f3d0b3ed-0d5f-4d43-8695-4c475d926b8f","triggerTime":"2023-12-19T00:54:00",
  "eventClass":"net.remgant.quartz.DeactivateDeviceEvent","eventImplementation":
  {"triggerTime":1.70294724E9,"deviceId":"123456"}}]
```
Delete a scheduled event:
```bash
curl -X DELETE http://localhost:8084/api/schedule/event/a4b2e4df-a964-410a-8f89-8f19c7b1562c
```
Returns a 204 and no data on success.
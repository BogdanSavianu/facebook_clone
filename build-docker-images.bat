@echo off
echo Building Docker images for all services...

docker build -t facebook-api-gateway ./apiGateway
docker build -t facebook-auth-service ./authService
docker build -t facebook-user-service ./userService
docker build -t facebook-social-graph-service ./socialGraphService
docker build -t facebook-content-service ./contentService
docker build -t facebook-group-service ./groupService
docker build -t facebook-messaging-service ./messagingService
docker build -t facebook-event-service ./eventService

echo Docker images build completed! 
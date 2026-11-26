docker stop runneringsis
docker rm runneringsis  
docker rmi runneringsis:latest
docker build --no-cache -t runneringsis:latest .
docker run -d --name runneringsis -p 8080:8080 -e NEW_RELIC_LICENSE_KEY=$NEW_RELIC_LICENSE_KEY -e NEW_RELIC_APP_NAME=RunnerIngsis runneringsis:latest
docker logs -f runneringsis






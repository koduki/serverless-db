# serverless-db project

This is serveless RDB on CloudRun.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
export GOOGLE_APPLICATION_CREDENTIALS=${YOUR_CREDENTIALS_PATH}
SERVERLESSDB_BUCKETNAME=${YOUR_BUCKET_NAME} ./mvnw quarkus:dev
```

## Packaging and running on local

```bash
./mvnw clean package
docker build -t gcr.io/${YOUR_PROJECT_ID}/serverless-db -f src/main/docker/Dockerfile.jvm .
docker run -it -e GOOGLE_APPLICATION_CREDENTIALS=$GOOGLE_APPLICATION_CREDENTIALS -e SERVERLESSDB_BUCKETNAME=${YOUR_BUCKET_NAME} -p 8080:8080 gcr.io/${YOUR_PROJECT_ID}/serverless-db
```

## Deploy to CloudRun

```bash
docker push gcr.io/${YOUR_PROJECT_ID}/serverless-db
gcloud run deploy --image gcr.io/${YOUR_PROJECT_ID}/serverless-db --region us-west1 --platform managed --service-name serverless-db --set-env-vars=SERVERLESSDB_BUCKETNAME=${YOUR_BUCKET_NAME}
```
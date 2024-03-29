#!/usr/bin/env bash
set -e
set -o pipefail

export APP_NAME=joshlong-tanzu-developer-center-blog-cronjob
export SECRETS=${APP_NAME}-secrets
export SECRETS_FN=$HOME/${SECRETS}
export IMAGE_NAME=gcr.io/${GCLOUD_PROJECT}/${APP_NAME}

docker rmi -f $IMAGE_NAME
cd $ROOT_DIR
./mvnw -DskipTests=true spring-javaformat:apply clean package spring-boot:build-image -Dspring-boot.build-image.imageName=$IMAGE_NAME
docker push $IMAGE_NAME

echo $IMAGE_NAME

touch $SECRETS_FN
echo writing to "$SECRETS_FN "
cat <<EOF >${SECRETS_FN}
GH_PAT=${GH_PAT}
EOF
kubectl delete secrets $SECRETS || echo "no secrets to delete."
kubectl create secret generic $SECRETS --from-env-file $SECRETS_FN
kubectl delete -f $ROOT_DIR/deploy/k8s/deployment.yaml || echo "couldn't delete the deployment as there was nothing deployed."
kubectl apply -f $ROOT_DIR/deploy/k8s

sleep 5

echo "running the cronjob once to kick things off..."

kubectl create job --from=cronjob/${APP_NAME} ${APP_NAME}-run-${RANDOM}


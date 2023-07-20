docker build -t data-retrieval .
docker run --name data-retrieval-test data-retrieval:latest lein test
docker run --name data-retrieval-job data-retrieval:latest lein run -m data-retrieval.core
kubectl create secret generic github --from-env-file=deployments/secrets/github.txt
kubectl create secret generic papercliff-core --from-env-file=deployments/secrets/papercliff-core.txt
kubectl create secret generic rapidapi --from-env-file=deployments/secrets/rapidapi.txt
kubectl apply -f deployments/cronjob.yaml
kubectl create job --from=cronjob/data-retrieval-cronjob data-retrieval-manual-job

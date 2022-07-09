# Feed Processor 2.0

This is meant to be a logical successor to [the original feed processor](https://github.com/developer-advocacy/feed-processor). this pulls in data from the GraphQL API for my blog, joshlong.com, and then makes it available as Markdown and HTML in the `developer-advocacy/activity-feed` repository, in the `output` branch.  


It's deployed as a `CronJob` on my Kubernetes cluster that runs periodically. You can kick off an instance of the `CronJob` manually with 

```shell
kubectl create job --from=cronjob/activity-feed-cronjob  activity-feed-cronjob-run-$RANDOM
```

##  A Github Personal Access Token 

Make sure when you provision it that the token supports `repo`, `admin:org`, `delete_repo`, and `project`.
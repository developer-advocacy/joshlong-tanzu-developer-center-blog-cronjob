# Tanzu Developer Center Feed Processor 2.0

This is meant to be a logical successor to [the original feeds processor](https://github.com/developer-advocacy/feed-processor). 

The program pulls in data from [the GraphQL API for my blog](https://api.joshlong.com/graphiql), and then makes it available as Markdown and HTML in the `developer-advocacy/activity-feeds` repository, in the `output` branch. This way, an automatic process in the [Tanzu Developer Center](https://github.com/vmware-tanzu/tanzu-dev-portal) can periodically pull in the new feeds.   

The program also pulls down the latest-and-greatest blogs that [I've written on the official Spring blog](https://spring.io/blog) and submits a Github _pull request_ to the Tanzu Developer Center Github with all the new changes. 

It's deployed as a [`CronJob`](https://kubernetes.io/docs/concepts/workloads/controllers/cron-jobs/) on my Kubernetes cluster that runs periodically. You can kick off an instance of the `CronJob` manually with 

```shell
kubectl create job --from=cronjob/activity-feeds-cronjob  activity-feeds-cronjob-run-$RANDOM
```



##  A Github Personal Access Token 

Make sure when you provision it that the token supports `repo`, `admin:org`, `delete_repo`, and `project`.


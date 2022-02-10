# Introduction

This chart bootstraps an shardingproxy deployment on a Kubernetes cluster using the Helm package manager. 

## Prerequisites

+ Kubernetes 1.6+
+ Helm

## Installing the Chart

- [helm charts values.yaml](shardingproxy/values.yaml)

To install the chart with the release name `shardingproxy`:

+ From github

```bash
git clone https://github.com/wl4g/xcloud-shardingproxy.git
cd xcloud-shardingproxy/charts/shardingproxy/
helm -n app-rds install shardingproxy .
```

+ or, From chart repos

```bash
helm repo add shardingproxy https://helm-repo.wl4g.io/charts
helm -n app-rds install shardingproxy wl4g/shardingproxy
```

> If you want to install an unstable version, you need to add `--devel` when you execute the `helm install` command.

+ Veifying example

```bash
$ kind version
kind v0.11.1 go1.16.4 linux/amd64

$ kubectl get no,deploy,po,svc,ep -owide
NAME                                 STATUS   ROLES                  AGE     VERSION   INTERNAL-IP   EXTERNAL-IP   OS-IMAGE       KERNEL-VERSION     CONTAINER-RUNTIME
node/kind-kubernetes-control-plane   Ready    control-plane,master   4h17m   v1.21.1   172.18.0.2    <none>        Ubuntu 21.04   5.4.0-88-generic   containerd://1.5.2

NAME                            READY   UP-TO-DATE   AVAILABLE   AGE   CONTAINERS      IMAGES                      SELECTOR
deployment.apps/shardingproxy   2/2     2            2           12m   shardingproxy   wl4g/shardingproxy:latest   app.kubernetes.io/instance=shardingproxy,app.kubernetes.io/name=shardingproxy

NAME                                 READY   STATUS    RESTARTS   AGE   IP            NODE                            NOMINATED NODE   READINESS GATES
pod/shardingproxy-57b9c84888-4m45q   1/1     Running   0          12m   10.244.0.50   kind-kubernetes-control-plane   <none>           <none>
pod/shardingproxy-57b9c84888-s8vcb   1/1     Running   0          12m   10.244.0.49   kind-kubernetes-control-plane   <none>           <none>

NAME                                       TYPE        CLUSTER-IP    EXTERNAL-IP   PORT(S)                       AGE     SELECTOR
service/jaeger-shardingproxy-headless      ClusterIP   None          <none>        <none>                        12m     <none>
service/kubernetes                         ClusterIP   10.96.0.1     <none>        443/TCP                       4h17m   <none>
service/mysql0-shardingproxy-headless      ClusterIP   None          <none>        <none>                        12m     <none>
service/mysql1-shardingproxy-headless      ClusterIP   None          <none>        <none>                        12m     <none>
service/mysql2-shardingproxy-headless      ClusterIP   None          <none>        <none>                        12m     <none>
service/shardingproxy                      ClusterIP   10.96.77.82   <none>        3308/TCP,8080/TCP,10108/TCP   12m     app.kubernetes.io/instance=shardingproxy,app.kubernetes.io/name=shardingproxy
service/zookeeper-shardingproxy-headless   ClusterIP   None          <none>        <none>                        12m     <none>

NAME                                         ENDPOINTS                                                          AGE
endpoints/jaeger-shardingproxy-headless      10.0.0.10:14250                                                    12m
endpoints/kubernetes                         172.18.0.2:6443                                                    4h17m
endpoints/mysql0-shardingproxy-headless      10.0.0.10:33060                                                    12m
endpoints/mysql1-shardingproxy-headless      10.0.0.10:33061                                                    12m
endpoints/mysql2-shardingproxy-headless      10.0.0.10:33062                                                    12m
endpoints/shardingproxy                      10.244.0.49:10108,10.244.0.50:10108,10.244.0.49:3308 + 1 more...   12m
endpoints/zookeeper-shardingproxy-headless   10.0.0.10:2181                                                     12m

$ kubectl port-forward --address 0.0.0.0 service/shardingproxy 3308:3308 &
Forwarding from 0.0.0.0:3308 -> 3308

$ echo 'show databases;' | mysql -h10.0.0.150 -P3308 -uwarehouse_ops0 -p123456
mysql: [Warning] Using a password on the command line interface can be insecure.
schema_name
warehousedb
```

> Tips: The above demo environment is a single cluster created by kind-v0.11.1, where `10.0.0.150` is the physical node IP.

> The access network path for the mysql client to query the database is: `10.0.0.150(client nodeIP)` --> `shardingproxy service(clusterIP)` --> `shardingproxy Pods(podsIP)` --> `mysql-shardingproxy-headles service(externalIP)` --> `mysqld`

## Uninstalling the Chart

To uninstall/delete the `shardingproxy` deployment:

```bash
helm del shardingproxy
```

## Configurable

The following table lists the configurable parameters of the shardingproxy chart and their default values.

| Parameter  | Description | Default Value |
| ---        |  ---        | ---           |
| `image.repository` | ShardingProxy Image name |wl4g/shardingproxy|
| `image.pullPolicy`  | The image pull policy  |IfNotPresent|
| `image.pullSecrets`  | The image pull secrets  |`[]` (does not add image pull secrets to deployed pods)|
| `envFromSecret` | The name pull a secret in the same kubernetes namespace which contains values that will be added to the environment | nil |
| `autoscaling.enabled` | Autoscaling enabled status. |true|
| `autoscaling.replicaCount` | Number of pods that are always running. | 2 |
| `persistence.enabled` | Enable shardingproxy persistence using PVC |false|
| `persistence.storageClass` | Storage class of backing PVC |`nil` (uses alpha storage class annotation)|
| `persistence.existingClaim` | ShardingProxy data Persistent Volume existing claim name, evaluated as a template |""|
| `persistence.accessMode` | PVC Access Mode for shardingproxy volume |ReadWriteOnce|
| `persistence.size` | PVC Storage Request for shardingproxy volume |20Mi|
| `resources.enabled` | Enable resource requests/limits |false|
| `resources.limits.cpu` | CPU resource requests/limits |500m|
| `resources.limits.memory` | Memory resource requests/limits |1024Mi|
| `resources.requests.cpu` | CPU resource requests/limits |500m|
| `resources.requests.memory` | Memory resource requests/limits |1024Mi|
| `initContainers` | Containers that run before the creation of shardingproxy containers. They can contain utilities or setup scripts. |`{}`|
| `podSecurityContext.enabled` | Pod security context enabled |true|
| `podSecurityContext.fsGroup` | Pod security fs group |1000|
| `podSecurityContext.fsGroupChangePolicy` | Enable pod security group policy |Always|
| `podSecurityContext.runAsUser` | Enable pod as uid |1000|
| `podSecurityContext.supplementalGroups` | Enable pod security supplemental groups |`[]`1000|
| `containerSecurityContext.enabled` | Enable container security context |false|
| `containerSecurityContext.runAsNonRoot` | Run container as root |true|
| `containerSecurityContext.runAsUser` | Run container as uid |1000|
| `nodeSelector` | Node labels for pod assignment |`{}`|
| `tolerations` | Toleration labels for pod assignment |`[]`|
| `affinity` | Map of node/pod affinities |`{}`|
| `shardingConfigs`  | ShardingProxy sharding configuration. see the [example](https://github.com/wl4g/xcloud-shardingproxy/blob/master/xcloud-shardingproxy-starter/src/main/resources/example/)|`{}`|
| `service.type`  | Kubernetes Service type. |ClusterIP|
| `service.proxyPortPort`  | Port for proxy JDBC. |3308|
| `service.dashboardPortPort`  | Port for dashboard. |18083|
| `service.prometheusPortPort`  | Port for prometheus. |10108|
| `service.nodePorts.proxy`  | Kubernetes node port for proxy JDBC. |  nil  |
| `service.nodePorts.dashboard`  | Kubernetes node port for dashboard. |  nil  |
| `service.nodePorts.prometheus`  | Kubernetes node port for prometheus API. |  nil  |
| `service.loadBalancerIP`  | loadBalancerIP for Service |  nil |
| `service.loadBalancerSourceRanges` |  Address(es) that are allowed when service is LoadBalancer | [] |
| `service.externalIPs` |   ExternalIPs for the service | [] |
| `service.annotations` |   Service annotations | `{}` (evaluated as a template)|
| `ingress.dashboard.enabled` | Enable ingress for shardingproxy Dashboard | false |
| `ingress.dashboard.ingressClassName` |    Set the ingress class for shardingproxy Dashboard |  nginx  |
| `ingress.dashboard.path` | Ingress path for shardingproxy Dashboard |  / |
| `ingress.dashboard.hosts` | Ingress hosts for shardingproxy prometheus API | dashboard.shardingproxy.local |
| `ingress.dashboard.tls` | Ingress tls for shardingproxy prometheus API | [] |
| `ingress.dashboard.annotations` | Ingress annotations for shardingproxy prometheus API | {} |
| `ingress.prometheus.enabled` |  Enable ingress for shardingproxy prometheus API |  false |
| `ingress.prometheus.ingressClassName` |    Set the ingress class for shardingproxy prometheus API |  nginx  |
| `ingress.prometheus.path` | Ingress path for shardingproxy prometheus API |    / |
| `ingress.prometheus.hosts` | Ingress hosts for shardingproxy prometheus API |  prometheus.shardingproxy.local |
| `ingress.prometheus.tls` | Ingress tls for shardingproxy prometheus API |  [] |
| `ingress.prometheus.annotations` | Ingress annotations for shardingproxy prometheus API | {} |

## FAQ

### How to troubleshoot Pods that are missing os tools

- Use ephemeral containers to debug running or crashed Pods: [kubernetes.io/docs/tasks/debug-application-cluster/debug-running-pod](https://kubernetes.io/docs/tasks/debug-application-cluster/debug-running-pod/)

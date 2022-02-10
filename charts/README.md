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

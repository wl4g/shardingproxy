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
cd xcloud-shardingproxy/kubernetes/helm/
helm install shardingproxy .
```

+ From chart repos

```bash
helm repo add shardingproxy https://helm-repo.wl4g.io/charts
helm install shardingproxy wl4g/shardingproxy
```

> If you want to install an unstable version, you need to add `--devel` when you execute the `helm install` command.

## Uninstalling the Chart

To uninstall/delete the `shardingproxy` deployment:

```bash
helm del shardingproxy
```

## Configuration

The following table lists the configurable parameters of the shardingproxy chart and their default values.

| Parameter  | Description | Default Value |
| ---        |  ---        | ---           |
| `replicaCount` | It is recommended to have odd number of nodes in a cluster, otherwise the shardingproxy cluster cannot be automatically healed in case of net-split. |3|
| `image.repository` | ShardingProxy Image name |wl4g/shardingproxy|
| `image.pullPolicy`  | The image pull policy  |IfNotPresent|
| `image.pullSecrets`  | The image pull secrets  |`[]` (does not add image pull secrets to deployed pods)|
| `envFromSecret` | The name pull a secret in the same kubernetes namespace which contains values that will be added to the environment | nil |
| `autoscaling.replicaCount` | Number of pods that are always running. | 2 |
| `persistence.enabled` | Enable shardingproxy persistence using PVC |false|
| `persistence.storageClass` | Storage class of backing PVC |`nil` (uses alpha storage class annotation)|
| `persistence.existingClaim` | ShardingProxy data Persistent Volume existing claim name, evaluated as a template |""|
| `persistence.accessMode` | PVC Access Mode for shardingproxy volume |ReadWriteOnce|
| `persistence.size` | PVC Storage Request for shardingproxy volume |20Mi|
| `initContainers` | Containers that run before the creation of shardingproxy containers. They can contain utilities or setup scripts. |`{}`|
| `resources` | CPU/Memory resource requests/limits |{}|
| `nodeSelector` | Node labels for pod assignment |`{}`|
| `tolerations` | Toleration labels for pod assignment |`[]`|
| `affinity` | Map of node/pod affinities |`{}`|
| `service.type`  | Kubernetes Service type. |ClusterIP|
| `service.proxy`  | Port for proxy JDBC. |3308|
| `service.dashboard`  | Port for dashboard. |18083|
| `service.prometheus`  | Port for prometheus. |10108|
| `service.nodePorts.proxy`  | Kubernetes node port for proxy JDBC. |nil|
| `service.nodePorts.dashboard`  | Kubernetes node port for dashboard. |nil|
| `service.nodePorts.prometheus`  | Kubernetes node port for prometheus API. |nil|
| `service.loadBalancerIP`  | loadBalancerIP for Service |  nil |
| `service.loadBalancerSourceRanges` |  Address(es) that are allowed when service is LoadBalancer | [] |
| `service.externalIPs` |   ExternalIPs for the service |   [] |
| `service.annotations` |   Service annotations |   {}(evaluated as a template)|
| `ingress.dashboard.enabled` | Enable ingress for shardingproxy Dashboard | false |
| `ingress.dashboard.ingressClassName` |    Set the ingress class for shardingproxy Dashboard |   |
| `ingress.dashboard.path` | Ingress path for shardingproxy Dashboard |  / |
| `ingress.dashboard.hosts` | Ingress hosts for shardingproxy prometheus API | dashboard.shardingproxy.local |
| `ingress.dashboard.tls` | Ingress tls for shardingproxy prometheus API | [] |
| `ingress.dashboard.annotations` | Ingress annotations for shardingproxy prometheus API | {} |
| `ingress.prometheus.enabled` |  Enable ingress for shardingproxy prometheus API |  false |
| `ingress.prometheus.ingressClassName` |    Set the ingress class for shardingproxy prometheus API |    |
| `ingress.prometheus.path` | Ingress path for shardingproxy prometheus API |    / |
| `ingress.prometheus.hosts` | Ingress hosts for shardingproxy prometheus API |  prometheus.shardingproxy.local |
| `ingress.prometheus.tls` | Ingress tls for shardingproxy prometheus API |  [] |
| `ingress.prometheus.annotations` | Ingress annotations for shardingproxy prometheus API |  {} |
| `shardingproxyConfig` | shardingproxy configuration item, see the [documentation](https://hub.docker.com/r/wl4g/shardingproxy) | |
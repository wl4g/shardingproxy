# XCloud Component Integration for Shardingsphere Proxy

> It's an enhanced package that integrates shardingsphere-proxy and shardingsphere-scaling

## 1. Deployments

### 1.1 **for Host (Optional)**

- 1.1.1 Compiling installation

```bash
# git clone https://github.com/wl4g/xcloud-component.git
cd xcloud-component
mvn clean install -DskipTests -Dmaven.test.skip=true -T 2C
```

- 1.1.2 Startup shardingproxy

```bash
java -jar shardingproxy-{version}-bin.jar 3308 /example/readwrite
# java -cp xxx com.wl4g.ShardingProxy 3308 /example/readwrite
```

### 1.2 **for Docker (Recommends)**

- Native release for [shardingsphere-proxy](https://github.com/apache/shardingsphere/tree/master/shardingsphere-proxy)

```bash
sudo mkdir -p /mnt/disk1/shardingsphere-proxy/{conf,ext-lib}
sudo mkdir -p /mnt/disk1/log/shardingsphere-proxy/

docker network create --subnet=172.8.8.0/24 mysqlnet

docker run -d \
--name ssp1 \
--net mysqlnet \
--restart no \
-p 3308:3308 \
-v /mnt/disk1/shardingsphere-proxy/conf:/opt/shardingsphere-proxy/conf/ \
-v /mnt/disk1/shardingsphere-proxy/ext-lib/:/opt/shardingsphere-proxy/ext-lib/ \
-v /mnt/disk1/log/shardingsphere-proxy/:/opt/shardingsphere-proxy/logs/ \
-e JVM_OPTS='-Djava.awt.headless=true' \
-e PORT=3308 \
apache/shardingsphere-proxy:5.0.0
```

- Enhanced release for [shardingproxy](https://github.com/wl4g/xcloud-component/tree/2.0.0-RC4-iamgateway/xcloud-component-integration/xcloud-component-integration-shardingproxy) (based on [shardingsphere-proxy](https://github.com/apache/shardingsphere/tree/master/shardingsphere-proxy))

```bash
sudo mkdir -p /mnt/disk1/shardingproxy/{conf,ext-lib}
sudo mkdir -p /mnt/disk1/log/shardingproxy/

docker network create --subnet=172.8.8.0/24 mysqlnet

docker run -d \
--name sp1 \
--net mysqlnet \
--restart no \
-p 3308:3308 \
-v /mnt/disk1/shardingproxy/conf:/opt/apps/ecm/shardingproxy-package.shardingsproxy-master-bin/conf/ \
-v /mnt/disk1/shardingproxy/ext-lib/:/opt/apps/ecm/shardingproxy-package/shardingproxy-master-bin/ext-lib/ \
-v /mnt/disk1/log/shardingproxy/:/opt/apps/ecm/shardingproxy-package/shardingproxy-master-bin/log/ \
-e JAVA_OPTS='-Djava.awt.headless=true' \
-e PORT=3308 \
wl4g/shardingproxy:2.0.0
```

## 2. Initialization example data

- Directories:

```text
├── exampledata
│  ├── group_sharding
│  │   ├── sharding1.jpg
│  │   ├── sharding2.jpg
│  │   └── userdb-sharding.sql
│  └── sharding
│      └── userdb-sharding.sql
```

> Notice: The example of non average slicing is not recommended for production (scenario: slicing according to different machine performance weight), because shardingsphere:5.0.0, It is recommended to use average sharding.

```sql
$MYSQL_HOME/bin/mysql -h127.0.0.1 -P3308 -uroot -p123456

use userdb;
SELECT * FROM userdb.t_user;
INSERT INTO userdb.t_user (id, name) VALUES (10000000, 'user-insert-1111');
UPDATE userdb.t_user SET name='user-update-2222' WHERE id=10000000;
DELETE FROM userdb.t_user WHERE id=10000000;
```

## 3. Failover integration

### 3.1 for MySQL [MGR](https://dev.mysql.com/doc/refman/5.7/en/group-replication.html) failover

- [https://dev.mysql.com/doc/refman/5.7/en/group-replication.html](https://dev.mysql.com/doc/refman/5.7/en/group-replication.html)

- [org.apache.shardingsphere.dbdiscovery.mgr.MGRDatabaseDiscoveryType.java](https://github.com/apache/shardingsphere/blob/5.0.0/shardingsphere-features/shardingsphere-db-discovery/shardingsphere-db-discovery-provider/shardingsphere-db-discovery-mgr/src/main/java/org/apache/shardingsphere/dbdiscovery/mgr/MGRDatabaseDiscoveryType.java)

- [https://github.com/apache/shardingsphere/blob/5.0.0/shardingsphere-proxy/shardingsphere-proxy-bootstrap/src/main/resources/conf/config-database-discovery.yaml](https://github.com/apache/shardingsphere/blob/5.0.0/shardingsphere-proxy/shardingsphere-proxy-bootstrap/src/main/resources/conf/config-database-discovery.yaml)

- [基于 Docker 离线部署 MYSQL MGR 高可用生产集群](https://blogs.wl4g.com/archives/2477)

- [基于 Host 离线部署 MYSQL MGR 高可用生产集群](https://blogs.wl4g.com/archives/650)

- The states of MGR primary and standby members

```sql
SELECT
    rgm.CHANNEL_NAME AS channelName,
    rgm.MEMBER_ID AS nodeId,
    rgm.MEMBER_HOST AS nodeHost,
    rgm.MEMBER_PORT AS nodePort,
    rgm.MEMBER_STATE AS nodeState,
    @@read_only AS readOnly,
    @@super_read_only AS superReadOnly,(
    CASE (SELECT TRIM(VARIABLE_VALUE) FROM `performance_schema`.`global_status` WHERE VARIABLE_NAME = 'group_replication_primary_member')
      WHEN '' THEN 'UNKOWN'
      WHEN rgm.MEMBER_ID THEN 'PRIMARY'
      ELSE 'STANDBY' END
    ) AS nodeRole
FROM
    `performance_schema`.`replication_group_members` rgm;

group_replication_applier  eb838b34-9deb-11eb-8677-c0b5d741e9d5  wanglsir-pro  13306 ONLINE  0  0  PRIMARY
group_replication_applier  05e9eb4f-9dec-11eb-8b2e-c0b5d741e9d5  wanglsir-pro  13308 ONLINE  0  0  STANDBY
group_replication_applier  3d4ed671-9dec-11eb-9723-c0b5d741e9d5  wanglsir-pro  13308 ONLINE  0  0  STANDBY
```

### 3.2 for PostgreSQL Cluster failover

TODO

### 3.3 for Oracle RAC failover

TODO

## 4. Metircs integration

- [https://shardingsphere.apache.org/document/5.0.0/cn/features/governance/observability/agent/](https://shardingsphere.apache.org/document/5.0.0/cn/features/governance/observability/agent/)

- Source codes refer: [org.apache.shardingsphere.agent.metrics.prometheus.service.PrometheusPluginBootService.java](https://github.com/apache/shardingsphere/blob/5.0.0/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-metrics/shardingsphere-agent-metrics-prometheus/src/main/java/org/apache/shardingsphere/agent/metrics/prometheus/service/PrometheusPluginBootService.java)

- Source codes refer: [org.apache.shardingsphere.agent.core.config.loader.AgentConfigurationLoader.java](https://github1s.com/apache/shardingsphere/blob/5.0.0/shardingsphere-agent/shardingsphere-agent-core/src/main/java/org/apache/shardingsphere/agent/core/config/loader/AgentConfigurationLoader.java)

- Agent configuration.

```bash
sudo cat <<-'EOF' >/opt/apps/ecm/shardingproxy-package/shardingproxy-master-bin/conf/agent.yaml
applicationName: cn-south1-a1-shardingproxy
ignoredPluginNames: # Ignored plugin set to make it invalidation.
  - Logging
  - Prometheus
  #- Zipkin
  #- Jaeger
  #- Opentracing
  #- OpenTelemetry

plugins:
  Logging:
    props:
      LEVEL: "DEBUG"
  Prometheus:
    host:  "localhost"
    port: 9090
    props:
      JVM_INFORMATION_COLLECTOR_ENABLED : "true"
  #Jaeger:
  #  host: "localhost"
  #  port: 5775
  #  props:
  #    SERVICE_NAME: "cn-south1-a1-shardingproxy"
  #    JAEGER_SAMPLER_TYPE: "const"
  #    JAEGER_SAMPLER_PARAM: "1"
  #    JAEGER_REPORTER_LOG_SPANS: "true"
  #    JAEGER_REPORTER_FLUSH_INTERVAL: "1"
  #Zipkin:
  #  host: "localhost"
  #  port: 9411
  #  props:
  #    SERVICE_NAME: "cn-south1-a1-shardingproxy"
  #    # Scrape span uri of zipkin service.
  #    URL_VERSION: "/api/v2/spans"
  #Opentracing:
  #  props:
  #    OPENTRACING_TRACER_CLASS_NAME: "org.apache.skywalking.apm.toolkit.opentracing.SkywalkingTracer"
  #OpenTelemetry:
  #  props:
  #    # Resource info of opentelemetry, multiple configurations can be separated by ','
  #    otel.resource.attributes: "service.name=cn-south1-a1-shardingproxy"
  #    otel.traces.exporter: "zipkin"
EOF
```

## 5. Tracing integration

TODO

## 6. FAQ

### 6.1 Can the same schema support different types of databases at the same time under read-write splitting and fragment splitting modes ?

Under the same schemaName, multiple sharding databases must be the same. See source code: [org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData](https://github.com/apache/shardingsphere/blob/5.0.0/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/metadata/ShardingSphereMetaData.java#L35) and [org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource](https://github.com/apache/shardingsphere/blob/5.0.0/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/metadata/resource/ShardingSphereResource.java#L48)

### 6.2 How can the `/cn-south1-a1-shardingproxy/states/datanodes/schema1` node in ZK disable datasources ?

- Source codes refer:

- [org.apache.shardingsphere.mode.metadata.persist.node.SchemaMetaDataNode.java](https://github.com/apache/shardingsphere/blob/5.0.0/shardingsphere-mode/shardingsphere-mode-core/src/main/java/org/apache/shardingsphere/mode/metadata/persist/node/SchemaMetaDataNode.java)

- [org.apache.shardingsphere.mode.metadata.persist.node.GlobalNode.java](https://github.com/apache/shardingsphere/blob/5.0.0/shardingsphere-mode/shardingsphere-mode-core/src/main/java/org/apache/shardingsphere/mode/metadata/persist/node/GlobalNode.java)

- [org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.node.ProcessNode.java](https://github.com/apache/shardingsphere/blob/5.0.0/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/process/node/ProcessNode.java)

- [org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.StatusNode.java](https://github1s.com/apache/shardingsphere/blob/5.0.0/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/status/StatusNode.java)

- [org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node.StorageStatusNode.java](https://github.com/apache/shardingsphere/blob/5.0.0/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/status/storage/node/StorageStatusNode.java)

### 6.3 How do I configuration failover ?

```yaml
cat server.yaml

props:
  failover-enable: true # Default by true
  # Failover admin dataSource configuration.
  # Notes: This configuration is used for read-write separation data source failover. Therefore, the same account
  #   password must be created for all master and slave databases before service startup.
  failover-configuration-json: |-
     {
         "inspectInitialDelayMs": 3000,
         "inspectMinDelayMs": 3000,
         "inspectMaxDelayMs": 10000,
         "adminDataSources": [{
             "schemaName": "userdb",
             "username": "root",
             "password": "123456",
             "mappings": [{
                 "internalAddr": "wanglsir-pro:33061",
                 "externalAddrs": [
                     "wl4g.debug:33061"
                 ]
             }, {
                 "internalAddr": "wanglsir-pro:33062",
                 "externalAddrs": [
                     "wl4g.debug:33062"
                 ]
             }, {
                 "internalAddr": "wanglsir-pro:33063",
                 "externalAddrs": [
                     "wl4g.debug:33063"
                 ]
             }]
         }]
     }
  # Notice: If failover is enabled and distributed governance mode is adopted, lock must be opened.
  lock-enabled: true # Default by false
```

> [Details refer to 'example/sharding-readwrite/server.yaml'](src/main/resources/example/sharding-readwrite/server.yaml)

| Attribute | Description |
| --- | --- |
| inspectInitialDelayMs | Monitor the initial start waiting time of the inspecting backend read/write dataSources group thread (ms). |
| inspectMinDelayMs | Monitor the min interval time inspecting read/write dataSources group thread (ms). |
| inspectMaxDelayMs | Monitor the max interval time inspecting read/write dataSources group thread (ms). |
| adminDataSources  | Admin dataSource configuration for inspection. |
| adminDataSources.schemaName | The virtual database schemaName corresponding to config-xx.yaml (Must be consistent). |
| adminDataSources.username | The account name of the data source grouped by the patrol database (some databases may be ordinary accounts, the query cluster state information no permission) |
| adminDataSources.password | Same as `adminDataSources.username` |
| adminDataSources.mappings.internalAddr | The access address of each data source library instance may be an external load balancing or proxy address (one-to-many) to external addresses. |
| adminDataSources.mappings.externalAddrs | The access address of each data source library instance may be an external load balancing or proxy address (many-to-one) to internal address. |

- **Notice**

- You can configure to enable or disable read-write failover as follows. `failover-enable: true|false`
- In the governance mode (cluster), the distributed lock must be enabled. It is disabled by default. &nbsp; `lock-enabled: true`
- Compatible with dataSources disabled in support registry center path: `/myShardingProxy/states/datanodes/mySchema`

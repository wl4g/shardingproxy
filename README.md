# An cloud-native db-sharding service based on [shardingsphere-proxy](https://github.com/apache/shardingsphere/tree/master/shardingsphere-proxy)

> It's an enhanced package that integrates shardingsphere-proxy and shardingsphere-scaling

## 1. Deployments

## 1.1 Preparing db instances for testing

- You first need to prepare a multi-instance database cluster for testing, here is `MySQL Group Replication` as an example. Refer docs to: [Deploy MGR high-availability production cluster based on Docker](https://blogs.wl4g.com/archives/2477)

- Assuming that the MGR cluster is now ready as follows:

```sql
SELECT
    (SELECT gv.VARIABLE_VALUE FROM performance_schema.global_variables gv WHERE gv.VARIABLE_NAME='group_replication_group_name') AS GROUP_NAME,
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

GROUP_NAME                            NODE_ID                               NODE_HOST     NODE_PORT NODE_STATE  READ_ONLY  SUPER_READ_ONLY  NODE_ROLE
5db40c3c-180c-11e9-afbf-005056ac6820  a7a2e5f2-60db-11ec-a680-0242ac08086f  rds-mgr-0     3306      ONLINE      0          0                PRIMARY
5db40c3c-180c-11e9-afbf-005056ac6820  a80be951-60db-11ec-b9a0-0242ac080870  rds-mgr-1     3306      ONLINE      0          0                STANDBY
5db40c3c-180c-11e9-afbf-005056ac6820  a88416b0-60db-11ec-939e-0242ac080871  rds-mgr-2     3306      ONLINE      0          0                STANDBY
```

### 1.2 Initialization MySQL

Notice: The example of non average slicing is not recommended for production (scenario: slicing according to different machine performance weight), because shardingsphere:5.1.0, It is recommended to use average sharding.

- Import exemple [userdb SQLs](xcloud-shardingproxy-starter/exampledata/sharding/userdb-sharding.sql)

```bash
cd $PROJECT_HOME/xcloud-shardingproxy-starter/
echo "source exampledata/sharding/userdb-sharding.sql" | mysql -h172.8.8.111 -P3306 -uroot -p123456
```

### 1.3 for Docker(usually for testing)

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

- Testing the effect of sharding

```sql
mysql -h127.0.0.1 -P3308 -uroot -p123456

use userdb;
SELECT * FROM userdb.t_user;
INSERT INTO userdb.t_user (id, name) VALUES (10000000, 'user-insert-1111');
UPDATE userdb.t_user SET name='user-update-2222' WHERE id=10000000;
DELETE FROM userdb.t_user WHERE id=10000000;
```

### 1.4 for Kubernetes(production recommend)

```bash
#TODO
```

## 2. Developer guide

- 2.1 Compiling

```bash
# git clone https://github.com/wl4g/xcloud-shardingproxy.git # (Upstream is newer)
git clone https://gitee.com/wl4g/xcloud-shardingproxy.git # (Domestic faster)
cd xcloud-shardingproxy
mvn clean install -DskipTests -Dmaven.test.skip=true -T 2C
```

- 2.2 Example startup

```bash
export SP_PROJECT_DIR=/opt/java-workspace/xcloud-shardingproxy
export SP_CONF_DIR=${SP_PROJECT_DIR}/xcloud-shardingproxy-starter/src/main/resources/example/sharding-readwrite/
export SP_JAVAAGENT=${SP_PROJECT_DIR}/xcloud-shardingproxy-agent-bootstrap/target/xcloud-shardingproxy-agent-bootstrap-5.1.0.jar
# Extension environment configuration.
export AGENT_PATH=${SP_PROJECT_DIR}/xcloud-shardingproxy-starter/src/main/resources/
export PLUGINS_PATH=${SP_PROJECT_DIR}/xcloud-shardingproxy-agent-extension/target/

java ${SP_JAVAAGENT} -jar shardingproxy-{version}-bin.jar 3308 ${SP_CONF_DIR}
# java ${SP_JAVAAGENT} -cp xxx com.wl4g.ShardingProxy 3308 ${SP_CONF_DIR}
```

## 3. Failover integration

### 3.1 for MySQL [MGR](https://dev.mysql.com/doc/refman/5.7/en/group-replication.html) failover

- [https://dev.mysql.com/doc/refman/5.7/en/group-replication.html](https://dev.mysql.com/doc/refman/5.7/en/group-replication.html)

- [org.apache.shardingsphere.dbdiscovery.mgr.MGRDatabaseDiscoveryType.java](https://github.com/apache/shardingsphere/blob/5.1.0/shardingsphere-features/shardingsphere-db-discovery/shardingsphere-db-discovery-provider/shardingsphere-db-discovery-mgr/src/main/java/org/apache/shardingsphere/dbdiscovery/mgr/MGRDatabaseDiscoveryType.java)

- [https://github.com/apache/shardingsphere/blob/5.1.0/shardingsphere-proxy/shardingsphere-proxy-bootstrap/src/main/resources/conf/config-database-discovery.yaml](https://github.com/apache/shardingsphere/blob/5.1.0/shardingsphere-proxy/shardingsphere-proxy-bootstrap/src/main/resources/conf/config-database-discovery.yaml)

- [Adjust discovery api feature. #13902](https://github.com/apache/shardingsphere/issues/13902)

#### 3.1.1 Add MGR static DNS

```bash
sudo cp /etc/hosts /etc/hosts.bak
sudo cat <<EOF >/etc/hosts
# for MGR testing
172.8.8.111 n0.rds.local
172.8.8.112 n1.rds.local
172.8.8.113 n2.rds.local
EOF
```

#### 3.1.2 Then need to modify the test configuration follows

- Extension database discovery configuration refer to example: [config-sharding-readwrite-userdb.yaml](src/main/resources/example/sharding-readwrite/server.yaml), The prefix of the following key names is : `rules.discoveryTypes.<myDiscoveryName>.props.`

| Attribute | Description |
|-|-|
| `extensionDiscoveryConfigJson.memberHostMappings.[0].<key>` | The access address of each dataSource correspond instance may be an external loadbalancing or proxy address (many-to-one) to internal address.(e.g: In the MGR cluster, the communication address of the member peer) |
| `extensionDiscoveryConfigJson.memberHostMappings.[0].<key>.[0]` | The access address of each dataSource correspond instance may be an external loadbalancing or proxy address (one-to-many) to external addresses. |

### 3.2 for PostgreSQL Cluster failover

```bash
#TODO
```

### 3.3 for Oracle RAC failover

```bash
#TODO
```

## 4. Metircs integration

- [https://shardingsphere.apache.org/document/5.1.0/cn/features/governance/observability/agent/](https://shardingsphere.apache.org/document/5.1.0/cn/features/governance/observability/agent/)

- Source codes refer: [org.apache.shardingsphere.agent.metrics.prometheus.service.PrometheusPluginBootService.java](https://github.com/apache/shardingsphere/blob/5.1.0/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-metrics/shardingsphere-agent-metrics-prometheus/src/main/java/org/apache/shardingsphere/agent/metrics/prometheus/service/PrometheusPluginBootService.java)

- Source codes refer: [org.apache.shardingsphere.agent.core.config.loader.AgentConfigurationLoader.java](https://github1s.com/apache/shardingsphere/blob/5.1.0/shardingsphere-agent/shardingsphere-agent-core/src/main/java/org/apache/shardingsphere/agent/core/config/loader/AgentConfigurationLoader.java)

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

Under the same schemaName, multiple sharding databases must be the same. See source code: [org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData](https://github.com/apache/shardingsphere/blob/5.1.0/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/metadata/ShardingSphereMetaData.java#L35) and [org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource](https://github.com/apache/shardingsphere/blob/5.1.0/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/metadata/resource/ShardingSphereResource.java#L48)

### 6.2 What data is stored in zookeeper and where is the source code?

- [org.apache.shardingsphere.mode.metadata.persist.node.SchemaMetaDataNode.java](https://github.com/apache/shardingsphere/blob/5.1.0/shardingsphere-mode/shardingsphere-mode-core/src/main/java/org/apache/shardingsphere/mode/metadata/persist/node/SchemaMetaDataNode.java)

- [org.apache.shardingsphere.mode.metadata.persist.node.GlobalNode.java](https://github.com/apache/shardingsphere/blob/5.1.0/shardingsphere-mode/shardingsphere-mode-core/src/main/java/org/apache/shardingsphere/mode/metadata/persist/node/GlobalNode.java)

- [org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.node.ProcessNode.java](https://github.com/apache/shardingsphere/blob/5.1.0/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/process/node/ProcessNode.java)

- [org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.StatusNode.java](https://github1s.com/apache/shardingsphere/blob/5.1.0/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/status/StatusNode.java)

- [org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node.StorageStatusNode.java](https://github.com/apache/shardingsphere/blob/5.1.0/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/status/storage/node/StorageStatusNode.java)

- for example zookeeper storage data directories.

```bash
/mgr-elasticjob/MGR-pr_userdb_g0db0
/mgr-elasticjob/MGR-pr_userdb_g0db1
/mgr-elasticjob/MGR-pr_userdb_g0db2
/mgr-elasticjob/MGR-pr_userdb_g0db0/config
/mgr-elasticjob/MGR-pr_userdb_g0db0/instances
/mgr-elasticjob/MGR-pr_userdb_g0db0/leader
/mgr-elasticjob/MGR-pr_userdb_g0db0/servers
/mgr-elasticjob/MGR-pr_userdb_g0db0/sharding
/mgr-elasticjob/MGR-pr_userdb_g0db0/instances/192.168.0.101@-@51940
/mgr-elasticjob/MGR-pr_userdb_g0db0/leader/election
/mgr-elasticjob/MGR-pr_userdb_g0db0/leader/sharding
/mgr-elasticjob/MGR-pr_userdb_g0db0/leader/election/instance
/mgr-elasticjob/MGR-pr_userdb_g0db0/servers/192.168.0.101
/mgr-elasticjob/MGR-pr_userdb_g0db0/sharding/0
/mgr-elasticjob/MGR-pr_userdb_g0db0/sharding/0/instance
/mgr-elasticjob/MGR-pr_userdb_g0db1/config
/mgr-elasticjob/MGR-pr_userdb_g0db1/instances
/mgr-elasticjob/MGR-pr_userdb_g0db1/leader
/mgr-elasticjob/MGR-pr_userdb_g0db1/servers
/mgr-elasticjob/MGR-pr_userdb_g0db1/sharding
/mgr-elasticjob/MGR-pr_userdb_g0db1/instances/192.168.0.101@-@51940
/mgr-elasticjob/MGR-pr_userdb_g0db1/leader/election
/mgr-elasticjob/MGR-pr_userdb_g0db1/leader/sharding
/mgr-elasticjob/MGR-pr_userdb_g0db1/leader/election/instance
/mgr-elasticjob/MGR-pr_userdb_g0db1/servers/192.168.0.101
/mgr-elasticjob/MGR-pr_userdb_g0db1/sharding/0
/mgr-elasticjob/MGR-pr_userdb_g0db1/sharding/0/instance
/mgr-elasticjob/MGR-pr_userdb_g0db2/config
/mgr-elasticjob/MGR-pr_userdb_g0db2/instances
/mgr-elasticjob/MGR-pr_userdb_g0db2/leader
/mgr-elasticjob/MGR-pr_userdb_g0db2/servers
/mgr-elasticjob/MGR-pr_userdb_g0db2/sharding
/mgr-elasticjob/MGR-pr_userdb_g0db2/instances/192.168.0.101@-@51940
/mgr-elasticjob/MGR-pr_userdb_g0db2/leader/election
/mgr-elasticjob/MGR-pr_userdb_g0db2/leader/sharding
/mgr-elasticjob/MGR-pr_userdb_g0db2/leader/election/instance
/mgr-elasticjob/MGR-pr_userdb_g0db2/servers/192.168.0.101
/mgr-elasticjob/MGR-pr_userdb_g0db2/sharding/0
/mgr-elasticjob/MGR-pr_userdb_g0db2/sharding/0/instance
/shardingproxy_0/lock
/shardingproxy_0/metadata
/shardingproxy_0/props
/shardingproxy_0/rules
/shardingproxy_0/status
/shardingproxy_0/lock/ack
/shardingproxy_0/lock/locks
/shardingproxy_0/metadata/userdb
/shardingproxy_0/metadata/userdb/dataSources
/shardingproxy_0/metadata/userdb/rules
/shardingproxy_0/metadata/userdb/schema
/shardingproxy_0/status/compute_nodes
/shardingproxy_0/status/storage_nodes
/shardingproxy_0/status/compute_nodes/online
/shardingproxy_0/status/compute_nodes/online/172.8.8.1@3308
/shardingproxy_0/status/storage_nodes/disable
/shardingproxy_0/status/storage_nodes/primary
/shardingproxy_0/status/storage_nodes/disable/userdb.ds_userdb_g0db0_0
/shardingproxy_0/status/storage_nodes/disable/userdb.ds_userdb_g0db0_1
/shardingproxy_0/status/storage_nodes/disable/userdb.ds_userdb_g0db0_2
/shardingproxy_0/status/storage_nodes/disable/userdb.ds_userdb_g0db1_0
/shardingproxy_0/status/storage_nodes/disable/userdb.ds_userdb_g0db1_1
/shardingproxy_0/status/storage_nodes/disable/userdb.ds_userdb_g0db1_2
/shardingproxy_0/status/storage_nodes/disable/userdb.ds_userdb_g0db2_0
/shardingproxy_0/status/storage_nodes/disable/userdb.ds_userdb_g0db2_1
/shardingproxy_0/status/storage_nodes/disable/userdb.ds_userdb_g0db2_2
/shardingproxy_0/status/storage_nodes/primary/userdb.ha_userdb_g0db2
```

### 6.3 If you want to test native [apache/shardingsphere/shardingsphere-proxy](https://github.com/apache/shardingsphere/tree/master/shardingsphere-proxy)

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
apache/shardingsphere-proxy:5.1.0
```

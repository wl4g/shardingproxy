# An cloud-native db-sharding service based on [shardingsphere-proxy](https://github.com/apache/shardingsphere/tree/master/shardingsphere-proxy)

> It's an enhanced package that integrates shardingsphere-proxy and shardingsphere-scaling

## 1. Deployments

### 1.1 Preparing MySQL MGR cluster for testing

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

### 1.2 Initializing for testing

Notice: The example of non average slicing is not recommended for production (scenario: slicing according to different machine performance weight), because shardingsphere:5.1.0, It is recommended to use average sharding.

- Import exemple [userdb SQLs](xcloud-shardingproxy-starter/exampledata/sharding/userdb-sharding.sql)

```bash
cd $PROJECT_HOME/xcloud-shardingproxy-starter/
echo "source exampledata/sharding/userdb-sharding.sql" | mysql -h172.8.8.111 -P3306 -uuserdb -p123456
```

### 1.3 Deploy on Docker(Testing recommend)

- Run [zookeeper](https://hub.docker.com/_/zookeeper) single container

```bash
docker run -d \
--name zk1 \
--net host \
--restart no \
-e JVMFLAGS="-Djava.net.preferIPv4Stack=true -Xmx512m" \
-e ZOO_MAX_CLIENT_CNXNS=60 \
zookeeper:3.6.0
```

- Run [jaeger](https://hub.docker.com/r/jaegertracing/all-in-one) single container

```bash
docker run -d \
--name jaeger1 \
--net host \
jaegertracing/all-in-one:1.30
```

- Run [shardingproxy](https://hub.docker.com/r/wl4g/shardingproxy) single container

```bash
mkdir -p /mnt/disk1/shardingproxy/{ext-lib/agentlib/conf,conf,ext-lib}
mkdir -p /mnt/disk1/log/shardingproxy/

# Prepare a example sharding configuration.
cp xcloud-shardingproxy-starter/src/main/resources/agent/conf/*.yaml /mnt/disk1/shardingproxy/ext-lib/agentlib/conf/
cp xcloud-shardingproxy-starter/src/main/resources/example/sharding-readwrite/*.yaml /mnt/disk1/shardingproxy/conf/

# The MySQL group replication network for demonstration. see: https://blogs.wl4g.com/archives/2477
#docker network create --subnet=172.8.8.0/24 mysqlnet

docker run -d \
--name sp1 \
--net host \
--restart no \
--add-host n0.rds.local:172.8.8.111 \
--add-host n1.rds.local:172.8.8.112 \
--add-host n2.rds.local:172.8.8.113 \
-e JAVA_OPTS='-Djava.awt.headless=true' \
-e SHARDING_PORT=3308 \
-v /mnt/disk1/shardingproxy/ext-lib/agentlib/conf/:/opt/shardingproxy/ext-lib/agentlib/conf/ \
-v /mnt/disk1/shardingproxy/conf/:/opt/shardingproxy/conf/ \
-v /mnt/disk1/log/shardingproxy/:/var/log/shardingproxy/ \
-p 3308:3308 \
wl4g/shardingproxy:5.1.0
```

- Testing for valid

```sql
mysql -h127.0.0.1 -P3308 -uuserdb -p123456

use userdb;
SELECT * FROM userdb.t_user;
INSERT INTO userdb.t_user (id, name) VALUES (10000000, 'user-insert-1111');
UPDATE userdb.t_user SET name='user-update-2222' WHERE id=10000000;
DELETE FROM userdb.t_user WHERE id=10000000;
```

### 1.4 Deploy on Kubernetes(Production recommend)

- [Installation with helm](kubernetes/helm/README.md)

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

### 3.1 MySQL [Group Replication](https://dev.mysql.com/doc/refman/5.7/en/group-replication.html)

- [https://dev.mysql.com/doc/refman/5.7/en/group-replication.html](https://dev.mysql.com/doc/refman/5.7/en/group-replication.html)

- [org.apache.shardingsphere.dbdiscovery.mgr.MGRDatabaseDiscoveryType.java](https://github.com/apache/shardingsphere/blob/5.1.0/shardingsphere-features/shardingsphere-db-discovery/shardingsphere-db-discovery-provider/shardingsphere-db-discovery-mgr/src/main/java/org/apache/shardingsphere/dbdiscovery/mgr/MGRDatabaseDiscoveryType.java)

- [https://github.com/apache/shardingsphere/blob/5.1.0/shardingsphere-proxy/shardingsphere-proxy-bootstrap/src/main/resources/conf/config-database-discovery.yaml](https://github.com/apache/shardingsphere/blob/5.1.0/shardingsphere-proxy/shardingsphere-proxy-bootstrap/src/main/resources/conf/config-database-discovery.yaml)

- [Adjust discovery api feature. #13902](https://github.com/apache/shardingsphere/issues/13902)

- 3.1.1 Add MGR static DNS

```bash
sudo cp /etc/hosts /etc/hosts.bak
sudo cat <<EOF >/etc/hosts
# for MGR testing
172.8.8.111 n0.rds.local
172.8.8.112 n1.rds.local
172.8.8.113 n2.rds.local
EOF
```

- 3.1.2 Then need to modify the test configuration follows

- Extension database discovery configuration refer to example: [config-sharding-readwrite-userdb.yaml](src/main/resources/example/sharding-readwrite/server.yaml), The prefix of the following key names is : `rules.discoveryTypes.<myDiscoveryName>.props.`

| Attribute | Description |
|-|-|
| `extensionDiscoveryConfigJson.memberHostMappings.[0].<key>` | The access address of each dataSource correspond instance may be an external loadbalancing or proxy address (many-to-one) to internal address.(e.g: In the MGR cluster, the communication address of the member peer) |
| `extensionDiscoveryConfigJson.memberHostMappings.[0].<key>.[0]` | The access address of each dataSource correspond instance may be an external loadbalancing or proxy address (one-to-many) to external addresses. |

### 3.2 PostgreSQL Cluster

```bash
#TODO
```

### 3.3 Oracle RAC

```bash
#TODO
```

## 4. Metircs integration

- [https://shardingsphere.apache.org/document/5.1.0/cn/features/governance/observability/agent/](https://shardingsphere.apache.org/document/5.1.0/cn/features/governance/observability/agent/)

- Source codes refer: [org.apache.shardingsphere.agent.metrics.prometheus.service.PrometheusPluginBootService.java](https://github.com/apache/shardingsphere/blob/5.1.0/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-metrics/shardingsphere-agent-metrics-prometheus/src/main/java/org/apache/shardingsphere/agent/metrics/prometheus/service/PrometheusPluginBootService.java)

- Source codes refer: [org.apache.shardingsphere.agent.core.config.loader.AgentConfigurationLoader.java](https://github1s.com/apache/shardingsphere/blob/5.1.0/shardingsphere-agent/shardingsphere-agent-core/src/main/java/org/apache/shardingsphere/agent/core/config/loader/AgentConfigurationLoader.java)

- Example agent.yaml [xcloud-shardingproxy-starter/src/main/resources/conf/agent.yaml](https://github.com/wl4g/xcloud-shardingproxy/blob/master/xcloud-shardingproxy-starter/src/main/resources/conf/agent.yaml)

```bash
# Gets prometheus metrics
curl http://localhost:10105/metrics
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
/cn_south1_a1_shardingproxy_0
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db0
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db1
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db2
/cn_south1_a1_shardingproxy_0/lock
/cn_south1_a1_shardingproxy_0/metadata
/cn_south1_a1_shardingproxy_0/props
/cn_south1_a1_shardingproxy_0/rules
/cn_south1_a1_shardingproxy_0/scaling
/cn_south1_a1_shardingproxy_0/status
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db0/config
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db0/instances
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db0/leader
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db0/servers
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db0/sharding
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db0/instances/10.0.0.114@-@137040
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db0/leader/election
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db0/leader/sharding
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db0/leader/election/instance
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db0/servers/10.0.0.114
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db0/sharding/0
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db0/sharding/0/instance
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db1/config
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db1/instances
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db1/leader
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db1/servers
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db1/sharding
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db1/instances/10.0.0.114@-@137040
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db1/leader/election
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db1/leader/sharding
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db1/leader/election/instance
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db1/servers/10.0.0.114
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db1/sharding/0
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db1/sharding/0/instance
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db2/config
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db2/instances
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db2/leader
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db2/servers
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db2/sharding
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db2/instances/10.0.0.114@-@137040
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db2/leader/election
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db2/leader/sharding
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db2/leader/election/instance
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db2/servers/10.0.0.114
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db2/sharding/0
/cn_south1_a1_shardingproxy_0/MGR-ha_userdb_g0db2/sharding/0/instance
/cn_south1_a1_shardingproxy_0/lock/ack
/cn_south1_a1_shardingproxy_0/lock/locks
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db0
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db1
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db2
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db0/dataSources
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db0/rules
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db0/tables
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db0/tables/t_user_0
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db0/tables/t_user_1
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db0/tables/t_user_2
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db1/dataSources
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db1/rules
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db1/tables
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db1/tables/t_user_0
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db1/tables/t_user_1
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db1/tables/t_user_2
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db2/dataSources
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db2/rules
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db2/tables
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db2/tables/t_user_0
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db2/tables/t_user_1
/cn_south1_a1_shardingproxy_0/metadata/userdb_g0db2/tables/t_user_2
/cn_south1_a1_shardingproxy_0/scaling/_finished_check
/cn_south1_a1_shardingproxy_0/scaling/_finished_check/config
/cn_south1_a1_shardingproxy_0/scaling/_finished_check/instances
/cn_south1_a1_shardingproxy_0/scaling/_finished_check/leader
/cn_south1_a1_shardingproxy_0/scaling/_finished_check/servers
/cn_south1_a1_shardingproxy_0/scaling/_finished_check/instances/10.0.0.114@-@137040
/cn_south1_a1_shardingproxy_0/scaling/_finished_check/leader/election
/cn_south1_a1_shardingproxy_0/scaling/_finished_check/leader/sharding
/cn_south1_a1_shardingproxy_0/scaling/_finished_check/leader/election/instance
/cn_south1_a1_shardingproxy_0/scaling/_finished_check/leader/sharding/necessary
/cn_south1_a1_shardingproxy_0/scaling/_finished_check/servers/10.0.0.114
/cn_south1_a1_shardingproxy_0/status/compute_nodes
/cn_south1_a1_shardingproxy_0/status/storage_nodes
/cn_south1_a1_shardingproxy_0/status/compute_nodes/online
/cn_south1_a1_shardingproxy_0/status/compute_nodes/online/172.8.8.1@3308
/cn_south1_a1_shardingproxy_0/status/storage_nodes/disable
/cn_south1_a1_shardingproxy_0/status/storage_nodes/primary
/cn_south1_a1_shardingproxy_0/status/storage_nodes/disable/userdb_g0db0.ds_userdb_g0db0_0
/cn_south1_a1_shardingproxy_0/status/storage_nodes/disable/userdb_g0db1.ds_userdb_g0db1_0
/cn_south1_a1_shardingproxy_0/status/storage_nodes/disable/userdb_g0db2.ds_userdb_g0db2_0
/cn_south1_a1_shardingproxy_0/status/storage_nodes/primary/userdb_g0db0.ha_userdb_g0db0
/cn_south1_a1_shardingproxy_0/status/storage_nodes/primary/userdb_g0db1.ha_userdb_g0db1
/cn_south1_a1_shardingproxy_0/status/storage_nodes/primary/userdb_g0db2.ha_userdb_g0db2
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

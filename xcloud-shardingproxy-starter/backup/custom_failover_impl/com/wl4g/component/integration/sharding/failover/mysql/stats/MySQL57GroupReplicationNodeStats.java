package com.wl4g.component.integration.sharding.failover.mysql.stats;

import static com.wl4g.component.common.collection.CollectionUtils2.safeList;

import java.util.List;

import com.wl4g.component.common.lang.StringUtils2;
import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MySQL57GroupReplicationNodeStats extends NodeStats {

    private List<GroupReplicationNodeInfo> nodes;

    private List<GroupReplicationNodeInfo> primaryNodes;

    private List<GroupReplicationNodeInfo> standbyNodes;

    @Override
    public boolean valid() {
        for (GroupReplicationNodeInfo n : nodes) {
            // Group Replication node role must be: PRIMARY/STANDBY only
            // effective, If the status is unknown, the current master node
            // cannot be determined.
            if (StringUtils2.eqIgnCase(n.getNodeRole(), "UNKOWN")) {
                return false;
            }
        }
        return !safeList(primaryNodes).isEmpty();
    }

    @Getter
    @Setter
    public static class GroupReplicationNodeInfo extends NodeInfo {
        private String channelName;
        private String nodeId;
        private String nodeHost;
        private Integer nodePort;
        private String nodeState;
        private String nodeRole;
        private String readOnly;
        private String superReadOnly;

        @Override
        public String getHost() {
            return nodeHost;
        }

        @Override
        public int getPort() {
            return nodePort;
        }

    }

}
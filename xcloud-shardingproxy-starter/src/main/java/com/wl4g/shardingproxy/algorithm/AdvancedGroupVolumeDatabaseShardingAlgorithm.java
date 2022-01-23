/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.shardingproxy.algorithm;

import java.util.Collection;

import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;

import lombok.extern.slf4j.Slf4j;

/**
 * The dataSources(tables) sharding based on advanced heterogeneous grouping
 * algorithm.
 * 
 * for example:
 * 
 * <pre>
 * {
 *   "id": [
 *     {
 *       lowerClose: 0,
 *       upperOpen: 4000_0000,
 *       expression: "rw_userdb_r0z0mgr0_db${(id % 2) as int}"
 *     },
 *     {
 *       lowerClose: 4000_0000,
 *       upperOpen: 20000_0000,
 *       expression: "rw_userdb_r0z0mgr1_db${(id % 7) as int}"
 *     }
 *   ]
 * }
 * </pre>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-01-19 v1.0.0
 * @since v1.0.0
 */
@Slf4j
public class AdvancedGroupVolumeDatabaseShardingAlgorithm extends BaseGroupVolumeShardingAlgorithm {

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames,
            final RangeShardingValue<Comparable<?>> shardingValue) {
        log.debug("The default determined range sharding all targets: {}, by shardingValue: {}, availableTargetNames: {}",
                availableTargetNames, shardingValue, availableTargetNames);
        return availableTargetNames;
    }

    @Override
    public String getType() {
        return "ADVANCED_GROUP_VOLUME_DATABASE";
    }

}

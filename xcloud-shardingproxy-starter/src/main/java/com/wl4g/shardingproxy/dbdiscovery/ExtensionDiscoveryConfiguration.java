/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.shardingproxy.dbdiscovery;

import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.serialize.JacksonUtils.parseJSON;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;

import com.google.common.net.HostAndPort;
import com.wl4g.component.common.lang.HostUtils;
import com.wl4g.shardingproxy.util.JdbcUtil;
import com.wl4g.shardingproxy.util.JdbcUtil.JdbcInformation;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link ExtensionDiscoveryConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-26 v1.0.0
 * @since v1.0.0
 */
@Getter
@Setter
@ToString
public class ExtensionDiscoveryConfiguration {

    public static final String PROPS_KEY = "extensionDiscoveryConfigJson";

    private List<Map<String, List<String>>> memberHostMappings;

    public static final class Util {
        public static ExtensionDiscoveryConfiguration build(String json) {
            return parseJSON(json, ExtensionDiscoveryConfiguration.class);
        }

        public static boolean matchs(ExtensionDiscoveryConfiguration config, String jdbcUrl, String memberAddr) {
            JdbcInformation info = JdbcUtil.resolve(jdbcUrl);
            return safeList(findMappingAddressesByMemberAddr(config, memberAddr)).stream().anyMatch(addr -> {
                HostAndPort address = HostAndPort.fromString(addr);
                return info.getPort() == address.getPort() && HostUtils.isSameHost(info.getHost(), address.getHost());
            });
        }

        public static List<String> findMappingAddressesByMemberAddr(ExtensionDiscoveryConfiguration config, String memberAddr) {
            if (isNull(config) || isNull(memberAddr)) {
                return emptyList();
            }
            HostAndPort memberAddress = HostAndPort.fromString(memberAddr);
            return safeList(config.getMemberHostMappings()).stream()
                    .filter(mapping -> mapping.containsKey(memberAddress.toString())).findFirst().orElse(emptyMap()).values()
                    .stream().flatMap(addrs -> addrs.stream()).collect(toList());
        }
    }

}

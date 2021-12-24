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
package com.wl4g.shardingproxy.dbdiscovery.mgr;

import com.wl4g.shardingproxy.dbdiscovery.ExtensionDiscoveryConfiguration;

/**
 * {@link ExtensionDiscoveryConfigTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-12-20 v1.0.0
 * @since v1.0.0
 */
public class ExtensionDiscoveryConfigTests {

    public static void main(String[] args) {
        String json = "{\n" + "             \"memberHostMappings\": [{\n" + "                 \"n0.rds.local:3306\": [\n"
                + "                     \"localhost:33061\"\n" + "                 ]\n" + "             }, {\n"
                + "                 \"n1.rds.local:3306\": [\n" + "                     \"localhost:33062\"\n"
                + "                 ]\n" + "             }, {\n" + "                 \"n2.rds.local:3306\": [\n"
                + "                     \"localhost:33063\"\n" + "                 ]\n" + "             }]\n" + "           }";

        System.out.println(json);
        ExtensionDiscoveryConfiguration config = ExtensionDiscoveryConfiguration.Util.build(json);
        System.out.println(config.getMemberHostMappings().get(0));
    }

}

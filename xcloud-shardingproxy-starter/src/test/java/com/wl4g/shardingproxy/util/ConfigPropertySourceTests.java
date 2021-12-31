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
package com.wl4g.shardingproxy.util;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link ConfigPropertySourceTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-12-20 v1.0.0
 * @since v1.0.0
 */
public class ConfigPropertySourceTests {

    public static void main(String[] args) {
        System.out.println(ConfigPropertySource.humpToLine("keepAlivedTime"));
        System.out.println(ConfigPropertySource.lineToHump("keep_alived_time"));
        System.out.println(ConfigPropertySource.pointToHump("keep.alived.time"));
        System.out.println(ConfigPropertySource.pointToLine("keep.alived.time"));

        Map<String, String> envMap = new HashMap<>();

        ConfigPropertySource source = new ConfigPropertySource() {
            private static final long serialVersionUID = 8674366211323332009L;

            @Override
            protected String findEnvironmentValue(String key) {
                return envMap.get(key);
            }
        };

        source.put("zkServerLists", "10.2.2.2:2181");
        System.out.println(source.getProperty("zkServerLists"));

        System.out.println("---------------------------------------------");
        envMap.clear();
        source.clear();
        envMap.put("ZK_SERVER_LISTS", "10.2.2.3:2181");
        source.put("zkServerLists", "10.2.2.4:2181");
        System.out.println(source.getProperty("zkServerLists"));

        System.out.println("---------------------------------------------");
        envMap.clear();
        source.clear();
        envMap.put("ZK_SERVER_LISTS", "10.2.2.5:2181");
        envMap.put("zkServerLists", "10.2.2.6:2181");
        source.put("zkServerLists", "10.2.2.7:2181");
        System.out.println(source.getProperty("zkServerLists"));
    }

}

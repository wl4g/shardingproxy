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
package org.apache.shardingsphere.sharding.algorithm.sharding.datetime;

import static java.lang.String.format;
import static java.lang.System.out;

import java.util.Properties;

import org.junit.Test;

/**
 * {@link AutoIntervalShardingAlgorithmTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-01-29 v1.0.0
 * @since v1.0.0
 */
public class AutoIntervalShardingAlgorithmTests {

    @Test
    public void testCalcAutoTablesAmount() {
        String lower = "2020-01-01 00:00:00";
        String upper = "2025-12-31 00:00:00";
        String shardingSec = "604800";// 1d(86400)|7d(604800)|31d(2678400)
        AutoIntervalShardingAlgorithm alg = new AutoIntervalShardingAlgorithm();
        alg.setProps(new Properties());
        alg.getProps().setProperty("datetime-lower", lower);
        alg.getProps().setProperty("datetime-upper", upper);
        alg.getProps().setProperty("sharding-seconds", shardingSec);
        alg.init();
        out.println(format("autoTablesSuffixAmount: %s of datetime from lower: '%s' to upper: '%s' and sharding-seconds: %s",
                alg.getAutoTablesAmount(), lower, upper, shardingSec));
    }

}

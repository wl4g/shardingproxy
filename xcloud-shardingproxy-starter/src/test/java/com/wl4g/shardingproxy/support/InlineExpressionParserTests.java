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
package com.wl4g.shardingproxy.support;

import static java.lang.String.format;
import static java.lang.System.out;

import java.util.List;

import org.apache.shardingsphere.sharding.support.InlineExpressionParser;
import org.junit.Test;

import groovy.lang.Closure;
import groovy.util.Expando;

/**
 * {@link InlineExpressionParserTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-01-23 v1.0.0
 * @since v1.0.0
 */
public class InlineExpressionParserTests {

    @Test
    public void parseShardingColumnExpr() {
        doParseShardingIDExpr("${9}");
        doParseShardingIDExpr("9");
    }

    @Test
    public void testParseActualNodesExpr() {
        doParseActualNodesExpr("rw_orderdb_r0z0mgr${0..1}_db${0..3}.t_order_${0..7}");
        doParseActualNodesExpr("rw_orderdb_r0z0mgr0_db${0..1}.t_order_${0..3},rw_orderdb_r0z0mgr1_db${0..3}.t_order_${0..7}");
    }

    private void doParseShardingIDExpr(String inlineExpr) {
        System.out.println(format("---------------- inlineExpr: %s -------------------", inlineExpr));
        Closure<?> closure = new InlineExpressionParser(inlineExpr).evaluateClosure().rehydrate(new Expando(), null, null);
        closure.setResolveStrategy(Closure.DELEGATE_ONLY);
        closure.setProperty("id", -1);
        System.out.println(closure.call().toString());
    }

    private void doParseActualNodesExpr(String actualNodesExpr) {
        System.out.println(format("---------------- actualNodesExpr: %s -------------------", actualNodesExpr));
        List<String> dataNodes = new InlineExpressionParser(actualNodesExpr).splitAndEvaluate();
        dataNodes.stream().forEach(dn -> out.println(dn));
        out.println(format("size: %s", dataNodes.size()));
    }

}

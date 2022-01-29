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
package com.wl4g.shardingproxy.authority;

import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;

import java.util.regex.Pattern;

import org.junit.Test;

/**
 * {@link SQLAdmissionControllerTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-01-21 v1.0.0
 * @since v1.0.0
 */
public class SQLAdmissionControllerTests {

    @Test
    public void testParsePrivilegeConfiguration() {
        String json = "{\"granted\":{\"userdb_admin0\":\"default_ddl_safety, base_dml_safety\",\"warehousedb_admin0\":\"default_ddl_safety, base_dml_safety, slight_strict_dml_safety\",\"paymentdb_admin0\":\"default_ddl_safety, base_dml_safety, slight_strict_dml_safety, general_strict_dml_safety\",\"orderdb_admin0\":\"default_ddl_safety, base_dml_safety, slight_strict_dml_safety\"},\"privileges\":{\"default_ddl_safety\":[{\"anyBlacklistSQLs\":[\"(.*)drop(\\\\s+)schema(\\\\s+)(.+)\",\"(.*)drop(\\\\s+)database(\\\\s+)(.+)\",\"(.*)drop(\\\\s+)table(\\\\s+)(.+)\",\"(.*)alert(\\\\s+)database(\\\\s+)(.+)\",\"(.*)alert(\\\\s+)table(\\\\s+)(.+)\"]}],\"base_dml_safety\":[{\"delete\":{\"requiredWhereCondidtion\":true}}],\"slight_strict_dml_safety\":[{\"update\":{\"requiredWhereCondidtion\":true}}],\"general_strict_dml_safety\":[{\"select\":{\"anyDenied\":true}}]}}";
        StandardPrivilegeConfiguration config = StandardPrivilegeConfiguration.build(json);
        System.out.println(toJSONString(config));
    }

    @Test
    public void testRegexMatches() {
        Pattern p0 = Pattern.compile("(.*)drop(\\s+)schema(\\s+)(.+)", Pattern.CASE_INSENSITIVE);
        System.out.println(p0.matcher(" drop   schema if exists orderdb").matches());

        Pattern p1 = Pattern.compile("(.*)drop(\\s+)database(\\s+)(.+)", Pattern.CASE_INSENSITIVE);
        System.out.println(p1.matcher(" drop   database if exists orderdb").matches());

        Pattern p2 = Pattern.compile("(.*)drop(\\s+)table(\\s+)(.+)", Pattern.CASE_INSENSITIVE);
        System.out.println(p2.matcher(" drop   table if exists t_order").matches());
        System.out.println(p2.matcher(" drop  table  t_order").matches());

        Pattern p3 = Pattern.compile("(.*)alert(\\s+)database(\\s+)(.+)", Pattern.CASE_INSENSITIVE);
        System.out.println(p3.matcher(" alert  database if exists orderdb").matches());

        Pattern p4 = Pattern.compile("(.*)alert(\\s+)table(\\s+)(.+)", Pattern.CASE_INSENSITIVE);
        System.out.println(p4.matcher(" alert   table if exists orderdb").matches());
    }

}

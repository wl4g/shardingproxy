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

import java.util.regex.Pattern;

/**
 * {@link SQLAdmissionControllerTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-01-21 v1.0.0
 * @since v1.0.0
 */
public class SQLAdmissionControllerTests {

    public static void main(String[] args) {
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

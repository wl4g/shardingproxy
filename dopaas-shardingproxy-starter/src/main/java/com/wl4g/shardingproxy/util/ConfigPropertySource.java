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

import static java.util.Objects.isNull;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link ConfigPropertySource}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-12-20 v1.0.0
 * @since v1.0.0
 */
public class ConfigPropertySource extends Properties {
    private static final long serialVersionUID = -1021135946737652193L;

    public ConfigPropertySource() {
    }

    public ConfigPropertySource(Properties props) {
        putAll(props);
    }

    @Override
    public Object get(Object key) {
        return getProperty((String) key);
    }

    @Override
    public String getProperty(String key) {
        String value = null;

        // The one priority is to get from the environment.
        value = findEnvironmentValue(key);
        if (isNull(value)) {
            value = findEnvironmentValue(pointToHump(key));
        }
        if (isNull(value)) {
            value = findEnvironmentValue(pointToLine(key));
        }
        if (isNull(value)) {
            value = findEnvironmentValue(humpToLine(key));
        }
        if (isNull(value)) {
            value = findEnvironmentValue(lineToHump(key));
        }
        // The secondary priority to get from the configuration.
        if (isNull(value)) {
            value = super.getProperty(key);
        }
        if (isNull(value)) {
            value = super.getProperty(pointToHump(key));
        }
        if (isNull(value)) {
            value = super.getProperty(pointToLine(key));
        }
        if (isNull(value)) {
            value = super.getProperty(humpToLine(key));
        }
        if (isNull(value)) {
            value = super.getProperty(lineToHump(key));
        }

        return value;
    }

    protected String findEnvironmentValue(String key) {
        return System.getenv().get(key);
    }

    public static String pointToHump(String key) {
        if (!"".equals(key)) {
            return lineToHump(pointToLine(key));
        }
        return key;
    }

    public static String pointToLine(String key) {
        if (!"".equals(key) && key.contains(".")) {
            return key.replace(".", "_");
        }
        return key;
    }

    public static String humpToLine(String key) {
        StringBuffer sb = new StringBuffer(64);
        Matcher matcher = Pattern.compile("[A-Z]").matcher(key);
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String lineToHump(String key) {
        StringBuffer sb = new StringBuffer(64);
        Matcher matcher = Pattern.compile("_(\\w)").matcher(key.toLowerCase());
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}

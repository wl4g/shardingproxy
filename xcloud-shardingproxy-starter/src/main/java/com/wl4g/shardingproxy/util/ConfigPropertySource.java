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
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

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

    @Override
    public String getProperty(String key) {
        // The one priority is obtained from the configuration.
        String value = super.getProperty(key);

        // The second priority is to obtain from the environment variable
        // with the same key name.
        if (isNull(value)) {
            value = getEnvironmentValue(key);
        }

        // The third priority is to obtain the key name from the environment
        // variable with all capital letters and underlined splicing.
        if (isNull(value)) {
            value = getEnvironmentValue(toUpperEnvironmentKey(key));
        }

        return value;
    }

    @Override
    public Object get(Object key) {
        return getProperty((String) key);
    }

    protected String getEnvironmentValue(String key) {
        return System.getenv().get(key);
    }

    public static String toUpperEnvironmentKey(String key) {
        key = trimToEmpty(key);
        if (key.contains(".")) {
            return key.replace(".", "_").toUpperCase();
        } else {
            return humpToLine(key).toUpperCase();
        }
    }

    public static String humpToLine(String key) {
        Matcher matcher = Pattern.compile("[A-Z]").matcher(key);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}

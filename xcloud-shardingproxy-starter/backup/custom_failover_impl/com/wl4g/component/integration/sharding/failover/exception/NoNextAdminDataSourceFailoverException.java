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
package com.wl4g.component.integration.sharding.failover.exception;

/**
 * {@link NoNextAdminDataSourceFailoverException}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-27 v1.0.0
 * @since v1.0.0
 */
public class NoNextAdminDataSourceFailoverException extends FailoverException {
    private static final long serialVersionUID = 841579612586305322L;

    public NoNextAdminDataSourceFailoverException() {
        this(null, null);
    }

    public NoNextAdminDataSourceFailoverException(String message) {
        this(message, null);
    }

    public NoNextAdminDataSourceFailoverException(String message, Exception cause) {
        super(message, cause);
    }

    public NoNextAdminDataSourceFailoverException(Exception cause) {
        super(cause);
    }

}

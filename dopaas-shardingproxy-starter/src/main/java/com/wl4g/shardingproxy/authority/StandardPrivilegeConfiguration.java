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

import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.component.common.serialize.JacksonUtils.parseJSON;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link StandardPrivilegeConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-01-20 v1.0.0
 * @since v1.0.0
 */
public class StandardPrivilegeConfiguration {

    @Getter
    @Setter
    private Map<String, String> granted = new HashMap<>();
    @Getter
    @Setter
    private Map<String, List<PrivilegeSpec>> privileges = new HashMap<>();

    // ----- Merged granted and privileges. -----

    @Getter
    private final Map<String, List<String>> mergedGranted = new HashMap<>();
    @Getter
    private final Map<String, List<PrivilegeSpec>> mergedPrivileges = new HashMap<>();

    public static StandardPrivilegeConfiguration build(final String json) {
        if (isBlank(json)) {
            return new StandardPrivilegeConfiguration();
        }
        StandardPrivilegeConfiguration that = parseJSON(json, StandardPrivilegeConfiguration.class);
        // Merging granted collection.
        safeMap(that.getGranted()).forEach((username, privilegeNames) -> that.getMergedGranted().put(username,
                Splitter.on(",").omitEmptyStrings().trimResults().splitToList(privilegeNames)));
        // Merging privileges collection.
        safeMap(that.getMergedGranted()).forEach((username, privilegeNames) -> that.getMergedPrivileges().put(username,
                safeMap(that.getPrivileges()).entrySet().stream().filter(s -> safeList(privilegeNames).contains(s.getKey()))
                        .map(e -> e.getValue()).flatMap(s -> s.stream()).collect(toList())));
        return that;
    }

    @Getter
    @Setter
    @ToString
    public static class PrivilegeSpec {
        private SelectSpec select = SelectSpec.EMPTY;
        private InsertSpec insert = InsertSpec.EMPTY;
        private UpdateSpec update = UpdateSpec.EMPTY;
        private DeleteSpec delete = DeleteSpec.EMPTY;
        private AlertDatabaseSpec alertDatabase = AlertDatabaseSpec.EMPTY;
        private AlertTableSpec alertTable = AlertTableSpec.EMPTY;
        private CreateDatabaseSpec createDatabase = CreateDatabaseSpec.EMPTY;
        private CreateTableSpec createTable = CreateTableSpec.EMPTY;
        private CreateFunctionSpec createFunction = CreateFunctionSpec.EMPTY;
        private DropDatabaseSpec dropDatabase = DropDatabaseSpec.EMPTY;
        private DropTableSpec dropTable = DropTableSpec.EMPTY;
        private TruncateSpec truncate = TruncateSpec.EMPTY;
        private List<String> anyBlacklistSQLs = new ArrayList<>();
    }

    @Getter
    @Setter
    @ToString
    public static class SelectSpec {
        public static final SelectSpec EMPTY = new SelectSpec();
        private boolean requiredWhereCondidtion = false;
    }

    @Getter
    @Setter
    @ToString
    public static class InsertSpec {
        public static final InsertSpec EMPTY = new InsertSpec();
        private boolean anyDenied = false;
    }

    @Getter
    @Setter
    @ToString
    public static class UpdateSpec {
        public static final UpdateSpec EMPTY = new UpdateSpec();
        private boolean requiredWhereCondidtion = false;
    }

    @Getter
    @Setter
    @ToString
    public static class DeleteSpec {
        public static final DeleteSpec EMPTY = new DeleteSpec();
        private boolean requiredWhereCondidtion = false;
    }

    @Getter
    @Setter
    @ToString
    public static class AlertDatabaseSpec {
        public static final AlertDatabaseSpec EMPTY = new AlertDatabaseSpec();
    }

    @Getter
    @Setter
    @ToString
    public static class AlertTableSpec {
        public static final AlertTableSpec EMPTY = new AlertTableSpec();
    }

    @Getter
    @Setter
    @ToString
    public static class CreateDatabaseSpec {
        public static final CreateDatabaseSpec EMPTY = new CreateDatabaseSpec();
    }

    @Getter
    @Setter
    @ToString
    public static class CreateTableSpec {
        public static final CreateTableSpec EMPTY = new CreateTableSpec();
    }

    @Getter
    @Setter
    @ToString
    public static class CreateFunctionSpec {
        public static final CreateFunctionSpec EMPTY = new CreateFunctionSpec();
    }

    @Getter
    @Setter
    @ToString
    public static class DropDatabaseSpec {
        public static final DropDatabaseSpec EMPTY = new DropDatabaseSpec();
    }

    @Getter
    @Setter
    @ToString
    public static class DropTableSpec {
        public static final DropTableSpec EMPTY = new DropTableSpec();
    }

    @Getter
    @Setter
    @ToString
    public static class TruncateSpec {
        public static final TruncateSpec EMPTY = new TruncateSpec();
    }
}

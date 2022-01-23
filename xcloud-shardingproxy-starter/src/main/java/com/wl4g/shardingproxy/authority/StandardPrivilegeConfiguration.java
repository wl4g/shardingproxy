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
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link StandardPrivilegeConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-01-20 v1.0.0
 * @since v1.0.0
 */
@Getter
@Setter
public class StandardPrivilegeConfiguration {
    private Map<String, List<String>> granted = new HashMap<>();
    private Map<String, List<StrategySpec>> strategy = new HashMap<>();

    // Merged granted collection.
    private Map<String, Set<StrategySpec>> merged = new HashMap<>();

    public static StandardPrivilegeConfiguration build(final String json) {
        if (isBlank(json)) {
            return new StandardPrivilegeConfiguration();
        }
        StandardPrivilegeConfiguration that = parseJSON(json, StandardPrivilegeConfiguration.class);
        // Merging granted strategy.
        safeMap(that.getGranted()).forEach((username, strategyNames) -> that.getMerged().put(username,
                safeMap(that.getStrategy()).entrySet().stream().filter(s -> safeList(strategyNames).contains(s.getKey()))
                        .map(e -> e.getValue()).flatMap(s -> s.stream()).collect(toSet())));
        return that;
    }

    @Getter
    @Setter
    public static class StrategySpec {
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
    public static class SelectSpec {
        public static final SelectSpec EMPTY = new SelectSpec();
    }

    @Getter
    @Setter
    public static class InsertSpec {
        public static final InsertSpec EMPTY = new InsertSpec();
    }

    @Getter
    @Setter
    public static class UpdateSpec {
        public static final UpdateSpec EMPTY = new UpdateSpec();
        private boolean requiredWhereCondidtion = false;
    }

    @Getter
    @Setter
    public static class DeleteSpec {
        public static final DeleteSpec EMPTY = new DeleteSpec();
        private boolean requiredWhereCondidtion = false;
    }

    @Getter
    @Setter
    public static class AlertDatabaseSpec {
        public static final AlertDatabaseSpec EMPTY = new AlertDatabaseSpec();
    }

    @Getter
    @Setter
    public static class AlertTableSpec {
        public static final AlertTableSpec EMPTY = new AlertTableSpec();
    }

    @Getter
    @Setter
    public static class CreateDatabaseSpec {
        public static final CreateDatabaseSpec EMPTY = new CreateDatabaseSpec();
    }

    @Getter
    @Setter
    public static class CreateTableSpec {
        public static final CreateTableSpec EMPTY = new CreateTableSpec();
    }

    @Getter
    @Setter
    public static class CreateFunctionSpec {
        public static final CreateFunctionSpec EMPTY = new CreateFunctionSpec();
    }

    @Getter
    @Setter
    public static class DropDatabaseSpec {
        public static final DropDatabaseSpec EMPTY = new DropDatabaseSpec();
    }

    @Getter
    @Setter
    public static class DropTableSpec {
        public static final DropTableSpec EMPTY = new DropTableSpec();
    }

    @Getter
    @Setter
    public static class TruncateSpec {
        public static final TruncateSpec EMPTY = new TruncateSpec();
    }
}

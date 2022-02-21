///*
// * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.wl4g.shardingproxy.agent.event;
//
//import java.util.LinkedList;
//
//import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
//import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;
//
//import com.google.common.eventbus.Subscribe;
//import com.wl4g.component.common.annotation.Reserved;
//
//import lombok.Getter;
//import lombok.extern.slf4j.Slf4j;
//
///**
// * {@link DatabaseDiscoveryEventHandler}
// * 
// * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
// * @version 2021-12-24 v1.0.0
// * @since v1.0.0
// * @see {@link org.apache.shardingsphere.dbdiscovery.mgr.MGRDatabaseDiscoveryType#updatePrimaryDataSource()}
// * @see {@link org.apache.shardingsphere.agent.metrics.prometheus.collector.ProxyInfoCollector}
// */
//@Getter
//@Slf4j
//@Reserved
//public class DatabaseDiscoveryEventHandler {
//
//    private final LinkedList<PrimaryDataSourceChangedEvent> eventQueue = new LinkedList<>();
//
//    private DatabaseDiscoveryEventHandler() {
//        ShardingSphereEventBus.getInstance().register(this);
//    }
//
//    /**
//     * {@link org.apache.shardingsphere.mode.manager.cluster.coordinator.ClusterContextManagerCoordinator#renew()}
//     * {@link org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.subscriber.StorageNodeStatusSubscriber#update()}
//     */
//    @Subscribe
//    public void onPrimaryDataSourceChanged(PrimaryDataSourceChangedEvent event) {
//        log.warn("Processing event: ({}), queue: {}, - {}.{}.{}", PrimaryDataSourceChangedEvent.class.getSimpleName(),
//                eventQueue.size(), event.getSchemaName(), event.getGroupName(), event.getDataSourceName());
//
//        // Add metrics queue.
//        if (eventQueue.size() > 16) {
//            eventQueue.pollFirst();
//        }
//        eventQueue.add(event);
//
//        // TODO Event notification ...
//    }
//
//    public static DatabaseDiscoveryEventHandler getInstance() {
//        return DbDiscoveryEventHandlerHolder.INSTANCE;
//    }
//
//    private static final class DbDiscoveryEventHandlerHolder {
//        private static final DatabaseDiscoveryEventHandler INSTANCE = new DatabaseDiscoveryEventHandler();
//    }
//
//}

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
package com.wl4g.component.integration.sharding.failover.jdbc;

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static java.util.Objects.nonNull;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.GenerousBeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

/**
 * {@link JdbcOperator}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-18 v1.0.0
 * @since v1.0.0
 */
public class JdbcOperator implements Closeable {
    private final Connection connection;

    public JdbcOperator(DataSource dataSource) {
        notNullOf(dataSource, "dataSource");
        try {
            this.connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public Map<String, Object> findOneMap(String sql, Object[] params) throws SQLException {
        QueryRunner qr = new QueryRunner();
        return qr.query(getConnection(), sql, new MapHandler(), params);
    }

    public <T> T findOneBean(String sql, Object[] params, Class<T> clazz) throws SQLException {
        QueryRunner qr = new QueryRunner();
        RowProcessor processor = new BasicRowProcessor(new GenerousBeanProcessor());
        return qr.query(getConnection(), sql, new BeanHandler<T>(clazz, processor), params);
    }

    public List<Map<String, Object>> findAllMap(String sql, Object[] params) throws SQLException {
        QueryRunner qr = new QueryRunner();
        return qr.query(getConnection(), sql, new MapListHandler(), params);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> List<T> findAllBean(String sql, Object[] params, Class<T> clazz) throws SQLException {
        QueryRunner qr = new QueryRunner();
        RowProcessor processor = new BasicRowProcessor(new GenerousBeanProcessor());
        return (List<T>) qr.query(getConnection(), sql, new BeanListHandler(clazz, processor), params);
    }

    public Object findOneCol(String sql, Object[] params, String colName) throws SQLException {
        QueryRunner qr = new QueryRunner();
        if (colName == null || colName.trim().length() == 0) {
            return qr.query(getConnection(), sql, new ScalarHandler<Object>(), params);
        } else {
            return qr.query(getConnection(), sql, new ScalarHandler<Object>(colName), params);
        }
    }

    public int insert(String sql, Object[] params) throws SQLException {
        QueryRunner qr = new QueryRunner();
        return qr.update(getConnection(), sql, params);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object insert(String sql, Object[] params, Class<?> clazz) throws SQLException {
        QueryRunner qr = new QueryRunner();
        RowProcessor processor = new BasicRowProcessor(new GenerousBeanProcessor());
        return qr.insert(getConnection(), sql, new BeanHandler(clazz, processor), params);
    }

    public void insertBatch(String sql, Object[][] params) throws SQLException {
        QueryRunner qr = new QueryRunner();
        qr.batch(getConnection(), sql, params);
    }

    public int update(String sql, Object[] params) throws SQLException {
        QueryRunner qr = new QueryRunner();
        return qr.update(getConnection(), sql, params);
    }

    public int[] updateBatch(String sql, Object[][] params) throws SQLException {
        QueryRunner qr = new QueryRunner();
        return qr.batch(getConnection(), sql, params);
    }

    public int delete(String sql, Object[] params) throws SQLException {
        return update(sql, params);
    }

    public int[] deleteBatch(String sql, Object[][] params) throws SQLException {
        return updateBatch(sql, params);
    }

    private Connection getConnection() throws SQLException {
        return connection;
    }

    @Override
    public void close() throws IOException {
        if (nonNull(connection)) {
            try {
                getConnection().close();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

}

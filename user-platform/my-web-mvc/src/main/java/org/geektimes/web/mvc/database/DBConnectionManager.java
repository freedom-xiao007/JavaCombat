/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.geektimes.web.mvc.database;

import org.geektimes.web.mvc.log.MyLogger;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * @author lw1243925457
 */
public class DBConnectionManager {

    private MyLogger log = new MyLogger(DBConnectionManager.class.getName());

    @Resource(name = "jdbc/UserPlatformDB")
    private DataSource dataSource;

    public Connection getConnection() {
        // 依赖查找
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        if (connection != null) {
            log.info("获取 JNDI 数据库连接成功！");
        }
        return connection;
    }
}

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package org.wushujames.connect.mysql;

import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Very simple connector that works with the console. This connector supports both source and
 * sink modes via its 'mode' setting.
 */
public class MySqlSourceConnector extends SourceConnector {
    private static final Logger log = LoggerFactory.getLogger(MySqlSourceConnector.class);

    public static final String HOST_CONFIG = "host";
    public static final String USER_CONFIG = "user";
    public static final String PASSWORD_CONFIG = "password";
    public static final String PORT_CONFIG = "port";
    public static final String REPLICATION_HOST_CONFIG = "replication_host";
    public static final String REPLICATION_USER_CONFIG = "replication_user";
    public static final String REPLICATION_PASSWORD_CONFIG = "replication_password";
    public static final String REPLICATION_PORT_CONFIG = "replication_port";
    public static final String MAXWELL_SCHEMA_DATABASE = "schema_database";
    public static final String WHITELIST_DATABASES = "include_dbs";
    public static final String WHITELIST_TABLES = "include_tables";
    public static final String TOPIC_PREFIX = "topic_prefix";

    private String host;
    private String user;
    private String password;
    private String port;
    private String replication_host;
    private String replication_user;
    private String replication_password;
    private String replication_port;
    private String maxwell_schema_database;
    private String whitelist_dbs;
    private String whitelist_tables;
    private String topic_prefix;
    

    @Override
    public void start(Map<String, String> props) {
        host = props.get(HOST_CONFIG);
        user = props.get(USER_CONFIG);
        password = props.get(PASSWORD_CONFIG);
        port = props.get(PORT_CONFIG);
        replication_host = props.get(REPLICATION_HOST_CONFIG);
        replication_user = props.get(REPLICATION_USER_CONFIG);
        replication_password = props.get(REPLICATION_PASSWORD_CONFIG);
        replication_port = props.get(REPLICATION_PORT_CONFIG);
        maxwell_schema_database = props.get(MAXWELL_SCHEMA_DATABASE);
        whitelist_dbs = props.get(WHITELIST_DATABASES);
        whitelist_tables = props.get(WHITELIST_TABLES);
        topic_prefix = props.get(TOPIC_PREFIX);
        
        if (host == null || host.isEmpty()) {
            throw new ConnectException("MySqlSourceConnector configuration must include 'host' setting");
        }
        if (user == null || user.isEmpty()) {
            throw new ConnectException("MySqlSourceConnector configuration must include 'user' setting");
        }
        if (password == null || password.isEmpty()) {
            throw new ConnectException("MySqlSourceConnector configuration must include 'password' setting");
        }
    }

    @Override
    public Class<? extends Task> taskClass() {
        return MySqlSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        ArrayList<Map<String, String>> configs = new ArrayList<>();
        // Only one input stream makes sense.
        Map<String, String> config = new HashMap<>();
        config.put("user", user);
        config.put("password", password);
        config.put("host", host);
        config.put("port", port);
        config.put("bootstrapper", "none");

        if ( replication_host == null || replication_user == null || replication_password == null || replication_port == null ) {
            config.put("replication_host", host);
            config.put("replication_password", password);
            config.put("replication_user", user);
            config.put("replication_port", port);
        } else {
            config.put("replication_host", replication_host);
            config.put("replication_password", replication_password);
            config.put("replication_user", replication_user);
            config.put("replication_port", replication_port);
        }

        if ( maxwell_schema_database == null || maxwell_schema_database.isEmpty() ) {
            maxwell_schema_database = "maxwell";
        }
        config.put("schema_database", maxwell_schema_database);

        if ( whitelist_dbs != null) {
            config.put("include_dbs", whitelist_dbs);
        }

        if ( whitelist_tables != null ) {
            config.put("include_tables", whitelist_tables);
        }

        if ( topic_prefix != null ) {
            if ( !topic_prefix.isEmpty() ) {
                config.put("topic_prefix", topic_prefix);
            }
        } else {
            config.put("topic_prefix", "maxwell");
        }
        configs.add(config);
        return configs;
    }

    @Override
    public void stop() {
        // Nothing to do since MySqlSourceConnector has no background monitoring.
    }

    @Override
    public String version() {
        return AppInfoParser.getVersion();
    }
}

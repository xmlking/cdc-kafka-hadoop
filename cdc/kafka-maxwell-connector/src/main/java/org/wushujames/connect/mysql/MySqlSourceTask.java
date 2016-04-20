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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import com.zendesk.maxwell.*;
import com.zendesk.maxwell.schema.ddl.InvalidSchemaError;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.or.common.glossary.Row;
import com.zendesk.maxwell.schema.SchemaCapturer;
import com.zendesk.maxwell.schema.SchemaStore;
import com.zendesk.maxwell.schema.Table;
import com.zendesk.maxwell.schema.ddl.InvalidSchemaError;


/**
 * MySqlSourceTask reads from stdin or a file.
 */
public class MySqlSourceTask extends SourceTask {
    private static final Logger log = LoggerFactory.getLogger(MySqlSourceTask.class);
    private final long MAX_IN_MEMORY_ELEMENTS = 10000;
    private static final String FILENAME_FIELD = "filename";
    private static final String POSITION_FIELD = "position";
    private static final ArrayList<String> MAXWELL_OPTIONS = new ArrayList<String>(
            Arrays.asList(
                "host",
                "replication_host",
                "port",
                "replication_port",
                "user",
                "replication_user",
                "password",
                "replication_password",
                "bootstrapper",
                "schema_database",
                "include_dbs",
                "include_tables"));

    private com.zendesk.maxwell.schema.Schema schema;
    private MaxwellConfig config;
    private MaxwellContext maxwellContext;
    private MaxwellReplicator replicator;
    private String topic_prefix;


    @Override
    public void start(Map<String, String> props) {
        try {
            Map<String, String> maxwellProps = new HashMap<String, String>();
            for (String key: props.keySet()) {
                if( MAXWELL_OPTIONS.contains(key)){
                    maxwellProps.put(key,props.get(key));
                    log.debug("Setting Maxwell Prop: " + key + " => " + maxwellProps.get(key));
                }
                if (key == "topic_prefix") {
                    this.topic_prefix = props.get(key);
                }
            }
            this.config = new MaxwellConfig(maxwellProps);

            BinlogPosition startAt = null;
            OffsetStorageReader offsetStorageReader = this.context.offsetStorageReader();
            Map<String, Object> offsetFromCopycat = offsetStorageReader.offset(sourcePartition());
            if (offsetFromCopycat != null) {
                System.out.println("have copycat offsets! " + offsetFromCopycat);
                startAt = new BinlogPosition((long) offsetFromCopycat.get(POSITION_FIELD),
                        (String) offsetFromCopycat.get(FILENAME_FIELD));
                if ( startAt != null) {
                    this.config.initPosition = startAt;
                }
            }

            if ( this.config.log_level != null )
                MaxwellLogging.setLevel(this.config.log_level);

            this.maxwellContext = new MaxwellContext(this.config);

            try ( Connection connection = this.maxwellContext.getReplicationConnectionPool().getConnection(); Connection schemaConnection = this.maxwellContext.getMaxwellConnectionPool().getConnection() ) {
                MaxwellMysqlStatus.ensureReplicationMysqlState(connection);
                MaxwellMysqlStatus.ensureMaxwellMysqlState(schemaConnection);

                SchemaStore.ensureMaxwellSchema(schemaConnection, this.config.databaseName);
                schemaConnection.setCatalog(this.config.databaseName);
                SchemaStore.upgradeSchemaStoreSchema(schemaConnection, this.config.databaseName);

                SchemaStore.handleMasterChange(schemaConnection, this.maxwellContext.getServerID(), this.config.databaseName);

                if ( startAt != null) {
                    log.info("Maxwell is booting, starting at " + startAt);
                    SchemaStore store = SchemaStore.restore(schemaConnection, this.maxwellContext);
                    this.schema = store.getSchema();
                } else {
                    log.info("no copycat offsets!");
                    initFirstRun(connection, schemaConnection);
                }
            } catch ( SQLException e ) {
                log.error("SQLException: " + e.getLocalizedMessage());
                log.error(e.getLocalizedMessage());
                throw e;
            }

            // TODO Auto-generated method stub
            this.replicator = new MaxwellReplicator(this.schema, null /* producer */, null /* bootstrapper */, this.maxwellContext, this.maxwellContext.getInitialPosition());
            try {
                replicator.setFilter(this.maxwellContext.buildFilter());
            } catch (MaxwellInvalidFilterException e) {
                log.error("Invalid maxwell filter", e);
                System.exit(1);
            }
            this.maxwellContext.start();

            this.replicator.beforeStart(); // starts open replicator

        } catch (Exception e) {
            throw new ConnectException("Error Initializing Maxwell", e);
        }
    }

    private void initFirstRun(Connection connection, Connection schemaConnection) throws SQLException, IOException, InvalidSchemaError {
        log.info("Maxwell is capturing initial schema");
        SchemaCapturer capturer = new SchemaCapturer(connection, this.maxwellContext.getCaseSensitivity());
        this.schema = capturer.capture();
        BinlogPosition pos = BinlogPosition.capture(connection);
        SchemaStore store = new SchemaStore(schemaConnection, this.maxwellContext.getServerID(), this.schema, pos, this.config.databaseName);
        store.save();
        this.maxwellContext.setPosition(pos);
    }



    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        ArrayList<SourceRecord> records = new ArrayList<SourceRecord>();
        boolean checkpoint = false;
        while(!checkpoint) {
            RowMap event;
            try {
                event = replicator.getRow();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return records;
            }
            try {
                this.maxwellContext.ensurePositionThread();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return records;
            }

            if (event == null) {
                return records;
            }

            if (event.getDatabase().equals(this.config.databaseName)) {
                return records;
            }

            String databaseName = event.getDatabase();
            String tableName = event.getTable();

            String topicName = databaseName + "." + tableName;
            if ( this.topic_prefix != null && !this.topic_prefix.isEmpty() ) {
                topicName = this.topic_prefix + "." + databaseName + "." + tableName;
            }


            Table table = replicator.getSchema().findDatabase(databaseName).findTable(tableName);

            Schema pkSchema = DataConverter.convertPrimaryKeySchema(table);

            Struct pkStruct = DataConverter.convertPrimaryKeyData(pkSchema, table, event);

            // create schema
            Schema rowSchema = DataConverter.convertRowSchema(table);

            Struct rowStruct = DataConverter.convertRowData(rowSchema, table, event);

            SourceRecord rec = new SourceRecord(
                    sourcePartition(),
                    sourceOffset(event),
                    topicName,
                    null, //partition
                    pkSchema,
                    pkStruct,
                    rowSchema,
                    rowStruct);


            records.add(rec);

            if ( records.size() > 10000 ) {
                    break;
            }

        }
        return records;

    }

	@Override
    public void stop() {
        log.trace("Stopping");
        try {
            this.replicator.beforeStop();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Map<String, String> sourcePartition() {
        return Collections.singletonMap("host", "192.168.59.103");
    }

    private Map<String, Object> sourceOffset(RowMap row) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put(POSITION_FIELD, row.getPosition().getOffset());
        m.put(FILENAME_FIELD, row.getPosition().getFile());

        return m;
    }

    @Override
    public String version() {
        return new MySqlSourceConnector().version();
    }
}

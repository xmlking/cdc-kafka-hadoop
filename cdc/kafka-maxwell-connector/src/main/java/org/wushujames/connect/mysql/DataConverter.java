package org.wushujames.connect.mysql;

import com.zendesk.maxwell.schema.columndef.*;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;

import com.zendesk.maxwell.schema.Table;
import com.zendesk.maxwell.RowMap;

/**
 * 
 * DataConverter handles translating Maxwell schemas to Kafka Connect schemas and row data to Kafka
 * Connect records.
 * 
 * @author jylcheng, Kristian Kaufmann
 *
 */
public class DataConverter {

    public static Schema convertPrimaryKeySchema(Table table) {
        String tableName = table.getName();
        String databaseName = table.getDatabase();
        SchemaBuilder pkBuilder = SchemaBuilder.struct().name(databaseName + "." + tableName + ".pk");

        for (String pk : table.getPKList()) {
            int columnNumber = table.findColumnIndex(pk);
            addFieldSchema(table, columnNumber, pkBuilder);
        }
        return pkBuilder.build();
    }

    public static Schema convertRowSchema(Table table) {
        String tableName = table.getName();
        String databaseName = table.getDatabase();
        SchemaBuilder builder = SchemaBuilder.struct().name( databaseName + "." + tableName);
        SchemaBuilder dataBuilder = SchemaBuilder.struct().name("data");
        SchemaBuilder beforeBuilder = SchemaBuilder.struct().name("before");

        for (int columnNumber = 0; columnNumber < table.getColumnList().size(); columnNumber++) {
            addFieldSchema(table, columnNumber, dataBuilder);
            addFieldSchema(table, columnNumber, beforeBuilder);
        }
        builder.field("data", dataBuilder.build());
        builder.field("old", beforeBuilder.build());
        builder.field("database", Schema.STRING_SCHEMA);
        builder.field("table", Schema.STRING_SCHEMA);
        builder.field("mut_type", Schema.STRING_SCHEMA);
        builder.field("xid", Schema.INT64_SCHEMA);
        builder.field("ts", Schema.INT64_SCHEMA);
        return builder.build();
    }

    private static void addFieldSchema(Table table, int columnNumber,
            SchemaBuilder builder) {
        // TODO Auto-generated method stub
        ColumnDef def = table.getColumnList().get(columnNumber);
        String columnName = def.getName();
        switch (def.getType()) {
            case "tinyint": //TINYINT
                builder.field(columnName, Schema.OPTIONAL_INT16_SCHEMA);
                break;
            case "smallint":
            case "mediumint":
            case "int":
            case "year":
                builder.field(columnName, Schema.OPTIONAL_INT64_SCHEMA);
                break;
            case "tinytext":
            case "text":
            case "mediumtext":
            case "longtext":
            case "varchar":
            case "char":
                builder.field(columnName, Schema.OPTIONAL_STRING_SCHEMA);
                break;
            case "tinyblob":
            case "blob":
            case "mediumblob":
            case "longblob":
            case "binary":
            case "varbinary":
            case "bit":
                builder.field(columnName, Schema.OPTIONAL_BYTES_SCHEMA);
                break;
            case "bigint":
                builder.field(columnName, Schema.OPTIONAL_INT64_SCHEMA);
                break;
            case "float":
            case "double":
                builder.field(columnName, Schema.OPTIONAL_FLOAT32_SCHEMA);
                break;
            case "decimal":
                builder.field(columnName, Schema.OPTIONAL_BYTES_SCHEMA);
                break;
            case "geometry":
            case "geometrycollection":
            case "linestring":
            case "multilinestring":
            case "multipoint":
            case "multipolygon":
            case "polygon":
            case "point":
                builder.field(columnName, Schema.OPTIONAL_STRING_SCHEMA);
                break;
            case "date":
            case "datetime":
            case "timestamp":
            case "time":
                builder.field(columnName, Schema.OPTIONAL_STRING_SCHEMA);
                break;
            case "enum":
                builder.field(columnName, Schema.OPTIONAL_STRING_SCHEMA);
                break;
            case "set":
                SchemaBuilder setSchema = SchemaBuilder.type(Schema.Type.ARRAY);
                EnumeratedColumnDef enumDef = (EnumeratedColumnDef) table.getColumnList().get(columnNumber);
                for(String s: enumDef.getEnumValues()) {
                    setSchema.field(s, Schema.OPTIONAL_BOOLEAN_SCHEMA);
                }
                builder.field(columnName, setSchema.build());
                break;
            default:
                throw new RuntimeException("unsupported type");
        }

    }

    static Struct convertPrimaryKeyData(Schema pkSchema, Table table, RowMap row) {
        Struct pkStruct = new Struct(pkSchema);

        for (String pk : table.getPKList()) {
            int idx = table.findColumnIndex(pk);
            ColumnDef def = table.getColumnList().get(idx);
            String columnName = def.getName();
            Object column = row.getData(columnName);
            addFieldData(pkStruct, def, column);
        }
        return pkStruct;
    }

    private static void addFieldData(Struct struct, ColumnDef columnDef,
            Object columnValue) {
        String columnName = columnDef.getName();
        switch (columnDef.getType()) {
            case "tinyint":
                IntColumnDef shortIntDef = (IntColumnDef) columnDef;
                Long l1 = (Long) shortIntDef.asJSON(columnValue);
                struct.put(columnName, l1.shortValue());
                break;
            case "smallint":
            case "mediumint":
            case "int":
                IntColumnDef intDef = (IntColumnDef) columnDef;
                struct.put(columnName, intDef.asJSON(columnValue));
                break;
            case "tinytext":
            case "text":
            case "mediumtext":
            case "longtext":
            case "varchar":
            case "char":
                StringColumnDef strDef = (StringColumnDef) columnDef;
                struct.put(columnName, strDef.asJSON(columnValue));
                break;
            case "tinyblob":
            case "blob":
            case "mediumblob":
            case "longblob":
            case "binary":
            case "varbinary":
            case "bit":
                byte[] bytes = (byte []) columnValue;
                struct.put(columnName, bytes);
            case "bigint":
                BigIntColumnDef bigIntDef = (BigIntColumnDef) columnDef;
                struct.put(columnName, bigIntDef.asJSON(columnValue));
                break;
            case "float":
            case "double":
                struct.put(columnName, columnValue);
                break;
            case "decimal":
                struct.put(columnName, (byte []) columnValue);
                break;
            case "geometry":
            case "geometrycollection":
            case "linestring":
            case "multilinestring":
            case "multipoint":
            case "multipolygon":
            case "polygon":
            case "point":
                struct.put(columnName, (byte []) columnValue);
                break;
            case "date":
                DateColumnDef dateDef = (DateColumnDef) columnDef;
                struct.put(columnName, dateDef.toSQL(columnValue));
                break;
            case "datetime":
            case "timestamp":
                DateTimeColumnDef dtDef = (DateTimeColumnDef) columnDef;
                struct.put(columnName, dtDef.toSQL(columnValue));
                break;
            case "year":
                YearColumnDef yearDef = (YearColumnDef) columnDef;
                struct.put(columnName, yearDef.toSQL(columnValue));
                break;
            case "time":
                TimeColumnDef timeDef = (TimeColumnDef) columnDef;
                struct.put(columnName, timeDef.toSQL(columnValue));
                break;
            case "enum":
                EnumColumnDef enumDef = (EnumColumnDef) columnDef;
                struct.put(columnName, enumDef.asJSON(columnValue));
                break;
            case "set":
                SetColumnDef setDef = (SetColumnDef) columnDef;
                SchemaBuilder setSchema = SchemaBuilder.type(Schema.Type.ARRAY);
                for(String element: setDef.getEnumValues()) {
                    setSchema.field(element, Schema.OPTIONAL_BOOLEAN_SCHEMA);
                }
                Struct set_struct = new Struct(setSchema);
                String[] elements = setDef.asJSON(columnValue).toString().split(",");
                for(String element: elements) {
                    set_struct.put(element, true);
                }
                struct.put(columnName, set_struct);
                break;
            default:
                throw new RuntimeException("unsupported type");
        }
    }

    static Struct convertRowData(Schema rowSchema, Table table, RowMap row) {
        Struct dataStruct = new Struct(rowSchema.field("data").schema());
        Struct oldStruct = new Struct(rowSchema.field("old").schema());
        for (int columnNumber = 0; columnNumber < table.getColumnList().size(); columnNumber++) {
            ColumnDef def = table.getColumnList().get(columnNumber);
            String columnName = def.getName();
            Object newValue = null;
            if( !row.hasData(columnName) ) {
                dataStruct.put(columnName,null);
            } else {
                newValue = row.getData(columnName);
                if (newValue == null) {
                    dataStruct.put(columnName, null);
                } else {
                    addFieldData(dataStruct, def, newValue);
                }
            }
            Object oldValue = null;
            if( row.getRowType() == "update" && row.hasOldData(columnName) ) {
                oldValue = row.getOldData(columnName);
                if (oldValue == null) {
                    oldStruct.put(columnName, null);
                } else {
                    addFieldData(oldStruct, def, oldValue);
                }
            } else {
                if(row.getRowType() == "update" && newValue != null) {
                    addFieldData(oldStruct,def,newValue);
                } else {
                    oldStruct.put(columnName, null);
                }
            }
        }
        Struct rowStruct = new Struct(rowSchema);
        rowStruct.put("data",dataStruct);
        rowStruct.put("old", oldStruct);
        rowStruct.put("database", row.getDatabase());
        rowStruct.put("table", row.getTable());
        rowStruct.put("ts", row.getTimestamp());
        rowStruct.put("xid", row.getXid());
        rowStruct.put("mut_type", row.getRowType());
        return rowStruct;
    }

}

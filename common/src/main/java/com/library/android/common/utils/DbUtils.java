package com.library.android.common.utils;

import android.provider.BaseColumns;

import com.library.android.common.appconstants.AppConstants;
import com.library.android.common.data.Column;

import java.util.List;

import static android.util.Log.d;

public class DbUtils implements BaseColumns {

    public static final String COLUMN_TYPE_TEXT = "TEXT NOT NULL";
    public static final String COLUMN_TYPE_TEXT_NULLABLE = "TEXT";
    public static final String COLUMN_TYPE_INTEGER = "INTEGER NOT NULL DEFAULT 0";
    public static final String COLUMN_TYPE_REAL = "REAL NOT NULL DEFAULT 0";

    /**
     * Gives SQLite create table query
     * <p>
     * Do not include primary key autoincrement integer id in {@code columnList}
     * as we are adding it here to reduce repetitive code
     * <p>
     * @param tableName Name of the table to be created
     * @param columnList List of columns to be included in table
     * @return String SQLite create table query
     * @since   1.0
     */
    public static String getCreateTableQuery(String tableName, List<Column> columnList) {
        if (columnList != null && columnList.size() > 0) {

            StringBuilder baseCreateQuery = new StringBuilder("CREATE TABLE " + tableName + " (");

            // Note: 11/24/2018 by sagar  This can be improvised further by not limiting the check for only 0th position
            if (!columnList.get(0).getColumnName().equalsIgnoreCase(BaseColumns._ID)) {
                columnList.add(0, new Column(BaseColumns._ID, "INTEGER PRIMARY KEY AUTOINCREMENT"));
            }

            for (Column column : columnList) {
                baseCreateQuery.append(column.getColumnName());
                baseCreateQuery.append(" ");
                baseCreateQuery.append(StringUtils.getDefaultString(column.getColumnProperties(), "TEXT"));
                baseCreateQuery.append(",");
            }

            baseCreateQuery.deleteCharAt(baseCreateQuery.length() - 1);
            baseCreateQuery.append(")");
            d(AppConstants.TAG, "DbUtils: getCreateTableQuery: " + baseCreateQuery.toString());
            return baseCreateQuery.toString();
        }
        return "";
    }

    /**
     * Drops given table if it is exist. More than delete. Removes the table completely with table structure.
     *
     * @since 1.0
     */
    public static String getDropTableQuery(String tableName) {
        return
                "DROP TABLE IF EXISTS " + tableName;
    }
}

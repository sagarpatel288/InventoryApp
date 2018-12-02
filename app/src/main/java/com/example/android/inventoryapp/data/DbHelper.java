package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;
import com.library.android.common.data.Column;
import com.library.android.common.utils.DbUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;


/**
 * Database helper class. All SQLiteDatabase related properties such as database version,
 * queries etc... are defined here.
 */
public class DbHelper extends SQLiteOpenHelper {

    /**
     * Application Database name
     *
     * @since 1.0
     */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Current application database version
     *
     * @since 1.0
     */
    // Note: 11/27/2018 by sagar  v2 change: Added column: unit price
    // Note: 11/27/2018 by sagar  v3 change: Added column: image string (uri) path
    private static final int DATABASE_VERSION = 3;


    /**
     * Public constructor of the class that will call parent constructor to create relevant SQLite database via super call
     *
     * @param context {@link Context}
     */
    public DbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbUtils.getCreateTableQuery(ProductEntry.TABLE_NAME, getInventoryColumns()));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DbUtils.getDropTableQuery(ProductEntry.TABLE_NAME));
        onCreate(db);
    }

    private List<Column> getInventoryColumns() {
        List<Column> columnList = new ArrayList<>();
        columnList.add(new Column(ProductEntry.COLUMN_PRODUCT_NAME, DbUtils.COLUMN_TYPE_TEXT));
        columnList.add(new Column(ProductEntry.COLUMN_PRODUCT_IMAGE, DbUtils.COLUMN_TYPE_TEXT_NULLABLE));
        columnList.add(new Column(ProductEntry.COLUMN_UNIT_PRICE, DbUtils.COLUMN_TYPE_REAL));
        columnList.add(new Column(ProductEntry.COLUMN_SUPPLIER_NAME, DbUtils.COLUMN_TYPE_TEXT));
        columnList.add(new Column(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, DbUtils.COLUMN_TYPE_TEXT));
        columnList.add(new Column(ProductEntry.COLUMN_QUANTITY, DbUtils.COLUMN_TYPE_INTEGER));
        columnList.add(new Column(ProductEntry.COLUMN_TOTAL_PRICE, DbUtils.COLUMN_TYPE_REAL));
        return columnList;
    }
}

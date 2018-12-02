package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import static android.util.Log.d;
import static com.example.android.inventoryapp.data.InventoryContract.CONTENT_AUTHORITY;
import static com.example.android.inventoryapp.data.InventoryContract.PATH_PRODUCTS;
import static com.example.android.inventoryapp.data.InventoryContract.PATH_PRODUCT_ID;
import static com.example.android.inventoryapp.data.InventoryContract.QueryType.QUERY_TYPE_UPDATE;
import static com.library.android.common.appconstants.AppConstants.TAG;

/**
 * {@link ContentProvider} for Inventory app.
 */
public class IproductProvider extends ContentProvider {

    /**
     * URI matcher code for the content URI for the products table
     */
    private static final int PRODUCTS = 100;
    /**
     * URI matcher code for the content URI for a single product in the products table
     */
    private static final int PRODUCT_ID = 101;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS, 100);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCT_ID, 101);
    }

    /**
     * Database helper object
     */
    private DbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        if (mDbHelper == null) {
            mDbHelper = new DbHelper(getContext());
        }
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // For the PRODUCTS code, query the products table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the products table.
                cursor = database.query(InventoryContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.products/products/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = InventoryContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the products table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Note: 11/26/2018 by sagar  Notifies changes
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return InventoryContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int uriCode = sUriMatcher.match(uri);
        Uri uriResult;
        switch (uriCode) {
            case PRODUCTS:
                uriResult = insertProduct(uri, contentValues);
                break;

            default:
                throw new IllegalArgumentException("Cannot insert unknown URI " + uri);
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return uriResult;
    }

    private Uri insertProduct(Uri uri, ContentValues contentValues) {
        // Note: 11/25/2018 by sagar  Go ahead only after validation
        if (validation(contentValues, InventoryContract.QueryType.QUERY_TYPE_INSERT)) {
            // Note: 11/25/2018 by sagar  We need writable database for insert operation
            SQLiteDatabase database = mDbHelper.getWritableDatabase();
            long id = database.insert(InventoryContract.ProductEntry.TABLE_NAME, null, contentValues);
            // Note: 11/25/2018 by sagar  Once we know id of newly inserted row, return the new uri
            // that contains generated id
            return ContentUris.withAppendedId(uri, id);
        } else {
            return uri;
        }
    }

    private boolean validation(ContentValues contentValues, int queryType) {
        //Check the type of query to ease conditional validation process
        boolean isInsertQuery = isInsertQuery(queryType);

        // Check that the name is not null
        if (isInsertQuery || contentValues.containsKey(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
            String productName = contentValues.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
            if (productName == null || productName.isEmpty()) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // Note: 11/27/2018 by sagar  Allow Product unit price to be 0
        if (isInsertQuery || contentValues.containsKey(InventoryContract.ProductEntry.COLUMN_UNIT_PRICE)) {
            float unitPrice = contentValues.getAsFloat(InventoryContract.ProductEntry.COLUMN_UNIT_PRICE);
            if (unitPrice == 0) {
                d(TAG, "IproductProvider: validation: product unit price is 0");
            }
        }

        // Note: 11/27/2018 by sagar  Allow Product quantity to be 0
        if (isInsertQuery || contentValues.containsKey(InventoryContract.ProductEntry.COLUMN_QUANTITY)) {
            int quantity = contentValues.getAsInteger(InventoryContract.ProductEntry.COLUMN_QUANTITY);
            if (quantity == 0){
                d(TAG, "IproductProvider: validation: product quantity is 0");
            }
        }

        // Note: 11/27/2018 by sagar  Allow Product total price to be 0
        if (isInsertQuery || contentValues.containsKey(InventoryContract.ProductEntry.COLUMN_TOTAL_PRICE)) {
            float totalPrice = contentValues.getAsFloat(InventoryContract.ProductEntry.COLUMN_TOTAL_PRICE);
            if (totalPrice == 0) {
                d(TAG, "IproductProvider: validation: product total price is 0");
            }
        }

        // Note: 11/27/2018 by sagar  Supplier name cannot be empty
        if (isInsertQuery || contentValues.containsKey(InventoryContract.ProductEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = contentValues.getAsString(InventoryContract.ProductEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null || supplierName.isEmpty()) {
                throw new IllegalArgumentException("Supplier name cannot be empty");
            }
        }

        // Note: 11/27/2018 by sagar  Allow empty phone number
        if (isInsertQuery || contentValues.containsKey(InventoryContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER)) {
            String supplierPhone = contentValues.getAsString(InventoryContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            if (supplierPhone == null || supplierPhone.isEmpty()) {
                d(TAG, "IproductProvider: validation: Product Supplier Phone Number is empty");
            }
        }

        return contentValues.size() > 0;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                return deleteProduct(uri, selection, selectionArgs);
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deleteProduct(uri, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);

        }
    }

    private int deleteProduct(Uri uri, String selection, String[] selectionArgs){
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int affectedRows = database.delete(InventoryContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
        if (affectedRows != 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        return affectedRows;
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        // Note: 11/25/2018 by sagar  Go ahead only after validation
        if (validation(contentValues, QUERY_TYPE_UPDATE)) {
//            SQLiteDatabase database = mDbHelper.getWritableDatabase();
//            long id = database.update(InventoryContract.ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);
            final int match = sUriMatcher.match(uri);
            switch (match) {
                case PRODUCTS:
                    return updateProduct(uri, contentValues, selection, selectionArgs);
                case PRODUCT_ID:
                    // For the PRODUCT_ID code, extract out the ID from the URI,
                    // so we know which row to update. Selection will be "_id=?" and selection
                    // arguments will be a String array containing the actual ID.
                    selection = InventoryContract.ProductEntry._ID + "=?";
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    return updateProduct(uri, contentValues, selection, selectionArgs);
                default:
                    throw new IllegalArgumentException("Update is not supported for " + uri);
            }
        } else {
            return -1;
        }
    }

    /**
     * Update products in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long affectedRows = database.update(InventoryContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);
        if (affectedRows != 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        return (int) affectedRows;
    }

    /**
     * @param queryType Type of query {@link InventoryContract.QueryType}
     * @return true if the query type is insert query
     */
    private boolean isInsertQuery(int queryType) {
        return queryType == InventoryContract.QueryType.QUERY_TYPE_INSERT;
    }

    /**
     * @param queryType Type of query {@link InventoryContract.QueryType}
     * @return true if the query type is update query
     */
    private boolean isUpdateQuery(int queryType) {
        return queryType == QUERY_TYPE_UPDATE;
    }
}
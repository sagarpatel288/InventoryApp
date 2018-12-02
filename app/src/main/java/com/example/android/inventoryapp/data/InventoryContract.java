package com.example.android.inventoryapp.data;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Table Properties such as table name, column names and other helping constants
 * Schema (constants for table and column names and properties) for inventory is defined here
 * It is a final class with private constructor because we will not extend it for any other class
 */
public final class InventoryContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     * If manifest file has provider tag, the name of the content authority here
     * must match the name of the authority in provider tag of manifest file.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventoryapp/products/ is a valid path for
     * looking at inventory data. content://com.example.android.inventoryapp/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PRODUCTS = "products";
    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventoryapp/products/# is a valid path for
     * looking at particular inventory data where # is replaced by an integer id.
     * content://com.example.android.inventoryapp/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PRODUCT_ID = "products/#";

    /**
     * Private constructor to prevent accidental access to this class and allowing only constant field access
     */
    private InventoryContract() {
    }

    /**
     * Helper class to identify requested query type for the database operation to ease validation process
     */
    public static final class QueryType {

        /**
         * Helping constant for validation process before altering database in {@link DbHelper}
         */
        public static final int QUERY_TYPE_INSERT = 10;
        /**
         * Helping constant for validation process before altering database in {@link DbHelper}
         */
        public static final int QUERY_TYPE_UPDATE = 11;

        private QueryType() {
        }
    }


    /**
     * Schema (constants for table and column names and properties) for inventory is defined here
     * It is a final class with private constructor because we will not extend it for any other class.
     */
    public static final class ProductEntry implements BaseColumns {

        public static final String TABLE_NAME = "products";
        public static final String _ID = BaseColumns._ID;

        /**
         * Note that column name cannot have space!
         *
         * @since 1.0
         */
        public static final String COLUMN_PRODUCT_NAME = "Product_Name";
        public static final String COLUMN_PRODUCT_IMAGE = "Product_Image";
        public static final String COLUMN_UNIT_PRICE = "Unit_Price_in_INR";
        public static final String COLUMN_TOTAL_PRICE = "Price_in_INR";
        public static final String COLUMN_QUANTITY = "Quantity";
        public static final String COLUMN_SUPPLIER_NAME = "Supplier_Name";
        public static final String COLUMN_SUPPLIER_PHONE_NUMBER = "Supplier_Phone_Number";

        /**
         * The content URI to access the product data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;


        /**
         * Private constructor to prevent accidental access to this class and allowing only constant field access
         */
        private ProductEntry() {
        }

    }
}

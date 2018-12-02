package com.example.android.inventoryapp.productlist.ui;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.adapters.IproductCursorAdapter;
import com.example.android.inventoryapp.addEditDetail.ui.AddEditDetailActivity;
import com.example.android.inventoryapp.base.BaseActivity;
import com.example.android.inventoryapp.data.DbHelper;
import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;
import com.example.android.inventoryapp.databinding.ActivityProductListBinding;
import com.example.android.inventoryapp.listeners.Callbacks;

import androidx.databinding.DataBindingUtil;

import static com.example.android.inventoryapp.data.InventoryContract.ProductEntry.CONTENT_URI;

public class ProductListActivity extends BaseActivity implements
        android.app.LoaderManager.LoaderCallbacks<Cursor>, Callbacks.OnChangeQuantity {

    private static final int PRODUCT_LOADER = 1;
    private DbHelper dbHelper;
    private int dataCount = 1;
    private float unitPrice = 10.0f;
    private IproductCursorAdapter cursorAdapter;
    /**
     * Must have one unique binding. Do not make more than one instance of binding or else it can give unexpected result.
     * Such as, no click listener!
     */
    private ActivityProductListBinding binding;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_product_list;
    }

    @Override
    protected void onViewStubInflated(View inflatedView, Bundle savedInstancSate) {
        binding = DataBindingUtil.bind(inflatedView);
        initTextView();
        if (binding != null) {
            binding.fab.setOnClickListener(v -> /*insertDummyData()*/ openAddEditDetail());
        }
    }

    @Override
    protected void initControllers() {

    }

    @Override
    protected void handleViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.label_product_list));
        }
    }

    @Override
    protected void setListeners() {
        setListView();
    }

    private void setListView() {
        binding.listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(ProductListActivity.this, AddEditDetailActivity.class);
            intent.setData(ContentUris.withAppendedId(CONTENT_URI, id));
            startActivity(intent);
        });
        binding.tvNoDataMsg.setText(getString(R.string.label_tap_floating_bottom_button_to_add_dummy_data));
        binding.listView.setEmptyView(binding.tvNoDataMsg);
        // Note: 11/25/2018 by sagar  Adapter for list view that is responsible for each row of product list item
        // Note: 11/25/2018 by sagar  Passing null because we have not initialized our cursor loader yet.
        // The adapter will have cursor once loader gets ready {@link onLoadFinished()}
        cursorAdapter = new IproductCursorAdapter(this, null);
        binding.listView.setAdapter(cursorAdapter);
        // Note: 11/25/2018 by sagar  Initializing the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    protected void onGetConnectionState(boolean isConnected) {

    }

    private void initTextView() {
        binding.tvNoDataMsg.setText(getString(R.string.label_tap_floating_bottom_button_to_add_dummy_data));
    }

    private void openAddEditDetail() {
        startActivity(new Intent(ProductListActivity.this, AddEditDetailActivity.class));
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String projection[] = new String[]{ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_UNIT_PRICE,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_TOTAL_PRICE,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };

        return new CursorLoader(this, CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> cursor) {
        cursorAdapter.swapCursor(null);
    }

    @Override
    public void onChangeQuantity(long itemRowId, int newQuantity, float unitPrice, float totalPrice) {
        // Note: 11/28/2018 by sagar  Set newQuantity to ContentValue key-pair
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.COLUMN_QUANTITY, newQuantity);
        // Note: 11/28/2018 by sagar  Update total price according to new quantities
        totalPrice = newQuantity * unitPrice;
        contentValues.put(ProductEntry.COLUMN_TOTAL_PRICE, totalPrice);
        // Note: 11/28/2018 by sagar  Access particular product through productId uri
        Uri productIdUri = ContentUris.withAppendedId(CONTENT_URI, itemRowId);
        // Note: 11/28/2018 by sagar  Update query
        getContentResolver().update(productIdUri, contentValues, null, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return true;
    }

    /**
     * Show confirmation dialog before performing delete operation
     * @since  1.0
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.msg_delete_all_products);
        builder.setPositiveButton(R.string.delete, (dialog, id) -> {
            // User clicked the "Delete" button, so delete the product.
            deleteAllEntries();
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Delete All Products
     * @since  1.0
     */
    private void deleteAllEntries() {
        getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu options from the res/menu/menu_on_product_listuct_list.xml file.
        // This adds menu items to the toolbar
        getMenuInflater().inflate(R.menu.menu_on_product_list, menu);
        return true;
    }
}

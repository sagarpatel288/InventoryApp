package com.example.android.inventoryapp.addEditDetail.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.base.BaseActivity;
import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.databinding.ActivityAddEditDetailBinding;
import com.google.android.material.textfield.TextInputLayout;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.library.android.common.appconstants.AppConstants;
import com.library.android.common.utils.EditTextUtils;
import com.library.android.common.utils.KeyboardUtils;
import com.library.android.common.utils.StringUtils;
import com.library.android.common.utils.ValidationUtil;
import com.library.android.common.utils.ViewUtils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import static android.util.Log.d;
import static com.example.android.inventoryapp.appconstants.AppConstants.MAX_QTY;
import static com.example.android.inventoryapp.appconstants.AppConstants.MAX_UNIT_PRICE;
import static com.example.android.inventoryapp.appconstants.AppConstants.MIN_QTY;
import static com.example.android.inventoryapp.appconstants.AppConstants.MIN_UNIT_PRICE;

public class AddEditDetailActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnTouchListener, View.OnClickListener, View.OnFocusChangeListener {

    private static final int PRODUCT_LOADER = 1;
    private ActivityAddEditDetailBinding binding;
    private Uri contentUri;
    private int quantities;
    private float unitPrice;
    private float totalPrice;
    private boolean autoIncrement;
    private boolean autoDecrement;
    private String productName;
    private String supplier;
    private String supplierPhone;
    private Uri imageUri;
    private Uri newImageUri;

    private String sourceProductName;
    private int sourceQuantities;
    private float sourceUnitPrice;
    private float sourceTotalPrice;
    private String sourceSupplier;
    private String sourceSupplierPhone;

    private final Handler updateHandler = new Handler();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_edit_detail;
    }

    @Override
    protected void onViewStubInflated(View inflatedView, Bundle savedInstancSate) {
        binding = ActivityAddEditDetailBinding.bind(inflatedView);
    }

    @Override
    protected void initControllers() {
        if (getIntent() != null) {
            if (getIntent().getData() != null) {
                contentUri = getIntent().getData();
            }
        }
    }

    @Override
    protected void handleViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            // Note: 11/27/2018 by sagar  Provides default up navigation clickable icon without implementation
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (contentUri != null) {
            initLoader();
            // Note: 11/27/2018 by sagar  Set Toolbar title for edit update operation
            getSupportActionBar().setTitle(getString(R.string.label_edit_data));
        } else {
            // Note: 11/27/2018 by sagar  No need to have delete option while creating, adding new item
            invalidateOptionsMenu();
            // Note: 11/27/2018 by sagar  Set Default values to empty views
            setMinQtyToEt();
            setMinUnitPriceToEt();
            // Note: 11/27/2018 by sagar  Set Toolbar title for fresh insert operation
            getSupportActionBar().setTitle(getString(R.string.label_add_data));
        }
    }

    private void initLoader() {
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    /**
     * Set Minimum quantity. This is helpful if user has left {@code binding.includeLayoutQuantity.etQuantity} empty
     *
     * @see #onFocusChange(View, boolean)
     * @since 1.0
     */
    private void setMinQtyToEt() {
        binding.includeLayoutQuantity.etQuantity.setText(String.valueOf(MIN_QTY));
        setQuantities();
    }

    /**
     * This is helpful if user has left {@code binding.etUnitPriceValue} empty
     * Then, Display unit price related changes
     *
     * @see #onFocusChange(View, boolean)
     * @since 1.0
     */
    private void setMinUnitPriceToEt() {
        binding.etUnitPriceValue.setText(String.valueOf(MIN_UNIT_PRICE));
        setUnitPrice();
        displayTotal(quantities, false);
    }

    /**
     * Take {@link #quantities} from {@code binding.includeLayoutQuantity.etQuantity}
     * Then, Display quantity value related changes
     *
     * @since 1.0
     */
    private void setQuantities() {
        quantities = Integer.parseInt(EditTextUtils.getString(binding.includeLayoutQuantity.etQuantity));
        displayTotal(quantities, false);
    }

    /**
     * Sets the value of {@link #unitPrice} from {@code binding.etUnitPriceValue}
     * Then, Display unitPrice related changes
     *
     * @since 1.0
     */
    private void setUnitPrice() {
        try {
            unitPrice = Float.parseFloat(EditTextUtils.getString(binding.etUnitPriceValue));
        } catch (NumberFormatException e) {
            unitPrice = Integer.parseInt(EditTextUtils.getString(binding.etUnitPriceValue));
        } catch (Exception e) {
            d(AppConstants.TAG, "AddEditDetailActivity: maintainPrice: " + e.getMessage());
        }
    }

    /**
     * Call this method if any of the value from {@link #quantities, {@link #unitPrice}} gets changed
     * Display quantity related changes
     *
     * @since 1.0
     */
    private void displayTotal(int quantities, boolean setEtQtyText) {
        if (setEtQtyText) {
            binding.includeLayoutQuantity.etQuantity.setText(String.valueOf(quantities));
        }
        totalPrice = quantities * unitPrice;
        binding.tvTotalPriceValue.setText(String.valueOf(totalPrice));
    }

    @Override
    protected void setListeners() {
        setEtQuntityListener();
        setEtUnitPriceListener();
        setEtProductNameListener();
        setEtSupplierNameListener();
        setEtSupplierPhoneListener();
        ViewUtils.setOnClickListener(this, binding.includeLayoutQuantity.tvBtnMinus,
                binding.includeLayoutQuantity.tvBtnPlus, binding.ivEdit, binding.viewEditBg, binding.btnOrder);
        ViewUtils.setOnFocusChangeListener(this, binding.etUnitPriceValue, binding.includeLayoutQuantity.etQuantity);
        ViewUtils.setOnTouchListener(this, binding.includeLayoutQuantity.tvBtnMinus, binding.includeLayoutQuantity.tvBtnPlus);
    }

    @Override
    protected void onGetConnectionState(boolean isConnected) {

    }

    private void setEtSupplierPhoneListener() {
        binding.etSupplierPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                supplierPhone = String.valueOf(s);
            }
        });
    }

    private void setEtSupplierNameListener() {
        binding.etSupplierName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                supplier = String.valueOf(s);
            }
        });
    }

    private void setEtProductNameListener() {
        binding.etProductName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                productName = String.valueOf(s);
            }
        });
    }

    private void setEtQuntityListener() {
        binding.includeLayoutQuantity.etQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // Note: 11/27/2018 by sagar  Don't let the quantity less than {@link MIN_QTY} and
                // greater than {@link MAX_QTY}
                maintainQtyLimit(String.valueOf(s));
            }
        });
    }

    private void setEtUnitPriceListener() {
        binding.etUnitPriceValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // Note: 11/27/2018 by sagar  Don't let the price less than {@link MIN_UNIT_PRICE}
                maintainPrice(String.valueOf(s));
            }
        });
    }

    /**
     * Note: 11/27/2018 by sagar  Quantity cannot be less than {@link com.example.android.inventoryapp.appconstants.AppConstants#MIN_QTY} or
     * greater than {@link com.example.android.inventoryapp.appconstants.AppConstants#MAX_QTY}
     *
     * @since 1.0
     */
    private void maintainQtyLimit(String s) {
        if (s != null && !s.isEmpty()) {
            if (Integer.parseInt(s) < MIN_QTY) {
                showMinQuantityMsg();
                setMinQtyToEt();
            } else if (Integer.parseInt(s) > MAX_QTY) {
                showMaxQuantityMsg();
                binding.includeLayoutQuantity.etQuantity.setText(String.valueOf(MAX_QTY));
            }
            EditTextUtils.setSelection(binding.includeLayoutQuantity.etQuantity);
            setQuantities();
        }
    }

    /**
     * Note: 11/27/2018 by sagar  Unit price cannot be less than {@link com.example.android.inventoryapp.appconstants.AppConstants#MIN_UNIT_PRICE}
     *
     * @since 1.0
     */
    private void maintainPrice(String s) {
        if (s != null && !s.isEmpty()) {
            if (Float.parseFloat(s) < MIN_UNIT_PRICE) {
                binding.etUnitPriceValue.setText(String.valueOf(MIN_UNIT_PRICE));
            } else if (Float.parseFloat(s) > MAX_UNIT_PRICE) {
                showMaxUnitPriceMsg();
                binding.etUnitPriceValue.setText(String.valueOf(MAX_UNIT_PRICE));
            }
            EditTextUtils.setSelection(binding.etUnitPriceValue);
            setUnitPrice();
            displayTotal(quantities, false);
        }
    }

    /**
     * Show user a toast message regarding to maximum unit price limit
     *
     * @since 1.0
     */
    private void showMaxUnitPriceMsg() {
        Toast.makeText(this, getString(R.string.msg_max_unit_price_changed), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the toolbar
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (contentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Do nothing for now
                saveItem();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!hasChanged()) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        (dialogInterface, i) -> {
                            // User clicked "Discard" button, navigate to parent activity.
                            NavUtils.navigateUpFromSameTask(AddEditDetailActivity.this);
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveItem() {
        // Note: 11/27/2018 by sagar  Proceed only if there is valid data
        // Note: 11/27/2018 sagar Check for validation only if user has changed anything
        if (hasChanged()) {
            if (hasValidData()) {
                ContentValues contentValues = getContentValues();
                long id = 0;
                // Note: 11/27/2018 by sagar  Check whether it is an edit operation or an insert operation
                if (contentUri == null) {
                    Uri resultUri = getContentResolver().insert(InventoryContract.ProductEntry.CONTENT_URI, contentValues);
                    // Note: 11/26/2018 by sagar  Gives id of newly inserted item
                    id = ContentUris.parseId(resultUri);
                } else {
                    // Note: 11/26/2018 by sagar  Gives total numbers of affected rows due to update operation
                    id = getContentResolver().update(contentUri, contentValues, null, null);
                }

                if (!(id < 0)) {
                    if (contentUri == null || hasChanged()) {
                        Toast.makeText(this, getString(R.string.msg_item_saved), Toast.LENGTH_SHORT).show();
                    } else if (contentUri != null && !hasChanged()) {
                        Toast.makeText(this, getString(R.string.msg_no_changes), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.label_error_with_saving_item), Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        } else {
            Toast.makeText(this, getString(R.string.msg_no_changes), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, (dialog, id) -> {
            // User clicked the "Delete" button, so delete the item.
            deleteProduct();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
            // User clicked the "Cancel" button, so dismiss the dialog
            // and continue editing the item.
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Delete the item
     * @since  1.0
     */
    private void deleteProduct() {
        int affectedRow = getContentResolver().delete(contentUri, null, null);
        if (!(affectedRow < 0)){
            Toast.makeText(this, getString(R.string.msg_product_deleted), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    /**
     * Check whether there is any change done by user or not
     * @since  1.0
     */
    private boolean hasChanged() {
        // Note: 11/27/2018 by sagar  Consider default values of unit price and quantity for fresh add/insert operation
        // Note: 11/27/2018 by sagar  Either it is edit operation and user has not changed anything then return false
        if (contentUri != null
                && (newImageUri == null || newImageUri.toString().isEmpty() || newImageUri == imageUri)
                && StringUtils.getDefaultString(sourceProductName, "").equalsIgnoreCase(EditTextUtils.getString(binding.etProductName))
                && StringUtils.getDefaultString(sourceSupplier, "").equalsIgnoreCase(EditTextUtils.getString(binding.etSupplierName))
                && StringUtils.getDefaultString(String.valueOf(sourceQuantities), "").equalsIgnoreCase(EditTextUtils.getString(binding.includeLayoutQuantity.etQuantity))
                && StringUtils.getDefaultString(String.valueOf(sourceUnitPrice), "").equalsIgnoreCase(EditTextUtils.getString(binding.etUnitPriceValue))
                && StringUtils.getDefaultString(String.valueOf(sourceTotalPrice), "").equalsIgnoreCase(String.valueOf(binding.tvTotalPriceValue.getText()))
                && StringUtils.getDefaultString(sourceSupplierPhone, "").equalsIgnoreCase(EditTextUtils.getString(binding.etSupplierPhone))) {
            return false;
            // Note: 11/27/2018 by sagar  OR if user has done nothing for insert operation then return false
            // Note: 11/27/2018 by sagar  Otherwise return true
        } else if (contentUri == null
                && newImageUri == null
                && !StringUtils.isNotNullNotEmpty(productName)
                && !StringUtils.isNotNullNotEmpty(supplier)
                && (!StringUtils.isNotNullNotEmpty(String.valueOf(quantities)) || StringUtils.getDefaultString(String.valueOf(quantities), "").equalsIgnoreCase(String.valueOf(MIN_QTY)))
                && (!StringUtils.isNotNullNotEmpty(String.valueOf(unitPrice)) || StringUtils.getDefaultString(String.valueOf(unitPrice), "").equalsIgnoreCase(String.valueOf(MIN_UNIT_PRICE)))
                && !StringUtils.isNotNullNotEmpty(supplierPhone)) {
            return false;
        }

        return true;
    }

    /**
     * Show a dialog to acknowledge the user about unsaved changes
     * @since  1.0
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, (dialog, id) -> {
            // User clicked the "Keep editing" button, so dismiss the dialog
            // and continue editing the product.
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Validation before database insert or update operation
     *
     * @since 1.0
     */
    private boolean hasValidData() {
        // Note: 11/27/2018 by sagar  Validation for mandatory input text fields
        // Note: 11/27/2018 by sagar  Product image and Supplier phone number are optional
        // Note: 11/27/2018 by sagar  Product name, Quantity, Unit Price and supplier name are mandatory
        if (!ValidationUtil.hasValue(binding.etProductName)) {
            Toast.makeText(this, getString(R.string.msg_please_enter_product_name), Toast.LENGTH_SHORT).show();
            showError(binding.tilProductName, getString(R.string.msg_please_enter_product_name));
            return false;
        } else if (!ValidationUtil.hasValue(binding.etUnitPriceValue)) {
            Toast.makeText(this, getString(R.string.msg_unit_price_cannot_be_empty), Toast.LENGTH_SHORT).show();
            showError(binding.tilUnitPriceValue, getString(R.string.msg_unit_price_cannot_be_empty));
            return false;
        } else if (!ValidationUtil.hasValue(binding.includeLayoutQuantity.etQuantity)) {
            Toast.makeText(this, getString(R.string.msg_quantity_cannot_be_empty), Toast.LENGTH_SHORT).show();
            showError(binding.includeLayoutQuantity.tilQuantity, getString(R.string.msg_quantity_cannot_be_empty));
            return false;
        } else if (!ValidationUtil.hasValue(binding.etSupplierName)) {
            Toast.makeText(this, getString(R.string.msg_please_enter_supplier_name), Toast.LENGTH_SHORT).show();
            showError(binding.tilSupplierName, getString(R.string.msg_please_enter_supplier_name));
            return false;
        }
        return true;
    }

    /**
     * Get content value from user input to insert or update database
     *
     * @since 1.0
     */
    private ContentValues getContentValues() {
        // Note: 11/27/2018 by sagar  Get user input data
        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME, EditTextUtils.getString(binding.etProductName));
        contentValues.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE, newImageUri != null ? newImageUri.toString() : "");
        contentValues.put(InventoryContract.ProductEntry.COLUMN_UNIT_PRICE, Float.parseFloat(EditTextUtils.getString(binding.etUnitPriceValue)));
        contentValues.put(InventoryContract.ProductEntry.COLUMN_QUANTITY, Integer.parseInt(EditTextUtils.getString(binding.includeLayoutQuantity.etQuantity)));
        contentValues.put(InventoryContract.ProductEntry.COLUMN_TOTAL_PRICE, Float.parseFloat(binding.tvTotalPriceValue.getText().toString()));
        contentValues.put(InventoryContract.ProductEntry.COLUMN_SUPPLIER_NAME, EditTextUtils.getString(binding.etSupplierName));
        contentValues.put(InventoryContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, EditTextUtils.getString(binding.etSupplierPhone));
        return contentValues;
    }

    /**
     * Show user an error related to input fields
     *
     * @since 1.0
     */
    private void showError(TextInputLayout til, String error) {
        if (til != null) {
            if (StringUtils.isNotNullNotEmpty(error)) {
                til.setError(error);
                KeyboardUtils.requestFocusOn(this, til.getEditText());
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String projection[] = new String[]{InventoryContract.ProductEntry._ID,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE,
                InventoryContract.ProductEntry.COLUMN_UNIT_PRICE,
                InventoryContract.ProductEntry.COLUMN_QUANTITY,
                InventoryContract.ProductEntry.COLUMN_TOTAL_PRICE,
                InventoryContract.ProductEntry.COLUMN_SUPPLIER_NAME,
                InventoryContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };

        return new CursorLoader(this, contentUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // FIXME: 11/27/2018 sagar This is the same thing we are doing in {@link IproductCursorAdapter}
        // Note: 11/27/2018 by sagar  It should be done through common method somehow

        // Note: 11/27/2018 by sagar  If there is no cursor or cursor has no data, terminate and return.
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Note: 11/27/2018 by sagar  There is no adapter to move cursor to first row for us.
        // We are required to first move cursor to position 0 from its default position -1.
        cursor.moveToFirst();
        // Note: 11/27/2018 by sagar  Get data from cursor.

        // Note: 11/25/2018 by sagar  Get the column indices for values
        int columnProductName = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int columnImageString = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
        int columnUnitPrice = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_UNIT_PRICE);
        int columnQuantity = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_QUANTITY);
        int columnTotalPrice = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_TOTAL_PRICE);
        int columnSupplier = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_SUPPLIER_NAME);
        int columnSupplierPhone = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

        // Note: 11/25/2018 by sagar  Use column indices to retrieve values
        sourceProductName = cursor.getString(columnProductName);
        String imagePath = cursor.getString(columnImageString);
        if (StringUtils.isNotNullNotEmpty(imagePath)) {
            imageUri = Uri.parse(imagePath);
        }
        sourceUnitPrice = cursor.getFloat(columnUnitPrice);
        sourceQuantities = cursor.getInt(columnQuantity);
        sourceTotalPrice = cursor.getFloat(columnTotalPrice);
        sourceSupplier = cursor.getString(columnSupplier);
        sourceSupplierPhone = cursor.getString(columnSupplierPhone);

        // Note: 11/27/2018 by sagar  Setting up retrieved data
        copyValues();
        setData();
    }

    /**
     * Copy source value to values that we are tracking and later will compare both values
     * @since  1.0
     */
    private void copyValues() {
        newImageUri = imageUri;
        productName = sourceProductName;
        unitPrice = sourceUnitPrice;
        quantities = sourceQuantities;
        totalPrice = sourceTotalPrice;
        supplier = sourceSupplier;
        supplierPhone = sourceSupplierPhone;
    }

    /**
     * Set Data to Views
     *
     * @since 1.0
     */
    private void setData() {
        if (imageUri != null) {
            setImage(imageUri);
        }
        binding.etUnitPriceValue.setText(String.valueOf(unitPrice));
        binding.includeLayoutQuantity.etQuantity.setText(String.valueOf(quantities));
        binding.tvTotalPriceValue.setText(String.valueOf(totalPrice));
        binding.etProductName.setText(productName);
        binding.etSupplierName.setText(supplier);
        binding.etSupplierPhone.setText(supplierPhone);
    }

    /**
     * Set Image from imageUri using Glide for memory efficiency
     *
     * @since 1.0
     */
    private void setImage(Uri imageUri) {
        ViewUtils.loadImage(this, imageUri, R.drawable.ic_img_not_found, R.drawable.ic_img_not_found, binding.ivProduct);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        EditTextUtils.clearEditText(binding.etProductName, binding.etUnitPriceValue, binding.etSupplierName, binding.etSupplierPhone);
        binding.ivProduct.setImageResource(0);
        imageUri = null;
        newImageUri = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_btn_minus:
                decreaseQuantity();
                break;

            case R.id.tv_btn_plus:
                increaseQuantity();
                break;

            case R.id.iv_edit:
            case R.id.view_edit_bg:
                imagePermission();
                break;

            case R.id.btn_order:
                callPhone();
                break;
        }
    }

    /**
     * Set phone number on dialer but let user to decide whether to call or not
     * @since  1.0
     */
    private void callPhone() {
        if (ValidationUtil.hasValue(binding.etSupplierPhone)){
            String uri = "tel:" + supplierPhone.trim();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(uri));
            startActivity(intent);
        }
    }

    /**
     * Decrease quantity
     *
     * @since 1.0
     */
    private void decreaseQuantity() {
        if (isMinQuantity()) {
            showMinQuantityMsg();
            return;
        }
        quantities--;
        displayTotal(quantities, true);
    }

    /**
     * Increase quantity
     *
     * @since 1.0
     */
    private void increaseQuantity() {
        if (isMaxQuantity()) {
            showMaxQuantityMsg();
            return;
        }
        quantities++;
        displayTotal(quantities, true);
    }

    private void imagePermission() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                selectImage();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage(getString(R.string.msg_permission_denied_rational_default))
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    /**
     * Check if quantity is minimum
     *
     * @since 1.0
     */
    private boolean isMinQuantity() {
        if (quantities == MIN_QTY) {
            autoDecrement = false;
            showMinQuantityMsg();
            return true;
        }
        return false;
    }

    /**
     * Show user a toast message regarding to minimum quantity limit
     *
     * @since 1.0
     */
    private void showMinQuantityMsg() {
        Toast.makeText(this, getString(R.string.msg_quantity_cannot_be_less_than_one), Toast.LENGTH_SHORT).show();
    }

    /**
     * Check if quantity is maximum
     *
     * @since 1.0
     */
    private boolean isMaxQuantity() {
        if (quantities == MAX_QTY) {
            autoIncrement = false;
            showMaxQuantityMsg();
            return true;
        }
        return false;
    }

    /**
     * Show user a toast message regarding to maximum quantity limit
     *
     * @since 1.0
     */
    private void showMaxQuantityMsg() {
        Toast.makeText(this, getString(R.string.msg_maximum_quantity_reached), Toast.LENGTH_SHORT).show();
    }

    /**
     * Open an application that can give an image
     *
     * @since 1.0
     */
    private void selectImage() {
//        CropImage.startPickImageActivity(this);
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setScaleType(CropImageView.ScaleType.CENTER_CROP)
                .setGuidelines(CropImageView.Guidelines.OFF)
                .start(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            switch (v.getId()) {
                case R.id.et_quantity:
                    if (!ValidationUtil.hasValue(binding.includeLayoutQuantity.etQuantity)) {
                        setMinQtyToEt();
                    }
                    break;

                case R.id.et_unit_price_value:
                    if (!ValidationUtil.hasValue(binding.etUnitPriceValue)) {
                        setMinUnitPriceToEt();
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.tv_btn_plus:
                //We do not want to continue if the quantity has reached to maximum limit.
                if (!isMaxQuantity()) {
                    //Identifies that the user has just touched the btn_increment
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        binding.includeLayoutQuantity.tvBtnPlus.performClick();
                        autoIncrement = true;
                        updateHandler.postDelayed(new QuantityModifier(), AppConstants.DELAY);
                    } else {
                        autoIncrement = false;
                        binding.includeLayoutQuantity.tvBtnPlus.setPressed(false);
                    }
                }
                break;

            case R.id.tv_btn_minus:
                //We do not want to continue if the quantity has reached to minimum limit.
                if (!isMinQuantity()) {
                    //Identifies that the user has just touched the btn_decrement
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        binding.includeLayoutQuantity.tvBtnMinus.performClick();
                        autoDecrement = true;
                        updateHandler.postDelayed(new QuantityModifier(), AppConstants.DELAY);
                    } else {
                        autoDecrement = false;
                        binding.includeLayoutQuantity.tvBtnMinus.setPressed(false);
                    }
                }
                break;
        }
        return true;
    }

    /**
     * Causes the Runnable (QuantityModifier) to be added to the message queue, to be run after the specified amount of time elapses.
     * The runnable will be run on the thread to which this handler is attached.
     *
     * @see QuantityModifier for the usage
     * @since 1.0
     */

    private void executeRunnableLoop() {
        updateHandler.postDelayed(new QuantityModifier(), AppConstants.DELAY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
                newImageUri = CropImage.getPickImageResultUri(this, data);
                setImage(newImageUri);
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (result != null && result.getUri() != null) {
                    newImageUri = result.getUri();
                    setImage(newImageUri);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!hasChanged()) {
            super.onBackPressed();
        } else {
            // Otherwise if there are unsaved changes, setup a dialog to warn the user.
            // Create a click listener to handle the user confirming that changes should be discarded.
            DialogInterface.OnClickListener discardButtonClickListener =
                    (dialogInterface, i) -> {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    };

            // Show dialog that there are unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
        }
    }

    /*
     * Dedicated thread to update ui
     */
    public class QuantityModifier implements Runnable {

        @Override
        public void run() {
            if (autoIncrement) {
                //We do not want to continue the loop if the quantity has reached to maximum limit.
                if (!isMaxQuantity()) {
                    increaseQuantity();
                    executeRunnableLoop();
                }
            } else if (autoDecrement) {
                //We do not want to continue the loop if the quantity has reached to minimum limit.
                if (!isMinQuantity()) {
                    decreaseQuantity();
                    executeRunnableLoop();
                }
            }
        }
    }
}

package com.example.android.inventoryapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.databinding.ItemProductListBinding;
import com.example.android.inventoryapp.listeners.Callbacks;
import com.library.android.common.appconstants.AppConstants;
import com.library.android.common.utils.StringUtils;
import com.library.android.common.utils.ViewUtils;

import androidx.databinding.DataBindingUtil;

import static com.example.android.inventoryapp.appconstants.AppConstants.MAX_QTY;
import static com.example.android.inventoryapp.appconstants.AppConstants.MIN_QTY;


/**
 * An adapter which is responsible to inflate view for each row of product list.
 * View is provided here and data is passed by host of this adapter.
 * {@link IproductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class IproductCursorAdapter extends CursorAdapter {

    private Callbacks.OnChangeQuantity onChangeQuantity;
    private Handler updateHandler = new Handler();
    private boolean autoIncrement;
    private boolean autoDecrement;

    /**
     * Constructs a new {@link IproductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public IproductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        this.onChangeQuantity = (Callbacks.OnChangeQuantity) context;
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_product_list, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the
     * product name TextView in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Note: 11/27/2018 by sagar  First we must ask for existing binding otherwise it will give an error
        // for view must have a tag.
        // Such error happens when we try to bind view which has already binding connected with it.
        /*https://stackoverflow.com/questions/41320627/android-databinding-view-tag-isnt-correct-on-viewnull*/
        // Note: 11/27/2018 by sagar  Data binding
        ItemProductListBinding binding = DataBindingUtil.getBinding(view);

        if (binding == null) {
            binding = ItemProductListBinding.bind(view);
        }

        // Note: 11/25/2018 by sagar  Get the column indices for values
        int columnRowId = cursor.getColumnIndex(InventoryContract.ProductEntry._ID);
        int columnProductName = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int columnImageUri = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
        int columnUnitPrice = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_UNIT_PRICE);
        int columnQuantity = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_QUANTITY);
        int columnTotalPrice = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_TOTAL_PRICE);

        // Note: 11/25/2018 by sagar  Use column indices to retrieve values
        long itemId = cursor.getLong(columnRowId);
        String productName = cursor.getString(columnProductName);
        String imagePath = cursor.getString(columnImageUri);
        float unitPrice = cursor.getFloat(columnUnitPrice);
        int quantities = cursor.getInt(columnQuantity);
        float totalPrice = cursor.getFloat(columnTotalPrice);

        // Note: 11/25/2018 by sagar  set values to view
        binding.tvName.setText(productName);
        if (StringUtils.isNotNullNotEmpty(imagePath)) {
            ViewUtils.loadImage(context, Uri.parse(imagePath), R.drawable.bg_circle_ring, R.drawable.bg_circle_ring, binding.civ);
        }
        binding.tvPrice.setText(String.format("%s: %s", context.getString(R.string.label_total_inr), String.valueOf(totalPrice)));
        binding.includeLayoutQuantity.tvQuantity.setText(String.valueOf(quantities));


        // Note: 11/28/2018 by sagar  Click listener for tv btn add
        binding.includeLayoutQuantity.tvBtnPlus.setOnClickListener(v -> {
            if (quantities < MAX_QTY) {
                increaseQuantity(itemId, quantities, unitPrice, totalPrice);
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.msg_maximum_quantity_reached), Toast.LENGTH_SHORT).show();
            }
        });

        // Note: 11/28/2018 by sagar  Click listener for tv btn minus
        binding.includeLayoutQuantity.tvBtnMinus.setOnClickListener(v -> {
            if (quantities > MIN_QTY) {
                decreaseQuantity(itemId, quantities, unitPrice, totalPrice);
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.msg_quantity_cannot_be_less_than_one), Toast.LENGTH_SHORT).show();
            }
        });

        // Note: 11/28/2018 by sagar  Set touch listener for continuous events
        ItemProductListBinding finalBinding = binding;
        binding.includeLayoutQuantity.tvBtnPlus.setOnTouchListener((v, event) -> {
            //We do not want to continue if the quantity has reached to maximum limit.
            if (quantities <= MAX_QTY) {
                //Identifies that the user has just touched the btn_increment
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    finalBinding.includeLayoutQuantity.tvBtnPlus.performClick();
                    autoIncrement = true;
                    updateHandler.postDelayed(new QuantityModifier(context, itemId, unitPrice, quantities), AppConstants.DELAY);
                } else {
                    autoIncrement = false;
                    autoDecrement = false;
                    finalBinding.includeLayoutQuantity.tvBtnPlus.setPressed(false);
                }
            }
            return true;
        });

        // Note: 11/28/2018 by sagar  Set touch listener for continuous events
        ItemProductListBinding finalBinding1 = binding;
        binding.includeLayoutQuantity.tvBtnMinus.setOnTouchListener((v, event) -> {
            //We do not want to continue if the quantity has reached to minimum limit.
            if (quantities > MIN_QTY) {
                //Identifies that the user has just touched the btn_decrement
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    finalBinding1.includeLayoutQuantity.tvBtnMinus.performClick();
                    autoDecrement = true;
                    updateHandler.postDelayed(new QuantityModifier(context, itemId, unitPrice, quantities), AppConstants.DELAY);
                } else {
                    autoDecrement = false;
                    finalBinding1.includeLayoutQuantity.tvBtnMinus.setPressed(false);
                }
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.msg_quantity_cannot_be_less_than_one), Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    /**
     * Increase quantities
     *
     * @since 1.0
     */
    private void increaseQuantity(long itemId, int quantities, float unitPrice, float totalPrice) {
        if (quantities < MAX_QTY) {
            quantities++;
            if (onChangeQuantity != null) {
                onChangeQuantity.onChangeQuantity(itemId, quantities, unitPrice, totalPrice);
            }
        } else {
            autoIncrement = false;
            autoDecrement = true;
        }
    }

    /**
     * Decrease quantities
     *
     * @since 1.0
     */
    private void decreaseQuantity(long itemId, int quantities, float unitPrice, float totalPrice) {
        if (quantities > MIN_QTY) {
            quantities--;
            if (onChangeQuantity != null) {
                onChangeQuantity.onChangeQuantity(itemId, quantities, unitPrice, totalPrice);
            }
        } else {
            autoIncrement = true;
            autoDecrement = false;
        }
    }

    /**
     * Causes the Runnable (QuantityModifier) to be added to the message queue, to be run after the specified amount of time elapses.
     * The runnable will be run on the thread to which this handler is attached.
     *
     * @param isIncrement whether to increase or decrease item
     * @param context context to show toast message
     * @param itemId      rowItem id of selected product
     * @param quantities  quantities of selected product
     * @param unitPrice   unit price of selected product
     * @see QuantityModifier for the usage
     * @since 1.0
     */
    private void executeRunnableLoop(boolean isIncrement, Context context, long itemId, int quantities, float unitPrice) {
        if (isIncrement) {
            if (quantities < MAX_QTY) {
                quantities++;
            } else {
                autoIncrement = false;
                autoDecrement = true;
            }
        } else {
            if (quantities > MIN_QTY) {
                quantities--;
            } else {
                autoDecrement = false;
                autoIncrement = true;
            }
        }
        updateHandler.postDelayed(new QuantityModifier(context, itemId, unitPrice, quantities), AppConstants.DELAY);
    }

    /*
     * Dedicated thread to update ui
     */
    private class QuantityModifier implements Runnable {

        private Context context;
        private long itemId;
        private float unitPrice;
        private int quantities;
        private float totalPrice;

        QuantityModifier(Context context, long itemId, float unitPrice, int quantities) {
            this.context = context;
            this.itemId = itemId;
            this.unitPrice = unitPrice;
            this.quantities = quantities;
            totalPrice = quantities * unitPrice;
        }

        @Override
        public void run() {
            if (autoIncrement) {
                //We do not want to continue the loop if the quantity has reached to maximum limit.
                if (quantities < MAX_QTY) {
                    increaseQuantity(itemId, quantities, unitPrice, totalPrice);
                    executeRunnableLoop(true, context, itemId, quantities, unitPrice);
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.msg_maximum_quantity_reached), Toast.LENGTH_SHORT).show();
                }
            } else if (autoDecrement) {
                //We do not want to continue the loop if the quantity has reached to minimum limit.
                if (quantities > MIN_QTY) {
                    decreaseQuantity(itemId, quantities, unitPrice, totalPrice);
                    executeRunnableLoop(false, context, itemId, quantities, unitPrice);
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.msg_quantity_cannot_be_less_than_one), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

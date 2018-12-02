package com.example.android.inventoryapp.listeners;

public abstract class Callbacks {

    public interface OnChangeQuantity {
        void onChangeQuantity(long itemRowId, int newQuantity, float unitPrice, float totalPrice);
    }
}

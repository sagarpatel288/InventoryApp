package com.library.android.common.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Helper class to create columns for any SQLite table
 */
public class Column implements Parcelable {

    public static final Parcelable.Creator<Column> CREATOR = new Parcelable.Creator<Column>() {
        @Override
        public Column createFromParcel(Parcel source) {
            return new Column(source);
        }

        @Override
        public Column[] newArray(int size) {
            return new Column[size];
        }
    };
    /**
     * Name of the column
     *
     * @since 1.0
     */
    private String columnName;
    /**
     * Properties of the column such as type and constraint/s if any as a single string with standard SQLite space separation
     *
     * @since 1.0
     */
    private String columnProperties;

    /**
     * Public Constructor
     *
     * @since 1.0
     */
    public Column(String columnName, String columnProperties) {
        this.columnName = columnName;
        this.columnProperties = columnProperties;
    }

    protected Column(Parcel in) {
        this.columnName = in.readString();
        this.columnProperties = in.readString();
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnProperties() {
        return columnProperties;
    }

    public void setColumnProperties(String columnProperties) {
        this.columnProperties = columnProperties;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.columnName);
        dest.writeString(this.columnProperties);
    }
}

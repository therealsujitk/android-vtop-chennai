package com.ashish.vtopchennai.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "receipts")
public class Receipt {
    @PrimaryKey
    public int number;

    @ColumnInfo(name = "amount")
    public Double amount;

    @ColumnInfo(name = "date")
    public String date;
}

package com.example.dontjusteat.models;

import java.util.List;



public class TableAvailability {
    public final Table table;
    public final List<Slot> slots;



    public TableAvailability(Table table, List<Slot> slots) {
        this.table = table;
        this.slots = slots;
    }
}


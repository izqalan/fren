package com.izqalan.messenger;

public class ListItem {

    private String item;
    private String itemId;

    public ListItem(){ }

    public ListItem(String item){

        this.item = item;

    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}

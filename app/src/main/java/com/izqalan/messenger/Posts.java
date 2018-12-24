package com.izqalan.messenger;

public class Posts {

    public Posts(){ }



    public String foodName;
    public String image;
    public String date;
    public String time;
    public String address;
    public String owner;

    public Posts(String foodName, String image, String date, String time,
                 String owner, String address){
        this.foodName = foodName;
        this.image = image;
        this.date =date;
        this.time = time;
        this.owner = owner;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }


    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

package com.izqalan.messenger;

public class Friends {
    public String name;
    public String bio;
    public String image;

    public Friends(){

    }

    public Friends(String name, String bio, String image){

        this.name = name;
        this.bio = bio;
        this.image = image;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

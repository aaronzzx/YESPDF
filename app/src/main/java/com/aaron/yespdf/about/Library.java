package com.aaron.yespdf.about;

class Library {

    private String name;
    private String author;
    private String introduce;

    Library(String name, String author, String introduce) {
        this.name = name;
        this.author = author;
        this.introduce = introduce;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }
}

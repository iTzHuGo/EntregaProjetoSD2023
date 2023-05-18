package com.example.servingwebcontent.beans;

public class Number {
    private int n = 0;

    public Number() {}
    
    public int getN() {
        return this.n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int next() {
        this.n += 1;
        return this.n;
    }
}

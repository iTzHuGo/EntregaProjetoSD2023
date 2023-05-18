package com.example.servingwebcontent.thedata;

public class Employee {
    private int id;
    private String name, telephone;
    private double salary;

    public Employee() {}

    public Employee(int id, String name, String telephone, double salary) {
        this.id = id;
        this.name = name;
        this.telephone = telephone;
        this.salary = salary;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public double getSalary() {
        return this.salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

}

package com.example.csabajuhasz.weathercheck;

public class Subscription {

    private int id;
    private String city;
    private String email;
    private double temperature;

    public Subscription(int id, String city, String email, double temperature) {
        this.id = id;
        this.city = city;
        this.email = email;
        this.temperature = temperature;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return /*"ID: " */+ id +  " City: " + city + " E-mail: " + email + " Temperature: " + temperature;
    }
}

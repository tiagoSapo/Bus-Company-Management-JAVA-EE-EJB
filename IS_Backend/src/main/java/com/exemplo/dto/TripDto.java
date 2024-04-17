
package com.exemplo.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TripDto implements Serializable {
    
    private long id;
    private String lineNumber;
    private Date departureTime;
    private String departure;
    private String destination;
    private int capacity;
    private double price;
    // nome cliente e nBilhetes
    private Map<String, Integer> clients;

    public TripDto() {
        this.clients = new HashMap<>();
        
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public Date getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Map<String, Integer> getClients() {
        return clients;
    }

    public void setClients(Map<String, Integer> clients) {
        this.clients = clients;
    }

    

    @Override
    public String toString() {
        return "TripDto{" + "lineNumber=" + lineNumber + ", departureTime=" 
                + departureTime + ", departure=" + departure + ", destination=" 
                + destination + ", capacity=" + capacity + ", price=" 
                + price + ", nClients=" + clients.size() + '}';
    }

    

    
}


package com.exemplo.model;

import com.exemplo.dto.TripDto;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "trip")
public class Trip implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "trip_id")
    private long tripId;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "departure_time", nullable = false)
    private Date departureTime;
    
    @Column(nullable = false)
    private String departure;
    
    @Column(nullable = false)
    private String destination;
    
    @Column(nullable = false)
    private int capacity;
    
    @Column(nullable = false)
    private double price;
    
    @Column(name = "line_number", nullable = false)
    private String lineNumber;
    
    /*@ManyToMany(mappedBy = "tickets", cascade = CascadeType.PERSIST, 
            fetch = FetchType.LAZY)*/
    @ManyToMany(mappedBy = "tickets", fetch = FetchType.LAZY)
    private List<Client> clients;

    public Trip() { clients = new ArrayList<>(); }

    public Trip(Date departureTime, String departure, String destination, int capacity, double price, String lineNumber)
            throws Exception {
        
        if (departureTime == null) {
            throw new Exception("saveTrip -> Partida invalida ou null");
        }
        this.departureTime = departureTime;
        
        if (StringUtils.isEmpty(departure)) {
            throw new Exception("saveTrip -> Partida invalida ou null");
        }
        this.departure = departure;
        
        if (StringUtils.isEmpty(destination)) {
            throw new Exception("saveTrip -> Destino invalido ou null");
        }
        this.destination = destination;
        
        if (capacity <= 0) {
            throw new Exception("saveTrip -> Capacidade da viagem <= 0");
        }
        this.capacity = capacity;
        
        if (price < 0) {
            throw new Exception("saveTrip -> Preco da viagem < 0");
        }
        this.price = price;
        
        if (StringUtils.isEmpty(lineNumber)) {
            throw new Exception("saveTrip -> Numero da linha invalido");
        }
        this.lineNumber = lineNumber;
        
        this.clients = new ArrayList<>();
    }
    
    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
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

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return "Trip{" + "tripId=" + tripId + ", departureTime=" + departureTime 
                + ", departure=" + departure + ", destination=" + destination 
                + ", capacity=" + capacity + ", price=" + price + ", lineNumber=" 
                + lineNumber + ", clients=" + clients.size() + '}';
    }

    
    
    public TripDto convertToDTO() {
        TripDto dto = new TripDto();
        dto.setId(tripId);
        dto.setCapacity(capacity);
        dto.setDeparture(departure);
        dto.setDepartureTime(departureTime);
        dto.setDestination(destination);
        dto.setPrice(price);
        dto.setLineNumber(lineNumber);
        
        Map<String, Integer> map = new HashMap<>();
        for (Client c: clients) {
            String clientName = c.getName();
            if (map.containsKey(clientName)) {
                int valor = map.get(clientName);
                map.put(clientName, valor + 1);
            } 
            else {
                map.put(clientName, 1);
            }
        }
        dto.setClients(map);
        
        return dto;
    }
    
    public static List<TripDto> convertListToDTO(List<Trip> trips) {
        List<TripDto> tripDtos = new ArrayList<>();
        trips.forEach((t) -> {
            tripDtos.add(t.convertToDTO());
        });
        return tripDtos;
    }
    
    public static Trip convertFromDTO(TripDto dto) {
        Trip trip = new Trip();
        trip.setCapacity(dto.getCapacity());
        trip.setDeparture(dto.getDeparture());
        trip.setDepartureTime(dto.getDepartureTime());
        trip.setDestination(dto.getDestination());
        trip.setPrice(dto.getPrice());
        trip.setLineNumber(dto.getLineNumber());
        return trip;
    }
    
}

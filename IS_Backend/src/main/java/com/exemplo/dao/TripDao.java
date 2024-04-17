
package com.exemplo.dao;

import com.exemplo.model.Client;
import com.exemplo.model.Trip;
import java.util.Date;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TripDao {
    
    private final Logger logger;
    private final EntityManager em;

    public TripDao(EntityManager em) {
        this.logger = LoggerFactory.getLogger(TripDao.class);
        this.em = em;
    }
    
    
    public Trip saveTrip(Trip trip) {

        Trip persistedTrip = this.findTripById(trip.getTripId());
        
        if (persistedTrip == null) {
            em.persist(trip);
            return trip;
        }
        
        return em.merge(trip);
    }
    
    public Trip findTripById(final long id) { 
        return em.find(Trip.class, id);
    }
    
    public Trip findTripByNumber(final String lineNumber) { 
        
        if (StringUtils.isEmpty(lineNumber))
            return null;
        
        
        TypedQuery<Trip> query = em.createQuery("select t from Trip t "
                + "where t.lineNumber = :number", 
                Trip.class);
        query.setParameter("number", lineNumber);
        
        List<Trip> trip = query.getResultList();
        return trip == null ? null: trip.get(0);
    }
    
    public Trip deleteTrip(final long id) {
        
        Trip trip = this.findTripById(id);
        
        double tripCost = trip.getPrice();
        List<Client> clients = trip.getClients();  
        clients.forEach((c) -> {
            c.chargeWallet(tripCost);
        });
        
        em.remove(trip);
        return trip;
    }
    
    public List<Trip> findTrips(Date start, Date end) {
        
        if (start == null || end == null) {
            return this.findTrips();
        }
        
        if (start.after(end)) {
            logger.error("findTrips -> Data de inicio " + start + " superior a data de fim " + end);
        }
        
        TypedQuery<Trip> query = em.createQuery("SELECT t FROM Trip t "
                + "WHERE t.departureTime > :start AND t.departureTime < :end", 
                Trip.class);
        query.setParameter("start", start, TemporalType.TIMESTAMP);
        query.setParameter("end", end, TemporalType.TIMESTAMP);
        
        return query.getResultList();
    }
    
    public List<Trip> findTrips() {
        TypedQuery<Trip> query = em.createQuery("select t from Trip t", Trip.class);
        return query.getResultList();
    }
    
    public List<Client> findAllClients() {
        return em.createQuery("SELECT c FROM Client c ", Client.class).getResultList();
    }
    


    public List<Trip> findTrips(String departure, String destination) {
        
        TypedQuery<Trip> query = em.createQuery("SELECT t FROM Trip t "
                + "WHERE t.departure = :partida AND t.destination = :destino", 
                Trip.class);
        query.setParameter("partida", departure);
        query.setParameter("destino", destination);
        
        return query.getResultList();
    }
    
}

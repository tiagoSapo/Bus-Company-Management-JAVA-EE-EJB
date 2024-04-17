
package com.exemplo.dao;

import com.exemplo.model.Client;
import com.exemplo.model.Trip;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientDao {
    
    private final EntityManager em;
    private final TripDao tripDao;
    private final Logger logger;

    public ClientDao(EntityManager em) {
        this.tripDao = new TripDao(em);
        this.em = em;
        this.logger = LoggerFactory.getLogger(ClientDao.class);
    }
    
    public ClientDao() {em =null; tripDao =null; logger = null;}
    
    // CRUD
    public Client saveClient(Client client) {
        
        Client persistedClient = this.findClientByEmail(client.getEmail());
        
        if (persistedClient != null) {
            return null;
        }
        
        client.setWallet(0d);
        em.persist(client);
        return client;
    }
    public Client updateClient(Client client) {
        
        if (client == null)
            return null;
        
        Client persistedClient = this.findClientByEmail(client.getEmail());
        if (persistedClient == null)
            return null;
        
        long clientId = persistedClient.getClientId();
        client.setClientId(clientId);
        client = em.merge(client);
        
        return client;
    }
    public Client deleteClient(final Long id) {
        
        if (id == null)
            return null;
        
        Client client = this.findClientById(id);
        em.remove(client);
        return client;
    }
    public Client findClientById(final Long id) { 
        return em.find(Client.class, id);
    }
    public Client findClientByEmail(final String email) {
        
        if (StringUtils.isEmpty(email))
            return null;
        
        TypedQuery<Client> query = em.createQuery("select c from Client c "
                + "where c.email = :mail", Client.class);
        query.setParameter("mail", email);
        
        try {
            return query.getSingleResult();
        } catch (NoResultException ex) {
            logger.debug("findClientByEmail -> cliente nao existe ou com password errada");
            return null;
        }
    }
    public List<Client> findClients() {
        
        TypedQuery<Client> query = em.createQuery("select c from Client c", Client.class);
        
        try {
            return query.getResultList();
        } catch (NoResultException ex) {
            logger.debug("findClients -> cliente nao existe ou com password errada");
            return null;
        }
    }
    
    // Outras
    public Client refundTrip(long clientId, long tripId) {
        
        Client cli = this.findClientById(clientId);
        List<Trip> clientTrips = cli.getTickets();
        
        for (Iterator<Trip> it = clientTrips.iterator(); it.hasNext(); ) {
            
            Trip tripTemp = it.next();
            
            if (tripTemp.getTripId() == tripId) {
                double tripCost = tripTemp.getPrice();
                cli.chargeWallet(tripCost);
                tripTemp.setCapacity(tripTemp.getCapacity() + 1);
                it.remove();
                break;
            }
        }
        
        return cli;
    }
    public Client purchaseTicket(String clientEmail, long tripId, int nTickets) {
        
        Client client = this.findClientByEmail(clientEmail);
        if (client == null)
            return null;
        
        Trip trip = tripDao.findTripById(tripId);
        if (trip == null || trip.getCapacity() <= 0)
            return null;
        
        double balance = client.getWallet() - (trip.getPrice() * nTickets);
        if (balance < 0) {
            logger.error(
                    "purchaseTicket -> Insuficient funds. Price = " + trip.getPrice() +
                    " future balance would be = " + balance);
            return null;
        }
        
        int busCapacity = trip.getCapacity() - nTickets;
        if (busCapacity < 0) {
            logger.error(
                    "purchaseTicket -> Bus " + tripId +
                    " capacity = " + trip.getCapacity() + " < " + busCapacity);
            return null;
        }
        
        trip.setCapacity(busCapacity);
        client.setWallet(balance);
        
        for (int i = 0; i < nTickets; i++)
            client.getTickets().add(trip);
        this.saveClient(client);
        
        logger.error("ticket bought");
        
        return client;
    }
}

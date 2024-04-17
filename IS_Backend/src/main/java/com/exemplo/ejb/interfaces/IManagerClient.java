
package com.exemplo.ejb.interfaces;

import com.exemplo.dto.ClientDto;
import com.exemplo.dto.TripDto;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

@Local
public interface IManagerClient {
    
    // CLIENT
    
    public ClientDto saveClient(String managerEmail, String managerPassword, ClientDto newClientDto);
    public ClientDto findClientById(String managerEmail, String managerPassword, long id);
    public ClientDto findClientByEmail(String managerEmail, String managerPassword, String email);
    public ClientDto deleteClient(String managerEmail, String managerPassword, long id);
    
    public List<ClientDto> findAllClients(String managerEmail, String managerPassword);
    
    // - Outros CLIENTS
    
    public Map<ClientDto, Integer> top5ClientsMostTrips(String managerEmail, String managerPassword);
    public List<ClientDto> findClientsByTrip(String managerEmail, String managerPassword, long tripId);

    // TRIPS
    
    public TripDto saveTrip(
            String managerEmail, String managerPassword, 
            Date departureTime, String departure, String destination, 
            int capacity, double price, String lineNumber
    );
    public TripDto findTripById(String managerEmail, String managerPassword, long id);
    public TripDto deleteTrip(String managerEmail, String managerPassword, long id);
    
    public List<TripDto> findAllTrips(String managerEmail, String managerPassword, Date start, Date end);
    public List<TripDto> findAllTrips(String managerEmail, String managerPassword, Date date);
}

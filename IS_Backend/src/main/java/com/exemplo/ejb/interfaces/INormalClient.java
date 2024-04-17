
package com.exemplo.ejb.interfaces;

import com.exemplo.dto.ClientDto;
import com.exemplo.dto.TripDto;
import java.util.Date;
import java.util.List;
import javax.ejb.Local;

@Local
public interface INormalClient {
    
    // email = email do Cliente
    // password = password do Client
    
    // CLIENT
    
    public ClientDto updateMyAccount(String email, String oldPassword, String newNome, String newPassword);
    public ClientDto getMyAccontInfo(String email, String password);
    public ClientDto deleteMyAccount(String email, String password);
    public ClientDto chargeWallet(String email, String password, double amount);
    
    
    // CLIENT Tickets
    
    public ClientDto purchaseTicket(String email, String password, long tripId, int nTickets);
    public ClientDto returnTicket(String email, String password, long tripId, int nTickets);

    
    // TRIPS
    public TripDto findTripById(long id);
    public List<TripDto> findAllTrips(Date start, Date end);
    public List<TripDto> findAllTrips(Date date);
    public List<TripDto> findAllTrips(String departure, String destination);
    
    public List<TripDto> findMyTrips(String email, String password);
}

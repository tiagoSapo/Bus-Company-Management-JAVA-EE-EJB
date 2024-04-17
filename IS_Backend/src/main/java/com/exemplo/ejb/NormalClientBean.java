
package com.exemplo.ejb;

import com.exemplo.dao.ClientDao;
import com.exemplo.dao.TripDao;
import com.exemplo.dto.ClientDto;
import com.exemplo.dto.TripDto;
import com.exemplo.ejb.interfaces.INormalClient;
import com.exemplo.model.Client;
import com.exemplo.model.Trip;
import com.exemplo.utils.Security;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateful
public class NormalClientBean implements INormalClient {
    
    @PersistenceContext(unitName = "Bus")
    private EntityManager em;

    private Logger logger;
    private ClientDao cdao;
    private TripDao tdao;
    
    @PostConstruct
    public void constructor() {
        
        cdao = new ClientDao(em);
        tdao = new TripDao(em);
        
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        logger = LoggerFactory.getLogger(NormalClientBean.class);
    }
    
    private Client checkClientCredentials(String email, String password) {
        
        if (StringUtils.isEmpty(email)) {
            logger.error("checkClientCredentials -> [Erro] Nome do cliente invalido ou null");
            return null;
        }
        if (StringUtils.isEmpty(password)) {
            logger.error("checkClientCredentials -> [Erro] Password do cliente invalida ou null");
            return null;
        }
        
        // Verificar Manager
        Client manager = cdao.findClientByEmail(email);
        if (manager == null) {
            logger.error("checkClientCredentials -> [Erro] Nao existem clientes com o email = " + email);
            return null;
        }
        
        // Validar credenciais
        boolean credentialsOk = Security.checkPassword(password, manager.getPassword());
        if (credentialsOk) {
            logger.debug("checkClientCredentials -> [OK] Cliente email = " + email + ", credenciais ok.");
            return manager;
        }
        else {
             logger.error("checkClientCredentials -> [Erro] Password invalida para email = " + email);
            return credentialsOk? manager : null;
        }
    }

    @Override
    public ClientDto updateMyAccount(String email, String oldPassword, String newName, String newPassword) {
        
        if (StringUtils.isEmpty(newName)) {
            logger.error("updateMyAccount -> [Erro] Novo nome invalido ou null");
            return null;
        }
        if (StringUtils.isEmpty(newPassword)) {
            logger.error("updateMyAccount -> [Erro] Nova password invalida ou null");
            return null;
        }
        
        Client client = this.checkClientCredentials(email, oldPassword);
        if (client == null) {
            logger.error("updateMyAccount -> [Erro] Cliente " + email + " nao existe ou com password errada");
            return null;
        }
        
        try {
            client.setName(newName);
            client.setPasswordString(newPassword);
            client = cdao.updateClient(client);
        } catch (Exception ex) {
            logger.error("updateMyAccount -> [Erro] Problema ao definir novos parametros para cliente " + email);
        }
        return client != null ? client.convertToDTO() : null;
    }

    @Override
    public ClientDto getMyAccontInfo(String email, String password) {
        
        Client client = this.checkClientCredentials(email, password);
        if (client == null) {
            logger.error("getMyAccontInfo -> Cliente " + email + " nao existe ou com password errada");
            return null;
        }
        
        return client.convertToDTO();
    }

    @Override
    public ClientDto deleteMyAccount(String email, String password) {
        
        Client client = this.checkClientCredentials(email, password);
        if (client == null) {
            logger.error("deleteMyAccount -> Cliente " + email + " nao existe ou com password errada");
            return null;
        }
        
        client = cdao.deleteClient(client.getClientId());
        return client != null ? client.convertToDTO() : null;
    }

    @Override
    public ClientDto chargeWallet(String email, String password, double amount) {
        
        if (amount < 0) {
            logger.error("chargeWallet -> Quantia invalida, inferior a 0 euros!");
            return null;
        }
        
        Client client = this.checkClientCredentials(email, password);
        if (client == null) {
            logger.error("chargeWallet -> Cliente " + email + " nao existe ou com password errada");
            return null;
        }
        
        client.chargeWallet(amount);
        client = cdao.updateClient(client);
        
        return client != null ? client.convertToDTO() : null;
    } 

    @Override
    public ClientDto purchaseTicket(String email, String password, long tripId, int nTickets) {
        
        if (nTickets < 0) {
            logger.error("purchaseTicket -> Numero de bilhetes invalido, inferior a 0!");
            return null;
        }

        Client client = this.checkClientCredentials(email, password);
        if (client == null) {
            logger.error("purchaseTicket -> Cliente " + email + " nao existe ou com password errada");
            return null;
        }
        
        client = cdao.purchaseTicket(email, tripId, nTickets);
        
        return client != null ? client.convertToDTO() : null;
    }

    @Override
    public ClientDto returnTicket(String email, String password, long tripId, int nTickets) {
        
        if (nTickets < 0) {
            logger.error("returnTicket -> Numero de bilhetes invalido, inferior a 0!");
            return null;
        }
        
        Client client = this.checkClientCredentials(email, password);
        if (client == null) {
            logger.error("returnTicket -> Cliente " + email + " nao existe ou com password errada.");
            return null;
        }
        
        Trip trip = tdao.findTripById(tripId);
        if (trip == null) {
            logger.error("returnTicket -> Viagem n." + tripId + " nao existe.");
            return null;
        }
        
        Iterator<Trip> it = client.getTickets().iterator();
        while (it.hasNext()) {
            Trip t = it.next();
            if (nTickets == 0)
                break;
            if (t.getTripId() == tripId) {
                // nao se pode fazer break; o cliente pode ter N viagens iguais!
                client.chargeWallet(trip.getPrice());
                t.setCapacity(t.getCapacity() + 1);
                it.remove();
                nTickets--;
            }
        }
        client = cdao.updateClient(client);
        
        return client != null ? client.convertToDTO() : null;
    }

    @Override
    public List<TripDto> findMyTrips(String email, String password) {
        
        Client client = this.checkClientCredentials(email, password);
        if (client == null) {
            logger.error("returnTicket -> Cliente " + email + " nao existe ou com password errada.");
            return null;
        }
        
        // Obs: Os 'tickets' sao trips
        return Trip.convertListToDTO(client.getTickets());
    }

    @Override
    public TripDto findTripById(long id) {
        
        Trip trip = tdao.findTripById(id);
        if (trip == null) {
            logger.error("findTripById -> Nao existe uma viagem com o n. " + id);
            return null;
        }
    
        return trip.convertToDTO();
    }

    @Override
    public List<TripDto> findAllTrips(Date start, Date end) {
        return Trip.convertListToDTO(tdao.findTrips(start, end));
    }

    @Override
    public List<TripDto> findAllTrips(Date date) {
        return Trip.convertListToDTO(tdao.findTrips(date, date));
    }

    @Override
    public List<TripDto> findAllTrips(String departure, String destination) {
        
        if (StringUtils.isEmpty(departure)) {
            logger.error("findTripsBy -> Nao foi indicado o ponto de partida");
            return null;
        }
        if (StringUtils.isEmpty(destination)) {
            logger.error("findTripsBy -> Nao foi indicado o destino");
            return null;
        }
        
        return Trip.convertListToDTO(tdao.findTrips(departure, destination));
    }
    
}


package com.exemple;

import com.exemplo.dto.ClientDto;
import com.exemplo.dto.TripDto;
import com.exemplo.ejb.interfaces.IManagerClient;
import com.exemplo.ejb.interfaces.INormalClient;
import com.exemplo.model.AccountType;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import static org.apache.commons.lang3.time.DateUtils.parseDate;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Named(value = "PrincipalController")
@SessionScoped
public class PrincipalController implements Serializable {
    
    // Para Session Map
    private static final String CLIENT = "client";
    private static final String PASSWORD = "password";
    private static final String LOGGED_IN = "login";
    
    // Views para jsf
    private ClientDto clientView;
    private TripDto tripView;
    private List<TripDto> tripsView;
    public static final String INFO = "info";
    
    private Logger logger;
    private List<Long> tripIds;
    
    @EJB
    private INormalClient ejb;
    @EJB
    private IManagerClient ejbManager;
    
    @PostConstruct
    public void constructor () {
        
        Map<String, Object> session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        
        ClientDto client = (ClientDto) session.get(CLIENT);
        Boolean loggedIn = (Boolean) session.get(LOGGED_IN);
        
        logger = LoggerFactory.getLogger(LoginController.class);

        if (client == null || loggedIn == null) {
            session.put(CLIENT, new ClientDto());
            session.put(PASSWORD, null);
            session.put(LOGGED_IN, false);
        }
        this.resetView();
    }
    
    /*
    /* Cliente - NORMAL
    */
    public String updateAccount() {

        Map<String, Object> session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        ClientDto pesistedClient = (ClientDto) session.get(CLIENT);
        
        String email = pesistedClient.getEmail(); // o email e' o id
        String oldPassword = this.getSessionPassword();
        String newName = clientView.getName();
        String newPassword = clientView.getPassword();
        
        logger.debug("updateAccount -> " + email + oldPassword + newName + newPassword);
        
        ClientDto client = ejb.updateMyAccount(email, oldPassword, newName, newPassword);
        if (client == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRO", "Parametros invalido");
            FacesContext.getCurrentInstance().addMessage(INFO, msg);
            this.resetView();
            return null;
        }
        
        session.put(PASSWORD, newPassword);
        session.put(CLIENT, client);
        session.put(LOGGED_IN, true);
        
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "SUCESSO", "Dados atualizados!");
        FacesContext.getCurrentInstance().addMessage(INFO, msg);
        
        this.resetView();
        return "/normal/principal";
    }
    public String deleteAccount() {
        
        Map<String, Object> session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        ClientDto client = (ClientDto) session.get(CLIENT);
        
        String email = client.getEmail();
        String password = (String) session.get(PASSWORD);
        
        ClientDto clientDto = ejb.deleteMyAccount(email, password);
        if (clientDto == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRO", "Credenciais invalidas! Nao é possível fechar conta"); 
            FacesContext.getCurrentInstance().addMessage(INFO, msg);
            
            this.resetView();
            return null;
        }
        
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "SUCESSO!", "Conta " + email + " fechada"); 
        FacesContext.getCurrentInstance().addMessage(INFO, msg);
        return this.sair();
    }  
    public String chargeWallet() {
        
        Map<String, Object> session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        
        double amount = clientView.getWallet();
        if (amount <= 0) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRO", "Valor invalido");
            FacesContext.getCurrentInstance().addMessage(INFO, msg);
            return "/normal/principal";
        }
        
        ClientDto pesistedClient = (ClientDto) session.get(CLIENT);
        String email = pesistedClient.getEmail();
        String password = this.getSessionPassword();
        
        pesistedClient = ejb.chargeWallet(email, password, amount);
        if (pesistedClient == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRO", "Valor invalido");
            FacesContext.getCurrentInstance().addMessage(INFO, msg);
            
            this.resetView();
            return null;
        }
        
        session.put(CLIENT, pesistedClient);
        session.put(LOGGED_IN, true);
        
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "SUCESSO", "Conta carregada com " + amount + " €!");
        FacesContext.getCurrentInstance().addMessage(INFO, msg);
        
        this.resetView();
        return "/normal/principal";
    }
    
    public void listTrips(String start, String end) {
        
        List<TripDto> trips;
        Date dateStart = null;
        Date dateEnd = null;
        
        logger.debug("listTrips -> start = " + start + " end = " + end);
        
        try {
            dateStart = parseDate(start, "yyyy-MM-dd'T'HH:mm");
            dateEnd = parseDate(end, "yyyy-MM-dd'T'HH:mm");
        } catch (ParseException ex) {
            FacesContext.getCurrentInstance().addMessage(INFO, 
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso - Datas inválidas", "Estão a ser apresentados todos os registos"));
        }
       
        ClientDto client = this.getSessionClient();
        if (client == null)
            return;
        String email = client.getEmail();
        String password = this.getSessionPassword();
        
        if (client.getAccountType().equals(AccountType.Manager.toString())) {
            trips = ejbManager.findAllTrips(email, password, dateStart, dateEnd);
            client = ejbManager.findClientByEmail(email, password, email);
        } 
        else {
            trips = ejb.findAllTrips(dateStart, dateEnd);
            client = ejb.getMyAccontInfo(email, password);
        }    
        
        if (trips != null) {
            tripsView = trips;
            tripIds = new ArrayList<>();
            tripsView.forEach((t) -> {
                tripIds.add(t.getId());
            });
            if (client != null) {
                this.getSession().put(CLIENT, client);
            }
        }
    }
    public void listTripsLocation(String departure, String destination) {
        
        List<TripDto> trips;
        
        logger.debug("listTripsLocation -> " + departure + " " + destination);
       
        ClientDto client = this.getSessionClient();
        if (client == null)
            return;
        String email = client.getEmail();
        String password = this.getSessionPassword();
        
        if (client.getAccountType().equals(AccountType.Manager.toString())) {
            return;
        } 
        else {
            trips = ejb.findAllTrips(departure, destination);
            client = ejb.getMyAccontInfo(email, password);
        }    
        
        if (trips != null) {
            tripsView = trips;
            tripIds = new ArrayList<>();
            tripsView.forEach((t) -> {
                tripIds.add(t.getId());
            });
            if (client != null) {
                this.getSession().put(CLIENT, client);
            }
        } 
        else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Pontos de partida e chegada não definidos");
            FacesContext.getCurrentInstance().addMessage(INFO, msg);
        }
    }
    
    public void buyTickets(Long tripId, Integer nTickets) {
        
        logger.debug(new StringBuilder()
                        .append("buyTickets -> ")
                        .append(tripId).append(" ")
                        .append(nTickets).append(" ")
                        .append(tripsView).toString()
        );
        
        if (nTickets == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRO", "Numero de bilhetes invalido");
            FacesContext.getCurrentInstance().addMessage(INFO, msg);
            return;
        }
        
        Map<String, Object> session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        ClientDto client = (ClientDto) session.get(CLIENT);
        String email = client.getEmail();
        String password = this.getSessionPassword();
        
        client = ejb.purchaseTicket(email, password, tripId, nTickets);
        if (client == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRO", "Saldo insuficiente!");
            FacesContext.getCurrentInstance().addMessage(INFO, msg);
        } 
        else {
            session.put(CLIENT, client);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "SUCESSO", "Comprado bilhete para autocarro n. " + tripId);
            FacesContext.getCurrentInstance().addMessage(INFO, msg);
        } 
    }
    public void listMyTickets() {
        
        ClientDto client = this.getSessionClient();
        if (client == null)
            return;
        
        String email = client.getEmail();
        String password = this.getSessionPassword();
        
        tripsView = ejb.findMyTrips(email, password);
        if (tripsView == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRO", "Credenciais invalidas");
            FacesContext.getCurrentInstance().addMessage(INFO, msg);        
            tripsView = new ArrayList<>();
        }
    }
    public void returnTicket(Long tripId, Integer nTickets) {
        
        logger.debug("returnTicket -> " + tripId + " " + nTickets);
        
        ClientDto client = this.getSessionClient();
        if (nTickets == null || client == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRO", "Numero de bilhetes invalido");
            FacesContext.getCurrentInstance().addMessage(INFO, msg);
            return;
        }
        client = ejb.returnTicket(client.getEmail(), this.getSessionPassword(), tripId, nTickets);
        if (client != null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "SUCESSO", "Um bilhete para autocarro n. " + tripId + " devolvido com sucesso!");
            FacesContext.getCurrentInstance().addMessage(INFO, msg);
            this.getSession().put(CLIENT, client);
        } 
        else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRO", "Nao existem bilhetes para autocarro n. " + tripId + "!");
            FacesContext.getCurrentInstance().addMessage(INFO, msg);
        } 
    }
    
    /*
    /* Cliente - MANAGER
    */
    public void deleteTrip(Long tripId, String start, String end) {
        
        // Start e End sao usados para atualizar as vistas com os valores dos filtros
        logger.debug("deleteTrip -> Apagar viagem " + tripId);
        
        String email = this.getSessionClient().getEmail();
        String password = this.getSessionPassword();
        
        TripDto trip = ejbManager.deleteTrip(email, password, tripId);
        if (trip != null) {
            FacesContext.getCurrentInstance().addMessage(INFO, new FacesMessage(FacesMessage.SEVERITY_INFO, "SUCESSO", 
                    "Autocarro n. " + tripId + " apagado. Dinheiro devolvido a clientes"));
            this.listTrips(start, end);
        } 
        else {
            FacesContext.getCurrentInstance().addMessage(INFO, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRO", 
                    "Autocarro n. " + tripId + " nao existe."));
        }
    }
    public String registerNewTrip() throws IOException {
        
        ClientDto client = this.getSessionClient();
        
        TripDto trip = ejbManager.saveTrip(client.getEmail(), this.getSessionPassword(),
                tripView.getDepartureTime(), 
                tripView.getDeparture(), 
                tripView.getDestination(), 
                tripView.getCapacity(), 
                tripView.getPrice(), 
                tripView.getLineNumber()
        );
        
        if (trip == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRO", 
                    "Viagem invalida ou já existe"); 
            FacesContext.getCurrentInstance().addMessage(INFO, msg);
            
            this.resetView();
            return null;
        }
        
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "SUCESSO", 
                "Nova viagem " + tripView.getLineNumber() + " registada com sucesso!"); 
        FacesContext.getCurrentInstance().addMessage(INFO, msg);
        
        this.resetView();
        return returnToMainPage();    
    }
    public Map<ClientDto, Integer> top5Clients() {
        
        String email = this.getSessionClient().getEmail();
        String password = this.getSessionPassword();
        
        if (this.getSessionClient().getAccountType().equals(AccountType.Manager.toString())) {
            return ejbManager.top5ClientsMostTrips(email, password);
        }
        
        return new HashMap<>();
    }
    
    /*
    /* Navegacao
    */
    public String returnToMainPage() throws IOException {
        
        if (this.getSessionClient().getAccountType().equals(AccountType.Manager.toString()))
            return "/manager/principal";
        else
            return "/normal/principal";
    }
    
    /*
    /* Gerir Sessao
    */
    public String welcomeMsg() {
        String name = this.getSessionClient().getName();
        int pos = name.length() - 1;
        return name.charAt(pos) == 'a' ? "Bem-vinda " : "Bem-vindo ";
    }
    public ClientDto getSessionClient() {
        Map<String, Object> session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        return (ClientDto) session.get(CLIENT);
    }
    public String getSessionPassword() {
        Map<String, Object> session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        return (String) session.get(PASSWORD);
    }
    public Map<String, Object> getSession() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
    }
    public String sair() {
        
        Map<String, Object> session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        session.put(CLIENT, new ClientDto());
        session.put(PASSWORD, null);
        session.put(LOGGED_IN, false);
        
        this.resetView();
        return "/index";       
    }
    private void resetView() {
        this.clientView = new ClientDto();
        this.tripView = new TripDto();
        this.tripsView = new ArrayList<>();
    }
    
    /*
    /* GETTERS E SETTERS
    */
    public ClientDto getClientView() {
        return clientView;
    }

    public void setClientView(ClientDto clientView) {
        this.clientView = clientView;
    }

    public TripDto getTripView() {
        return tripView;
    }

    public void setTripView(TripDto tripView) {
        this.tripView = tripView;
    } 

    public List<Long> getTripIds() {
        return tripIds;
    }

    public void setTripIds(List<Long> tripIds) {
        this.tripIds = tripIds;
    }

    public List<TripDto> getTripsView() {
        return tripsView;
    }

    public void setTripsView(List<TripDto> tripsView) {
        this.tripsView = tripsView;
    } 
}

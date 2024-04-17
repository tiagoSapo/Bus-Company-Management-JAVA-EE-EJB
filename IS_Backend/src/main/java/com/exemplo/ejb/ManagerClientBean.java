
package com.exemplo.ejb;

import com.exemplo.ejb.interfaces.IManagerClient;
import com.exemplo.dao.ClientDao;
import com.exemplo.dao.TripDao;
import com.exemplo.dto.ClientDto;
import com.exemplo.dto.TripDto;
import com.exemplo.model.AccountType;
import com.exemplo.model.Client;
import com.exemplo.model.Trip;
import com.exemplo.utils.Security;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateful
public class ManagerClientBean implements IManagerClient {
    
    private Logger logger;
    
    @PersistenceContext(unitName = "Bus")
    private EntityManager em;
    
    private ClientDao cdao;
    private TripDao tdao;
    
    @PostConstruct
    public void constructor() {
        
        this.cdao = new ClientDao(em);
        this.tdao = new TripDao(em);
        
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        logger = LoggerFactory.getLogger(ManagerClientBean.class);
    }
    
    private Client checkManagerCredentials(String email, String password) {
        if (StringUtils.isEmpty(email)) {
            logger.error("checkManagerCredentials -> [Erro] Nome do manager invalido ou null");
            return null;
        }
        if (StringUtils.isEmpty(password)) {
            logger.error("checkManagerCredentials -> [Erro] Password do manager invalida ou null");
            return null;
        }
        
        // Verificar Manager
        Client manager = cdao.findClientByEmail(email);
        if (manager == null) {
            logger.error("checkManagerCredentials -> [Erro] Nao existem clientes com o email = " + email);
            return null;
        }
        
        // Validar credenciais
        boolean credentialsOk = Security.checkPassword(password, manager.getPassword());
        if (credentialsOk) {
            logger.debug("checkManagerCredentials -> [OK] Cliente email = " + email + ", credenciais ok.");
            return manager;
        }
        else {
             logger.error("checkManagerCredentials -> [Erro] Password invalida para email = " + email);
            return credentialsOk? manager : null;
        }
    }
    
    @Override
    public ClientDto saveClient(String managerEmail, String managerPassword, ClientDto newClientDto) {
        
        if (newClientDto == null) {
            logger.error("saveClient -> [Erro] ClientDto igual a null");
            return null;
        }
        
        Client manager = checkManagerCredentials(managerEmail, managerPassword);
        if (manager == null) {
            logger.error("saveClient -> [Erro] Cliente manager " + managerEmail + " nao existe ou com password errada");
            return null;
        }
        
        // Verificar novo cliente
        try {
            Client newClient = new Client(newClientDto);
            newClient = cdao.saveClient(newClient);
            
            return newClient != null ? newClient.convertToDTO() : null;
        } catch (Exception ex) {
            logger.error("saveClient -> [Erro] Problema ao registar " + newClientDto.getEmail());
            return null;
        }
    }
    @Override
    public ClientDto findClientById(String managerEmail, String managerPassword, long id) {
        
        Client manager = this.checkManagerCredentials(managerEmail, managerPassword);
        if (manager == null) {
            logger.error("findClientById -> [Erro] Cliente manager " + managerEmail + " nao existe ou com password errada");
            return null;
        }
        
        Client client = cdao.findClientById(id);
        if (client == null) {
            logger.error("findClientById -> [Erro] Cliente " + id + " nao existe");
            return null;
        }
        return client.convertToDTO();
    }
    @Override
    public ClientDto deleteClient(String managerEmail, String managerPassword, long id) {

        Client manager = this.checkManagerCredentials(managerEmail, managerPassword);
        if (manager == null) {
            logger.error("deleteClient -> [Erro] Cliente manager " + managerEmail + " nao existe ou com password errada");
            return null;
        }
        
        Client client = cdao.deleteClient(id);
        if (client == null) {
            logger.error("deleteClient -> [Erro] Cliente " + id + " nao existe");
            return null;
        }
        
        return client.convertToDTO();
    }
    @Override
    public ClientDto findClientByEmail(String managerEmail, String managerPassword, String email) {

        Client manager = this.checkManagerCredentials(managerEmail, managerPassword);
        if (manager == null) {
            logger.error("findClientById -> [Erro] Cliente manager " + managerEmail + " nao existe ou com password errada");
            return null;
        }
        
        Client client = cdao.findClientByEmail(email);
        if (client == null) {
            logger.error("findClientById -> [Erro] Cliente " + email + " nao existe");
            return null;
        }
        
        return client.convertToDTO();
    }
    
    
    @Override
    public Map<ClientDto, Integer> top5ClientsMostTrips(String managerEmail, String managerPassword) {
        
        Client manager = this.checkManagerCredentials(managerEmail, managerPassword);
        if (manager == null) {
            logger.error("findClientsByTrip -> [Erro] Credenciais do manager invalidas");
            return null;
        }
        
        List<Client> clients = tdao.findAllClients();
        Map<ClientDto, Integer> mapTemp = new HashMap<>();
        
        
        Iterator<Client> it = clients.iterator();
        while (it.hasNext()) {
            
            Client client = it.next();
            if (client.getAccountType() == AccountType.Manager) {
                // Se for manager -> ignorar (nao e' necessario consultar as viagens dos managers)
                continue;
            }
            
            ClientDto clientDto = client.convertToDTO();
            Integer nTickets = client.getTickets().size();
            
            mapTemp.put(clientDto, nTickets);
        }
        
        
        Map<ClientDto, Integer> mapTop5 = mapTemp.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        
        return mapTop5;
    }

    @Override
    public List<ClientDto> findClientsByTrip(String managerEmail, String managerPassword, long tripId) {
        
        Client manager = this.checkManagerCredentials(managerEmail, managerPassword);
        if (manager == null) {
            logger.error("findClientsByTrip -> [Erro] Credenciais do manager invalidas");
            return null;
        }
        
        Trip trip = tdao.findTripById(tripId);
        if (trip == null) {
            logger.error("findClientsByTrip -> [Erro] Nao existem viagens com o n. " + tripId);
            return null;
        }
        
        return Client.convertListToDTO(trip.getClients());
    }

    @Override
    public List<TripDto> findAllTrips(String managerEmail, String managerPassword, Date start, Date end) {
        
        Client manager = this.checkManagerCredentials(managerEmail, managerPassword);
        if (manager == null) {
            logger.error("findClientsByTrip -> [Erro] Credenciais do manager invalidas");
            return null;
        }
        
        return Trip.convertListToDTO(tdao.findTrips(start, end));
    }

    @Override
    public List<TripDto> findAllTrips(String managerEmail, String managerPassword, Date date) {
        return this.findAllTrips(managerEmail, managerPassword, date, date);
    }
    
    
    @Override
    public TripDto saveTrip(String managerEmail, String managerPassword, 
            Date departureTime, String departure, String destination, 
            int capacity, double price, String lineNumber) {
        
        Client manager = this.checkManagerCredentials(managerEmail, managerPassword);
        if (manager == null) {
            logger.debug("saveTrip -> [Erro] Credenciais do manager invalidas");
            return null;
        }
        
        Trip trip;
        try {
            trip = new Trip(departureTime, departure, destination, capacity, price, lineNumber);
            trip = tdao.saveTrip(trip);
        }
        catch (Exception ex) {
            logger.debug("saveTrip -> [Erro] Numero da linha invalido");
            return null;
        }
        
        return trip != null ? trip.convertToDTO() : null;
    }
    @Override
    public TripDto findTripById(String managerEmail, String managerPassword, long id) {
        
        Client manager = this.checkManagerCredentials(managerEmail, managerPassword);
        if (manager == null) {
            logger.debug("findTripById -> [Erro] Credenciais do manager invalidas");
            return null;
        }

        Trip trip = tdao.findTripById(id);
        if (trip == null) {
            logger.debug("findTripById -> [Erro] Nao existem viagens com o n. " + id);
            return null;
        }
        
        return trip.convertToDTO();
    }
    @Override
    public TripDto deleteTrip(String managerEmail, String managerPassword, long id) {
        
        Client manager = this.checkManagerCredentials(managerEmail, managerPassword);
        if (manager == null) {
            logger.debug("deleteTrip -> [Erro] Credenciais do manager invalidas");
            return null;
        }
        
        Trip trip = tdao.findTripById(id);
        if (trip == null) {
            logger.debug("deleteTrip -> [Erro] Nao existem viagens com o n. " + id);
            return null;
        }
        
        // Devolver DINHEIRO aos clientes e avisa-los via EMAIL
        double tripCost = trip.getPrice();
        String assuntoEmail = "Viagem cancelada - Aplicacao Web de Autocarros";
        String msgEmail = new StringBuilder()
                .append("Informamos que a viagem ")
                .append(id).append(" foi cancelada. O dinheiro da viagem ")
                .append(tripCost)
                .append(" € foi devolvido para a sua conta")
                .toString();
        
        Iterator<Client> it = trip.getClients().iterator();
        while (it.hasNext()) {
            Client client = it.next();
            client.chargeWallet(tripCost);
            client.getTickets().remove(trip);
            it.remove();
            try {
                this.enviarMail(client.getEmail(), assuntoEmail, msgEmail);
            } catch (Exception ex) {
                logger.error("enviarMail -> [Erro] Problema ao enviar email. Erro = " + ex);
            }
        }
        
        trip = tdao.deleteTrip(id);       
        return trip.convertToDTO();
    }
    
    @Override
    public List<ClientDto> findAllClients(String managerEmail, String managerPassword) {
        
        Client manager = this.checkManagerCredentials(managerEmail, managerPassword);
        if (manager == null) {
            logger.debug("findClientsByTrip -> [Erro] Credenciais do manager invalidas");
            return null;
        }
        
        return Client.convertListToDTO(tdao.findAllClients());
    }
    
       
    // EMAILS
    private void enviarMail(String emailDestinatario, String assunto, String mensagem) throws Exception {
        
        final String MAIL = "tiagosimoes4@sapo.pt";
        final String PASS = "424242";
        
        if (StringUtils.isEmpty(emailDestinatario)) {
            logger.error("enviarMail -> [Erro] Email invalido");
            return;
        }
        if (StringUtils.isEmpty(assunto)) {
            logger.error("enviarMail -> [Erro] Assunto invalido");
            return;
        }
        if (StringUtils.isEmpty(mensagem)) {
            logger.error("enviarMail -> [Erro] Mensagem invalida");
            return;
        }
        
        // Recipient's email ID needs to be mentioned.
        String to = emailDestinatario;

        // Sender's email ID needs to be mentioned
        String from = MAIL;

        // Assuming you are sending email from through gmails smtp
        String host = "smtp.sapo.pt";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MAIL, PASS);
            }
        });

        // Used to debug SMTP issues
        session.setDebug(true);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject(assunto);

            // Now set the actual message
            message.setText(mensagem);

            logger.debug("enviarMail -> [OK] A enviar mensagem...");
            // Send message
            Transport.send(message);
            logger.debug("enviarMail -> [OK] Mensagem enviada com sucesso.");
        } catch (MessagingException mex) {
            throw mex;
        }
    }

}

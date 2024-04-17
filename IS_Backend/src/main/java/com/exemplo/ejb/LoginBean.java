
package com.exemplo.ejb;

import com.exemplo.dao.ClientDao;
import com.exemplo.dao.TripDao;
import com.exemplo.dto.ClientDto;
import com.exemplo.ejb.interfaces.ILogin;
import com.exemplo.model.AccountType;
import com.exemplo.model.Client;
import com.exemplo.model.Trip;
import com.exemplo.utils.Security;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LoginBean implements ILogin {
    
    @PersistenceContext(unitName = "Bus")
    private EntityManager em;
    
    private ClientDao cdao;
    private TripDao tdao;
    private Logger logger;
    
    @PostConstruct
    public void constructor() {
        
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        logger = LoggerFactory.getLogger(LoginBean.class);
        cdao = new ClientDao(em);
        tdao = new TripDao(em);
        
        try {
            cdao.saveClient(new Client("Administrator", "admin", "admin", AccountType.Manager));
            logger.debug("constructor -> Criado cliente administrador");
        } 
        catch (Exception ex) {
            logger.error("constructor -> erro ao criar cliente. Erro = " + ex);
        }
    } 
    
    @Schedule(hour = "23", minute = "0", second = "0", persistent = false)
    public void schedule() {       
        List<Trip> trips = tdao.findTrips();
        
        double revenueTotal = 0;
        for (Trip t: trips) {
            double price = t.getPrice();
            double nClients = t.getClients().size();
            revenueTotal += nClients * price;
        }
        logger.info("[SCHEDULE] Lucro total das viagens de hoje = " + revenueTotal);
    }
    

    @Override
    public ClientDto login(String email, String password) {
        
        Client client = cdao.findClientByEmail(email);
        if (client == null) {
            logger.error("login -> Email invalido");
            return null;
        }
        
        boolean passwordMatches = Security.checkPassword(password, client.getPassword()); 
        if (!passwordMatches) {
            logger.error("login -> Password invalida para conta existente");
            return null;
        }
        
        return client.convertToDTO();
    }

    @Override
    public ClientDto createNewAccount(String name, String email, String password) { 
        
        try {
            ClientDto client = cdao.saveClient(new Client(name, email, password, AccountType.Normal)).convertToDTO();
            
            String msg = new StringBuilder()
                    .append("createNewAccount -> [OK] Cliente com nome = ")
                    .append(name)
                    .append(", password = ")
                    .append(password)
                    .append(", email = ")
                    .append(email)
                    .append(" registado com SUCESSO!").toString();
            logger.debug(msg);
            return client;
        } 
        catch (Exception ex) {
            String msg = new StringBuilder()
                    .append("createNewAccount -> [Erro] ao registar movo cliente, com nome = ")
                    .append(name)
                    .append(", password = ")
                    .append(password)
                    .append(", email = ")
                    .append(email).toString();
            logger.error(msg);
            return null;
        }
    }
    
}

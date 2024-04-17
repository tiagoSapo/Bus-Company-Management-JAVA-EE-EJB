
package com.exemple;

import static com.exemple.PrincipalController.INFO;
import com.exemplo.dto.ClientDto;
import com.exemplo.ejb.LoginBean;
import com.exemplo.ejb.interfaces.ILogin;
import com.exemplo.model.AccountType;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


@Named(value = "LoginController")
@RequestScoped
public class LoginController implements Serializable {
    
    private static final String LOGIN_FAILED_MESSAGE = "Email ou palavra-passe incorretos!";
    private static final String REGISTRY_FAILED_MESSAGE = "Ja existe uma conta com este email!";
    
    // Para Session Map
    private static final String CLIENT = "client";
    private static final String PASSWORD = "password";
    private static final String LOGGED_IN = "login";
    
    // View para jsf
    private ClientDto clientView;
    
    private Logger logger;
    
    @EJB
    private ILogin loginEjb;
    
    @PostConstruct
    public void constructor () {
        
        Map<String, Object> session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        
        ClientDto client = (ClientDto) session.get(CLIENT);
        Boolean loggedIn = (Boolean) session.get(LOGGED_IN);

        if (client == null || loggedIn == null) {
            session.put(CLIENT, new ClientDto());
            session.put(PASSWORD, null);
            session.put(LOGGED_IN, false);
        }
        
        logger = LoggerFactory.getLogger(LoginController.class);
        this.resetView();
    }
    
    public String authenticateClient() throws IOException {
        
        Map<String, Object> session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        
        ClientDto client = loginEjb.login(
                clientView.getEmail(), 
                clientView.getPassword()
        );
        
        if (client != null) {
            session.put(CLIENT, client);
            session.put(PASSWORD, clientView.getPassword());
            session.put(LOGGED_IN, true);
            
            logger.debug("authenticateClient -> Cliente = " + session.get(CLIENT) + ":" + session.get(PASSWORD));
            this.resetView();
            
            // Se for manager
            if (client.getAccountType().equals(AccountType.Manager.toString()))
                return "/manager/principal";
            else
                return "/normal/principal";
        }
        else {
            
            
            FacesContext.getCurrentInstance().addMessage(INFO, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRO", LOGIN_FAILED_MESSAGE));
            logger.error(LOGIN_FAILED_MESSAGE);
            
            this.resetView();
            return null;
        }
    }

    public String registerNewClient() {
        
        Map<String, Object> session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        
        ClientDto client = loginEjb.createNewAccount(
                clientView.getName(), 
                clientView.getEmail(),
                clientView.getPassword()
        );
        
        if (client != null) {
            session.put(CLIENT, client);
            session.put(PASSWORD, clientView.getPassword());
            session.put(LOGGED_IN, true);
            
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "SUCESSO", new StringBuilder()
                            .append("Novo cliente '").append(clientView.getName())
                            .append("' registado com sucesso!").toString()
            );
            FacesContext.getCurrentInstance().addMessage(INFO, msg);
            
            this.resetView();
            return "/normal/principal";
        }
        else {
            FacesContext.getCurrentInstance().addMessage(INFO, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRO", REGISTRY_FAILED_MESSAGE));
            this.resetView();
            return null;
        }
    }
    
        
    
    // Navegacao
    public String returnToIndex() {
        return "/index";
    }


    public ClientDto getClientView() {
        return clientView;
    }

    public void setClientView(ClientDto clientView) {
        this.clientView = clientView;
    }
    
    
    private void resetView() {
        this.clientView = new ClientDto();
    }
}

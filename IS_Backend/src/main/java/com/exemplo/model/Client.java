package com.exemplo.model;

import com.exemplo.dto.ClientDto;
import com.exemplo.utils.Security;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "client")
public class Client implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "client_id")
    private long clientId;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private byte[] password;

    @Column(nullable = false)
    private double wallet;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    
    //@ManyToMany
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "client_trip",
            joinColumns = {
                    @JoinColumn(name = "client_id", referencedColumnName = "client_id",
                            nullable = false, updatable = false)},
            inverseJoinColumns = {
                    @JoinColumn(name = "trip_id", referencedColumnName = "trip_id",
                            nullable = false, updatable = false)})
    private List<Trip> tickets;

    public Client() {
        tickets = new ArrayList<>();
    }
    
    public Client(String name, String email, String password, AccountType accountType) throws Exception {
        
        if (accountType == null)
            throw new Exception("[CLIENT] accountType invalido");
        this.accountType = accountType;
        
        if (StringUtils.isEmpty(email))
            throw new Exception("[CLIENT] email invalido");
        this.email = email;
        
        if (StringUtils.isEmpty(name))
            throw new Exception("[CLIENT] name invalido");
        this.name = name;
        
        if (StringUtils.isEmpty(password))
            throw new Exception("[CLIENT] password invalida");
        this.password = Security.encrypt(password).getBytes();
        
        this.wallet = 0;
        this.tickets = new ArrayList<>();
    }
    
    public Client(ClientDto dto) throws Exception {
        
        if (dto.getAccountType() == null) {
            throw new Exception("[CLIENT] accountType invalido");
        }
        else if (dto.getAccountType().equals(AccountType.Manager.toString())) {
            this.accountType = AccountType.Manager;
        }
        else {
            this.accountType = AccountType.Normal;
        }
        
        if (StringUtils.isEmpty(dto.getEmail()))
            throw new Exception("[CLIENT] email invalido");
        this.email = dto.getEmail();
        
        if (StringUtils.isEmpty(dto.getName()))
            throw new Exception("[CLIENT] name invalido");
        this.name = dto.getName();
        
        if (StringUtils.isEmpty(dto.getPassword()))
            throw new Exception("[CLIENT] password invalida");
        this.password = Security.encrypt(dto.getPassword()).getBytes();
        
        this.wallet = 0;
        this.tickets = new ArrayList<>();
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
    
    public List<Trip> getTickets() {
        return tickets;
    }

    public void setTickets(List<Trip> tickets) {
        this.tickets = tickets;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }
    
    public void setPasswordString(String password) throws Exception {
        if (StringUtils.isEmpty(password))
            throw new Exception("[CLIENT] password invalida");
        this.password = Security.encrypt(password).getBytes();
    }

    public double getWallet() {
        return wallet;
    }

    public void setWallet(double wallet) {
        this.wallet = wallet;
    }

    @Override
    public String toString() {
        return "Client{" + "name=" + name + ", email=" + email + ", password=" + Arrays.toString(password) + ", accountType=" + accountType + ", tickets=" + tickets.size() + ", wallet=" + wallet + '}';
    }
    
    public ClientDto convertToDTO() {
        ClientDto client = new ClientDto();
        client.setAccountType(accountType.toString());
        client.setEmail(this.email);
        client.setName(this.name);
        client.setPassword("******?");
        client.setWallet(this.wallet);
        client.setnTickets(tickets.size());
        return client;
    }
    
    public static List<ClientDto> convertListToDTO(List<Client> clients) {
        List<ClientDto> cliDtos = new ArrayList<>();
        clients.forEach((c) -> {
            cliDtos.add(c.convertToDTO());
        });
        return cliDtos;
    }
    
    public double chargeWallet(double amount) {
        wallet += amount;
        return wallet;
    }
}

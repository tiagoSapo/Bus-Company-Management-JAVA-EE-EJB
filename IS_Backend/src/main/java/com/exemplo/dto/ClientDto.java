package com.exemplo.dto;

import java.io.Serializable;
import java.util.List;

public class ClientDto implements Serializable {

    private String name;
    private String email;
    private String password;
    private double wallet;
    private String accountType;
    private int nTickets;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getWallet() {
        return wallet;
    }

    public void setWallet(double wallet) {
        this.wallet = wallet;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public int getnTickets() {
        return nTickets;
    }

    public void setnTickets(int nTickets) {
        this.nTickets = nTickets;
    }

    @Override
    public String toString() {
        return "Client{" + "name=" + name + ", email=" + email + ", password=" 
                + password + ", accountType=" + accountType + ", tickets=" 
                + nTickets + ", wallet=" + wallet + '}';
    }
    
}

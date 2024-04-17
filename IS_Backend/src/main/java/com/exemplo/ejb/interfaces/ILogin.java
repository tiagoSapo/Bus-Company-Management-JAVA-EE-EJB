
package com.exemplo.ejb.interfaces;

import com.exemplo.dto.ClientDto;
import javax.ejb.Local;

@Local
public interface ILogin {
    public ClientDto login(String email, String password);
    public ClientDto createNewAccount(String name, String email, String password);
}

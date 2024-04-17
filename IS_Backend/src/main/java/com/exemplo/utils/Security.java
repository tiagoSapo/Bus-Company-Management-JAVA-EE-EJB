
package com.exemplo.utils;

import java.util.Arrays;
import java.util.Base64;

public class Security {
    
    public static boolean checkPassword(String givenPassword, byte[] persistedPassword) {
        
        byte[] encodedPassword = Base64.getEncoder().encode(givenPassword.getBytes());
        return Arrays.equals(encodedPassword, persistedPassword);
    }
    
    // Encripta palavra-passe e devolve o resultado sob forma de String
    public static String encrypt(String password) {
        
        byte[] encodedBytes = Base64.getEncoder().encode(password.getBytes());
        return new String(encodedBytes);
    }
    
    public static void decrypt() {
        
        /*byte[] encodedBytes = Base64.getEncoder().encode("Test".getBytes());
        System.out.println("encodedBytes " + new String(encodedBytes));
        
        String encriptado = new String(encodedBytes); 
        
        byte[] decodedBytes = Base64.getDecoder().decode(encriptado);
        System.out.println("decodedBytes " + new String(decodedBytes));*/
        
    }
}

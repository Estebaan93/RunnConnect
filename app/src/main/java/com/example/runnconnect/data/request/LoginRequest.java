//data/request/LoginRequest
package com.example.runnconnect.data.request;

import androidx.navigation.internal.Log;

public class LoginRequest { //dtos de entrada que se envian al servidor
  private String email;
  private String password;


  public LoginRequest (String email, String password){
    this.email=email;
    this.password=password;
  }

  //get set


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


}

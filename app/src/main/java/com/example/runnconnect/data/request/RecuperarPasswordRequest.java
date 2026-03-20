package com.example.runnconnect.data.request;

public class RecuperarPasswordRequest {
  private String email;

  public RecuperarPasswordRequest(String email){
    this.email= email;
  }

  //get set
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

}

package com.example.runnconnect.data.preferencias;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.runnconnect.data.model.Usuario;
import com.example.runnconnect.data.response.LoginResponse;

public class SessionManager {
  private static final String PREF_NOMBRE="session_sp";
  private static final String KEY_TOKEN="token";
  private static final String KEY_USER_ID = "idUsuario";
  private static final String KEY_USER_NAME = "nombre";
  private static final String KEY_USER_EMAIL = "email";
  private static final String KEY_USER_TYPE = "tipoUsuario";
  private static final String KEY_USER_AVATAR = "imgAvatar";
  private final SharedPreferences sp;
  private final SharedPreferences.Editor editor;


  //constructor
  public SessionManager(Context context){
    sp=context.getSharedPreferences(PREF_NOMBRE, 0); //0 private
    editor=sp.edit();
  }

  //guardar token
  public void guardarToken(String token){
    editor.putString(KEY_TOKEN, token);
    editor.apply();
  }

  //leer token
  public String leerToken(){
    return sp.getString(KEY_TOKEN, null);
  }

  //guardar sesion
  public void guardarSesionUsuario(LoginResponse response){
    Usuario usuario= response.getUsuario();
    guardarToken(response.getToken()); //guardamos el token

    editor.putInt(KEY_USER_ID, usuario.getIdUsuario());
    editor.putString(KEY_USER_NAME, usuario.getNombre());
    editor.putString(KEY_USER_EMAIL, usuario.getEmail());
    editor.putString(KEY_USER_TYPE, usuario.getTipoUsuario());
    editor.putString(KEY_USER_AVATAR, usuario.getImgAvatar());
    editor.apply();
  }

  //cerrar sesion
  public void cerrarSesion(){
    editor.clear();
    editor.apply();
  }

  //obtener tipo de usuario
  public String getTipoUsuario(){
   return sp.getString(KEY_USER_TYPE,"runner");
  }


}

package com.example.runnconnect.data.preferencias;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SincronizarPerfil {

  private static final MutableLiveData<Long> eventoPerfilActualizado = new MutableLiveData<>();

  public static LiveData<Long> getEventoPerfilActualizado() {
    return eventoPerfilActualizado;
  }

  public static void notificarPerfilActualizado() {
    eventoPerfilActualizado.postValue(System.currentTimeMillis());
  }
}

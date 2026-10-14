package com.example.runnconnect.data.fcm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class NotificacionSyncManager {

  private static final MutableLiveData<Long> eventoNuevaNotificacion = new MutableLiveData<>();

  public static LiveData<Long> getEventoNuevaNotificacion() {
    return eventoNuevaNotificacion;
  }

  public static void notificarNuevaLlegada() {
    eventoNuevaNotificacion.postValue(System.currentTimeMillis());
  }
}

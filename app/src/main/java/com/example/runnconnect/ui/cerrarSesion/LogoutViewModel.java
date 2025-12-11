//ui/cerrarSesion/LogoutViewModel
package com.example.runnconnect.ui.cerrarSesion;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.UsuarioRepositorio;

public class LogoutViewModel extends AndroidViewModel {
  private final UsuarioRepositorio repo;
  private final MutableLiveData<Boolean> navigarAlLogin= new MutableLiveData<>();
  private final MutableLiveData<Boolean> ordenCerrarDialogo = new MutableLiveData<>();
  public LogoutViewModel(@NonNull Application application) {
    super(application);
    this.repo = new UsuarioRepositorio(application);
  }

  public LiveData<Boolean>getNavegarAlLogin(){
    return navigarAlLogin;
  }
  public LiveData<Boolean>getOrdenCerrarDialogo(){
    return ordenCerrarDialogo;
  }

  public void cerrarSesion(){
    //destruimos el token
    repo.cerrarSesion();

    //nav al login con exito
    navigarAlLogin.setValue(true);

  }

  public void volverAtras(){
    //el usuario vuelve atras
    ordenCerrarDialogo.setValue(true);
  }


}

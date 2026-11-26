package com.example.runnconnect.ui.runner.terminos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.R;

public class TerminosCondicionesViewModel extends AndroidViewModel {

  private final MutableLiveData<String> titulo = new MutableLiveData<>("Deslinde de Responsabilidades");
  private final MutableLiveData<String> contenido = new MutableLiveData<>();

  public TerminosCondicionesViewModel(@NonNull Application application) {
    super(application);
    contenido.setValue(application.getString(R.string.deslinde_responsabilidades_texto));
  }

  public LiveData<String> getTitulo() {
    return titulo;
  }

  public LiveData<String> getContenido() {
    return contenido;
  }
}

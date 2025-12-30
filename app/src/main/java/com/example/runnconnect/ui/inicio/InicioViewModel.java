//ui/runner/inicio/InicioViewModel
package com.example.runnconnect.ui.inicio;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.model.Noticia;
import com.example.runnconnect.data.repositorio.NoticiasRepositorio;

import java.util.List;

public class InicioViewModel extends AndroidViewModel {
  private final NoticiasRepositorio repositorio;
  private final MutableLiveData<List<Noticia>> listaNoticias = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
  private final MutableLiveData<String> mensajeError = new MutableLiveData<>();

  // Constructor que recibe Application
  public InicioViewModel(@NonNull Application application) {
    super(application);

    repositorio = new NoticiasRepositorio();

    cargarNoticias();
  }

  public LiveData<List<Noticia>> getListaNoticias() { return listaNoticias; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getMensajeError() { return mensajeError; }

  public void cargarNoticias() {
    isLoading.setValue(true);
    repositorio.obtenerNoticias(new NoticiasRepositorio.NoticiasCallback() {
      @Override
      public void onSuccess(List<Noticia> noticias) {
        isLoading.postValue(false);
        listaNoticias.postValue(noticias);
      }

      @Override
      public void onError(String mensaje) {
        isLoading.postValue(false);
        mensajeError.postValue(mensaje);
      }
    });
  }

}
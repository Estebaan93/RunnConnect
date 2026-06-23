//ui/runner/inicio/InicioViewModel
package com.example.runnconnect.ui.inicio;

import android.app.Application;
import android.view.View;

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

  //nuevo 22-06
  private final MutableLiveData<Boolean> listaVacia = new MutableLiveData<>(true);
  private final MutableLiveData<String> errorText = new MutableLiveData<>();
  private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>(View.GONE);


  // Constructor que recibe Application
  public InicioViewModel(@NonNull Application application) {
    super(application);

    repositorio = new NoticiasRepositorio();

    cargarNoticias();
  }

  public LiveData<List<Noticia>> getListaNoticias() { return listaNoticias; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<Boolean> getListaVacia() { return listaVacia; }
  public LiveData<String> getErrorText() { return errorText; }
  public LiveData<Integer> getErrorVisibility() { return errorVisibility; }


  public void cargarNoticias() {
    isLoading.setValue(true);
    //nuevo 26-06
    ocultarError();

    repositorio.obtenerNoticias(new NoticiasRepositorio.NoticiasCallback() {
      @Override
      public void onSuccess(List<Noticia> noticias) {
        isLoading.postValue(false);
        listaNoticias.postValue(noticias);
        //nuevo 26-06
        listaVacia.postValue(noticias == null || noticias.isEmpty());
      }

      @Override
      public void onError(String mensaje) {
        isLoading.postValue(false);
        //nuevo 26-06
        mostrarError(mensaje); //error de carga
        listaVacia.postValue(false); //ocultar recyclerView
      }
    });
  }

  //nuevo 26-06
  private void mostrarError(String mensaje) {
    errorText.postValue(mensaje);
    errorVisibility.postValue(View.VISIBLE);
  }
  //nuevo 26-06
  private void ocultarError() {
    errorVisibility.postValue(View.GONE);
  }

}
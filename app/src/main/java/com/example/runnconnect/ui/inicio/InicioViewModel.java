//ui/runner/inicio/InicioViewModel
package com.example.runnconnect.ui.inicio;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Xml;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

//import com.example.runnconnect.data.conexion.ApiClient;
//import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.model.Noticia;
//import com.example.runnconnect.data.preferencias.SessionManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class InicioViewModel extends AndroidViewModel {
  //private final ApiService apiService;
  //private final SessionManager sessionManager;
  private final MutableLiveData<List<Noticia>> listaNoticias = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

  //nuevo 22-06
  private final MutableLiveData<Boolean> listaVacia = new MutableLiveData<>(true);
  private final MutableLiveData<String> errorText = new MutableLiveData<>();
  private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>(View.GONE);

  // Estados de navegacion del navegador (errores)
  private final MutableLiveData<String> errorNavegacionText = new MutableLiveData<>();
  private final MutableLiveData<Integer> errorNavegacionVisibility = new MutableLiveData<>(View.GONE);

  private static final String RSS_URL = "https://gist.githubusercontent.com/Estebaan93/46557f304368d30e1ddc4d0e6f0ec202/raw/gistfile1.txt";
  private final OkHttpClient client = new OkHttpClient();

  // Constructor que recibe Application
  public InicioViewModel(@NonNull Application application) {
    super(application);
    //this.apiService = ApiClient.getApiService();
    //this.sessionManager = new SessionManager(application);

    cargarNoticias();
  }

  public LiveData<List<Noticia>> getListaNoticias() { return listaNoticias; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<Boolean> getListaVacia() { return listaVacia; }
  public LiveData<String> getErrorText() { return errorText; }
  public LiveData<Integer> getErrorVisibility() { return errorVisibility; }
  public LiveData<String> getErrorNavegacionText() { return errorNavegacionText; }
  public LiveData<Integer> getErrorNavegacionVisibility() { return errorNavegacionVisibility; }

  public void cargarNoticias() {
    isLoading.setValue(true);
    ocultarError();

    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.execute(() -> {
      try {
        List<Noticia> noticias = conectarYParsear();
        isLoading.postValue(false);
        listaNoticias.postValue(noticias);
        listaVacia.postValue(noticias == null || noticias.isEmpty());
      } catch (Exception e) {
        e.printStackTrace();
        Log.e("NoticiasError", "Fallo final: " + e.getMessage());
        isLoading.postValue(false);
        mostrarError("No se pudo conectar. Intenta nuevamente");
        listaVacia.postValue(false);
      }
    });
  }

  private List<Noticia> conectarYParsear() throws IOException, XmlPullParserException {
    Request request = new Request.Builder()
            .url(RSS_URL)
            .build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new IOException("El servidor rechazo la conexion: " + response.code());
      }

      ResponseBody body = response.body();
      if (body != null) {
        InputStream stream = body.byteStream();

        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(stream, null);
        parser.nextTag();

        return leerFeed(parser);
      } else {
        throw new IOException("Respuesta vacia del servidor");
      }
    }
  }

  private List<Noticia> leerFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
    List<Noticia> noticias = new ArrayList<>();
    parser.require(XmlPullParser.START_TAG, null, "rss");
    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.getEventType() != XmlPullParser.START_TAG) continue;
      String name = parser.getName();
      if (name.equals("channel")) {
        noticias.addAll(leerCanal(parser));
      } else {
        skip(parser);
      }
    }
    return noticias;
  }

  private List<Noticia> leerCanal(XmlPullParser parser) throws IOException, XmlPullParserException {
    List<Noticia> items = new ArrayList<>();
    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.getEventType() != XmlPullParser.START_TAG) continue;
      String name = parser.getName();
      if (name.equals("item")) {
        items.add(leerItem(parser));
      } else {
        skip(parser);
      }
    }
    return items;
  }

  private Noticia leerItem(XmlPullParser parser) throws IOException, XmlPullParserException {
    String titulo = null, link = null, descripcion = null, fecha = null, imagen = null;

    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.getEventType() != XmlPullParser.START_TAG) continue;
      String name = parser.getName();
      switch (name) {
        case "title": titulo = leerTexto(parser); break;
        case "link": link = leerTexto(parser); break;
        case "pubDate": fecha = leerTexto(parser); break;
        case "content:encoded":
        case "description":
          String contenido = leerTexto(parser);
          if (descripcion == null) descripcion = contenido;
          if (imagen == null) imagen = extraerImagenDeHtml(contenido);
          Log.d("srcImagenRepo", "imgRepoCard: " + imagen);
          break;
        default: skip(parser); break;
      }
    }
    return new Noticia(titulo, descripcion, link, imagen, fecha);
  }

  private String leerTexto(XmlPullParser parser) throws IOException, XmlPullParserException {
    String result = "";
    if (parser.next() == XmlPullParser.TEXT) {
      result = parser.getText();
      parser.nextTag();
    }
    return result;
  }

  private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
    if (parser.getEventType() != XmlPullParser.START_TAG) throw new IllegalStateException();
    int depth = 1;
    while (depth != 0) {
      switch (parser.next()) {
        case XmlPullParser.END_TAG: depth--; break;
        case XmlPullParser.START_TAG: depth++; break;
      }
    }
  }

  private String extraerImagenDeHtml(String html) {
    if (html == null) return null;

    Pattern pattern = Pattern.compile("src\\s*=\\s*['\"]([^'\"]+)['\"]");
    Matcher matcher = pattern.matcher(html);
    if (matcher.find()) return matcher.group(1);
    return null;
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

  public void onErrorAlAbrirNavegador() {
    errorNavegacionText.setValue("No se pudo abrir el enlace");
    errorNavegacionVisibility.setValue(View.VISIBLE);

    // Ocultamos despues de 5 segundos
    new Handler(Looper.getMainLooper()).postDelayed(() -> 
      errorNavegacionVisibility.setValue(View.GONE), 5000);
  }

}
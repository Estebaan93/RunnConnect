package com.example.runnconnect.data.repositorio;

import android.util.Log;
import android.util.Xml;

import com.example.runnconnect.data.model.Noticia;

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


public class NoticiasRepositorio {
  //feed noticias SL
  private static final String RSS_URL = "https://gist.githubusercontent.com/Estebaan93/46557f304368d30e1ddc4d0e6f0ec202/raw/17dedcf1622104eb0f18f5d94408d3fd084f4c08/gistfile1.txt";
  private final OkHttpClient client = new OkHttpClient();

  public interface NoticiasCallback {
    void onSuccess(List<Noticia> noticias);
    void onError(String mensaje);
  }

  public void obtenerNoticias(NoticiasCallback callback) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.execute(() -> {
      try {
        List<Noticia> noticias = conectarYParsear();
        callback.onSuccess(noticias);
      } catch (Exception e) {
        e.printStackTrace();
        Log.e("NoticiasError", "Fallo final: " + e.getMessage());
        callback.onError("No se pudo conectar. El servidor bloquea la solicitud.");
      }
    });
  }

  private List<Noticia> conectarYParsear() throws IOException, XmlPullParserException {
    // CAMBIO CLAVE: Usamos Headers de PC Windows para saltar el bloqueo 403
    Request request = new Request.Builder()
            .url(RSS_URL)
            .build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        // Si aún así falla (ej. sigue dando 403), lanzamos excepcion con el codigo
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

  // PARSEO XML

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
    // Regex simple para buscar src="..."
    Pattern pattern = Pattern.compile("src\\s*=\\s*['\"]([^'\"]+)['\"]");
    Matcher matcher = pattern.matcher(html);
    if (matcher.find()) return matcher.group(1);
    return null;
  }


}

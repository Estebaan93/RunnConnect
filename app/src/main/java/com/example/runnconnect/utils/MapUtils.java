package com.example.runnconnect.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class MapUtils {
  public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
    try {
      Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
      if (vectorDrawable == null) return null;
      vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
      Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(bitmap);
      vectorDrawable.draw(canvas);
      return BitmapDescriptorFactory.fromBitmap(bitmap);
    } catch (Exception e) {
      return BitmapDescriptorFactory.defaultMarker();
    }
  }
}

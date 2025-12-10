//ui/runner/inicio/InicioViewModel
package com.example.runnconnect.ui.runner.inicio;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class InicioViewModel extends AndroidViewModel {
  private final MutableLiveData<String> mText;
  public InicioViewModel(@NonNull Application application) {
    super(application);
    mText=new MutableLiveData<>();
    mText.setValue("Feed de noticias");
  }

  // TODO: Implement the ViewModel

  public LiveData<String>getText(){
    return mText;
  }

}
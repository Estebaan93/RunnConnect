//ui/eventosPublicos/EventosPublicosViewModel
package com.example.runnconnect.ui.eventosPublicos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


public class EventosPublicosViewModel extends AndroidViewModel {

  private final MutableLiveData<String> mText;

  public EventosPublicosViewModel(@NonNull Application application) {
    super(application);
    mText = new MutableLiveData<>();
    mText.setValue("This is home fragment");
  }

  public LiveData<String> getText() {
    return mText;
  }
}
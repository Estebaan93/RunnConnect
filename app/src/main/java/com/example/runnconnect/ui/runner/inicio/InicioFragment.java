//ui/runner/inicio/InicioFragment
package com.example.runnconnect.ui.runner.inicio;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.runnconnect.databinding.FragmentInicioBinding;

public class InicioFragment extends Fragment {
  private FragmentInicioBinding binding;
  private InicioViewModel mViewModel;


  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    //iniciamos el vm
    mViewModel= new ViewModelProvider(this).get(InicioViewModel.class);

    //inflamos
    binding=FragmentInicioBinding.inflate(inflater, container, false);
    View root= binding.getRoot();
    //
    TextView textView=binding.textInicio;
    mViewModel.getText().observe(getViewLifecycleOwner(), text->{
      textView.setText(text);
    });



    return root;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null; // Evitar fugas de memoria
  }

}
package com.example.f4sINV.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.f4sINV.Parametros;
import com.example.f4sINV.R;
import com.example.f4sINV.activity.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class FragmentInvCurso extends Fragment {

    private View view;
    TextView txtSesionesInv, txtEtiSave, txtUltFecHor, txtUltObs;
    private MainActivity mContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mContext = (MainActivity) getActivity();

        view=inflater.inflate(R.layout.fragment_inv_curso, container, false);

        txtSesionesInv = (TextView) view.findViewById(R.id.tv_SesionesInv);
        txtEtiSave = (TextView) view.findViewById(R.id.tv_EtiSave);
        txtUltFecHor = (TextView) view.findViewById(R.id.tv_UltFecHor);
        txtUltObs = (TextView) view.findViewById(R.id.tv_UltObs);

        //Guarda valores
        SharedPreferences preferencias = mContext.getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
        int sesionesInv = preferencias.getInt(Parametros.pref_SESIONES_INV, 0);
        int etiSesionesSave = preferencias.getInt(Parametros.pref_ETI_SESIONES_SAVE, 0);
        String fechaHoraSave = preferencias.getString(Parametros.pref_FECHA_HORA_SAVE, "");
        String observacion = preferencias.getString(Parametros.pref_OBSERVACION_SAVE, "");

        txtSesionesInv.setText(sesionesInv +"");
        txtEtiSave.setText(etiSesionesSave +"");
        txtUltFecHor.setText(fechaHoraSave);
        txtUltObs.setText(observacion);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //binding = null;
    }
}
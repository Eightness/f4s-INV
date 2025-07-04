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

import com.example.f4sINV.Parametros;
import com.example.f4sINV.R;
import com.example.f4sINV.activity.TabToolsActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class ToolLogFragment  extends Fragment {

    private static final String TAG = "ToolLogFragment";

    private TextView tv_titulo;
    private TextView tv_log;
    private TabToolsActivity mContext;

    //private static String tabActiva;

    public ToolLogFragment() {
        // Required empty public constructor

    }
    public static ToolLogFragment newInstance() {
        //tabActiva = tab;
        ToolLogFragment fragment = new ToolLogFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //Log.i(TAG, "onCreateView");
        return inflater.inflate(R.layout.tool_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = (TabToolsActivity) getActivity();

        tv_titulo = (TextView) getView().findViewById(R.id.tv_titulo);
        tv_log = (TextView) getView().findViewById(R.id.tv_log);

        //Guarda valor
        SharedPreferences preferencias = mContext.getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString(mContext.pref_PANTA, "TABtool1");
        editor.apply();

        salidaLog();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //Desacoplamiento del Fragment de la Actividad por invocación de otra pestaña
        //-> Si está escaneando RFID pararlo ! y salir a otra espetaña que se ha invocado
    }


    private void salidaLog (){

        File fic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Parametros.ficLog);
        tv_titulo.setText("Fichero Log: " + fic.getName());

        String texto="";
        try {
            FileInputStream f = new FileInputStream(fic);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(f));
            int n=0;
            String linea;
            do {
                 linea = entrada.readLine();
                 if (linea!=null){
                     texto+=linea+"\n";
                 }
            } while (linea!=null);
            f.close();
            tv_log.setText(texto);
        }
        catch (FileNotFoundException e) {
            //Log.e(TAG, e.getMessage(), e);
            //Toast.makeText(mContext, "Error FileNotFoundException: " + e.getMessage(), Toast.LENGTH_LONG).show();
            //Log.i("TAG", "FileNotFoundException");

        } catch (Exception e) {
            //Log.e(TAG, e.getMessage(), e);
            //Toast.makeText(mContext, "Error Exception: "+e.getMessage(),Toast.LENGTH_LONG).show();
            //Log.i("TAG","Exception");
        }
    }
}

package com.example.f4sINV.activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.Gen2Entity;

//public class BaseFragmentActivity extends FragmentActivity {
public class BaseAppActivity extends AppCompatActivity {

    private static final String TAG = "BaseAppActivity";

    public RFIDWithUHFUART mReader;

    private static String[] listSesion = {"S0","S1","S2","S3"};
    private static String[] listTarget= {"A","B","AB"};
    private static String[] listFreq = {
            "0 -Ni se sabe qué zona (¿?MHz)",
            "1 -Ni se sabe qué zona (¿?MHz)",
            "2 -Ni se sabe qué zona (¿?MHz)",
            "3 -Ni se sabe qué zona (¿?MHz)",
            "ETSI Standard (865-868MHz)",
            "Otros..."};

    private static String[] listProto = {
            "ISO 18000-6C",
            "GB/T 29768",
            "GJB 7377.1",
            "ISO 18000-6B"};
    private static String[] listRFLink = {
            "DSB_ASK/FM0/40KHz",
            "PR_ASK/Miller4/250KHz",
            "PR_ASK/Miller4/300KHz",
            "DSB_ASK/FM0/400KHz"};

    public String sesion, target, qVal;
    public int q;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void initUHF() {

        try {
            Log.i(TAG, "initUHF() try");
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception ex) {

            Log.i(TAG, "Exception ex)");
            toastMessage(ex.getMessage());

            return;
        }

        if (mReader != null) {
            //Inicializa reader
            new InitTask().execute();
        }
    }

    public void toastMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public void getParamEPC(){
        //Carga la configuración por defecto
        sesion="Sin IdSesión";          //iD Sesion S0, S1, S2, S3
        target="Sin target A/B";        //Target A, B o AB
        qVal ="Sin Q";        //Target A, B o AB
        int idx=0;
        Gen2Entity p=mReader.getGen2();
        if (p != null) {
            idx = p.getQuerySession();
            if (idx != -1){
                sesion =  "Idx:" + idx + " - " + listSesion[p.getQuerySession()];
            }
            idx = p.getQueryTarget();
            if (idx != -1){
                target =  "Idx:" + idx + " - " + listTarget[idx];
            }
            idx = p.getQ();
            if (idx != -1){
                qVal =  "Valor Q = " + idx;
            }
        }
    }

    //public boolean setParamEPC(int idxSesion, int idxTarget, int valq){
    //* Valor o índice de Q – No parece que lo use esta API de Chw
    public boolean setParamEPC(int idxSesion, int idxTarget){
        if(mReader!=null) {
            Gen2Entity p=mReader.getGen2();
            if (p != null) {
                p.setQuerySession(idxSesion);
                p.setQueryTarget(idxTarget);
                //p.setQ(valq);                 //No se acualiza ningún valor de Q
                return mReader.setGen2(p);
            }
        }
        return false;
    }

    public String getPower() {
        String result = "Falla Reader";
        if(mReader!=null) {
            int idx = mReader.getPower();
            if (idx != -1){
                result = idx + " mW";
            }
        } else {
            result =  "Reader no conectado";
        }
        return  result;
    }

    public boolean setPower(int mDb){
        return mReader.setPower(mDb);
    }

    public String getFreq() {
        String result = "Falla Reader";
        if(mReader!=null) {
            int idx = mReader.getFrequencyMode();
            if (idx != -1){
                int count = listFreq.length;
                result = "Idx:" + idx + " - " + listFreq[idx > count - 1 ? count - 1 : idx];
            }
        } else {
            result =  "Reader no conectado";
        }
        return  result;
    }

    //No se utiliza el seteo de Frecuencia de Región por no ser fiable con las distimtas API's se fija con la tool AppCenter de Cahinway
    public boolean setFreq(int idx){
        return mReader.setFrequencyMode(idx);
    }

    public String getRFLink() {
        String result = "Falla Reader";
        if(mReader!=null) {
            int idx = mReader.getRFLink();
            if (idx != -1){
                int count = listRFLink.length;
                result = "Idx:" + idx + " - " + listRFLink[idx > count - 1 ? count - 1 : idx];
            }
        } else {
            result =  "Reader no conectado";
        }
        return  result;
    }

    public boolean setRFLink(int idx){
        return mReader.setRFLink(idx);
    }

    //NO recuperar Protocolo, casca con C72 - solo permite idx=0 = "18000-6C"
    public String getProto() {
        String result = "Falla Reader";
        if(mReader!=null) {
            int idx = mReader.getProtocol();
            if (idx != -1){
                int count = listProto.length;
                result = "Idx:" + idx + " - " + listProto[idx > count - 1 ? count - 1 : idx];
            }
        } else {
            result =  "Reader no conectado";
        }
        return  result;
    }

    //NO Actualiza C72 Protocolo - solo permite idx=0 = "18000-6C"
    public boolean setProto(int idx){
        return mReader.setProtocol(idx);
    }


    public String getSWVersion() {
        if(mReader!=null) {
            return mReader.getVersion();
        }
        return "Sin Reader conectado";
    }

    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub

            return mReader.init();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mypDialog.cancel();
            if (!result) {
                Toast.makeText(BaseAppActivity.this, "init fail", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(BaseAppActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("init...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }
    }
}

package com.example.f4sINV.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.example.f4sINV.Parametros;
import com.example.f4sINV.UhfInfo;
import com.example.f4sINV.R;
import com.example.f4sINV.fragment.FragmentInvCurso;
import com.example.f4sINV.fragment.MiDialogAlert;
import com.example.f4sINV.fragment.UHFReadTagFragment2;
import com.example.f4sINV.fragment.UHFReadTagFragment4;
import com.example.f4sINV.fragment.UHFReadTagFragment5;
import com.example.f4sINV.tools.UIHelper;
import com.google.android.material.tabs.TabLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


//public class TabActivity extends AppCompatActivity {
public class TabInventarioActivity extends BaseAppActivity {

    private static final String TAG = "TabIventarioActivity";

    UHFReadTagFragment2 uhfReadTagFragment2;
    UHFReadTagFragment4 uhfReadTagFragment4;
    UHFReadTagFragment5 uhfReadTagFragment5;

    //Para sabe en qué fragmento está y activar el gatillo de C72 o C5, o el boton scan de C5
    private String fragmentoActivo="";

    public UhfInfo uhfInfo = new UhfInfo();
    public ArrayList<HashMap<String, String>> tagList = new ArrayList<HashMap<String, String>>();;

    HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private SoundPool soundPool;
    private float volumnRatio;
    private AudioManager am;

    //JFA Variables de PREFERENCIAS
    public static final String pref_PANTA = "panta";

    //Resumen de inventario en curso
    public int sesionesInv;             //Sesiones inventario guardadas
    public int etiSesionesSave;         //Etiquetas guardadas
    public int sesionInv;               //Sesión inventario en curso
    public boolean iniSesion= true;     //v.4.3 Para guardar Fecha/Hora en 1era lectura de inventario
    public String fecHor_IniInv;        //v.4.3 Fecha/Hora de 1era lectura de inventario para control de INACTIVADAS con Inventario en curso en f4sGestion_Mon

    private int vPower = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_inventario);
        //Log.i(TAG, "onCreate");

        //Guarda valor
        SharedPreferences preferencias = getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString(pref_PANTA, "PANTA2");        //Inicializa siempre con PANTA2
        editor.apply();

        sesionesInv = preferencias.getInt(Parametros.pref_SESIONES_INV, 0);
        sesionInv  = sesionesInv +1;   //Sigueinte sesion en curso
        //etiSesionesSave = PreferenceManager.getDefaultSharedPreferences(this).getInt(pref_ETI_SESIONES_SAVE, 0);
        etiSesionesSave = preferencias.getInt(Parametros.pref_ETI_SESIONES_SAVE, 0);

        //Prestaña
        //Codigo de 3 Tabs sin VewPager en version Prueba10.0
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("INVENTARIO").setTag("PANTA2"));
        tabs.addTab(tabs.newTab().setText("ETI. INACTIVAS").setTag("PANTA4"));
        tabs.addTab(tabs.newTab().setText("TRASPASO").setTag("PANTA5"));

        //final TextView texto = findViewById(R.id.texto);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //Log.i(TAG, "onTabSelected");

                //Saca la pestaña que corresponda
                selectFragment(tab.getTag().toString());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //Log.i(TAG, "onTabReselected");
                //toastMessage("Seleccionda pestaña");
            }
        });

        //v.3.0
        uhfInfo.setErrF4s(0);       //Inicializa errores de traspaso entre fragments
        if (!readFileIni()){
            uhfInfo.setErrF4s(1);   //Error en fic INF_f4s.ini
        }

        //Iniciar UHF y sonidos
        initUHF();
        initSound();

        //Uan vez inicializado el reader con initUHF() realiza el Setter de los param EPC
        //-------------------------------------------------------------------------------
/*
        //Actualiza Frecuencia de zona a PIÑO FIJO ETSI
        //No se utiliza el seteo de Frecuencia de Región por no ser fiable con las distimtas API's se fija con la tool AppCenter de Cahinway
        if (!setFreq(4)){
            UIHelper.writeLog("Idx Frecuencia " + "4" + " no actualizada", false);
        }
*/
        //Log inicio de INVENTARIO
        UIHelper.writeLog("%03%-INVENTARIO Sesion: " + sesionInv, true);

        //En pruebas con cambios en AppCenter NO actualices nada para ver si recupera lo MISMO que lo configurado en AppCenter
        //Actualiza Potencia mDb
        if (!setPower(Parametros.potencia)){
            UIHelper.writeLog("Potencia " + vPower + " no actualizada", false);
        }
        //Actualiza Sesion y Target - ATENCION = valor Q debe ser a piño fijo = 1 en esta API de Chw que NO lo usa
        if (!setParamEPC(Parametros.idxSesion, Parametros.idxTarget)){
            UIHelper.writeLog("Idx Sesion, Target " + Parametros.idxSesion + "," + Parametros.idxTarget + " no actualizada", false);
        }

        //Actualiza RF Link
        if (!setRFLink(Parametros.idxRFLink)){
            UIHelper.writeLog("Idx RF Link " + Parametros.idxRFLink + " no actualizada", false);
        }

/*
        //NO Actualiza en C5 y C72 ni recupera en C72 Protocolo - solo permite idx=0 = "18000-6C"
        if (!setProto(1)){
            UIHelper.writeLog("Idx Protocolo " + "0" + " no actualizada", false);
        }
        //NO recuperar Protocolo, casca con C72 - solo permite idx=0 = "18000-6C"
        UIHelper.writeLog("Protocolo: " + getProto(), false);
*/
        //Recupera version SW Chainway
        UIHelper.writeLog("Versión SW Chainway: " + getSWVersion(), false);

        //Recupera Frecuencia de zona
        UIHelper.writeLog("Frecuencia: " + getFreq(), false);

        //Recupera Potencia
        UIHelper.writeLog("Potencia: " + getPower(), false);

        getParamEPC();  //Métdod para recuperar de tacada las descripciones de los param EPC sesion, target AB y Q
        UIHelper.writeLog("Id Sesion: " + sesion, false);
        UIHelper.writeLog("Target: " + target, false);
        UIHelper.writeLog("Valor Q: " + qVal, false);

        //Recupera RF Link
        UIHelper.writeLog("RF Link: " + getRFLink(), false);
    }

    //SISTEMA DE ESCANEADO INIT/STOP COMO INTERRUPTOR con onKeyUp
    //=======================================================================================================================
    //1.- Usar el onKeyUp
    //2.- Asegurar que el Fragmemt UHFReadTagFragment2 de scaeno

    //-------------------------------------------------------------
    //onKeyUp se dispara cuando el usuario deja de apretar la tecla
    //-------------------------------------------------------------
    //Este nos interesa ya que se dispara solo una vez cuando
    //Usar este para usarlo como INTERRUPTOR:
    // - Si scaneado ACTIVO -> PARAR el escaneo
    // - Si scaneado APARADO -> ACTIVAR el escaneo
   @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
       //Log.i(TAG,"fragmentoActivo = " + fragmentoActivo + " onKeyUp = " +keyCode);
       if (keyCode == 4){   //4= atrás Android
           //El código 4 = botón de Abndroid BackPressed, siempre activarlo y en cada Fragment actuará el callback programado:
           //OnBackPressedCallback callback = new OnBackPressedCallback(true) que decide si ir atrás o NO por inventario con lecturas
           this.onBackPressed();
           return false; // Indicar a Android que hemos manejado el evento y no lo propague más
       }
       //Para escanear RFID con gatillo o pulsadores asegurar que estamos en el Fragmeent de escanear RFID = "PANTA2"
       if(!("PANTA2".equals(fragmentoActivo))){
            return false; // Indicar a Android que no hemos manejado el evento y lo propague
        }
        //Codigos:
        //C72 y C5 -------------
        //Gatillo de pistola = 293,, Btn derecho/izquierdo scan =
        //C66 ------------- sin gatillo de pistola
        //Btn derecho = 293 / izquierdo = 291
        if (keyCode == 293 || keyCode == 291){
            // Lanzar el método público de iniciar/parar escaneo
            uhfReadTagFragment2.readTag();
            return true; // Indicar a Android que hemos manejado el evento y no lo propague más
        }
        return false; // Indicar a Android que no hemos manejado el evento y lo propague
    }

    /*
    //onKeyDown Cuando el usuario deja de apretar la tecla
    //----------------------------------------------------
    //No usar este ya que se dispara TANTAS VECES como el user siga apretando la tecla
    //Este no interesa ya que el gatillo de scan está presionado MUCHO tiempo y se dispaparia sin parar
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG,"Fragmento :" + fragmentoActivo + " onKeyDown = " +keyCode);
        if (keyCode == keyCode ){
            // Hacer algo
            return true; // Indicar a Android que hemos manejado el evento y no lo propague más
        }
        return false; // Indicar a Android que no hemos manejado el evento y lo propague
    }
*/
    //=======================================================================================================================

    @Override
    protected void onDestroy() {
        //Log.i(TAG, "onDestroy");

        releaseSoundPool();
        if (mReader != null) {
            mReader.free();
        }
        super.onDestroy();
        //android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Guarda valor
        SharedPreferences preferencias = getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
        String panta = preferencias.getString(pref_PANTA, "PANTA2");
        //Log.i(TAG, "onResume Preferencia panta TabActivity = " + panta);

        //Recupera la pestaña que corresponda
        selectFragment(panta);
    }

    private void selectFragment(String keyTab){

        //Guarda el fragmento Activo para los eventos de gatillo y botones
        fragmentoActivo =keyTab;

        switch (keyTab){
            case "PANTA2":
                if(uhfReadTagFragment2 ==null) {
                    //Ejemplo de envio de dos parametros
                    uhfReadTagFragment2 = uhfReadTagFragment2.newInstance();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragtab, uhfReadTagFragment2)
                        .commit();
                break;
            case "PANTA4":
                if(uhfReadTagFragment4 ==null) {
                    //Ejemplo de envio de dos parametros
                    uhfReadTagFragment4 = uhfReadTagFragment4.newInstance();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragtab, uhfReadTagFragment4)
                        .commit();
                break;
            case "PANTA5":
                if(uhfReadTagFragment5 ==null) {
                    //Ejemplo de envio de dos parametros
                    uhfReadTagFragment5 = uhfReadTagFragment5.newInstance();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragtab, uhfReadTagFragment5)
                        .commit();
                break;
        }

    }

    //v.3.0 - Por si alguna vez se quiere utilizar un fic .INI
    private boolean readFileIni(){
        FileInputStream is;

        final File fic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "INV_f4s.ini");
        if (fic.exists())
        {
            try {
                is = new FileInputStream(fic);
            } catch (IOException e) {
                return false;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String linea = null;
                String reg = null;
                while ((linea = reader.readLine()) != null) {
                    if ( linea.length() > 7){
                        reg = linea.substring(0,6);
                        //Con la primera marca de "******" deja de leer el fichero
                        if (reg == "******"){
                            break;
                        }
                        switch (reg)
                        {
/*
                            case "idxLNK":
                                idxRFLink = Integer.parseInt(linea.substring(7));
                                break;
*/
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }

    }

    private void initSound() {

    soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
    soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
    soundMap.put(2, soundPool.load(this, R.raw.beep_stop_read, 1));
    soundMap.put(3, soundPool.load(this, R.raw.beep_error, 1));
    soundMap.put(4, soundPool.load(this, R.raw.beep_ftp_ok, 1));
    soundMap.put(5, soundPool.load(this, R.raw.beep_warning_soft, 1));
    soundMap.put(6, soundPool.load(this, R.raw.beep_warning_hard, 1));

    am = (AudioManager) this.getSystemService(AUDIO_SERVICE);// 实例化AudioManager对象
}

    public void playSound(int id) {
        float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 返回当前AudioManager对象的最大音量值
        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);// 返回当前AudioManager对象的音量值
        volumnRatio = audioCurrentVolume / audioMaxVolume;
        try {
            soundPool.play(soundMap.get(id), volumnRatio,
                    volumnRatio,
                    1,
                    0,
                    1
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseSoundPool() {
        if(soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}

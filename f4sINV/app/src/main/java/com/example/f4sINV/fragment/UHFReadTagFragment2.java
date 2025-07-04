package com.example.f4sINV.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.example.f4sINV.Parametros;
import com.example.f4sINV.activity.TabInventarioActivity;
import com.example.f4sINV.R;
import com.example.f4sINV.UhfInfo;
import com.example.f4sINV.tools.StringUtils;
import com.example.f4sINV.tools.UIHelper;

import com.rscja.deviceapi.entity.UHFTAGInfo;

public class UHFReadTagFragment2 extends KeyDwonFragment {
    private static final String TAG = "UHFReadTagFragment2";

    private boolean loopFlag = false;

    private HashMap<String, Integer> mapTempDatas = new HashMap<String, Integer>();

    Button BtClear;
    TextView tv_count;
    TextView tv_total;

    Button BtInventory;
    private TabInventarioActivity mContext;
    private HashMap<String, String> map;

    private int total;
    private String campo;

    //public static final String TAG_EPC = "tagEPC";
    public static final String TAG_EPC_TID = "tagEpcTID";
    public static final String TAG_COUNT = "tagCount";
    public static final String TAG_RSSI = "tagRssi";

    //JFA
    private static final String DIALOGO = "Dlg" + TAG;              //Nombre único
    private static final int REQUEST_result = 0;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            UHFTAGInfo info = (UHFTAGInfo) msg.obj;
            if (Parametros.servicio){
                //Metodo rico con trabajo en servicio más pesado en datos añadiendo EPCs a HashMap con vista de adaptador con tagList
                //addDataToList(info.getEPC(),mergeTidEpc(info.getTid(), info.getEPC(),info.getUser()), info.getRssi());
                addDataToListServ(info.getEPC());
            } else {
                //Metodo ligero en modo de usuario con mayor rendimiento de lecturas sin añadir EPCs a HashMap con vista de adaptador con tagList
                addDataToList(info.getEPC());
            }
        }
    };

    public UHFReadTagFragment2() {
        // Required empty public constructor
    }
    public static UHFReadTagFragment2 newInstance() {
        UHFReadTagFragment2 fragment = new UHFReadTagFragment2();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.i(TAG, "onCreateView - Total = " + total);
        return inflater.inflate(R.layout.uhf_readtag_fragment2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Log.i(TAG, "onViewCreated");
        mContext = (TabInventarioActivity) getActivity();

        BtClear = (Button) getView().findViewById(R.id.BtClear);
        tv_count = (TextView) getView().findViewById(R.id.tv_count);
        tv_total = (TextView) getView().findViewById(R.id.tv_total);
        BtInventory = (Button) getView().findViewById(R.id.BtInventory);

        BtClear.setOnClickListener(new BtClearClickListener());
        BtInventory.setOnClickListener(new BtInventoryClickListener());

        //tv_count.setText(mContext.tagList.size()+"");
        tv_count.setText(mapTempDatas.size()+"");
        mContext.uhfInfo.setTempDatas(mapTempDatas);    //Comparte a global para que pueda recuperar UHFReadTagFragment5 aunque NO se grabe nada con el otro .setTempDatas(mapTempDatas) del final

        //Guarda los valores por defecto de Preferencias si es la 1º carga de la Apll
        SharedPreferences preferencias = mContext.getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
        int etiSesionesSave = preferencias.getInt(Parametros.pref_ETI_SESIONES_SAVE, 0);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString(mContext.pref_PANTA, "PANTA2");
        editor.apply();

        total = mContext.uhfInfo.getCount();
        tv_total.setText(total+"");
        tv_total.setText(etiSesionesSave +"");

        //Errores que pueden venir de TabInventarioActivity, activar la respuesta con acción DIALOGO_ALERT_CON_BTN_ATRAS
        //en onActivityResult(..) para que haya pantalla atrás inmediata, no siga
        if (mContext.uhfInfo.getErrF4s()==1){
            mContext.playSound(6);
            salidaAlerta("ERROR de fichero",
                    "Problemas con fichero INF_f4s.ini",
                    "- Avise al Servicio Técnico.",
                    R.drawable.baseline_warning_amber_24, Parametros.DIALOGO_ALERT_CON_BTN_ATRAS);
        } else if (mContext.uhfInfo.getErrF4s()==2){
            mContext.playSound(6);
            salidaAlerta("ERROR de fichero",
                    "Problemas con fichero INF_f4s.txt",
                    "- Avise al Servicio Técnico.",
                    R.drawable.baseline_warning_amber_24, Parametros.DIALOGO_ALERT_CON_BTN_ATRAS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.i(TAG, "onPause");

        if (BtInventory.getText().equals(mContext.getString(R.string.title_stop_Inventory))) {
            stopInventory();
        }
    }

    //Estrategia elegida para bloquear la acción “Botón atrás” si hay inventario en marcha y puede ELIMINAR objetos en curso
    //----------------------------------------------------------------------------------------------------------------------
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //Log.i(TAG, "onAttach");

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //Para invenrario si está en marcha
                if (BtInventory.getText().equals(mContext.getString(R.string.title_stop_Inventory))) {
                    stopInventory();
                }
                // ASi hay inventario en CURSO NO SE PUEDE ir Atrás ya que esto ELIMINARIA DATOS INVENTARIO
                //------------------------------------------------------------------------------------------
                if (mapTempDatas.size()==0){
                    this.setEnabled(false);
                    requireActivity().onBackPressed();
                } else {
                    //No se puede ir atrás si hay inventario en curso
                    mContext.playSound(5);
                    salidaAlerta("Atrás NO permitido",
                            "Abandonar solo si:",
                            "1.- Sesión de inventario guardada para seguir más tarde.\n\n" +
                                    "2.- Inventario finalizado y enviado al servidor.\n\n" +
                                    "3.- Sesión de inventario eliminada.",
                            R.drawable.baseline_warning_amber_24, Parametros.DIALOGO_ALERT_SIN_BTN_ATRAS);
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.i(TAG, "onResume");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //Desacoplamiento del Fragment de la Actividad por invocación de otra pestaña
        //-> Si está escaneando RFID pararlo ! y salir a otra espetaña que se ha invocado
        //Log.i(TAG, "onDetach");
        if (BtInventory.getText().equals(mContext.getString(R.string.title_stop_Inventory))) {
            stopInventory();
        }
    }

    //private void addDataToList(String epc,String epcAndTidUser, String rssi) {
    //En Servicio Metodo rico con trabajo en servicio más pesado en datos añadiendo EPCs a HashMap con vista de adaptador con tagList
    private void addDataToListServ(String epc) {
        String iniEPC="";
        if (StringUtils.isNotEmpty(epc)) {
            iniEPC = epc.substring(0,4);
            //Solo añade como EPC de producto los EPCs con x3008 e inactivados x1111 y en el futuro de especiales x9999
            if (iniEPC.equals(Parametros.epcROB) || iniEPC.equals(Parametros.epcINAC)){
                //Solo si es un EPC no repetido por control de diccionario map
                if (!mapTempDatas.containsKey(epc)) {
                    String hexIdArt, hexContEPC;
                    Integer IdArt, ContEPC;
                    hexIdArt = epc.substring(8,16);
                    hexContEPC = epc.substring(16);
                    IdArt = Integer.decode("0x"+hexIdArt);
                    ContEPC = Integer.decode("0x"+hexContEPC);
                    map = new HashMap<String, String>();
                    //map.put(TAG_EPC, epc);
                    //map.put(TAG_EPC_TID, epcAndTidUser);
                    map.put(TAG_EPC_TID, epc);
                    //map.put(TAG_COUNT, String.valueOf(1));
                    //map.put(TAG_RSSI, rssi);
                    map.put(TAG_COUNT, String.valueOf(IdArt));
                    map.put(TAG_RSSI, String.valueOf(ContEPC));
                    mContext.tagList.add(map);
                    //Añade al diccionario un EPC no repetido
                    mapTempDatas.put(epc,total++);
                    //Aquí no llevamos el adapter, saca el tv_count del ArrayList<HashMap<String, String>> de POJO UhfInfo
                    tv_count.setText(mapTempDatas.size()+"");
                    //Chicharra si se detecta un EPC INACTIVO! y lo muestra en salida siempre aunque en modo servicio
                    if (iniEPC.equals(Parametros.epcINAC)){
                        mContext.playSound(3);
                    }else if (Parametros.pitido){                 //Si Preferencias tiene activo el pitido
                        mContext.playSound(1);
                    }
                }
            }
        }
    }

    //No Servicio - Metodo ligero en modo de usuario con mayor rendimiento de lecturas sin añadir EPCs a HashMap con vista de adaptador con tagList
    private void addDataToList(String epc) {
        String iniEPC="";
        if (StringUtils.isNotEmpty(epc)) {
            //Segmentos de EPC
            //Ejemplo                300803E900004EEA0000003A    - 12 bytes = 24 caracteres HEX
            //Posiciones             012345678901234567890123
            //Máscara antirrobo      0123                        - 2 bytes = 4 carácteres Substring(0,4)
            //Cod Cliente                4567                    - 2 bytes = 4 carácteres Substring(4,8)
            //Cod Artículo                   89012345            - 4 bytes = 8 carácteres Substring(8,16)
            //Contador EPC                           67890123    - 4 bytes = 8 carácteres Substring(16)
            iniEPC = epc.substring(0,4);
            //Solo añade como EPC de producto los EPCs con x3008 e inactivados x1111 y en el futuro de especiales x9999
            if (iniEPC.equals(Parametros.epcROB) || iniEPC.equals(Parametros.epcINAC)){
                //Solo si es un EPC no repetido por control de diccionario map
                if (!mapTempDatas.containsKey(epc)) {
                    //Añade al diccionario un EPC no repetido
                    mapTempDatas.put(epc,total++);
                    //Aquí no llevamos el adapter, saca el tv_count del ArrayList<HashMap<String, String>> de POJO UhfInfo
                    tv_count.setText(mapTempDatas.size()+"");

                    //Chicharra si se detecta un EPC INACTIVO! y lo muestra en salida siempre aunque en modo servicio
                    if (iniEPC.equals(Parametros.epcINAC)){
                        String hexIdArt, hexContEPC;
                        Integer IdArt, ContEPC;
                        hexIdArt = epc.substring(8,16);
                        hexContEPC = epc.substring(16);
                        IdArt = Integer.decode("0x"+hexIdArt);
                        ContEPC = Integer.decode("0x"+hexContEPC);
                        map = new HashMap<String, String>();
                        //map.put(TAG_EPC, epc);
                        //map.put(TAG_EPC_TID, "no requiere");
                        map.put(TAG_EPC_TID, epc);
                        map.put(TAG_COUNT, String.valueOf(IdArt));
                        map.put(TAG_RSSI, String.valueOf(ContEPC));
                        mContext.tagList.add(map);
                        mContext.playSound(3);
                    }else if (Parametros.pitido){                 //Si Preferencias tiene activo el pitido
                        mContext.playSound(1);
                    }
                }
            }
        }
    }

    public class BtClearClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            //if (mContext.tagList.size()==0){
            if (mapTempDatas.size()==0){
                mContext.playSound(6);
                salidaAlerta("No hay lecturas",
                        "Sesión sin etiquetas leidas",
                        "- No hay eliminación que realizar.",
                        R.drawable.baseline_warning_amber_24, Parametros.DIALOGO_ALERT_SIN_BTN_ATRAS);
                return;
            }else {
                FragmentManager manager = getFragmentManager();
                MiDialogFragment dialog = MiDialogFragment.newInstance(TAG, Parametros.DIALOGO_ACCION_CLEAR);
                dialog.setTargetFragment(UHFReadTagFragment2.this, REQUEST_result);
                dialog.show(manager, DIALOGO);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String extra_accion = (String) data.getSerializableExtra(Parametros.EXTRA_ACCION);
        campo = (String) data.getSerializableExtra(Parametros.EXTRA_EDIT_TEXT);

        switch (extra_accion){
            case Parametros.DIALOGO_ALERT_CON_BTN_ATRAS:
                //Regresa a AcivityMain
                getActivity().onBackPressed();
                break;
            case Parametros.DIALOGO_ALERT_SIN_BTN_ATRAS:
                //No hacer nada
                break;
            case Parametros.DIALOGO_ACCION_CLEAR:
                if (resultCode == Activity.RESULT_CANCELED){
                    return;
                }
                if (resultCode == Activity.RESULT_OK){
                    clearData();
                    UIHelper.writeLog("%50%-Elimina lecturas de sesión", false);
                    UIHelper.writeLog("%51%-Observación: " + campo, false);
                    UIHelper.writeLog("%52%---------------------", false);
                    mContext.uhfInfo=new UhfInfo();
                }
                break;
        }
    }

    private void clearData() {
        tv_count.setText("0");
        tv_total.setText("0");
        total = 0;
        mContext.tagList.clear();
        mapTempDatas.clear();
        mContext.playSound(4);
    }

    public class BtInventoryClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            readTag();
        }
    }

    //private void readTag() {
    //Se cambia a PUBLIC para que desde la Actividad TabInventarioActivity se lance el métodp por el gatillo
    public void readTag() {
        if (BtInventory.getText().equals(mContext.getString(R.string.btInventory))) {
            if (mContext.mReader.startInventoryTag()) {
                BtInventory.setText(mContext.getString(R.string.title_stop_Inventory));
                loopFlag = true;
                setViewEnabled(false);
                //Log arranque de sesión de inventario
                UIHelper.writeLog("%01%-Inicia lecturas", false);

                //Si es la 1era lectura de inventario de esta sesion
                if (mContext.iniSesion){
                    Date date = new Date();
                    DateFormat hourdateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss ");
                    mContext.fecHor_IniInv = hourdateFormat.format(date);
                }

                //Arranca el Hilo de lecturas en continuo de invenatrio
                new TagThread().start();

            } else {
                stopInventory();
                UIHelper.ToastMessage(mContext, "uhf_msg_inventory_open_fail");
                mContext.playSound(2);
            }
        } else {
            stopInventory();
            mContext.playSound(2);
        }
    }

    private void setViewEnabled(boolean enabled) {
        BtClear.setEnabled(enabled);
    }

    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            setViewEnabled(true);
            if (mContext.mReader.stopInventory()) {
                BtInventory.setText(mContext.getString(R.string.btInventory));
                //UIHelper.writeLog("%10%-Stop lecturas, etiquetas de sesion: " + mContext.tagList.size(), false);
                UIHelper.writeLog("%10%-Stop lecturas, etiquetas de sesion: " + mapTempDatas.size(), false);

                //v.4.0 esto viene de addDataToList
                mContext.uhfInfo.setTempDatas(mapTempDatas);
                mContext.uhfInfo.setTagList(mContext.tagList);
                mContext.uhfInfo.setCount(mContext.uhfInfo.getCount() + total);

            } else {
                //No existe en strings la clave
                UIHelper.ToastMessage(mContext, "uhf_msg_inventory_stop_fail");
            }
        }
    }

    class TagThread extends Thread {
        public void run() {
            UHFTAGInfo uhftagInfo;
            Message msg;
            while (loopFlag) {
                uhftagInfo = mContext.mReader.readTagFromBuffer();
                if (uhftagInfo != null) {
                    msg = handler.obtainMessage();
                    msg.obj = uhftagInfo;
                    //msg.obj = uhftagInfo.getEPC();
                    handler.sendMessage(msg);
                    //Aqui pita SIEMPRE cada vez que lee un tag aunque se repita - es la linea ORIGINAL
                    //mContext.playSound(1);
                }
            }
        }
    }

    private String mergeTidEpc(String tid, String epc,String user) {
        String data="EPC:"+ epc;
        if (!TextUtils.isEmpty(tid) && !tid.equals("0000000000000000") && !tid.equals("000000000000000000000000")) {
            data+= "\nTID:" + tid ;
        }
        if(user!=null && user.length()>0) {
            data+="\nUSER:"+user;
        }
        return  data;
    }

    @Override
    public void myOnKeyDwon() {
        //Log.i(TAG, "onOnKeyDwon");
        readTag();
    }

    private void salidaAlerta(String argTitulo, String argSubTitulo, String argMsg, int image, String dialogo_alert){

        MiDialogAlert dialog = MiDialogAlert.newInstance(argTitulo, argSubTitulo, argMsg, image, dialogo_alert);
        FragmentManager manager = getFragmentManager();
        dialog.setTargetFragment(UHFReadTagFragment2.this, REQUEST_result);
        dialog.show(manager, DIALOGO);
    }

}


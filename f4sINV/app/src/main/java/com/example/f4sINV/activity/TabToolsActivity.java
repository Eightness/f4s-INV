package com.example.f4sINV.activity;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.f4sINV.Parametros;
import com.example.f4sINV.R;
import com.example.f4sINV.fragment.ToolInventarioFragment;
import com.example.f4sINV.fragment.ToolLogFragment;
import com.google.android.material.tabs.TabLayout;


public class TabToolsActivity extends AppCompatActivity {

    private static final String TAG = "TabToolsActivity";

    public static final String pref_PANTA = "pantaTabTools";
    TabLayout tabs;

    private ToolLogFragment toolLogFragment;
    private ToolInventarioFragment toolInventarioFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_tools);

        Log.i(TAG, "onCreate");

        //Guarda los valores por defecto de inicio en Preferencias si es la 1º carga de la Apll
        SharedPreferences preferencias = getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString(pref_PANTA, "TABtool1");        //Inicializa siempre con PANTA2
        editor.apply();


        //Prestaña
        //Codigo de 3 Tabs sin VewPager en version Prueba10.0
        tabs = findViewById(R.id.tabs_tools);
        tabs.addTab(tabs.newTab().setText("ACTIVIDAD").setTag("TABtool1"));
        tabs.addTab(tabs.newTab().setText("INVENTARIO").setTag("TABtool2"));

/*
        //A continuación técnica para modificar el estilo, font, bold, etc tras seleccion etc
        //Junto con la Clase adjunta abajo:
        //OnTabSelectedListener implements TabLayout.OnTabSelectedListener

        tabs.setOnTabSelectedListener(new OnTabSelectedListener());

        int tabCount = tabs.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            TabLayout.Tab tab = tabs.getTabAt(i);
            if (tab != null) {
                TextView tabTextView =
                        (TextView) LayoutInflater.from(this).inflate(R.layout.tab_item, tabs, false);
                tabTextView.setText(tab.getText());
                // First tab is the selected tab, so if i==0 then set Tabs_Selected style
                tabTextView.setTextAppearance(getApplicationContext(), i == 0 ? R.style.TextAppearance_Tabs_Selected
                        : R.style.TextAppearance_Tabs);
                tab.setCustomView(tabTextView);
            }
        }
 */

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectFragment(tab.getTag().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //Log.i(TAG, "onTabUnselected");
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //Log.i(TAG, "onTabReselected");
            }
        });
    }

    @Override
    protected void onDestroy() {
        //Log.e("zz_pp","onDestroy()");
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //String panta = PreferenceManager.getDefaultSharedPreferences(this).getString(pref_PANTA, "TAB1");
        SharedPreferences preferencias = getSharedPreferences(Parametros.PREFERENCIAS_app, MODE_PRIVATE);
        String panta = preferencias.getString(pref_PANTA, "TABtool1");

        Log.i(TAG, "onResume panta = " + panta);
        //Recupera la pestaña que corresponda
        selectFragment(panta);
    }

    private void selectFragment(String keyTab){

        switch (keyTab){
            case "TABtool1":
                //tabs.getTabAt(0).select();
                if(toolLogFragment ==null) {
                    //Ejemplo de envio de dos parametros
                    toolLogFragment = toolLogFragment.newInstance();
                } else{

                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragtab, toolLogFragment)
                        .commit();

                break;
            case "TABtool2":
                //tabs.getTabAt(0).select();
                if(toolInventarioFragment ==null) {
                    //Ejemplo de envio de dos parametros
                    toolInventarioFragment = toolInventarioFragment.newInstance();
                } else{

                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragtab, toolInventarioFragment)
                        .commit();

                break;
        }
    }
}

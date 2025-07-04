package com.example.f4sINV.tools;

//import com.example.uhf.R;
import com.example.f4sINV.Parametros;
import com.example.f4sINV.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class UIHelper {

	public static void ToastMessage(Context cont, String msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, int msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, String msg, int time) {
		Toast.makeText(cont, msg, time).show();
	}

    public static void alert(Activity act, int titleInt, int messageInt,
                             int iconInt) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle(titleInt);
            builder.setMessage(messageInt);
            builder.setIcon(iconInt);

            builder.setNegativeButton(R.string.close, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void alert(Activity act, String title, String message, int iconInt) {
    //Modificado JFA
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            //builder.setTitle(titleInt);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setIcon(iconInt);

            builder.setNegativeButton(R.string.close, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void alert_SiNo(Activity act, String title, String message, int iconInt) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setIcon(iconInt);

            builder.setPositiveButton(R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(R.string.close, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    public static void atrasBloqueado(Activity act){
        UIHelper.alert(act, "Abandonar solo si:", "1.- Inventario guardado a más tarde,\n2.- Inventario enviado a Host,\n3.- Inventario eliminado.", R.drawable.logo_f4s);
    }
*/
    public static void writeLog(String msg, boolean fecHor) {

        Date date;
        String fechaHora;

        File ficLog = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Parametros.ficLog);


        try {
            //Log de Actividad salida de tipo APPEND = true
            FileOutputStream f = new FileOutputStream(ficLog,true);
            f = new FileOutputStream(ficLog,true);

            if (fecHor){
                //De momento no usar
                date = new Date();
                DateFormat hourdateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss ");
                fechaHora = "%01%-" + hourdateFormat.format(date);
                f.write(fechaHora.getBytes());
                f.write("->".getBytes());
                f.write("\n".getBytes());
            }
            f.write(msg.getBytes());
            f.write("\n".getBytes());

            f.close();
        }
        catch (FileNotFoundException e) {
            //No hacer nada de momento - NO graba nada

        } catch (Exception e) {
            //No hacer nada de momento - NO graba nada
        }
    }
}


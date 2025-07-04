package com.example.f4sINV.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.f4sINV.Parametros;
import com.example.f4sINV.R;

public class MiDialogAlert extends DialogFragment {

    private static final String ARG_Titulo = "ARG_Titulo";
    private static final String ARG_SubTitulo = "ARG_SubTitulo";
    private static final String ARG_Msg = "ARG_Msg";
    private static final String ARG_Img = "ARG_Img";
    private static final String ARG_Accion = "ARG_Accion";

    //public static final String EXTRA_ACCION= "com.example.f4sINV.intent.accion";

    private String argTitulo;
    private String argSubTitulo;
    private String argMsg;
    private int argImg;
    private String argAccion;

    TextView txtTitulo,txtSubTitulo, txtMsg;
    Button btSi;
    ImageView icono;

    public static MiDialogAlert newInstance(String argTitulo, String argSubTitulo, String argMsg, int image, String argAccion){
        Bundle args = new Bundle();
        args.putString(ARG_Titulo, argTitulo);
        args.putString(ARG_SubTitulo, argSubTitulo);
        args.putString(ARG_Msg, argMsg);
        args.putInt(ARG_Img, image);
        args.putString(ARG_Accion, argAccion);

        MiDialogAlert fragment = new MiDialogAlert();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);

        argTitulo = (String) getArguments().getSerializable(ARG_Titulo);
        argSubTitulo = (String) getArguments().getSerializable(ARG_SubTitulo);
        argMsg = (String) getArguments().getSerializable(ARG_Msg);
        argImg = (int) getArguments().getSerializable(ARG_Img);
        argAccion = (String) getArguments().getSerializable(ARG_Accion);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_alert,null);
        txtTitulo = (TextView) v.findViewById(R.id.txtTitulo);
        txtSubTitulo = (TextView) v.findViewById(R.id.txtSubTitulo);
        txtMsg = (TextView) v.findViewById(R.id.txtMsg);
        btSi = (Button) v.findViewById(R.id.btSi);
        icono = (ImageView) v.findViewById(R.id.imageView);
        // android:src="@drawable/logo_f4s"

        txtTitulo.setText(argTitulo);
        txtSubTitulo.setText(argSubTitulo);
        txtMsg.setText(argMsg);
        icono.setImageResource(argImg);

        btSi.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                sendResult(Activity.RESULT_OK, argAccion);
                dismiss();      //Abandonar el DialogAlert
            }
        });

        return new AlertDialog.Builder(getActivity()).setView(v).create();
    }

    private  void sendResult(int resultCode, String accion){
        if (getTargetFragment()==null){
            return;
        }
        Intent intent = new Intent();

        intent.putExtra(Parametros.EXTRA_ACCION, accion);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}

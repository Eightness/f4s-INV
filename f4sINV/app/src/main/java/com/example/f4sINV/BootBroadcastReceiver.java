package com.example.f4sINV;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.f4sINV.activity.MainActivity;

//import com.example.uhf.activity.UHFMainActivity;

/**
 * Created by Administrator on 2018-12-24.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("zp_add","-------BootBroadcastReceiver--------");
        //Intent inte = new Intent(context, UHFMainActivity.class);
        Intent inte = new Intent(context, MainActivity.class);
        context.startActivity(inte);
    }

}

package com.example.battleships;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SMSReceiver extends BroadcastReceiver
{

    public SMSReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        SetupActivity.current.receiveChikka(intent);
    }
}

/*if(SetupActivity.current != null)
        {
            SetupActivity.current.receiveChikka(intent);
        }
        else
        {
            PlayActivity.current.receiveChikka(intent);
  }*/

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
        if(SetupActivity.current != null)
        {
            SetupActivity.current.receiveChikka(intent);
        }
        else if(PlayActivity.current != null)
        {
            PlayActivity.current.receiveChikka(intent);
        }
    }
}
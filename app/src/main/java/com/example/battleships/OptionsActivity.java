package com.example.battleships;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.ToggleButton;


public class OptionsActivity extends ActionBarActivity
{
    private Tone optionsToggleTone = new Tone(8000, 300, 0.1);
    private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;
    private boolean soundOn = true;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        settings = getSharedPreferences("settings", MODE_PRIVATE);
        prefEditor = settings.edit();
        Switch soundSwitch = (Switch) findViewById(R.id.soundswitch);
        Switch smsPromptSwitch = (Switch) findViewById(R.id.smspromptswitch);
        ToggleButton tileSizeToggle = (ToggleButton) findViewById(R.id.tilesizetoggle);

        soundSwitch.setChecked(settings.getBoolean("Sound", true));
        smsPromptSwitch.setChecked(settings.getBoolean("SMSPrompt", false));
        tileSizeToggle.setChecked(settings.getInt("TileSize", 36) == 36);
    }
    public void switchSound(View soundSwitch)
    {
        prefEditor.putBoolean("Sound", soundOn = !settings.getBoolean("Sound", false));
        prefEditor.apply();
        optionsToggleTone.play(soundOn);
    }
    public void switchSMSPrompt(View SMSPromptSwitch)
    {
        prefEditor.putBoolean("SMSPrompt", !settings.getBoolean("SMSPrompt", true));
        prefEditor.apply();
        optionsToggleTone.play(soundOn);
    }
    public void toggleTileSize(View tileSizeToggle)
    {
        if(((ToggleButton)tileSizeToggle).isChecked())
        {
            prefEditor.putInt("TileSize", 36);
        }
        else
        {
            prefEditor.putInt("TileSize", 24);
        }
        prefEditor.apply();
        optionsToggleTone.play(soundOn);
    }
}
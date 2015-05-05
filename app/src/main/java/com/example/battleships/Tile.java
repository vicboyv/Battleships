package com.example.battleships;

import android.content.Context;
import android.graphics.Color;
import android.widget.Button;

public class Tile extends Button
{
    public int x;
    public int y;
    public Ship occupyingShip;
    public Tile(Context context, int pX, int pY)
    {
        super(context);
        this.x = pX;
        this.y = pY;
        this.setBackgroundColor(Color.BLUE);
    }
}

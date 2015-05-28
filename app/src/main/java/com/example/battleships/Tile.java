package com.example.battleships;

import android.content.Context;
import android.graphics.Color;
import android.widget.Button;

public class Tile extends Button
{
    public int x;
    public int y;
    public Ship occupyingShip;
    public char state = '0';
    public Tile(Context context, int pX, int pY)
    {
        super(context);
        this.x = pX;
        this.y = pY;
        this.setBackgroundColor(Color.BLUE);
        this.state = '0';
    }
    public void attack()
    {
        this.state = (char)((int)this.state + 2);
        switch(this.state)
        {
            case '2' : this.setBackgroundColor(Color.CYAN); break;
            case '3' : this.setBackgroundColor(Color.RED); break;
            default: this.setBackgroundColor(Color.BLACK); break;
        }
    }
}

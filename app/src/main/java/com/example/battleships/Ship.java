package com.example.battleships;


import android.content.Context;
import android.view.View;
import android.widget.Button;

import java.util.LinkedList;

public class Ship extends Button
{
    public int length;
    public Tile[] occupyingTile;
    public Button selectionButton;
    public Ship(Button pSelectionButton, int pLength, Tile[] pOccupyingTile)
    {
        super(pSelectionButton.getContext());
        this.selectionButton = pSelectionButton;
        this.length = pLength;
        this.occupyingTile = pOccupyingTile;
    }
}

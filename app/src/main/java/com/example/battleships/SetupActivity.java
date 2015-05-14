package com.example.battleships;

//LACKING:
//Placement search algo
//CONNECT system
//Tap origin to cancel placement

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;


public class SetupActivity extends Activity {
    public static SetupActivity current;
    private Tile tileButton[][] = new Tile[10][10];
    private Button shipButton[] = new Button[5];
    private LinkedList<Button> availableShips = new LinkedList<>();
    private LinkedList<Tile> availableTiles = new LinkedList<>();
    private String phaseState;
    private Tile selectedTile;
    private Button selectedShip;
    private HashMap<Button, Ship> shipInfo = new HashMap<>();
    private Tone shipSelectionTone = new Tone(8000, 800, 0.1);
    private Tone shipPlacementTone = new Tone(8000, 1000, 0.1);
    private Tone shipOrientationTone = new Tone(8000, 500, 0.1);
    private Tone shipUndoTone = new Tone(8000, 300, 0.2);
    private SharedPreferences settings;
    private boolean soundOn = true;
    private Button connectButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        current = this;

        settings = getSharedPreferences("settings", MODE_PRIVATE);
        soundOn = settings.getBoolean("Sound", true);

        this.connectButton = (Button)findViewById(R.id.connectButton);
        this.connectButton.setEnabled(false);

        shipButton[0] = (Button)findViewById(R.id.shipA);
        shipButton[1] = (Button)findViewById(R.id.shipB);
        shipButton[2] = (Button)findViewById(R.id.shipC);
        shipButton[3] = (Button)findViewById(R.id.shipD);
        shipButton[4] = (Button)findViewById(R.id.shipE);
        shipInfo.put(shipButton[0], new Ship(shipButton[0], 5, null));
        shipInfo.put(shipButton[1], new Ship(shipButton[1], 4, null));
        shipInfo.put(shipButton[2], new Ship(shipButton[2], 3, null));
        shipInfo.put(shipButton[3], new Ship(shipButton[3], 3, null));
        shipInfo.put(shipButton[4], new Ship(shipButton[4], 2, null));

        for(Button pickedShip : shipButton)
        {
            pickedShip.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View object)
                {
                    targetShip((Button) object);
                }
            });
            availableShips.add(pickedShip);
        }

        GridLayout board = (GridLayout)findViewById(R.id.boardLayout);
        board.removeAllViews();

        board.setColumnCount(10);
        board.setRowCount(10);
        int tileSize = settings.getInt("TileSize", 36);
        for(int y = 0; y < 10; y++)
        {
            for(int x = 0; x < 10; x++)
            {
                tileButton[x][y] = new Tile(this, x, y);

                GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                param.height = getDP(tileSize);
                param.width = getDP(tileSize);
                param.rightMargin = getDP(tileSize / 12);
                param.topMargin = getDP(tileSize / 12);
                param.setGravity(Gravity.CENTER);
                param.columnSpec = GridLayout.spec(x);
                param.rowSpec = GridLayout.spec(y);

                tileButton[x][y].setLayoutParams(param);

                tileButton[x][y].setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View object)
                    {
                        targetTile((Tile) object);
                    }
                });

                availableTiles.add(tileButton[x][y]);
                board.addView(tileButton[x][y]);
            }
        }
        for(Button pickedButton : shipButton)
        {
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams((getDP(tileSize) * 5), ((getDP(tileSize) + 3) * 5));
            param.height = getDP(tileSize);
            param.width = getDP(tileSize) * shipInfo.get(pickedButton).length;
            param.rightMargin = getDP(tileSize / 12);
            param.topMargin = getDP(tileSize / 12);
            pickedButton.setLayoutParams(param);
        }

        this.phaseState = "Ship Selection";

    }

    public int getDP(int size)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getResources().getDisplayMetrics());
    }
    public void targetTile(Tile tile)
    {
        if(this.phaseState.equals("Ship Placement"))
        {
            if(availableTiles.contains(tile))
            {
                for (Tile pickedTile : availableTiles)
                {
                    pickedTile.setBackgroundColor(Color.BLUE);
                }
                if (tile != null)
                {
                    tile.setBackgroundColor(Color.YELLOW);
                    this.selectedTile = tile;
                    this.phaseState = "Ship Orientation";
                    shipPlacementTone.play(soundOn);
                }
            }
        }

        else if(this.phaseState.equals("Ship Orientation"))
        {
            if(this.selectedTile != tile)
            {
                try
                {
                    occupyTiles(findAvailableTiles(this.selectedTile, tile, shipInfo.get(selectedShip).length));
                    this.selectedShip.setBackgroundColor(Color.LTGRAY);
                    this.availableShips.remove(selectedShip);
                    this.phaseState = "Ship Selection";
                    shipOrientationTone.play(soundOn);
                    this.selectedTile = null;
                    this.selectedShip = null;
                    //Toast.makeText(getApplicationContext(), retrieveBlueprint(), Toast.LENGTH_LONG).show();

                } catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(), "Invalid placement!", Toast.LENGTH_SHORT).show();
                }
                if(availableShips.size() == 0)
                {
                    this.connectButton.setEnabled(true);
                }

            }
            else
            {
                tile.setBackgroundColor(Color.BLUE);
                availableTiles.add(tile);
                this.selectedTile = null;
                this.phaseState = "Ship Placement";
                shipUndoTone.play(soundOn);
            }
        }
        else if(phaseState.equals("Ship Selection"))
        {
            if(tile.occupyingShip != null)
            {
                Ship undoShip = tile.occupyingShip;
                availableShips.add(undoShip.selectionButton);
                undoShip.selectionButton.setBackgroundColor(Color.GRAY);
                for (Tile pickedTile : shipInfo.get(undoShip.selectionButton).occupyingTile)
                {
                    pickedTile.setBackgroundColor(Color.BLUE);
                    pickedTile.occupyingShip = null;
                    availableTiles.add(pickedTile);
                }
                shipInfo.get(undoShip.selectionButton).occupyingTile = null;
                this.connectButton.setEnabled(false);
                shipUndoTone.play(soundOn);

            }
        }
    }
    public void targetShip(Button ship)
    {
        if(this.phaseState.equals("Ship Selection") || this.phaseState.equals("Ship Placement") || this.phaseState.equals("Ship Orientation"))
        {

            if (availableShips.contains(ship))
            {
                if(selectedShip != ship)
                {
                    for (Button pickedShip : availableShips)
                    {
                        pickedShip.setBackgroundColor(Color.GRAY);
                    }
                    ship.setBackgroundColor(Color.YELLOW);
                    this.selectedShip = ship;
                    if (!this.phaseState.equals("Ship Orientation"))
                    {
                        this.phaseState = "Ship Placement";
                    }

                    shipSelectionTone.play(soundOn);
                }
                else
                {
                    ship.setBackgroundColor(Color.GRAY);
                    this.selectedShip = null;
                    this.phaseState = "Ship Selection";
                    if(this.selectedTile != null)
                    {
                        this.selectedTile.setBackgroundColor(Color.BLUE);
                    }
                    shipUndoTone.play(soundOn);
                }
            }
        }
    }
    public boolean verticalPlacementOrientation(Tile originTile, Tile targetTile)
    {
        return Math.abs(originTile.x - targetTile.x) <= Math.abs(originTile.y - targetTile.y);
    }
    public void occupyTiles(Tile occupiedTiles[])
    {
        for(Tile pickedTile : occupiedTiles)
        {
            pickedTile.setBackgroundColor(Color.GRAY);
            pickedTile.occupyingShip = shipInfo.get(selectedShip);
            this.availableTiles.remove(pickedTile);
        }
        occupiedTiles[0].occupyingShip.occupyingTile = occupiedTiles;
    }
    public Tile[] selectTrialTiles(Tile originTile, Tile targetTile, int length) //Needs to be smarter
    {
        LinkedList<Tile> selectedTiles = new LinkedList<>();
        if(!verticalPlacementOrientation(originTile, targetTile))
        {
            int cursor = originTile.x;
            while(length-- > 0 && cursor < 10 && cursor > -1)
            {
                selectedTiles.add(tileButton[cursor][originTile.y]);
                cursor += placementHeading(originTile, targetTile);
            }
        }
        else
        {
            int cursor = originTile.y;
            while(length-- > 0 && cursor < 10 && cursor > -1)
            {
                selectedTiles.add(tileButton[originTile.x][cursor]);
                cursor += placementHeading(originTile, targetTile);
            }
        }
        return selectedTiles.toArray(new Tile[1]);
    }
    public Tile[] findAvailableTiles(Tile originTile, Tile targetTile, int length) //TODO
    {
        boolean tilesAvailable = true;
        Tile[] trialTiles;
        int tryCount = 0;
        do
        {
            tryCount++;
            trialTiles = selectTrialTiles(originTile, targetTile, length);
            if(trialTiles.length != length)
            {
                tilesAvailable = false;
            }
            for (Tile pickedTile : trialTiles)
            {
                if (pickedTile.occupyingShip != null)
                {
                    tilesAvailable = false;
                }
            }

            int originX = Math.abs((originTile.x + (trialHeadingState(tryCount) * trialOrientation(tryCount, true))) % 10);
            int originY = Math.abs((originTile.y + (trialHeadingState(tryCount) * trialOrientation(tryCount, false))) % 10);
            originTile = tileButton[originX][originY];
            originTile.setBackgroundColor(Color.GREEN);

            int targetX = Math.abs((targetTile.x + (trialHeadingState(tryCount) * trialOrientation(tryCount, true))) % 10);
            int targetY = Math.abs((targetTile.y + (trialHeadingState(tryCount) * trialOrientation(tryCount, false))) % 10);
            targetTile = tileButton[targetX][targetY];
            targetTile.setBackgroundColor(Color.RED); //debugging code
            try
            {
                //Thread.sleep(1000);
            }
            catch(Exception e)
            {

            }

            originTile.setBackgroundColor(Color.BLUE);
            targetTile.setBackgroundColor(Color.BLUE);
        }
        while(!tilesAvailable && tryCount < 50);
        return trialTiles;
        /*Tile[] trialTiles = selectTrialTiles(originTile, targetTile, length);
        boolean tilesAvailable = true;
        if(trialTiles.length != length)
        {
            tilesAvailable = false;
        }
        for (Tile pickedTile : trialTiles)
        {
            if (pickedTile.occupyingShip != null)
            {
                tilesAvailable = false;
            }
        }
        if(tilesAvailable)
        {
            return trialTiles;
        }
        else
        {
            return null;
        }*/

    }

    public int trialOrientation(int count, boolean isX)
    {
        int state = 1;
        int stateSize = 1;
        int stateInstances = 1;
        for(int tries = 1; tries < count; tries++)
        {
            if(stateSize == stateInstances)
            {
                state = (state + 1) % 2;
                if(state == 1)
                {
                    stateSize++;
                }
                stateInstances = 0;
            }

            stateInstances++;
        }
        if(isX)
        {
            return state;
        }
        else
        {
            return (state + 1) % 2;
        }
    }

    public int trialHeadingState(int count) //TODO
    {
        int stateCap = 1;
        int stateChangeCount = 1;
        for(int tries = 1; tries <= count; tries++)
        {
            if(stateCap == tries)
            {
                stateCap = stateCap + (2 * stateChangeCount);
                stateChangeCount++;
            }
        }
        if(stateChangeCount % 2 == 0)
        {
            return 1;
        }
        else
        {
            return -1;
        }
    }
    public int placementHeading(Tile originTile, Tile targetTile)
    {
        if(verticalPlacementOrientation(originTile, targetTile))
        {
            return Math.max(-1, Math.min(targetTile.y - originTile.y, 1));
        }
        else
        {
            return Math.max(-1, Math.min(targetTile.x - originTile.x, 1));
        }
    }
    public void connectChikka(View v)
    {
        this.connectButton.setText("WAITING");
        this.connectButton.setEnabled(false);
        String smsBody = "CONNECT." + retrieveBlueprint();
        if(settings.getBoolean("SMSPrompt", true))
        {
            /*Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.setData(Uri.parse("smsto: " + SecretData.shortcode));
            sendIntent.putExtra("sms_body", smsBody);
            startActivity(sendIntent);*/
            openPlay(retrieveBlueprint(), retrieveBlueprint(), -1);
        }
        else
        {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(SecretData.shortcode, null, smsBody, null, null);
        }
    }
    public void receiveChikka(Intent intent)
    {
        final Bundle bundle = intent.getExtras();
        String phoneNumber;
        String message = null;
        try
        {
            if (bundle != null)
            {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for(Object sms : pdusObj)
                {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) sms);
                    phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    if(phoneNumber.equals(SecretData.shortcode))
                    {
                        message = currentMessage.getDisplayMessageBody();
                    }
                }
                String piece[] = message.split(".");
                if(message != null)
                {
                    if(piece[0].equals("CONNECT")) //change to UPDATE
                    {
                        openPlay(retrieveBlueprint(), piece[1], Integer.parseInt(piece[2]));
                    }
                }
            }
        } catch (Exception e)
        {
        }
    }
    public void openPlay(String playerBlueprint, String opponentBlueprint, int turn)
    {
        Intent intent = new Intent(this, PlayActivity.class);
        intent.putExtra("Player Blueprint", playerBlueprint);
        intent.putExtra("Opponent Blueprint", opponentBlueprint);
        intent.putExtra("Assigned Turn", turn);
        startActivity(intent);
        SetupActivity.current = null;
        this.finish();
    }
    public String retrieveBlueprint()
    {
        String blueprint = "";
        for(int y = 0; y < 10; y++)
        {
            for(int x = 0; x < 10; x++)
            {
                if (tileButton[x][y].occupyingShip == null)
                {
                    blueprint = blueprint + "0";
                }
                else
                {
                    blueprint = blueprint + "1";
                }
            }
        }
        return blueprint;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

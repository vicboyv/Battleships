package com.example.battleships;

import android.app.Activity;
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

import java.util.LinkedList;


public class PlayActivity extends Activity
{
    private Tile opponentTile[][] = new Tile[10][10];
    private Tile playerTile[][] = new Tile[10][10];
    private LinkedList<Tile> selectableTiles = new LinkedList<>();
    private String opponentBlueprint = "";
    private Tile selectedTile;
    private SharedPreferences settings;
    private boolean soundOn = true;
    private Button fireButton;
    private String phase;
    private int playerTurn = 0;
    private Tone tileTargetTone = new Tone(8000, 700, 0.1);
    private Tone fireTargetTone = new Tone(8000, 450, 0.5);
    public static PlayActivity current;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        PlayActivity.current = this;

        settings = getSharedPreferences("settings", MODE_PRIVATE);
        soundOn = settings.getBoolean("Sound", true);

        final Bundle bundle = getIntent().getExtras();

        String playerBlueprint = bundle.getString("Player Blueprint", "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        opponentBlueprint = bundle.getString("Opponent Blueprint", "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        playerTurn = bundle.getInt("Assigned Turn", 0);

        fireButton = (Button)findViewById(R.id.fireButton);
        fireButton.setEnabled(false);

        GridLayout opponentBoard = (GridLayout)findViewById(R.id.opponentBoardLayout);
        opponentBoard.removeAllViews();
        opponentBoard.setColumnCount(10);
        opponentBoard.setRowCount(10);
        int tileSize = settings.getInt("TileSize", 36);
        for(int y = 0; y < 10; y++)
        {
            for(int x = 0; x < 10; x++)
            {
                opponentTile[x][y] = new Tile(this, x, y);

                GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                param.height = getDP(tileSize);
                param.width = getDP(tileSize);
                param.rightMargin = getDP(tileSize / 12);
                param.topMargin = getDP(tileSize / 12);
                param.setGravity(Gravity.CENTER);
                param.columnSpec = GridLayout.spec(x);
                param.rowSpec = GridLayout.spec(y);

                opponentTile[x][y].setLayoutParams(param);

                opponentTile[x][y].setOnClickListener(
                        new View.OnClickListener()
                        {
                            public void onClick(View object)
                            {
                                targetTile((Tile) object);
                            }
                        });

                selectableTiles.add(opponentTile[x][y]);

                opponentBoard.addView(opponentTile[x][y]);
            }

        }
        refreshBoardViewer(opponentTile, opponentBlueprint, false);

        GridLayout playerBoard = (GridLayout)findViewById(R.id.playerBoardLayout);
        playerBoard.removeAllViews();
        playerBoard.setColumnCount(10);
        playerBoard.setRowCount(10);
        for(int y = 0; y < 10; y++)
        {
            for(int x = 0; x < 10; x++)
            {
                playerTile[x][y] = new Tile(this, x, y);

                GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                param.height = (getDP(tileSize) / 3) * 2;
                param.width = (getDP(tileSize) / 3) * 2;
                param.rightMargin = ((getDP(tileSize) / 3) * 2) / 12;
                param.topMargin = ((getDP(tileSize) / 3) * 2) / 12;
                param.setGravity(Gravity.CENTER);
                param.columnSpec = GridLayout.spec(x);
                param.rowSpec = GridLayout.spec(y);

                playerTile[x][y].setLayoutParams(param);

                playerBoard.addView(playerTile[x][y]);
            }
        }
        refreshBoardViewer(playerTile, playerBlueprint, true);

        if(this.playerTurn == -1)
        {
            waitPhase();
        }
        else
        {
            attackPhase();
        }

    }
    public void waitPhase()
    {
        this.phase = "Waiting";
        for(Tile pickedTile : this.selectableTiles)
        {
            pickedTile.setEnabled(false);
        }
        fireButton.setText("WAITING");
        fireButton.setEnabled(false);
    }
    public void attackPhase()
    {
        if(lostAllShips(this.playerTile))
        {
            this.phase = "Victorious";
            fireButton.setText("VICTORY!");
            fireButton.setEnabled(false);
            refreshBoardViewer(opponentTile, opponentBlueprint, true);
        }
        else if(lostAllShips(this.opponentTile))
        {
            this.phase = "Defeated";
            fireButton.setText("DEFEAT!");
            fireButton.setEnabled(false);
            refreshBoardViewer(opponentTile, opponentBlueprint, true);
        }
        else
        {
            this.phase = "Attacking";
            for (Tile pickedTile : this.selectableTiles)
            {
                pickedTile.setEnabled(true);
            }
            fireButton.setText("FIRE");
            fireButton.setEnabled(false);
        }
    }
    public void targetTile(Tile tile)
    {
        if(selectableTiles.contains(tile))
        {
            refreshBoardViewer(opponentTile, opponentBlueprint, false);
            this.selectedTile = tile;
            tile.setBackgroundColor(Color.YELLOW);
            this.fireButton.setEnabled(true);
            this.tileTargetTone.play(soundOn);
        }
    }
    public void fireTile(View v)
    {
        this.selectedTile.attack();
        this.selectableTiles.remove(this.selectedTile);
        this.opponentBlueprint = refreshBlueprint(opponentTile, opponentBlueprint);
        this.playerTurn = Math.max(0, this.playerTurn) + 1;
        this.fireTargetTone.play(soundOn);
        this.sendAction(false);
        this.waitPhase();
    }
    public void forfeitMatch(View v)
    {
        this.sendAction(true);
        this.phase = "Defeated";
        fireButton.setText("DEFEAT!");
        fireButton.setEnabled(false);
        refreshBoardViewer(opponentTile, opponentBlueprint, true);
    }
    public String refreshBlueprint(Tile[][] boardTile, String blueprint)
    {
        char[] mutableBlueprint = blueprint.toCharArray();
        for(Tile[] row : boardTile)
        {
            for (Tile tile : row)
            {
                mutableBlueprint[tile.x + (tile.y * 10)] = tile.state;
            }
        }
        return String.valueOf(mutableBlueprint);
    }
    public void refreshBoardViewer(Tile[][] boardTile, String blueprint, boolean ownBoard)
    {
        for(Tile[] row : boardTile)
        {
            for(Tile tile : row)
            {
                tile.state = blueprint.charAt(tile.x + (10 * tile.y));
                switch(blueprint.charAt(tile.x + (10 * tile.y)))
                {
                    case '0' : tile.setBackgroundColor(Color.BLUE); break;
                    case '1' : if(ownBoard)
                                {
                                    tile.setBackgroundColor(Color.GRAY);
                                }
                                else
                                {
                                    tile.setBackgroundColor(Color.BLUE);
                                }
                                break;
                    case '2' : tile.setBackgroundColor(Color.CYAN); break;
                    case '3' : tile.setBackgroundColor(Color.RED); break;
                    default: tile.setBackgroundColor(Color.BLACK); break;
                }
            }
        }
    }

    public int getDP(int size)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getResources().getDisplayMetrics());
    }
    public void sendAction(boolean forfeit)
    {
        String smsBody;
        if(!forfeit)
        {
            smsBody = "ACTION." + opponentBlueprint + "." + playerTurn;

        }
        else
        {
            smsBody = "FORFEIT";
        }
        if (settings.getBoolean("SMSPrompt", true))
        {
            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.setData(Uri.parse("smsto: " + SecretData.shortcode));
            sendIntent.putExtra("sms_body", smsBody);
            startActivity(sendIntent);
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
                if(message != null)
                {
                    String piece[] = message.split("\\.");
                    if(piece[0].equals("UPDATE"))
                    {
                        //Integer.parseInt(piece[2])) (TURNS)
                        this.updatePlayer(piece[1]);
                        this.attackPhase();
                    }
                    else if(piece[0].equals("FORFEIT"))
                    {
                        this.phase = "Victorious";
                        fireButton.setText("VICTORY!");
                        fireButton.setEnabled(false);
                        refreshBoardViewer(opponentTile, opponentBlueprint, true);
                        for (Tile pickedTile : this.selectableTiles)
                        {
                            pickedTile.setEnabled(false);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
        }
    }
    public void updatePlayer(String newBlueprint)
    {
        refreshBlueprint(playerTile, newBlueprint);
        refreshBoardViewer(playerTile, newBlueprint, true);
    }

    public boolean lostAllShips(Tile[][] blueprint)
    {
        for(Tile[] row : blueprint)
        {
            for(Tile tile : row)
            {
                if (tile.state == '1')
                {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

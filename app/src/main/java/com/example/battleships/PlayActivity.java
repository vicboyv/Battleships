package com.example.battleships;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
    private String playerBlueprint = "";
    private String opponentBlueprint = "";
    private Tile selectedTile;
    private SharedPreferences settings;
    private boolean soundOn = true;
    private Button fireButton;
    private String phase;
    private int playerTurn = 0;
    public static PlayActivity current;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        PlayActivity.current = this;

        final Bundle bundle = getIntent().getExtras();

        settings = getSharedPreferences("settings", MODE_PRIVATE);
        soundOn = settings.getBoolean("Sound", true);

        fireButton = (Button)findViewById(R.id.fireButton);
        fireButton.setEnabled(false);

        playerBlueprint = bundle.getString("Player Blueprint", "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        opponentBlueprint = bundle.getString("Opponent Blueprint", "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        playerTurn = bundle.getInt("Assigned Turn", 0);

        GridLayout opponentBoard = (GridLayout)findViewById(R.id.opponentBoardLayout);
        opponentBoard.removeAllViews();

        opponentBoard.setColumnCount(10);
        opponentBoard.setRowCount(10);
        for(int y = 0; y < 10; y++)
        {
            for(int x = 0; x < 10; x++)
            {
                opponentTile[x][y] = new Tile(this, x, y);

                GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                param.height = getDP(36);
                param.width = getDP(36);
                param.rightMargin = 3;
                param.topMargin = 3;
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
        refreshBoardViewer(opponentTile, opponentBlueprint, true);
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
                param.height = getDP(24);
                param.width = getDP(24);
                param.rightMargin = 2;
                param.topMargin = 2;
                param.setGravity(Gravity.CENTER);
                param.columnSpec = GridLayout.spec(x);
                param.rowSpec = GridLayout.spec(y);

                playerTile[x][y].setLayoutParams(param);

                playerBoard.addView(playerTile[x][y]);
            }
        }
        refreshBoardViewer(playerTile, playerBlueprint, true);
    }
    public void targetTile(Tile tile)
    {
        if(selectableTiles.contains(tile))
        {
            refreshBoardViewer(opponentTile, opponentBlueprint, false);
            this.selectedTile = tile;
            tile.setBackgroundColor(Color.YELLOW);
            this.fireButton.setEnabled(true);
        }
    }
    public void fireTile(View v)
    {
        this.selectedTile.attack();
        this.selectableTiles.remove(this.selectedTile);
        this.opponentBlueprint = refreshBlueprint(opponentTile, opponentBlueprint);
        this.playerTurn = Math.max(0, this.playerTurn) + 1;
        sendAttack();
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
                    case '2' : tile.setBackgroundColor(Color.parseColor("navy")); break;
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
    public void sendAttack()
    {
        for(Tile pickedTile : selectableTiles)
        {
            pickedTile.setEnabled(false);
        }

        fireButton.setText("WAITING");
        fireButton.setEnabled(false);

        String smsBody = "ACTION." + opponentBlueprint + "." + playerTurn;
        if (settings.getBoolean("SMSPrompt", true))
        {
            /*Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.setData(Uri.parse("smsto: " + SecretData.shortcode));
            sendIntent.putExtra("sms_body", smsBody);
            startActivity(sendIntent);*/
        } else
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
                    if(piece[0].equals("UPDATE"))
                    {
                        //Integer.parseInt(piece[2])) (TURNS)
                        updatePlayer(piece[1]);
                    }
                }
            }
        } catch (Exception e)
        {
        }
    }

    public void updatePlayer(String newBlueprint)
    {
        refreshBlueprint(playerTile, playerBlueprint);
        refreshBoardViewer(playerTile, playerBlueprint, true);
        this.fireButton.setEnabled(true);
        this.fireButton.setText("FIRE!");
        for(Tile pickedTile : selectableTiles)
        {
            pickedTile.setEnabled(true);
        }
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

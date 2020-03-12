package de.ur.mi.android.excercises.starter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import de.ur.mi.android.excercises.starter.Adapter.ItemAdapter;
import de.ur.mi.android.excercises.starter.Constants.Constants;
import de.ur.mi.android.excercises.starter.Database.DatabaseAdapter;
import de.ur.mi.android.excercises.starter.Domain.ListItem;

import static de.ur.mi.android.excercises.starter.Constants.Constants.EXIT_TIME;
import static de.ur.mi.android.excercises.starter.Constants.Constants.KEY_RECEPT_NAME;
import static de.ur.mi.android.excercises.starter.Constants.Constants.MIN_TIME_BETWEEN_SHAKES_MILLISECS;
import static de.ur.mi.android.excercises.starter.Constants.Constants.SHAKE_THRESHOLD;

/**
 * Created by Sabrina Hartl on 08.08.2017.
 */

public class ListPage extends Activity implements SensorEventListener {


    private DatabaseAdapter receptDB;
    private SensorManager sensorMgr;
    private long mLastShakeTime;
    private SensorEventListener listener = this;
    private ItemAdapter adapter;
    private boolean isListview = true;
    private MenuItem mListMenuItem;
    private MenuItem mGridMenuItem;
    private ArrayList itemList = new ArrayList<ListItem>();
    private String[] fromFieldNames = new String[]{DatabaseAdapter.KEY_NAME, DatabaseAdapter.KEY_KATEGORY, DatabaseAdapter.KEY_IMAGE};
    int[] toViewIDs = new int[]{R.id.receptName, R.id.kategory, R.id.receptImage};

    private Boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupListView();
        setupDatabase();
        setupSensor();
        addObject();
    }

    //Menü bzw Buttons im ActionBar erzeugen
    public boolean onCreateOptionsMenu(Menu menu) {
        //Actionbar Optionen erzeuegn
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        //GridView und Listview Button zuordnen
        mGridMenuItem = menu.findItem(R.id.grid_view_button);
        mListMenuItem = menu.findItem(R.id.list_view_button);

        return super.onCreateOptionsMenu(menu);
    }

    //Wenn ein Button im Menü/Action Bar ausgewählt wurde
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // wenn der Button mit der id add_button ausgewählt wurde
            case R.id.add_button:
                addButtonClicked();
                break;
            // wenn der Button mit der id grid_view_button ausgewählt wurde
            case R.id.grid_view_button:
                isListview = false;
                //version abfragen
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    this.invalidateOptionsMenu();
                }
                //Gridview anzeigen lassen und Liste refresehen
                setupGridView();
                refreshGridView();
                break;
            // wenn der Button mit der id list_view_button ausgewählt wurde
            case R.id.list_view_button:
                isListview = true;
                //version abfragen
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    this.invalidateOptionsMenu();
                }
                //Listview anzeigen lassen und Liste refreshen
                setupListView();
                refreshListView();
                break;
            default:
                Toast.makeText(getApplicationContext(), R.string.failedToast, Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }

    //Boolische Variable, ob momentan der ListviewModus oder GridViewModus angezeigt wird
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //Wenn im Listview Modus
        if (isListview) {
            mListMenuItem.setVisible(false); // list Button verstecken
            mGridMenuItem.setVisible(true); // grid Button anzeigen
            //Wenn im GridView Modus
        } else if (!isListview) {
            mListMenuItem.setVisible(true); // list Button anzeigen
            mGridMenuItem.setVisible(false); // gird Button verstecken
        }

        return true;
    }

    //Datenbank erstellen und öffnen
    private void setupDatabase() {
        receptDB = new DatabaseAdapter(this);
        receptDB.open();
        //hier auch momentane Einträge aktualisieren
        refreshListView();
    }

    private void setupListView() {
        setContentView(R.layout.activity_listview);
        setAdapter();
        ListView list = (ListView) findViewById(R.id.listView);
        list.setOnItemLongClickListener(longClickListener);
        list.setOnItemClickListener(clickListener);
        list.setAdapter(adapter);
    }

    private void refreshListView() {
        Cursor cursor = receptDB.getAllRows();
        SimpleCursorAdapter myCursorAdapter;
        myCursorAdapter = new SimpleCursorAdapter(getBaseContext(), R.layout.list_item_listview, cursor, fromFieldNames, toViewIDs, 0);
        myCursorAdapter.setViewBinder(viewBinder);
        ListView myList = (ListView) findViewById(R.id.listView);
        myList.setAdapter(myCursorAdapter);
        myCursorAdapter.notifyDataSetChanged();
        checkEmptyState();
    }

    private void setupGridView() {
        setContentView(R.layout.activity_gridview);
        setAdapter();
        GridView list = (GridView) findViewById(R.id.gridView);
        list.setOnItemLongClickListener(longClickListener);
        list.setOnItemClickListener(clickListener);
        list.setAdapter(adapter);
    }

    private void refreshGridView() {
        Cursor cursor = receptDB.getAllRows();
        SimpleCursorAdapter myCursorAdapter;
        myCursorAdapter = new SimpleCursorAdapter(getBaseContext(), R.layout.list_item_grid, cursor, fromFieldNames, toViewIDs, 0);
        myCursorAdapter.setViewBinder(viewBinder);
        GridView myList = (GridView) findViewById(R.id.gridView);
        myList.setAdapter(myCursorAdapter);
    }

    //Intent abfragen und gegebenenfalls ein neues Object in der Liste anlegen
    private void addObject() {
        Intent intent = getIntent();
        if (intent.hasExtra(KEY_RECEPT_NAME)) {
            //id aus Intent auslesen
            long id = intent.getLongExtra(Constants.KEY_ID, 0);
            //Extras des Intents in einem Listitem speichern
            ListItem newObject = getListItem(intent);
            //Abfragen ob es ein passender Intent ist und das Element noch nicht in der Liste vorkommt
            if (id != 0) {
                //wenn bereits eine id, also ein bearbeiteter Eintra, diesen updaten
                receptDB.update(id, newObject);
            } else {
                //in der Datenbank speichern
                receptDB.insertReceptItem(newObject);
            }
            //und aktualisieren
            refreshListView();
        }
    }

    //Daten aus Intent holen und in einem ListItem speichern
    private ListItem getListItem(Intent i){
        //Daten aus Intent auslesen
        String name = i.getExtras().getString(Constants.KEY_RECEPT_NAME);
        String kategory = i.getExtras().getString(Constants.KEY_KATEGORY);
        String ingredients = i.getExtras().getString(Constants.KEY_INGREDIENTS);
        String directions = i.getExtras().getString(Constants.KEY_DIRECTIONS);

        byte[] imageBytes = i.getByteArrayExtra(Constants.KEY_IMAGE);
        //neues Object mit den Attributen füllen
        ListItem newObject = new ListItem(name, kategory, ingredients, directions, imageBytes);
        return newObject;
    }

    //Ein Rezept aus der Liste öffnen
    private void openObject(long id) {
        //Sensor deaktivieren, dass er nicht ausversehen ausgelöst werden kann
        sensorMgr.unregisterListener(listener);
        //Intent der eine neue Seite mit den Inhalten öffnet
        Intent openObject = new Intent(ListPage.this, OpenObject.class);
        //Bestimmtes Rezeot anhand der id auswählen
        Cursor itemToOpen = receptDB.getRow(id);
        //notwenidge Informationen an den Inten übergeben und starten
        if (itemToOpen != null && itemToOpen.moveToFirst()) {
            openObject.putExtra(Constants.KEY_RECEPT_NAME, itemToOpen.getString(1));
            openObject.putExtra(Constants.KEY_KATEGORY, itemToOpen.getString(2));
            openObject.putExtra(Constants.KEY_INGREDIENTS, itemToOpen.getString(3));
            openObject.putExtra(Constants.KEY_DIRECTIONS, itemToOpen.getString(4));
            openObject.putExtra(Constants.KEY_IMAGE, itemToOpen.getBlob(5));
            openObject.putExtra(Constants.KEY_ID, id);
            startActivity(openObject);
            finish();
        }
        itemToOpen.close();
    }

    //Aktion wenn der Add Button geklickt wurde
    private void addButtonClicked() {
        //Bewegungssensor ausschalten, da er sonst oft aus Versehen erscheint
        sensorMgr.unregisterListener(listener);
        //Intent starten der auf die AddObject Seite weiterleitet
        Intent newItemIntent = new Intent(ListPage.this, AddObject.class);
        startActivity(newItemIntent);
        finish();
    }

    //Dem Adapter Kontext und ListItems zuordnen
    private void setAdapter(){
        adapter = new ItemAdapter(ListPage.this, itemList);
    }

    //Was passieren soll, wenn noch keine Einträge vorhanden sind
    private void checkEmptyState(){
        //Zählen der Einträge in der Datenbank
        int count = receptDB.getAllRows().getCount();
        //Platzhalterbild initalisieren
        ImageView emptyState = (ImageView) findViewById(R.id.emptyState);
        //Wenn Einträge vorhanden sind, wird das Bild versteckt
        if (count > 0) emptyState.setVisibility(View.GONE);
        //Wenn keine Einträge vorhanden sind, wird das Bild angezeigt
        if (count == 0) emptyState.setVisibility(View.VISIBLE);
    }

    private void setupSensor() {
        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorMgr.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    //Bewegungssensor erstellen
    @Override
    public void onSensorChanged(SensorEvent event) {
        //Soll nur funktionieren, wenn Einträge vorhanden sind
        if (receptDB.getAllRows().getCount() != 0) {
            //Sensor implementieren
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                long curTime = System.currentTimeMillis();
                if ((curTime - mLastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {

                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];

                    double acceleration = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2))
                            - SensorManager.GRAVITY_EARTH;

                    if (acceleration > SHAKE_THRESHOLD) {
                        alertDialogRandomRecept(curTime);
                    }
                }
            }
        }
    }

    //Auf Veränderungen des Bewegungssensors achten
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //Abfrage/Dialog für random Rezept
    private void alertDialogRandomRecept(final long curTime) {
        //Alert erstellen
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        //Sensor deaktivieren
        sensorMgr.unregisterListener(listener);
        //eigenes Layout laden und anzeigen
        View content =  inflater.inflate(R.layout.alert_dialog, null);
        builder.setView(content);
        //Texte setzen
        ((TextView) content.findViewById(R.id.dialogTitle)).setText(R.string.randomReceptTitle);
        ((TextView) content.findViewById(R.id.dialogText)).setText(R.string.randomReceptMsg);
        //Was soll passieren, wenn ja ausgewählt wird
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mLastShakeTime = curTime;
                randomRecept();
                dialog.dismiss();
            }
        });
        //Was soll passieren, wenn nein ausgewählt wird
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        AlertDialog alert = builder.create();
        alert.show();
    }

    //zufällige Zahl kreieren und zufälliges Rezept öffnen
    private void randomRecept() {
        Random random = new Random();
        long randomLong = (long) random.nextInt(receptDB.getAllRows().getCount()) + 1;
        openObject(randomLong);
    }

    //Methode damit die Zurück Taste des Geräts funktioniert
    @Override
    public void onBackPressed() {
        //Sensor deaktivieren
        sensorMgr.unregisterListener(listener);
        // wenn man bereits zweimal Zurück-Taste gedrückt wurde
        if (exit) {
            finish(); // App schließen
        } else {
            //Toast ausgeben
            Toast.makeText(this, R.string.exit, Toast.LENGTH_SHORT).show();
            //Exit auf true, damit die App beendet werden kann
            exit = true;
            //Wenn innerhalb der nächsten 4 Sekunden nicht nochmal Zurück gedrückt, exit wieder auf false
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, EXIT_TIME);
        }
    }

    //ListItem löschen
    private void removeTaskAtPosition(long id) {
        //Item löschen und Einträge aktualisieren
        receptDB.deleteItem(id);
        refreshListView();
    }

    //Abfrage ob man das Item sicher löschen möchte
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void alertDialogDelete(long id) {
        //Bewegungssensor deaktivieren, da er sonst oft aus versehen ausgelöst wird
        sensorMgr.unregisterListener(listener);
        //Alert erstellen
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        //eigenes Layout laden und anzeigen
        View content =  inflater.inflate(R.layout.alert_dialog, null);
        builder.setView(content);
        //Zexte setzen
        setText(content);
        //Antwortmöglichkeiten setzen
        setAnswers(builder, id);
        AlertDialog alert = builder.create();
        //Sensor wieder aktivieren
        setupSensor();
        alert.show();
    }

    private void setText(View v) {
        //Texte setzen
        ((TextView) v.findViewById(R.id.dialogTitle)).setText(R.string.deleteReceptTitle);
        ((TextView) v.findViewById(R.id.dialogText)).setText(R.string.deleteReceptMsg);
    }

    private void setAnswers(AlertDialog.Builder builder,long id) {
        final long _id = id;
        //Was soll passieren, wenn ja ausgewählt wird
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeTaskAtPosition(_id);
                dialog.dismiss();
            }
        });
        //Was soll passieren, wenn nein ausgewählt wird
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    //Listener zum Lauschen auf longClick zum löscen von Einträgen
    private AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            //wenn lange auf einen Eintrag geklickt wird, wird die Löschabfrage gestartet
            alertDialogDelete(id);
            return true;
        }
    };

    //Listener zum Lauschen auf einen Klick
    private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            //wenn kurs auf einen Eintrag geklickt wird, wird die Methode zum öffnen der Detailseite aufgerufen
            openObject(l);
        }
    };

    // Bild aus Datenbank laden
    private SimpleCursorAdapter.ViewBinder viewBinder = new SimpleCursorAdapter.ViewBinder() {
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view.getId() == R.id.receptImage) {
                // ByteArray aus Datenbank
                byte[] byteArray = cursor.getBlob(columnIndex);
                setImage(byteArray, view);
                return true;
            } else {
                //default setting einbauen
                return false;
            }
        }
    };

    //Konvretieren und setzten des Bildes
    private void setImage(byte[] ba, View v) {
        // Konvertieren ByteArray in Bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(ba, 0, ba.length);
        ImageView iconImageView = (ImageView) v;
        iconImageView.setImageBitmap(bitmap);
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        receptDB.close();
    }

}


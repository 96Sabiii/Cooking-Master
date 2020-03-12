package de.ur.mi.android.excercises.starter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import de.ur.mi.android.excercises.starter.Constants.Constants;

/**
 * Created by Sabrina Hartl on 12.08.2017.
 */

public class OpenObject extends Activity {

    private String nameString, kategoryString, ingredientsString, directionsString;
    private TextView name, kategory, ingredients, directions;
    private ImageView image;
    private byte[] imageByteArray;
    private Bitmap bitmap;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_object);
        setupButton();
        setRecept();
    }

    //Menü bzw Button in ActionBar erstellen
    public boolean onCreateOptionsMenu(Menu menu) {
        //Actionbar Optionen erzeuegn
        MenuInflater inflater = getMenuInflater();
        //Menü für Unterseiten einbauen
        inflater.inflate(R.menu.sub, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Methode, wenn ein Button im ActionBar ausgewählt wird
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // wenn der Button mit der id back_button ausgewählt wurde
            case R.id.back_button:
                //Methode starten, die einen wieder zur Hauptseite bringt
                backIntent();
                break;
            default:
                //Default Möglichkeit abfangen
                Toast.makeText(getApplicationContext(), R.string.failedToast, Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    //Bearbeitungsbutton erstellen
    private void setupButton() {
        Button editButton = (Button) findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                editIntent();
            }
        });
    }

    //Rezeptdaten an einen Intent übergeben
    private void editIntent() {
        //Intent erstellen, der einen wieder die Hauptseite öffnet
        Intent editIntent = new Intent(OpenObject.this, AddObject.class);
        //darin alle Texte übergeben
        editIntent.putExtra(Constants.KEY_RECEPT_NAME, nameString);
        editIntent.putExtra(Constants.KEY_KATEGORY, kategoryString);
        editIntent.putExtra(Constants.KEY_INGREDIENTS, ingredientsString);
        editIntent.putExtra(Constants.KEY_DIRECTIONS, directionsString);
        editIntent.putExtra(Constants.KEY_IMAGE, imageByteArray);
        editIntent.putExtra(Constants.KEY_ID, id);
        startActivity(editIntent);
        finish();

    }

    // Intent zurück zur Hauptseite
    private void backIntent() {
        Intent back = new Intent(OpenObject.this, ListPage.class);
        startActivity(back);
        finish();
    }

    //Zurück Taste des Smartphones aktivieren
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backIntent();
    }

    //Rezept mit allen Daten in den passenden Views anzeigen
    private void setRecept(){
        setupViews();
        getExtras();
        setExtras();
    }

    //Views erstellen
    private void setupViews() {
        name = (TextView) findViewById(R.id.receptNameText);
        kategory = (TextView) findViewById(R.id.kategoryText);
        ingredients = (TextView) findViewById(R.id.ingredientsText);
        directions = (TextView) findViewById(R.id.directionsText);
        image = (ImageView) findViewById(R.id.recept_img);
    }

    //Übergebene Daten aus dem Intent auslesen und speichern
    private void getExtras(){
        //Intent abfangen
        Intent i = getIntent();
        //abgefangene Daten in Variablen speichern
        nameString = i.getExtras().getString(Constants.KEY_RECEPT_NAME);
        kategoryString = i.getExtras().getString(Constants.KEY_KATEGORY);
        ingredientsString = i.getExtras().getString(Constants.KEY_INGREDIENTS);
        directionsString = i.getExtras().getString(Constants.KEY_DIRECTIONS);
        imageByteArray = i.getByteArrayExtra(Constants.KEY_IMAGE);
        id = i.getLongExtra(Constants.KEY_ID, 0);
        //cast
        bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
    }

    //gespeicherte Daten in Views setzen
    private void setExtras() {
        name.setText(nameString);
        kategory.setText(kategoryString);
        ingredients.setText(ingredientsString);
        directions.setText(directionsString);
        image.setImageBitmap(bitmap);
    }

}

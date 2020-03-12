package de.ur.mi.android.excercises.starter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.ur.mi.android.excercises.starter.Constants.Constants;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static de.ur.mi.android.excercises.starter.Constants.Constants.CANCLE;
import static de.ur.mi.android.excercises.starter.Constants.Constants.DATE_FORMAT;
import static de.ur.mi.android.excercises.starter.Constants.Constants.FILE_URI;
import static de.ur.mi.android.excercises.starter.Constants.Constants.FROM_GALLERY;
import static de.ur.mi.android.excercises.starter.Constants.Constants.GALLERY_REQUEST_CODE;
import static de.ur.mi.android.excercises.starter.Constants.Constants.IMAGE_DIRECTORY_NAME;
import static de.ur.mi.android.excercises.starter.Constants.Constants.KEY_DIRECTIONS;
import static de.ur.mi.android.excercises.starter.Constants.Constants.KEY_ID;
import static de.ur.mi.android.excercises.starter.Constants.Constants.KEY_IMAGE;
import static de.ur.mi.android.excercises.starter.Constants.Constants.KEY_INGREDIENTS;
import static de.ur.mi.android.excercises.starter.Constants.Constants.KEY_KATEGORY;
import static de.ur.mi.android.excercises.starter.Constants.Constants.KEY_RECEPT_NAME;
import static de.ur.mi.android.excercises.starter.Constants.Constants.MAX_IMAGE_SIZE;
import static de.ur.mi.android.excercises.starter.Constants.Constants.TAKE_PICTURE;

/**
 * Created by Sabrina Hartl on 09.08.2017.
 */

public class AddObject extends Activity {

    private EditText ingredients, directions, receptName;
    private String newIngredientsText, newDirectionsText , newReceptNameText, newKategoryText;
    private Spinner kategory;
    private Bitmap bitmap;
    private Uri fileUri;
    private ImageView photo;
    private long id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_object);
        setupViews();
        setupButtons();
        checkIntent();
    }

    //Menü bzw Button in ActionBar erstellen
    public boolean onCreateOptionsMenu(Menu menu) {
        //Actionbar Optionen erzeuegn
        MenuInflater inflater = getMenuInflater();
        //Menü für Unterseiten einbauen
        inflater.inflate(R.menu.sub, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Wenn ein Button im ActionBar ausgewählt wird
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // wenn der Button mit der id back_button ausgewählt wurde
            case R.id.back_button:
                //Methode starten, die einen zurück auf die Hauptseite bringt
                backButtonClicked();
                break;
            default:
                //Default Möglichkeit abfangen
                Toast.makeText(getApplicationContext(), R.string.failedToast, Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    //onClick Listeners auf die drei Buttons
    private void setupButtons() {
        //Wenn der add_image button ausgewählt wird, wird die Methode selectImage gestartet
        //Bei dieser Methode kann man dann ein Bild hochladen
        Button addImageButton = (Button) findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        //Wenn der save Button ausgewählt wird, wird die methode saveButtonClicked gestartet.
        //Diese speichert die eingebenen Daten in einem Intent und geht zurück zur Hauptseite
        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                saveButtonClicked();
            }
        });
    }

    //Views erstellen
    private void setupViews() {
        ingredients = (EditText) findViewById(R.id.enterIngredients);
        directions = (EditText) findViewById(R.id.enterDirections);
        receptName = (EditText) findViewById(R.id.receptName);
        kategory = (Spinner)  findViewById(R.id.kategory_spinner);
        photo = (ImageView) findViewById(R.id.photoView);
    }

    //zurück auf die ListPage springen wenn der back button geklickt ist
    private void backButtonClicked() {
        Intent backIntent = new Intent(AddObject.this, ListPage.class);
        startActivity(backIntent);
        finish();
    }

    //Methode um die Zurücktaste des Smartphones benutzten zu können
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backButtonClicked();
    }

    //Überprüfen ob ein Intent mit den Daten für ein neues Rezept übergeben wird,
    //also wenn ein Rezept zum Bearbeiten geöffnet wird
    private boolean checkIntent() {
        Intent i = getIntent();
        //Wenn der Intent ein Extra mit dem Wert KEY_RECEPT_NAME hat:
        if (i.hasExtra(KEY_RECEPT_NAME)){
            //Vorhandene Daten holen und in Views anzeigen
            getExtras(i);
            return true;
        }
        else return false;
    }

    //Daten aus Intent auslesen und in die passenden Views setzen
    private void getExtras(Intent i) {
        receptName.setText(i.getExtras().getString(KEY_RECEPT_NAME));
        kategory.setSelection(getSpinnerIndex(kategory, i.getExtras().getString(KEY_KATEGORY)));
        ingredients.setText(i.getExtras().getString(KEY_INGREDIENTS));
        directions.setText(i.getExtras().getString(KEY_DIRECTIONS));
        byte[] imageByteArray = i.getByteArrayExtra(KEY_IMAGE);
        //Array in Bitmap umwandeln
        bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
        photo.setImageBitmap(bitmap);
        id = i.getLongExtra(Constants.KEY_ID, 0);
    }

    //Methode zum Herausfinden, welchen Index der übergebene String im Spinner ist
    private int getSpinnerIndex(Spinner spinner, String myString) {
        int index = 0;

        //alle Möglichkeiten durchegehen und String mit den Texten aus dem Spinner vergelichen
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        //gefundenen Index zurück geben
        return index;
    }

    //Eingabedaten speichern wenn der speichern-Button geklickt wird
    private void saveButtonClicked() {
        //Daten aus den Views auslesen
        getEntries();
        //In Intent übergeben
        if(ingredients != null && ! TextUtils.isEmpty(newIngredientsText.trim()) &&
                directions != null && ! TextUtils.isEmpty(newDirectionsText.trim()) &&
                receptName !=  null && ! TextUtils.isEmpty(newReceptNameText.trim()))  {
            backAndSaveIntent();
            //wenn kein Input oder nicht alle Daten angegeben:
        }else{
            //Toast mit Hinweis ausgeben
            Toast toast = Toast.makeText(this, R.string.noEntryToast, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //auslagern?
    private void backAndSaveIntent() {
        //Intent erstellen, der wieder die Hauptseite öffnet
        Intent backAndSaveIntent = new Intent(AddObject.this, ListPage.class);
        //Darin alle eingegebenen Daten übergeben
        putExtras(backAndSaveIntent);
        //Eingaben zurücketzen
        resetViews();
        startActivity(backAndSaveIntent);
        finish();
    }

    //Rezeptdaten an den Intent übergeben
    private void putExtras(Intent i) {
        i.putExtra(KEY_DIRECTIONS, newDirectionsText);
        i.putExtra(KEY_INGREDIENTS, newIngredientsText);
        i.putExtra(KEY_RECEPT_NAME, newReceptNameText);
        i.putExtra(KEY_KATEGORY, newKategoryText);
        i.putExtra(KEY_ID, id);
        //wenn Bild gemacht wurde, das übergeben, wenn nicht: Platzhalterbild
        Bitmap img_Bitmap;
        if (bitmap!=null){img_Bitmap = bitmap;}
        else {img_Bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.turkey);}
        i.putExtra(KEY_IMAGE, getBytes(img_Bitmap));
    }

    //zurücksetzten der Views
    private void resetViews() {
        directions.setText("");
        ingredients.setText("");
        receptName.setText("");
        //photo.setImageResource(android.R.color.transparent);
    }

    // konvertieren von bitmap zu byteArray
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    //Input der Eingabefelder abfragen, in Strings umwandeln und in Variablen speichern
    public void getEntries() {
        newIngredientsText = ingredients.getText().toString();
        newDirectionsText = directions.getText().toString();
        newReceptNameText = receptName.getText().toString();
        newKategoryText = kategory.getSelectedItem().toString();
    }

    //Dialog der fragt ob Bild aufnehmen oder aus Galerie nehmen
    private void selectImage() {
        //Antwortmöglichkeiten erstellen
        final CharSequence[] items = { TAKE_PICTURE, FROM_GALLERY, CANCLE};
        //Alert erstellen
        AlertDialog.Builder builder = new AlertDialog.Builder(AddObject.this);
        LayoutInflater inflater = this.getLayoutInflater();

        /*
        //eigenes Layout laden und anzeigen -> Fehler

        View content =  inflater.inflate(R.layout.alert_dialog, null);
        builder.setView(content); //statt setView
        //Text setzen
        ((TextView) content.findViewById(R.id.dialogTitle)).setText(R.string.addPictureTitle);
        */
        builder.setTitle(R.string.addPictureTitle);
        //Aktionen bei den Antwortmöglichkeiten festlegen
        builder.setItems(items, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int item) {
                //Was soll passieren, wenn man ein Foto aufnehmen will
                if (items[item].equals(TAKE_PICTURE)) {
                        captureImage();
                    //Was soll passieren, wenn man ein Foto aus der Galerie wählen will
                } else if (items[item].equals(FROM_GALLERY)) {
                        chooseFromGallery();
                    //Was soll passieren, wenn man den Dialog abbrechen will
                } else if (items[item].equals(CANCLE)) {
                    dialog.dismiss();
                }
            }
        });
        //alert anzeigen
        builder.show();
    }

    //Bild aus Galerie wählen
    private void chooseFromGallery() {
        //Intent der die Galerie App startet
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        //Foto an Intent anhängen
        pickPhoto.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        //starten des Intents
        startActivityForResult(pickPhoto , GALLERY_REQUEST_CODE);
    }

    //Foto mit Kamera aufnehmen
    private void captureImage() {
        //Intent der die Kamera startet
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        //Foto an Intent anhängen
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // starten des Intents
        startActivityForResult(intent, Constants.CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(FILE_URI, fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileUri = savedInstanceState.getParcelable(FILE_URI);
    }

    //Abfragen ob Ergebnisse ok und die passende Funktion auslösen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Prüfen ob die Daten ok sind
        if (resultCode == RESULT_OK) {
            //Wenn mit der Kamera aufgenommen folgende Funktion auslösen
            if (requestCode == Constants.CAMERA_REQUEST_CODE) {
                previewCapturedImage();
                //Wenn Foto aus Galerie gewählt folgende Funktion auslösen
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                previewGalleryImage(data);
            }
            //Wenn Kameraaufnahme abgebrochen wird Toast auslösen
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), R.string.cameraCanceltToast, Toast.LENGTH_SHORT).show();
            //Wenn ein Fehler passiert Toast auslösen
        } else {
            Toast.makeText(getApplicationContext(), R.string.failedToast, Toast.LENGTH_SHORT).show();
        }
    }

    //foto aufnehmen und in der Preview anzeigen lassen
    private void previewCapturedImage() {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            //Bitmap aus dem pfad laden
            bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);
            //Größe anpassen
            bitmap = getResizedBitmap(bitmap, MAX_IMAGE_SIZE);
            //Im Imageview anzeigen lassen
            photo.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    //Datei aus Galerie wählen und in Preview anzeige
    private void previewGalleryImage(Intent data) {
        try{
            // pickedImage data lesen
            Uri pickedImage = data.getData();
            String[] filePath = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            //Bild aus Pfad laden
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeFile(imagePath, options);
            //Größe verkleinern
            bitmap = getResizedBitmap(bitmap, MAX_IMAGE_SIZE);
            //Foto in ImageView setzen
            photo.setImageBitmap(bitmap);

            // Cursor schließen
            cursor.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);
        // Speicherort erstellen
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        // Dateiname erstellen
        String timeStamp = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
    }

    //verkleinert das Bild, sodass die maximale größe der intent mitzugebenden Daten nicht überschritten wird
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        //Größe des Bildes abfragen
        int width = image.getWidth();
        int height = image.getHeight();

        //Größe des Bildes anpassen
        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

}

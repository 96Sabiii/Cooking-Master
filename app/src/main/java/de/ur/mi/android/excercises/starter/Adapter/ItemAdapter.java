package de.ur.mi.android.excercises.starter.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.ur.mi.android.excercises.starter.Domain.ListItem;
import de.ur.mi.android.excercises.starter.R;

/**
 * Created by Sabrina Hartl on 11.08.2017.
 */

public class ItemAdapter extends ArrayAdapter<ListItem> {

    private List<ListItem> itemList;
    private Context context;

    //Konstruktor
    public ItemAdapter(Context context, List<ListItem> itemList) {
        super(context, R.layout.list_item_listview, itemList);
        this.context = context;
        this.itemList = itemList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item_listview, null);
        }

        //Views in Variablen speichern
        TextView receptName = (TextView) v.findViewById(R.id.receptName);
        TextView kategory = (TextView) v.findViewById(R.id.kategory);
        ImageView image = (ImageView) v.findViewById(R.id.receptImage);

       //Daten in layout
        ListItem item = itemList.get(position);

        //Zutaten und Anweisungen speichern
        String ingredients = item.getIngredients();
        String directions = item.getDirection();
        byte[] imageBytes = item.getImage();


        //Daten setzen
        receptName.setText(item.getName());
        kategory.setText(item.getKategory());
        image.setImageBitmap(getImage(imageBytes));

        return v;
    }

    // Von Bytearray in Bitmap konvertieren
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

}


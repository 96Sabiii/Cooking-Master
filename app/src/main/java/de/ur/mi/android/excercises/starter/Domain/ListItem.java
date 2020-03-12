package de.ur.mi.android.excercises.starter.Domain;

/**
 * Created by Sabrina Hartl on 11.08.2017.
 */

public class ListItem {


    String name, kategory, ingredients, direction;
    byte[] image;

    //Konstruktor
    public ListItem(String name, String kategory, String ingredients, String direction, byte[] image) {
        this.name = name;
        this.kategory = kategory;
        this.ingredients = ingredients;
        this.direction = direction;
        this.image = image;
    }

    //Getter Methoden der einzelnen Variablen

    public String getName() {
        return name;
    }

    public String getKategory() {
        return kategory;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getDirection() {
        return direction;
    }

    public byte[] getImage(){ return image; }
}

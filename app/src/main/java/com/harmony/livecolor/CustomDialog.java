package com.harmony.livecolor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;

/**
 * CustomDialog class
 *   Manages the custom dialog and its methods for easier creation.
 *   Takes the activity context to know where it is displaying the dialog
 *   As well as the color to be saved once the dialog is complete
 *
 * AlertDialog alertDialogSave, alertDialogName: respective dialogs for showSaveDialog() and showSetNameDialog()
 * AlertDialog.Builder builder: used to build both dialogs
 * View saveDialogView, setNameDialogView: set the views of the dialogs to their respective XML files
 *
 * EditText newPaletteName allows access to the user's input palette name for adding to the database
 */
public class CustomDialog implements SaveDialogRecyclerViewAdapter.OnListFragmentInteractionListener {

    Context context;
    Activity activity;

    ColorDatabase colorDB;
    //TODO: add palette database

    AlertDialog alertDialogSave, alertDialogName;
    AlertDialog.Builder builder;
    View saveDialogView, setNameDialogView;
    EditText newPaletteName;

    String name, hex, rgb, hsv, id, newName;
    boolean newColor;

    ArrayList<MyPalette> paletteList;

    /**
     * Constructor for Custom Dialog
     * For when a new color needs to be saved to the database
     * @param context must be an Activity context
     */
    public CustomDialog(Context context,String name, String hex, String rgb, String hsv){
        this.context = context;
        activity = (Activity) context;

        colorDB = new ColorDatabase(activity);
        //TODO: initialize PaletteDatabase

        this.name = name;
        this.hex = hex;
        this.rgb = rgb;
        this.hsv = hsv;
        this.id = "";
        this.newName = "";

        newColor = true;
    }

    /**
     * Constructor for Custom Dialog
     * For when a palette only needs to be renamed
     * @param context must be an Activity context, is the activity that the dialog is displaying on
     */
    public CustomDialog(Context context, String id){
        this.context = context;
        activity = (Activity) context;

        colorDB = new ColorDatabase(activity);
        //TODO: initialize PaletteDatabase

        this.id = id;
        this.name = "";
        this.hex = "";
        this.rgb = "";
        this.hsv = "";
        this.newName = "";

        newColor = false;
    }

    /**
     * Creates and displays the inital dialog for saving a color.
     */
    public void showSaveDialog(){
        builder = new AlertDialog.Builder(context);

        saveDialogView = activity.getLayoutInflater().inflate(R.layout.dialog_save_color,null);
        LinearLayout savedColorsItem = saveDialogView.findViewById(R.id.savedColorsItem);
        LinearLayout newPaletteItem = saveDialogView.findViewById(R.id.newPaletteItem);

        initPalettes();

        initRecycler();

        builder.setView(saveDialogView);

        newPaletteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogSave.dismiss();
                showSetNameDialog();
            }
        });

        savedColorsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorDB.addColorInfoData(name, hex, rgb, hsv);
                //TODO: implement palette database part
                alertDialogSave.dismiss();
                Toast.makeText(context,
                        "Color has been saved to Saved Colors",
                        Toast.LENGTH_SHORT).show();
            }
        });

        alertDialogSave = builder.create();
        alertDialogSave.show();
    }

    /**
     * initialize palette list from database for recycler
     * for displaying existing palettes that can be saved to
     */
    private void initPalettes() {
        //initialize ArrayList<MyPalette> here
        paletteList = new ArrayList<>();
        //will access palettes from database and put into MyPalette objects
        //TODO: Andrew's database code/method call will go here
        //Temporary Palettes atm:
        MyColor magenta = new MyColor("1","Hot Pink", "#FF00FF", "(255, 0, 255)","(5:001, 255, 255)");
        MyColor yellow = new MyColor("2","Highlighter", "#FFFF00", "(255, 255, 0)","(1:001, 255, 255)");
        MyColor cyan = new MyColor("3","Hot Cyan", "#00FFFF", "(0, 255, 255)","(3:001, 255, 255)");
        //test 3 colors
        ArrayList<MyColor> colorList1 = new ArrayList<>();
        colorList1.add(magenta);
        colorList1.add(yellow);
        colorList1.add(cyan);
        paletteList.add(new MyPalette("2","Three Colors",colorList1));
        //test 6 colors
        ArrayList<MyColor> colorList2 = new ArrayList<>();
        colorList2.add(magenta);
        colorList2.add(yellow);
        colorList2.add(cyan);
        colorList2.add(magenta);
        colorList2.add(yellow);
        colorList2.add(cyan);
        paletteList.add(new MyPalette("3","Six Colors",colorList2));
        //test 10+ colors
        ArrayList<MyColor> colorList3 = new ArrayList<>();
        colorList3.add(magenta);
        colorList3.add(yellow);
        colorList3.add(cyan);
        colorList3.add(magenta);
        colorList3.add(yellow);
        colorList3.add(cyan);
        colorList3.add(magenta);
        colorList3.add(yellow);
        colorList3.add(cyan);
        colorList3.add(magenta);
        colorList3.add(yellow);
        colorList3.add(cyan);
        paletteList.add(new MyPalette("3","Ten+ Colors",colorList3));
    }

    /**
     * initialize recycler with the palette info
     * for displaying existing palettes that can be saved to
     */
    private void initRecycler() {
        //get the RecyclerView from the view
        RecyclerView recyclerView = saveDialogView.findViewById(R.id.dialogRecycler);
        //then initialize the adapter, passing in the list
        SaveDialogRecyclerViewAdapter adapter = new SaveDialogRecyclerViewAdapter(context, paletteList);
        //set item listener that gets position
        adapter.setOnListFragmentInteractionListener(this);
        //add dividers
        DividerItemDecoration divider = new DividerItemDecoration(context, VERTICAL);
        recyclerView.addItemDecoration(divider);
        //and set the adapter for the RecyclerView
        recyclerView.setAdapter(adapter);
        //and set the layout manager as well
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    /**
     * Creates and displays the dialog for creating a new palette to add a color to.
     */
    public void showSetNameDialog(){
        //set up builder
        builder = new AlertDialog.Builder(context);

        //add appropriate view to builder
        setNameDialogView = activity.getLayoutInflater().inflate(R.layout.dialog_rename_palette,null);
        newPaletteName = setNameDialogView.findViewById(R.id.newPaletteName);

        builder.setView(setNameDialogView);

        //Positive Confirm Button Click Listener that adds the input palette name to the database
        //TODO: implement palette database part
        builder.setPositiveButton("Save Palette", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //gets the input name from edittext field
                newName = newPaletteName.getText().toString();

                //if you are creating a new name for a palette
                if(newColor) {
                    Log.d("PAIGE", "setName is for new palette");
                    Cursor colorData = colorDB.getColorInfoData();
                    //adds the color to the database
                    colorDB.addColorInfoData(name, hex, rgb, hsv);
                    colorData.moveToLast();
                    //gets newest added color and adds it to the palette
                    colorDB.addPaletteInfoData(newName, colorData.getString(0));

                    dialog.dismiss();
                    Toast.makeText(context,
                            "New palette \"" + newName + "\" created!",
                            Toast.LENGTH_SHORT).show();
                } //if you are renaming an existing palette
                else {
                    Log.d("PAIGE", "setName is for existing palette");
                    boolean nameChanged = colorDB.changePaletteName(id,newName);
                    dialog.dismiss();
                    if(nameChanged) {
                        //obtain the new palette name to update the current activity with said name
                        TextView tvPaletteName = activity.findViewById(R.id.paletteName);
                        tvPaletteName.setText(newName);
                        //send confirmation message
                        Toast.makeText(context,
                                "Set palette name to \"" + newName + "\"",
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //send error message
                        Toast.makeText(context,
                                "Changing palette name failed",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        alertDialogName = builder.create();
        alertDialogName.show();
    }

    /**
     * listener from the recycler, saves a color to the selected palette
     * @param palette contains the data for the selected palette
     */
    @Override
    public void onListFragmentInteraction(MyPalette palette) {
        colorDB.addColorInfoData(name, hex, rgb, hsv);
        //TODO: implement palette database part
        alertDialogSave.dismiss();
        Toast.makeText(context,
                "Saved color to \"" + palette.getName() + "\"",
                Toast.LENGTH_SHORT).show();
    }
}

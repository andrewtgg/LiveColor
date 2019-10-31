package com.harmony.livecolor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.graphics.Color.RGBToHSV;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ColorPickerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ColorPickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ColorPickerFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public ColorPickerFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ColorPickerFragment newInstance() {
        ColorPickerFragment fragment = new ColorPickerFragment();
        //Bundle args = new Bundle();
        //args can be bundled and sent through here if needed
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //if arguments are needed ever, use this to set them to static values in the class
        }
    }

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1888;
    static final int REQUEST_TAKE_PHOTO = 1;

    ImageView mImageView;
    Uri image_uri;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("Lifecycles", "onCreateView: ColorPickerFragment created");

        //Set title on action bar to match current fragment
        getActivity().setTitle(
                getResources().getText(R.string.app_name) +
                        " - " + getResources().getText(R.string.title_color_picker)
        );

        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_color_picker, container, false);

        Button button = rootView.findViewById(R.id.button1);
        mImageView = rootView.findViewById(R.id.pickingImage);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(getActivity()
                                ,                      "com.harmony.livecolor.fileprovider", photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });

        //onClickListener for
        mImageView = rootView.findViewById(R.id.pickingImage);
        //Adds a listener to get the x and y coordinates of taps and update the display
        mImageView.setOnTouchListener(handleTouch);

        return rootView;
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                galleryAddPic();


                setPic();

            }
        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {

        // A seperate Method to get the timestamp ina formatted manner
        String timeStamp = getCurrentTimeStamp();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(
                Environment.DIRECTORY_PICTURES);
        String imgStore = storageDir.getAbsolutePath()
                + File.separator + "SurveyPhotos";
        storageDir = new File(imgStore);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File imageFile = null;
        try {
            imageFile = File.createTempFile(imageFileName,
                    ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }

    public static String getCurrentTimeStamp() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        return timeStamp;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    // https://stackoverflow.com/a/39588899
    // For Sprint 2 User Story 2.
    //TODO there's clearly some sort of error in either getting the coordinates or turning them into a color. Not quite sure where.
    //TODO maybe the image view's size is changing, maybe the image dimensions do not match the imageview size (stretched/compressed to fit)
    private View.OnTouchListener handleTouch = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            //retrieve image from view
            ImageView pickedImage = view.findViewById(R.id.pickingImage);
            //get image as bitmap to get color data
            Bitmap bitmap = ((BitmapDrawable)pickedImage.getDrawable()).getBitmap();
            //This should get us x and y with respect to the ImageView we click on.
            int x = (int) event.getX();
            int y = (int) event.getY();
            Log.d("DEBUG S2US2", "ImageView click x="+x+" y="+y);
            //If you click and drag outside the image this function still fires, but with
            //  negative x,y, causing a crash on bitmap.getPixel()
            //There seems to be no problem with x and y being greater than the image width/height?
            //  Or if there is, it results in a bad color value but not a crash.
            if(x < 0 || y < 0)
                return true; //I'm not sure if our return value really matters.
            //get color int from said pixel coordinates
            int pixel = bitmap.getPixel(x,y);
            Log.d("DEBUG S2US2", "onClick: color int = " + pixel);
            //send to Gabby's script to updated the displayed values on screen
            //if android doesn't like us sending the whole color object we can send the color string
            //and use Color.valueOf() on Gabby's end
            updateColorValues(view, pixel);
            return true;
        }
    };

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d("Lifecycles", "onViewCreated: View Created for Color Picker Fragment");
        updateColorValues(getView(),Color.WHITE);
    }

    public void updateColorValues(View view, int colorNew){
        Log.d("DEBUG", "updateColorValues: called");
        Log.d("DEBUG", "updateColorValues: color int = " + colorNew);

        //fetch the color from pickedColor
        int RV = Color.red(colorNew);
        int GV = Color.green(colorNew);
        int BV = Color.blue(colorNew);

        Log.d("DEBUG", "updateColorValues: red = " + RV + ", blue = " + GV + ", blue = " + BV);

        //update the RGB value displayed
        String rgb = String.format("(%1$d, %2$d, %3$d)",RV,GV,BV);
        String fullRGB = String.format("RGB: %1$s",rgb);  //add "RGB: " and rgb together
        Log.d("DEBUG", "updateColorValues: fullRGB = " + fullRGB);
        //I (Dustin) changed all the calls to view.findViewById to getActivity().findViewById.
        //Is the view argument to updateColorValues needed?
        TextView rgbDisplay = getActivity().findViewById(R.id.RGBText);//get the textview that displays the RGB value
        rgbDisplay.setText(fullRGB); //set the textview to the new RGB: rgbvalue

        //update the HEX value displayed
        String hexValue = String.format("#%06X", (0xFFFFFF & colorNew)); //get the hex representation minus the first ff
        String fullHEX = String.format("HEX: %1$s",hexValue);
        Log.d("DEBUG", "updateColorValues: fullHEX = " + fullHEX);
        TextView hexDisplay = getActivity().findViewById(R.id.HEXText);
        hexDisplay.setText(fullHEX);

        //update the HSV value displayed
        float[] hsvArray = new float[3];
        RGBToHSV(RV,GV,BV,hsvArray);
        int hue = Math.round(hsvArray[0]);
        String fullHSV = String.format("HSV: (%1$d, %2$.3f, %3$.3f)",hue,hsvArray[1],hsvArray[2]);
        TextView hsvDisplay = getActivity().findViewById(R.id.HSVText);
        hsvDisplay.setText(fullHSV);

        ImageView colorDisplay = getActivity().findViewById(R.id.pickedColorDisplayView);
        colorDisplay.setBackgroundColor(colorNew);
    }
}
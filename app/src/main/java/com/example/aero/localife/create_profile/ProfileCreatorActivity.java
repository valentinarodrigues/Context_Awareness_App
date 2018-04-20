package com.example.aero.localife.create_profile;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aero.localife.DatabaseHelperActivity;
import com.example.aero.localife.ProfileAdapterActivity;
import com.example.aero.localife.ProfileListActivity;
import com.example.aero.localife.R;
import com.example.aero.localife.profile_settings.ProfileSettingsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileCreatorActivity extends AppCompatActivity {

    //Variable Declarations
    FloatingActionButton fabForProfileCreation;
    GPSLocationServiceActivity gpsLocationServiceActivity;
    DatabaseHelperActivity databaseHelperActivity;
    ListView listView;

    //LOG Strings
    private String TAG = "ProfileCreatorActivity";
    private static final String PROFILE_SELECTED_TAG = "Profile Selected LOG:";
    private static final String DIALOG_DECLINED_TAG = "Dialog Declined LOG:";
    public final int LOCATION_PERMISSION = 111;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION);
        }
        //inflating the layout for profile-creation activity
        setContentView(R.layout.activity_profile_creator);
        
        databaseHelperActivity = new DatabaseHelperActivity(getApplicationContext());

        listView = (ListView) findViewById(R.id.listview_profile_creator);

        displayProfiles();

        //Code to handle the floating action button to create profile
        fabForProfileCreation = (FloatingActionButton) findViewById(R.id.fab_profile_creator);
        fabForProfileCreation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayout layout = new LinearLayout(ProfileCreatorActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                final AlertDialog.Builder builder =
                        new AlertDialog.Builder(ProfileCreatorActivity.this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle(R.string.profile_creator_dialog_title);

                //Code for EditText for ProfileName
                final EditText editTextProfileName = new EditText(ProfileCreatorActivity.this);
                editTextProfileName.setHint(R.string.profile_creator_dialog_message);
                editTextProfileName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                layout.addView(editTextProfileName);

                final CheckBox checkBoxCurrentLocation = new CheckBox(ProfileCreatorActivity.this);
                checkBoxCurrentLocation.setChecked(true);
                checkBoxCurrentLocation.setTextSize(16);
                checkBoxCurrentLocation.setPadding(0, 16, 0, 16);
                checkBoxCurrentLocation.setText("Get current location");
                checkBoxCurrentLocation.setTextColor(Color.rgb(0, 0, 0));
                layout.addView(checkBoxCurrentLocation);

                builder.setView(layout, 40, 20, 40, 24);

                builder.setPositiveButton(R.string.profile_creator_dialog_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String enteredProfileName = editTextProfileName.getText().toString().trim();
                        Boolean rowExist = databaseHelperActivity.getProfileListEmptyStatus();

                        gpsLocationServiceActivity = new GPSLocationServiceActivity(ProfileCreatorActivity.this);
                        String matchedProfileName = databaseHelperActivity.getProfileValue(enteredProfileName);
                        if (editTextProfileName.getText().toString().trim().isEmpty()) {

                            Toast.makeText(getApplicationContext(), "Cannot create blank profile!", Toast.LENGTH_SHORT).show();

                        } else if (!checkBoxCurrentLocation.isChecked()){

                            Toast.makeText(ProfileCreatorActivity.this, "Select the checkbox to fetch current location!", Toast.LENGTH_SHORT).show();

                        } else if (!gpsLocationServiceActivity.canGetLocation()) {

                            gpsLocationServiceActivity.showSettingsAlert();

                        } else
                        if(rowExist && editTextProfileName.getText().toString().trim().equals(matchedProfileName))
                        {

                            Log.i("Profile Matched LOG", matchedProfileName + "matched!");
                            Toast.makeText(ProfileCreatorActivity.this, "Cannot create duplicate profiles!", Toast.LENGTH_SHORT).show();

                        } else {

                            //code to fetch the latitude & longitude for the current profile
                            String latitude = String.valueOf(gpsLocationServiceActivity.getLatitude());
                            String longitude = String.valueOf(gpsLocationServiceActivity.getLongitude());
                            String profileName =  editTextProfileName.getText().toString().trim();
                            String latitudeValue = latitude.substring(0, 7);
                            String longitudeValue = longitude.substring(0, 7);
                            String statusValue = "OFF";
                            databaseHelperActivity.createNewProfile(new ProfileListActivity(profileName, latitudeValue, longitudeValue, statusValue));
                            displayProfiles();
                            dialog.dismiss();

                        }

                    }
                });

                builder.setNegativeButton(R.string.profile_creator_dialog_decline, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Log.i(DIALOG_DECLINED_TAG, "Declined!");
                        dialog.cancel();

                    }
                });

                builder.show();

            }
        });

        registerForContextMenu(listView);
        Intent intent = new Intent(getApplicationContext(), LocationCheckerServiceActivity.class);
        startService(intent);

    }



    private void displayProfiles() {
        List<ProfileListActivity> profileListItems = new ArrayList<ProfileListActivity>();
        profileListItems.clear();
        profileListItems = databaseHelperActivity.getAllProfiles();
        ProfileAdapterActivity profileAdapter = new ProfileAdapterActivity(ProfileCreatorActivity.this, (ArrayList<ProfileListActivity>) profileListItems);
        for (ProfileListActivity pl : profileListItems){
            String log = "Profile Name: " + pl.getProfileName() + " Latitude: " + pl.getLatitudeValue() + " Longitude: " + pl.getLongitudeValue() + " Bluetooth Status: " + pl.getBluetoothStatus();
            Log.d("Profile created: ", log);
        }
        listView.setAdapter(profileAdapter);
        final List<ProfileListActivity> finalProfileListItems = profileListItems;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Code to handle the item click events for listview
                String selectedProfile = finalProfileListItems.get(position).getProfileName();
                Log.i(PROFILE_SELECTED_TAG, "Opening "+selectedProfile);
                gotoProfileSettingsActivity(selectedProfile);

            }
        });


        registerForContextMenu(listView);
        Intent intent = new Intent(getApplicationContext(), LocationCheckerServiceActivity.class);
        startService(intent);
    }

    private void gotoProfileSettingsActivity(String selectedProfile) {

        Intent intent = new Intent(ProfileCreatorActivity.this, ProfileSettingsActivity.class);
        intent.putExtra("Profile Selected", selectedProfile);
        startActivity(intent);

    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
//    {
//        String name="empty";
//        super.onCreateContextMenu(menu, v, menuInfo);
//        menu.setHeaderTitle(name);
//        menu.add(0, v.getId(), 0, "Call");
//        menu.add(0, v.getId(), 0, "SMS");
//
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item){
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        int itemId = info.position;
//        // myList.get(listPosition).getTitle();//list item title
//        if(item.getTitle()=="Call"){
//            databaseHelperActivity.deleteTitle(itemId);
//            Toast.makeText(this, "Item id [" + itemId + "]", Toast.LENGTH_SHORT).show();
//        }
//        else{
//            return false;
//        }
//        return true;
//    }
        @Override
    protected void onResume() {
        super.onResume();
        displayProfiles();

    }
}



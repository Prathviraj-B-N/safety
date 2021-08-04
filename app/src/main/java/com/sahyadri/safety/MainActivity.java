package com.sahyadri.safety;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LocationListener {
    protected ImageButton police, ambulance, fire, changeSOS,custom;
    int i =1;
    protected Spinner dropdown = null;
    private static final int REQUEST_CALL = 1;
    private static final int REQUEST_LOC = 2;
//    private static final long MIN_DISTANCE_FOR_UPDATE = 10;
//    private static final long MIN_TIME_FOR_UPDATE = 1000 * 60 * 2;
    protected String police_num, ambulance_num, fire_num, custom_num;
    protected String NUM;
    protected LocationManager locationManager;
//    Location location;
    String pincode = "575007";
    FirebaseFirestore db;

    FirebaseAuth mAuth;

    void initButtons() {
        //DONE
        police = findViewById(R.id.police);
        ambulance = findViewById(R.id.ambulance);
        fire = findViewById(R.id.fire);
        custom = findViewById(R.id.SOS);
        dropdown = findViewById(R.id.spinner);
        changeSOS = findViewById(R.id.changeSOS);

    }
    String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initButtons();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        db = FirebaseFirestore.getInstance();
        Objects.requireNonNull(getSupportActionBar()).hide();
        initLocation();
        initSpinner();
        listen();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                if (data != null){
                    String Num = data.getStringExtra("num");
                    Log.d("retrieved", "onActivityResult: "+Num);
                    updateNum(Num);
                }



            }
        }
    }

    private void updateNum(String num) {
        HashMap userMap = new HashMap();
        userMap.put("phone", num);

        db.collection("users").document(currentUserId)
                .set(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Do what you want
                        custom_num = num;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
        Log.d("updateNum", "updateNum success ");
    }
    private void listen() {
        //todo:pincode in toast
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(i==1){
                    i=0;
                }
                else{
                    pincode = parent.getSelectedItem().toString();
                    Log.d("pin_changed", parent.getSelectedItem().toString());
                    Toast.makeText(getBaseContext(), "Location:" + pincode, Toast.LENGTH_SHORT).show();
                    dataRead(pincode);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        police.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "calling police", Toast.LENGTH_SHORT).show();
                NUM = police_num;
                makePhoneCall();
                NUM = "";
            }
        });
        ambulance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "calling ambulance", Toast.LENGTH_SHORT).show();
                NUM = ambulance_num;
                makePhoneCall();
                NUM = "";
            }
        });
        fire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "calling fire", Toast.LENGTH_SHORT).show();
                NUM = fire_num;
                makePhoneCall();
                NUM = "";
            }
        });
        custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "calling a friend", Toast.LENGTH_SHORT).show();
                NUM = custom_num;
                Log.d("sos num", "N"+NUM);
                makePhoneCall();
                NUM = "";
            }
        });
        changeSOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, NumGetter.class);
                myIntent.putExtra("key", "-"); //Optional parameters
                MainActivity.this.startActivityForResult(myIntent,100);
            }
        });
    }


    void makePhoneCall() {
        Log.d("makePhoneCall", "N"+NUM);
        if (NUM.trim().length() > 0) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                String dial = "tel:" + NUM;
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(dial));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                startActivity(intent);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            }
        } else {
            Toast.makeText(getBaseContext(), "No contacts available", Toast.LENGTH_SHORT).show();
        }
    }

    void dataRead(String pincode) {
        String TAG = "dataread";

        DocumentReference docRef = db.collection("phoneNumbers").document(pincode);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        police_num =document.getData().get("police").toString();
                        ambulance_num =document.getData().get("ambulance").toString();
                        fire_num =document.getData().get("fire").toString();
                        Log.d("contact", pincode);
                        Log.d("contact", "police: " + police_num);
                        Log.d("contact", "amb: " + ambulance_num);
                        Log.d("contact", "fire: " + fire_num);

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        DocumentReference docRef2 = db.collection("users").document(currentUserId);
        docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        custom_num =document.getData().get("phone").toString();
                        Log.d("contact", "custom: " + custom_num);


                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    //TODO:populate with pincodes
    private void initSpinner() {
        Log.d("entryExit", "onItemSelected: en : initSpinner");

        String[] items = new String[]{pincode,"576201", "575007"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setSelection(0, true);
        Log.d("entryExit", "onItemSelected: ex : initSpinner");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makePhoneCall();
                    Log.d("phonePermission", "onRequestPermissionsResult: ");
                }
                break;

            case REQUEST_LOC:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                }
                break;

            default:
                Toast.makeText(getBaseContext(), "Some Permission denied", Toast.LENGTH_SHORT).show();

        }

    }

    void initLocation(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOC);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationEnabled();
        getLocation();
    }

    private void locationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(com.sahyadri.safety.MainActivity.this)
                    .setTitle("Enable GPS Service")
                    .setMessage("We need your GPS location to show Near Places around you.")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5, (LocationListener) this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

//            tvCity.setText(addresses.get(0).getLocality());
//            tvState.setText(addresses.get(0).getAdminArea());
//            tvCountry.setText(addresses.get(0).getCountryName());
//            tvLocality.setText(addresses.get(0).getAddressLine(0));

            pincode = addresses.get(0).getPostalCode();
            dataRead(pincode);
            Log.d("gotPin", pincode);

        } catch (Exception e) {
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
//
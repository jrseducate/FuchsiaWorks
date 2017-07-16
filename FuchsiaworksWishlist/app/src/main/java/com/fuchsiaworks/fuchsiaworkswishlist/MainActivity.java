package com.fuchsiaworks.fuchsiaworkswishlist;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.samples.vision.barcodereader.BarcodeCaptureActivity;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.LinkedList;

@SuppressWarnings("FieldCanBeLocal")
public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "BarcodeMain";
    private static final int GOOGLE_BARCODE_CAPTURE = 9001;
    private static final int ZXING_BARCODE_CAPTURE = 49374;

    SharedPreferences sharedPreferences;

    static MainActivity mainActivity;
    static Context context;

    static LocationManager locationManager;

    private FloatingActionButton fabAddItem;
    private FloatingActionButton btnClearItems;
    private FloatingActionButton fabFlash;
    private FloatingActionButton fabCaptureType;

    private RecyclerView recyclerViewWishlist;
    WishlistAdapter wishlistAdapter;
    private LinearLayoutManager linearLayoutManager;

    WishlistItem itemEditing = null;

    private boolean selectingForDelete;

    private enum scanner_used
    {
        Google_Scanner("Google Vision API"),
        Zxing_Scanner("Zxing Barcode Scanner");

        String name;
        scanner_used(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }

    scanner_used scannerUsed = scanner_used.Google_Scanner;
    boolean useFlash = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //NOTE(Jeremy): Sets the content view when starting the application
        //NOTE(Jeremy): Activity related stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        mainActivity = this;
        context = mainActivity.getApplicationContext();
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        // NOTE(Jeremy): Used to determine if selecting for the purpose of deleting
        selectingForDelete = false;

        //TODO(Jeremy): You're not supposed to be using this silly :p
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //NOTE(Jeremy): The _RecyclerView_ used to display all actively viewed _WishlistItem_
        recyclerViewWishlist = (RecyclerView) findViewById(R.id.ma_listWishlist);

        //NOTE(Jeremy): The _WishlistAdapter_ used to take _WishlistItem_ and properly
        //              display them in the _RecyclerView_
        wishlistAdapter = new WishlistAdapter(sharedPreferences, R.layout.button_with_icon);
        wishlistAdapter.loadWishlistItems(sharedPreferences);
        recyclerViewWishlist.setAdapter(wishlistAdapter);

        //NOTE(Jeremy): The _LinearLayoutManager_ is used to display the items as a list in
        //              the _RecyclerView_
        linearLayoutManager = new LinearLayoutManager(context);
        recyclerViewWishlist.setLayoutManager(linearLayoutManager);

        //NOTE(Jeremy): The _FloatingActionButton_ used to scan a new _WishlistItem_
        fabAddItem = (FloatingActionButton) findViewById(R.id.ma_btnAddItem);
        fabAddItem.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch(scannerUsed)
                {
                    case Google_Scanner:
                    {
                        initiateScanGoogle();
                    } break;
                    case Zxing_Scanner:
                    {
                        initiateScanZxing();
                    } break;
                }
            }
        });

        //NOTE(Jeremy): The _FloatingActionButton_ used to remove all items
        btnClearItems = (FloatingActionButton) findViewById(R.id.ma_fabClearItems);
        btnClearItems.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!selectingForDelete)
                {
                    wishlistAdapter.beginItemSelection();
                    selectingForDelete = true;
                    fabAddItem.setEnabled(false);
                    fabAddItem.getBackground().setColorFilter(Color.parseColor("#404040"), PorterDuff.Mode.SRC_ATOP);
                }
                else
                {
                    LinkedList<WishlistItem> wishlistItems = wishlistAdapter.endItemSelection();
                    for (WishlistItem item : wishlistItems)
                        wishlistAdapter.remove(item);
                    selectingForDelete = false;
                    fabAddItem.setEnabled(true);
                    fabAddItem.getBackground().setColorFilter(Color.parseColor("#FFFF4081"), PorterDuff.Mode.SRC_ATOP);
                }
            }
        });

        fabFlash = (FloatingActionButton)findViewById(R.id.ma_fabFlash);
        fabFlash.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                useFlash = !useFlash;

                if (useFlash)
                {
                    Toast displayMessage = Toast.makeText(context, "Flash (On)", Toast.LENGTH_SHORT);
                    displayMessage.show();
                }
                else
                {
                    Toast displayMessage = Toast.makeText(context, "Flash (Off)", Toast.LENGTH_SHORT);
                    displayMessage.show();
                }
            }
        });

        fabCaptureType = (FloatingActionButton)findViewById(R.id.ma_fabCaptureType);
        fabCaptureType.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int nextScannerUsedIndex = (scannerUsed.ordinal() + 1);
                nextScannerUsedIndex = (nextScannerUsedIndex >= scanner_used.values().length) ? 0 : nextScannerUsedIndex;
                scannerUsed = scanner_used.values()[nextScannerUsedIndex];

                Toast displayMessage = Toast.makeText(context, "Active Scanner: (" + scannerUsed.toString() + ")", Toast.LENGTH_SHORT);
                displayMessage.show();
            }
        });
    }

    private void initiateScanGoogle()
    {
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash);

        startActivityForResult(intent, GOOGLE_BARCODE_CAPTURE);
    }

    //NOTE(Jeremy): Used to scan using the ZXwing App
    public void initiateScanZxing()
    {
        IntentIntegrator scanIntegrator = new IntentIntegrator(mainActivity);
        scanIntegrator.initiateScan();
    }

    //NOTE(Jeremy): Used to retrieve scan result using the ZXwing App
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if (requestCode == GOOGLE_BARCODE_CAPTURE)
        {
            if (resultCode == CommonStatusCodes.SUCCESS)
            {
                if (intent != null)
                {
                    Barcode barcode = intent.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);

                    String scanContent = barcode.displayValue;
                    String scanFormat = String.valueOf(barcode.format);

                    itemEditing = new WishlistItem();

                    itemEditing.name = itemEditing.barcode = scanContent;
                    itemEditing.barcodeFormat = scanFormat;
                    itemEditing.getWebpageIfNeeded();

                    Location location = getLastBestLocation();

                    itemEditing.gpsLatitude = location.getLatitude();
                    itemEditing.gpsLongitude = location.getLongitude();

                    itemEditing.gpsSet = true;

                    if (itemEditing != null && !wishlistAdapter.itemAlreadyScanned(itemEditing))
                    {
                        showEditScreen();
                        wishlistAdapter.add(itemEditing);
                    }
                    else if (wishlistAdapter.itemAlreadyScanned(itemEditing))
                    {
                        Toast toastError = Toast.makeText(getApplicationContext(), "Barcode Scanning Failed: " + "Item already scanned!", Toast.LENGTH_SHORT);
                        toastError.show();
                    }
                    else
                    {
                        Toast toastError = Toast.makeText(getApplicationContext(), "Barcode Scanning Failed: " + "Invalid barcode scanned!", Toast.LENGTH_SHORT);
                        toastError.show();
                    }

                    itemEditing = null;
                } else
                {
                    Toast toastError = Toast.makeText(getApplicationContext(), "Barcode Scanning Failed: " + "No barcode captured, intent data is null", Toast.LENGTH_SHORT);
                    toastError.show();
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else
            {
                Toast toastError = Toast.makeText(getApplicationContext(), "Barcode Scanning Failed: " + CommonStatusCodes.getStatusCodeString(resultCode), Toast.LENGTH_SHORT);
                toastError.show();
                Log.d(TAG, CommonStatusCodes.getStatusCodeString(resultCode));
            }
        }
        else if (requestCode == ZXING_BARCODE_CAPTURE)
        {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

            if (scanningResult != null)
            {
                String scanContent = scanningResult.getContents();
                String scanFormat = scanningResult.getFormatName();

                itemEditing = new WishlistItem();

                itemEditing.name = itemEditing.barcode = scanContent;
                itemEditing.barcodeFormat = scanFormat;
                itemEditing.getWebpageIfNeeded();

                Location location = getLastBestLocation();

                itemEditing.gpsLatitude = location.getLatitude();
                itemEditing.gpsLongitude = location.getLongitude();

                itemEditing.gpsSet = true;

                if (itemEditing != null && !wishlistAdapter.itemAlreadyScanned(itemEditing))
                {
                    showEditScreen();
                    wishlistAdapter.add(itemEditing);
                }
                else if (wishlistAdapter.itemAlreadyScanned(itemEditing))
                {
                    Toast toastError = Toast.makeText(getApplicationContext(), "Barcode Scanning Failed: " + "Item already scanned!", Toast.LENGTH_SHORT);
                    toastError.show();
                }
                else
                {
                    Toast toastError = Toast.makeText(getApplicationContext(), "Barcode Scanning Failed: " + "Invalid barcode scanned!", Toast.LENGTH_SHORT);
                    toastError.show();
                }


                itemEditing = null;

            } else
            {
                Toast toastError = Toast.makeText(getApplicationContext(), "Barcode Scanning Failed...", Toast.LENGTH_SHORT);
                toastError.show();
            }
        }
    }

    Location getLastBestLocation()
    {
        if (locationManager == null)
        {
            locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return null;
        }
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    public void showEditScreen()
    {
        Intent editItemIntent = new Intent(this, EditActivity.class);
        EditActivity.itemEditing = itemEditing;

        startActivity(editItemIntent);
    }
}

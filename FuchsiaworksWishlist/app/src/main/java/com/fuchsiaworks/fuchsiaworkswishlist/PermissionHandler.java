package com.fuchsiaworks.fuchsiaworkswishlist;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * The permission handler used to resolve
 * any required permissions
 */

class PermissionHandler
{
    static PermissionHandler handler = new PermissionHandler();

    private static final int PERMISSION_ACCESS_LOCATION = 1024;
    private static LocationManager locationManager;

    Location getLastBestLocation(Activity activity)
    {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return null;
        }

        if (locationManager == null)
        {
            locationManager = (LocationManager)
                    activity.getSystemService(Context.LOCATION_SERVICE);
        }
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (locationGPS != null)
        {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;
        if (locationNet != null)
        {
            NetLocationTime = locationNet.getTime();
        }

        if (GPSLocationTime - NetLocationTime > 0)
        {
            return locationGPS;
        }
        else
        {
            return locationNet;
        }
    }

    void getPermissionForLocation(Activity activity)
    {
        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.ACCESS_COARSE_LOCATION))
            {
                Toast toastError = Toast.makeText(activity, "Get Last Best Location Failed: " + "ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission not granted!", Toast.LENGTH_SHORT);
                toastError.show();
            }
            else
            {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                     Manifest.permission.ACCESS_COARSE_LOCATION},
                                     PERMISSION_ACCESS_LOCATION);
            }
        }
    }

    @SuppressWarnings("UnusedParameters")
    void onRequestPermissionsResult(Activity activity, int requestCode, String permissions[], int[] grantResults)
    {
        switch(requestCode)
        {
            case PERMISSION_ACCESS_LOCATION:
            {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast toastError = Toast.makeText(activity.getApplicationContext(), "Gps Location permission is now granted! Location can now be set.", Toast.LENGTH_SHORT);
                    toastError.show();
                }
            } break;
        }
    }
}

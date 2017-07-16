package com.fuchsiaworks.fuchsiaworkswishlist;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.Locale;

/**
 * Created by fuchs on 7/15/2017.
 */

public class EditActivity extends AppCompatActivity
{
    private static EditActivity editActivity;
    private static Context context;

    static WishlistItem itemEditing;

    private int PICK_IMAGE_REQUEST = 1;

    ImageView image;
    TextView txtName;
    TextView txtQuantity;
    TextView txtQuantityDesired;

    boolean stillEditing = false;

    static void saveItemEditing()
    {
        MainActivity.mainActivity.wishlistAdapter.saveWishlistItem(MainActivity.mainActivity.sharedPreferences, itemEditing);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (!stillEditing)
        {
            MainActivity.mainActivity.wishlistAdapter.notifyDataSetChanged();
            itemEditing = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        editActivity = this;
        context = editActivity.getApplicationContext();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_item);

        image = (ImageView) findViewById(R.id.ei_imgImage);
        txtName = (TextView) findViewById(R.id.ei_txtName);
        txtQuantity = (TextView) findViewById(R.id.ei_txtQuantity);
        txtQuantityDesired = (TextView) findViewById(R.id.ei_txtQuantityDesired);

        updateDisplay();

        Button btnEditImage = (Button) findViewById(R.id.ei_btnEditImage);
        btnEditImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                stillEditing = true;

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        Button btnEditName = (Button) findViewById(R.id.ei_btnEditName);
        btnEditName.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(editActivity);
                builder.setTitle("Title");

                final EditText input = new EditText(editActivity);

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(itemEditing.nameOverride);
                input.setHint(itemEditing.name);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String nameOverride = input.getText().toString();
                        if (nameOverride != null && !nameOverride.isEmpty())
                        {
                            itemEditing.nameOverride = nameOverride;
                            txtName.setText(itemEditing.nameOverride);
                        } else
                        {
                            itemEditing.nameOverride = null;
                            txtName.setText(itemEditing.name);
                        }

                        saveItemEditing();
                        updateDisplay();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
        Button btnEditQuantityDesired = (Button) findViewById(R.id.ei_btnEditQuantityDesired);
        btnEditQuantityDesired.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Dialog dialog = new Dialog(editActivity);
                dialog.setTitle("Quantity Desired");
                dialog.setContentView(R.layout.dialog_number);

                Button btnSet = (Button) dialog.findViewById(R.id.dn_btnSet);
                Button btnCancel = (Button) dialog.findViewById(R.id.dn_btnCancel);

                final NumberPicker numberPicker = (NumberPicker) dialog.findViewById(R.id.dn_numberPicker);
                numberPicker.setMaxValue(32);
                numberPicker.setMinValue(1);
                numberPicker.setValue(itemEditing.quantityDesired);
                numberPicker.setWrapSelectorWheel(true);

                btnSet.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int quantityDesired = numberPicker.getValue();
                        itemEditing.quantityDesired = quantityDesired;
                        txtQuantityDesired.setText(String.valueOf(itemEditing.quantityDesired));

                        saveItemEditing();
                        updateDisplay();

                        dialog.dismiss();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        Button btnEditQuantity = (Button) findViewById(R.id.ei_btnEditQuantity);
        btnEditQuantity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Dialog dialog = new Dialog(editActivity);
                dialog.setTitle("Quantity");
                dialog.setContentView(R.layout.dialog_number);

                Button btnSet = (Button) dialog.findViewById(R.id.dn_btnSet);
                Button btnCancel = (Button) dialog.findViewById(R.id.dn_btnCancel);

                final NumberPicker numberPicker = (NumberPicker) dialog.findViewById(R.id.dn_numberPicker);
                numberPicker.setMaxValue(itemEditing.quantityDesired);
                numberPicker.setMinValue(0);
                numberPicker.setValue(itemEditing.quantity);
                numberPicker.setWrapSelectorWheel(true);

                btnSet.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int quantity = numberPicker.getValue();
                        itemEditing.quantity = quantity;
                        txtQuantity.setText(String.valueOf(itemEditing.quantity));

                        saveItemEditing();
                        updateDisplay();

                        dialog.dismiss();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        FloatingActionButton fabResetImage = (FloatingActionButton) findViewById(R.id.ei_fabResetImage);
        fabResetImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                itemEditing.imageUrlOverride = null;
                saveItemEditing();
                updateDisplay();
            }
        });

        FloatingActionButton fabResetName = (FloatingActionButton) findViewById(R.id.ei_fabResetName);
        fabResetName.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                itemEditing.nameOverride = null;
                saveItemEditing();
                updateDisplay();
            }
        });

        Button btnViewGpsLocation = (Button) findViewById(R.id.ei_btnViewGpsLocation);
        btnViewGpsLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (itemEditing.gpsSet)
                {
                    stillEditing = true;
                    if (itemEditing.gpsLocation != null)
                    {

                    } else
                    {
                        String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=\"%f,%f\"", itemEditing.gpsLatitude, itemEditing.gpsLongitude, itemEditing.gpsLatitude, itemEditing.gpsLongitude);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
                else
                {
                    Toast toastError = Toast.makeText(getApplicationContext(), "No gps location is set...", Toast.LENGTH_SHORT);
                    toastError.show();
                }
            }
        });

        Button btnEditGpsLocation = (Button) findViewById(R.id.ei_btnEditGpsLocation);
        btnEditGpsLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        FloatingActionButton fabSetGpsLocation = (FloatingActionButton) findViewById(R.id.ei_fabSetGpaLocation);
        fabSetGpsLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Location location = MainActivity.mainActivity.getLastBestLocation();

                itemEditing.gpsLatitude = location.getLatitude();
                itemEditing.gpsLongitude = location.getLongitude();

                itemEditing.gpsSet = true;
                saveItemEditing();

                Toast toastError = Toast.makeText(getApplicationContext(), "Gps now set to: " + itemEditing.gpsLatitude + ", " + itemEditing.gpsLongitude, Toast.LENGTH_SHORT);
                toastError.show();
            }
        });

    }

    private void updateDisplay()
    {
        txtName.setText((itemEditing.nameOverride != null) ? itemEditing.nameOverride : itemEditing.name);
        txtQuantity.setText(String.valueOf(itemEditing.quantity));
        txtQuantityDesired.setText(String.valueOf(itemEditing.quantityDesired));

        if (itemEditing.imageUrlOverride != null)
        {
            image.setImageBitmap(null);
            Picasso.with(image.getContext())
                    .cancelRequest(image);
            Picasso.with(image.getContext())
                    .load(itemEditing.imageUrlOverride)
                    .fit()
                    .centerInside()
                    .into(image);
        }
        else if (itemEditing.imageUrl != null && !itemEditing.imageUrl.isEmpty())
        {
            image.setImageBitmap(null);
            Picasso.with(image.getContext())
                    .cancelRequest(image);
            Picasso.with(image.getContext())
                    .load(itemEditing.imageUrl)
                    .fit()
                    .centerInside()
                    .into(image);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            itemEditing.imageUrlOverride = data.getData();

            saveItemEditing();
            updateDisplay();
        }

        stillEditing = false;
    }
}
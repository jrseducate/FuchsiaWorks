package com.fuchsiaworks.fuchsiaworkswishlist;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.LinkedList;

/**
 * Author: Jeremy
 * Note: The wishlist adapter provides and stores data involved in displaying
 * wishlist items
 */

class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder>
{
    private SharedPreferences preferences;
    private LinkedList<WishlistItem> items;
    private int itemLayout;

    private boolean isSelecting;
    private LinkedList<WishlistItem> itemsSelected;

    WishlistAdapter(SharedPreferences preferences, int itemLayout)
    {
        this.items = new LinkedList<>();
        this.itemLayout = itemLayout;
        this.preferences = preferences;
        this.isSelecting = false;
    }

    void beginItemSelection()
    {
        isSelecting = true;

        itemsSelected = new LinkedList<>();
    }

    LinkedList<WishlistItem> endItemSelection()
    {
        isSelecting = false;

        LinkedList<WishlistItem> itemsSelectedPtr = itemsSelected;
        itemsSelected = null;

        return itemsSelectedPtr;
    }

    LinkedList<WishlistItem> getWishlistItems()
    {
        return items;
    }

    private WishlistItem getWishlistItem(int index)
    {
        return items.get(index);
    }

    //NOTE(Jeremy): Loads all _WishlistItem_ from application preferences
    void loadWishlistItems(SharedPreferences preferences)
    {
        int wishlistCount = preferences.getInt("wishlistitem-count", 0);

        Log.v("FWW-MainActivity", "Loading Wishlist Items...");
        Log.v("FWW-MainActivity", "Wishlist Items Count: " + wishlistCount);
        Log.v("FWW-MainActivity", "");

        for (int index = 0; index < wishlistCount; index++)
        {
            items.add(index, new WishlistItem());
            WishlistItem item = items.get(index);

            item.name = preferences.getString("wishlistitem-" + index + "-name", "INVALID");
                item.nameOverride = preferences.getString("wishlistitem-" + index + "-nameoverride", null);
            item.imageUrl = preferences.getString("wishlistitem-" + index + "-imageurl", "INVALID");
            String imageUrlOverride = preferences.getString("wishlistitem-" + index + "-imageurloverride", null);
            if (imageUrlOverride != null)
                item.imageUrlOverride = Uri.parse(imageUrlOverride);
            item.quantity = preferences.getInt("wishlistitem-" + index + "-quantity", 0);
            item.quantityDesired = preferences.getInt("wishlistitem-" + index + "-quantitydesired", 0);
            item.barcode = preferences.getString("wishlistitem-" + index + "-barcode", "INVALID");
            item.barcodeFormat = preferences.getString("wishlistitem-" + index + "-barcodeformat", "INVALID");
            item.gpsSet = preferences.getBoolean("wishlistitem-" + index + "-gpsset", false);
            item.gpsLatitude = preferences.getFloat("wishlistitem-" + index + "-gpslatitude", 0);
            item.gpsLongitude = preferences.getFloat("wishlistitem-" + index + "-gpslongitude", 0);

            logItem(item);
        }
    }

    private void logItem(WishlistItem item)
    {
        Log.v("FWW-MainActivity", "Wishlist Item Name: " + item.name);
        Log.v("FWW-MainActivity", "Wishlist Item Name Override: " + item.nameOverride);
        Log.v("FWW-MainActivity", "Wishlist Item Image Url: " + item.imageUrl);
        Log.v("FWW-MainActivity", "Wishlist Item Image Url Override: " + item.imageUrlOverride);
        Log.v("FWW-MainActivity", "Wishlist Item Quantity Collected: " + item.quantity);
        Log.v("FWW-MainActivity", "Wishlist Item Quantity Desired: " + item.quantityDesired);
        Log.v("FWW-MainActivity", "Wishlist Item Barcode: " + item.barcode);
        Log.v("FWW-MainActivity", "Wishlist Item Barcode Format: " + item.barcodeFormat);
        Log.v("FWW-MainActivity", "Wishlist Item Gps Set: " + item.gpsSet);
        Log.v("FWW-MainActivity", "Wishlist Item Gps Latitude: " + item.gpsLatitude);
        Log.v("FWW-MainActivity", "Wishlist Item Gps Longitude: " + item.gpsLongitude);
        Log.v("FWW-MainActivity", "");
    }

    //NOTE(Jeremy): Saves a _WishlistItem_ to the application preferences
    void saveWishlistItem(SharedPreferences preferences, WishlistItem item)
    {
        Log.v("FWW-MainActivity", "Saving Wishlist Item...");

        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("wishlistitem-count", items.size());

        Log.v("FWW-MainActivity", "Wishlist Items Count: " + items.size());
        Log.v("FWW-MainActivity", "-----------");

        int index = items.indexOf(item);

        editor.putString("wishlistitem-" + index + "-name", item.name)
                .putString("wishlistitem-" + index + "-nameoverride", item.nameOverride)
                .putString("wishlistitem-" + index + "-imageurl", item.imageUrl)
                .putString("wishlistitem-" + index + "-imageurloverride", (item.imageUrlOverride != null) ? item.imageUrlOverride.toString() : null)
                .putInt("wishlistitem-" + index + "-quantity", item.quantity)
                .putInt("wishlistitem-" + index + "-quantitydesired", item.quantityDesired)
                .putString("wishlistitem-" + index + "-barcode", item.barcode)
                .putString("wishlistitem-" + index + "-barcodeformat", item.barcodeFormat)
                .putBoolean("wishlistitem-" + index + "-gpsset", item.gpsSet)
                .putFloat("wishlistitem-" + index + "-gpslatitude", (float)item.gpsLatitude)
                .putFloat("wishlistitem-" + index + "-gpslongitude", (float)item.gpsLongitude);

        if (item.imageUrlOverride == null)
            editor.remove("wishlistitem-" + index + "-imageurloverride");
        if (item.nameOverride == null || item.nameOverride.isEmpty())
            editor.remove("wishlistitem-" + index + "-nameoverride");


        logItem(item);

        editor.commit();
    }

    //NOTE(Jeremy): Saves a _WishlistItem_ to the application preferences
    void saveWishlistItems(SharedPreferences preferences)
    {
        Log.v("FWW-MainActivity", "Removing Wishlist Item...");

        for (int i = 0; i < items.size(); i++)
        {
            saveWishlistItem(preferences, items.get(i));
        }

        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("wishlistitem-count", items.size());

        Log.v("FWW-MainActivity", "Wishlist Items Count: " + items.size());
        Log.v("FWW-MainActivity", "-----------");

        editor.remove("wishlistitem-" + items.size() + "-name")
                .remove("wishlistitem-" + items.size() + "-nameoverride")
                .remove("wishlistitem-" + items.size() + "-imageurl")
                .remove("wishlistitem-" + items.size() + "-imageurloverride")
                .remove("wishlistitem-" + items.size() + "-quantity")
                .remove("wishlistitem-" + items.size() + "-quantitydesired")
                .remove("wishlistitem-" + items.size() + "-barcode")
                .remove("wishlistitem-" + items.size() + "-barcodeformat")
                .remove("wishlistitem-" + items.size() + "-gpsset")
                .remove("wishlistitem-" + items.size() + "-gpslatitude")
                .remove("wishlistitem-" + items.size() + "-gpslongitude");

        Log.v("FWW-MainActivity", "Item Removed!");
        Log.v("FWW-MainActivity", "-----------");

        editor.commit();
    }

    public boolean itemAlreadyScanned(WishlistItem item)
    {
        boolean itemInItems = false;
        for (WishlistItem wI : items)
            if (wI.barcode.equals(item.barcode))
            {
                itemInItems = true;
                break;
            }

            return itemInItems;
    }

    public int add(WishlistItem item, int position)
    {
        if (!itemAlreadyScanned(item))
        {
            items.add(position, item);
            notifyItemInserted(position);

            saveWishlistItem(preferences, item);
        }
        else
            position = WishlistItem.INVALID_WISHLISTITEM_POS;

        return position;
    }

    public int add(WishlistItem item)
    {
        return add(item, items.size());
    }

    void remove(int index)
    {
        items.remove(index);
        notifyItemRemoved(index);

        saveWishlistItems(preferences);
    }

    void remove(WishlistItem item)
    {
        remove(items.indexOf(item));
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        WishlistItem item = items.get(position);

        if (item.nameOverride != null && !item.nameOverride.isEmpty())
            holder.name.setText(item.nameOverride);
        else
            holder.name.setText(item.name);

        holder.quantity.setText(String.valueOf(item.quantity) + " / " + String.valueOf(item.quantityDesired));

        if (item.imageUrlOverride != null)
        {
            holder.image.setImageBitmap(null);
            Picasso.with(holder.image.getContext())
                    .cancelRequest(holder.image);
            Picasso.with(holder.image.getContext())
                    .load(item.imageUrlOverride)
                    .fit()
                    .centerInside()
                    .into(holder.image);
        }
        else if (item.imageUrl != null && !item.imageUrl.isEmpty())
        {
            holder.image.setImageBitmap(null);
            Picasso.with(holder.image.getContext())
                    .cancelRequest(holder.image);
            Picasso.with(holder.image.getContext())
                    .load(item.imageUrl)
                    .fit()
                    .centerInside()
                    .into(holder.image);
        }

        if (item.quantity == item.quantityDesired)
        {
            int filterColor = Color.parseColor("#22FF22");
            holder.itemView.getBackground().setColorFilter(filterColor, PorterDuff.Mode.SRC_ATOP);
        }
        else
        {
            holder.itemView.getBackground().clearColorFilter();
        }

        holder.itemView.setTag(item.barcode);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        final ViewGroup viewParent = parent;
        View v = LayoutInflater.from(viewParent.getContext()).inflate(itemLayout, viewParent, false);
        v.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int index = viewParent.indexOfChild(v);
                WishlistItem item = getWishlistItem(index);

                if (isSelecting)
                {
                    if (!itemsSelected.contains(item)) {
                        itemsSelected.add(item);
                        int filterColor = Color.parseColor("#FF00FF");
                        v.getBackground().setColorFilter(filterColor, PorterDuff.Mode.SRC_ATOP);
                    }
                    else
                    {
                        itemsSelected.remove(item);
                        v.getBackground().clearColorFilter();
                    }
                }
                else
                {
                    MainActivity.mainActivity.itemEditing = item;
                    MainActivity.mainActivity.showEditScreen();
                }
            }
        });
        return new ViewHolder(v);
    }

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        View itemView;
        TextView name;
        TextView quantity;
        ImageView image;

        ViewHolder(View itemView)
        {
            super(itemView);

            this.itemView = itemView;
            this.name = (TextView) itemView.findViewById(R.id.bwi_txtName);
            this.quantity = (TextView) itemView.findViewById(R.id.bwi_txtQuantity);
            this.image = (ImageView) itemView.findViewById(R.id.bwi_imgImage);
        }
    }
}

package com.fuchsiaworks.fuchsiaworkswishlist;

import android.annotation.SuppressLint;
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
    static WishlistAdapter wishlistAdapter;
    private MainActivity mainActivity;

    SharedPreferences preferences;
    private LinkedList<WishlistItem> items;
    private int itemLayout;

    private static boolean isSelecting;
    private LinkedList<WishlistItem> itemsSelected;

    WishlistAdapter(MainActivity mainActivity, SharedPreferences preferences, int itemLayout)
    {
        wishlistAdapter = this;
        this.mainActivity = mainActivity;
        this.items = new LinkedList<>();
        this.itemLayout = itemLayout;
        this.preferences = preferences;
        isSelecting = false;
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

    //NOTE(Jeremy): Loads all _WishlistItem_ from application preferences
    void loadWishlistItems(SharedPreferences preferences)
    {
        int wishlistCount = preferences.getInt("wishlistitem-count", 0);

        Log.v("FWW-WishlistAdapter", "Loading Wishlist Items...");
        Log.v("FWW-WishlistAdapter", "Wishlist Items Count: " + wishlistCount);
        Log.v("FWW-WishlistAdapter", "");

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
        Log.v("FWW-WishlistAdapter", "Wishlist Item Name: " + item.name);
        Log.v("FWW-WishlistAdapter", "Wishlist Item Name Override: " + item.nameOverride);
        Log.v("FWW-WishlistAdapter", "Wishlist Item Image Url: " + item.imageUrl);
        Log.v("FWW-WishlistAdapter", "Wishlist Item Image Url Override: " + item.imageUrlOverride);
        Log.v("FWW-WishlistAdapter", "Wishlist Item Quantity Collected: " + item.quantity);
        Log.v("FWW-WishlistAdapter", "Wishlist Item Quantity Desired: " + item.quantityDesired);
        Log.v("FWW-WishlistAdapter", "Wishlist Item Barcode: " + item.barcode);
        Log.v("FWW-WishlistAdapter", "Wishlist Item Barcode Format: " + item.barcodeFormat);
        Log.v("FWW-WishlistAdapter", "Wishlist Item Gps Set: " + item.gpsSet);
        Log.v("FWW-WishlistAdapter", "Wishlist Item Gps Latitude: " + item.gpsLatitude);
        Log.v("FWW-WishlistAdapter", "Wishlist Item Gps Longitude: " + item.gpsLongitude);
        Log.v("FWW-WishlistAdapter", "");
    }

    //NOTE(Jeremy): Saves a _WishlistItem_ to the application preferences
    @SuppressLint("ApplySharedPref")
    void saveWishlistItem(SharedPreferences preferences, WishlistItem item)
    {
        Log.v("FWW-WishlistAdapter", "Saving Wishlist Item...");

        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("wishlistitem-count", items.size());

        Log.v("FWW-WishlistAdapter", "Wishlist Items Count: " + items.size());
        Log.v("FWW-WishlistAdapter", "-----------");

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
    @SuppressLint("ApplySharedPref")
    void saveWishlistItems(SharedPreferences preferences)
    {
        Log.v("FWW-WishlistAdapter", "Saving Wishlist Items...");

        for (int i = 0; i < items.size(); i++)
        {
            saveWishlistItem(preferences, items.get(i));
        }

        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("wishlistitem-count", items.size());

        Log.v("FWW-WishlistAdapter", "Wishlist Items Count: " + items.size());
        Log.v("FWW-WishlistAdapter", "-----------");

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

        editor.commit();
    }

    boolean itemAlreadyScanned(WishlistItem item)
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

    int add(WishlistItem item, int position)
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

    int add(WishlistItem item)
    {
        return add(item, items.size());
    }

    void remove(int index)
    {
        items.remove(index);
        notifyItemRemoved(index);
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
        holder.item = items.get(position);

        if (holder.item.nameOverride != null && !holder.item.nameOverride.isEmpty())
            holder.name.setText(holder.item.nameOverride);
        else
            holder.name.setText(holder.item.name);

        holder.quantity.setText(String.valueOf(holder.item.quantity) + " / " + String.valueOf(holder.item.quantityDesired));

        if (holder.item.imageUrlOverride != null)
        {
            holder.image.setImageBitmap(null);
            Picasso.with(holder.image.getContext())
                    .cancelRequest(holder.image);
            Picasso.with(holder.image.getContext())
                    .load(holder.item.imageUrlOverride)
                    .fit()
                    .centerInside()
                    .into(holder.image);
        }
        else if (holder.item.imageUrl != null && !holder.item.imageUrl.isEmpty())
        {
            holder.image.setImageBitmap(null);
            Picasso.with(holder.image.getContext())
                    .cancelRequest(holder.image);
            Picasso.with(holder.image.getContext())
                    .load(holder.item.imageUrl)
                    .fit()
                    .centerInside()
                    .into(holder.image);
        }

        if (holder.item.quantity == holder.item.quantityDesired)
        {
            int filterColor = Color.parseColor("#22FF22");
            holder.itemView.getBackground().setColorFilter(filterColor, PorterDuff.Mode.SRC_ATOP);
        }
        else
        {
            holder.itemView.getBackground().clearColorFilter();
        }

        holder.itemView.setTag(holder.item.barcode);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);

        return new ViewHolder(v);
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        WishlistItem item;

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

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            if (isSelecting)
            {
                if (!wishlistAdapter.itemsSelected.contains(item))
                {
                    wishlistAdapter.itemsSelected.add(item);
                    int filterColor = Color.parseColor("#FF00FF");
                    v.getBackground().setColorFilter(filterColor, PorterDuff.Mode.SRC_ATOP);
                }
                else
                {
                    wishlistAdapter.itemsSelected.remove(item);
                    v.getBackground().clearColorFilter();
                }
            }
            else
            {
                wishlistAdapter.mainActivity.itemEditing = item;
                wishlistAdapter.mainActivity.showEditScreen();
            }
        }
    }
}

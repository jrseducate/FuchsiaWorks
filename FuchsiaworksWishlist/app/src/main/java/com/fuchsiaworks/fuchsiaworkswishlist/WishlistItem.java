package com.fuchsiaworks.fuchsiaworkswishlist;

import android.net.Uri;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


/**
 * Author: Jeremy
 * Note: The wishlist item provides the fields and methods necessary to manage
 * keeping track of any desired wishlist items
 */

class WishlistItem
{
    static final int INVALID_WISHLISTITEM_POS = -1;
    String name;
    String nameOverride;

    String imageUrl;
    Uri imageUrlOverride;

    int quantity;
    int quantityDesired;

    public String barcode;
    String barcodeFormat;

    boolean gpsSet;

    String gpsLocation;

    double gpsLatitude;
    double gpsLongitude;

    WishlistItem()
    {
        this.name = "";
        this.nameOverride = null;

        this.imageUrl = "";
        this.imageUrlOverride = null;

        this.quantity = 0;
        this.quantityDesired = 0;

        this.barcode = "";
        this.barcodeFormat = "";

        this.gpsSet = false;
        this.gpsLocation = null;
        this.gpsLatitude = 0;
        this.gpsLongitude = 0;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    void getWebpageIfNeeded()
    {
        WishlistItem item = this;

        if (item.hasWebpageAttributes());
        else if (item.getWebpage_BarcodeLookup());
        else if (item.getWebpage_UpcItemDb());
    }

    private boolean getWebpage_BarcodeLookup()
    {
        WishlistItem item = this;

        String name = "";
        String imageUrl = "";
        String webpageUrl = "https://www.barcodelookup.com/" + item.barcode;

        Log.v("FWW-MainActivity", "Attempting webpage lookup for...");
        Log.v("FWW-MainActivity", "Webpage URL: " + webpageUrl);
        Log.v("FWW-MainActivity", "Wishlist Item Barcode: " + item.name);

        try
        {
            URL url = new URL(webpageUrl);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

            Log.v("FWW-MainActivity", "Webpage Response Message: ");
            Log.v("FWW-MainActivity", urlConnection.getResponseMessage());
            Log.v("FWW-MainActivity", "-----------");

            String contentType = urlConnection.getContentType();
            String charset = getCharSet(contentType);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Document doc = Jsoup.parse(in, charset, webpageUrl);

            if (doc != null)
            {
                Log.v("FWW-MainActivity", "Searching for preview image...");
                Log.v("FWW-MainActivity", "-----------");
                Element imagePreview = doc.getElementById("img_preview");

                if (imagePreview != null)
                {
                    Log.v("FWW-MainActivity", "Successfully found preview image");
                    Log.v("FWW-MainActivity", "-----------");

                    Log.v("FWW-MainActivity", "Searching preview image for src attribute...");
                    imageUrl = imagePreview.attr("src");

                    Log.v("FWW-MainActivity", "Src Attribute:");
                    Log.v("FWW-MainActivity", imageUrl);

                    Log.v("FWW-MainActivity", "Searching preview image for alt attribute...");
                    name = imagePreview.attr("alt");

                    Log.v("FWW-MainActivity", "Alt Attribute:");
                    Log.v("FWW-MainActivity", name);
                }
                else
                {
                    Log.v("FWW-MainActivity", "Failed to find preview image");
                }

                return item.updateWebpageAttributes(name, imageUrl);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    private boolean getWebpage_UpcItemDb()
    {
        WishlistItem item = this;

        String name = "";
        String imageUrl = "";
        String webpageUrl = "http://www.upcitemdb.com/upc/" + item.barcode;

        Log.v("FWW-MainActivity", "Attempting webpage lookup for...");
        Log.v("FWW-MainActivity", "Webpage URL: " + webpageUrl);
        Log.v("FWW-MainActivity", "Wishlist Item Barcode: " + item.name);

        try
        {
            URL url = new URL(webpageUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            Log.v("FWW-MainActivity", "Webpage Response Message: ");
            Log.v("FWW-MainActivity", urlConnection.getResponseMessage());
            Log.v("FWW-MainActivity", "-----------");

            String contentType = urlConnection.getContentType();
            String charset = getCharSet(contentType);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Document doc = Jsoup.parse(in, charset, webpageUrl);

            if (doc != null)
            {
                Log.v("FWW-MainActivity", "Searching for preview image...");
                Log.v("FWW-MainActivity", "-----------");
                Elements imagePreviews = doc.getElementsByClass("product amzn");

                if (imagePreviews != null && !imagePreviews.isEmpty())
                {
                    Log.v("FWW-MainActivity", "Successfully found preview image");
                    Log.v("FWW-MainActivity", "-----------");

                    if (imagePreviews.size() == 1)
                        Log.v("FWW-MainActivity", "Successfully found preview image");
                    else
                        Log.v("FWW-MainActivity", "Multiple possible preview images found, selecting the first one...");

                    Element imagePreview = imagePreviews.first();

                    Log.v("FWW-MainActivity", "Searching preview image for src attribute...");
                    imageUrl = imagePreview.attr("src");

                    Log.v("FWW-MainActivity", "Src Attribute:");
                    Log.v("FWW-MainActivity", imageUrl);

                    Log.v("FWW-MainActivity", "Searching preview image for alt attribute...");
                    name = imagePreview.attr("alt");

                    Log.v("FWW-MainActivity", "Alt Attribute:");
                    Log.v("FWW-MainActivity", name);
                }
                else
                {
                    Log.v("FWW-MainActivity", "Failed to find preview image");
                }

                return item.updateWebpageAttributes(name, imageUrl);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    private boolean hasWebpageAttributes()
    {
        WishlistItem item = this;

        return item.imageUrl != null && !item.imageUrl.isEmpty() &&
                item.name != null && !item.name.isEmpty();
    }

    private boolean updateWebpageAttributes(String name, String imageUrl)
    {
        WishlistItem item = this;
        boolean result = false;

        if (imageUrl != null && !imageUrl.isEmpty() &&
                name != null && !name.isEmpty())
        {
            item.imageUrl = imageUrl;
            item.name = name;
            result = true;
        }
        return result;
    }

    private static String getCharSet(String contentType)
    {
        String[] values = contentType.split(";"); // values.length should be 2
        String charset = "";

        for (String value : values)
        {
            value = value.trim();

            if (value.toLowerCase().startsWith("charset="))
            {
                charset = value.substring("charset=".length());
            }
        }
        if (charset.isEmpty())
        {
            charset = "UTF-8"; //Assumption
        }
        return charset;
    }

}

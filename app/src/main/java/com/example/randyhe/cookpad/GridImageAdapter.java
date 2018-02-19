package com.example.randyhe.cookpad;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by Asus on 2/12/2018.
 */

public class GridImageAdapter extends ArrayAdapter<String> {
    private Context c;
    private LayoutInflater inflate;
    private int layoutResource;
    private String nappend;
    private ArrayList<String> imgurls;

    public GridImageAdapter(Context context,
            int layoutresource,
            String append,
            ArrayList<String> imgUrls)
    {
        super(context,layoutresource,imgUrls);
        c = context;
        inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource=layoutresource;
        nappend = append;
        imgurls = imgUrls;
    }

    private static class ViewHolder
    {
        ImageView recipePics;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        final ViewHolder vHolder;
        if(convertView == null)
        {
            convertView = inflate.inflate(layoutResource,parent,false);
            vHolder = new ViewHolder();
            vHolder.recipePics = (ImageView) convertView.findViewById(R.id.recipePic);
        }
        else
        {
            vHolder = (ViewHolder) convertView.getTag();
        }

        String imgUrl = getItem(position);

        if(vHolder != null)
        {
          ImageLoader imageLoad = ImageLoader.getInstance();
            imageLoad.displayImage(imgUrl, vHolder.recipePics, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
        }

        return convertView;
    }
}

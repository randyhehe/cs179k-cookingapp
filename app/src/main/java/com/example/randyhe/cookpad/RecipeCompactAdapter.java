package com.example.randyhe.cookpad;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by randyhe on 3/5/18.
 */

public class RecipeCompactAdapter extends RecyclerView.Adapter<RecipeCompactAdapter.ViewHolder> {
    private List<RecipeCompactObject> mDataset;
    private Context context;
    private FirebaseStorage fbStorage;
    private StorageReference storageReference;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public ViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    public RecipeCompactAdapter(List<RecipeCompactObject> myDataset, Context c) {
        mDataset = myDataset;
        context = c;

        fbStorage = FirebaseStorage.getInstance();
        storageReference = fbStorage.getReference();
    }

    @Override
    public RecipeCompactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_profile_recipebutton, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final View recipeItem = holder.view;
        final ImageView recipeImage = recipeItem.findViewById(R.id.imageView);
        final CircleImageView userPic = recipeItem.findViewById(R.id.userPic);
        final TextView userName = recipeItem.findViewById(R.id.username);
        final TextView recipeName = recipeItem.findViewById(R.id.recipeName);
        final TextView recipeTime = recipeItem.findViewById(R.id.recipeTime);
        final TextView recipeServings = recipeItem.findViewById(R.id.recipeServings);
        final TextView recipeBio = recipeItem.findViewById(R.id.recipeBio);

        final RecipeCompactObject recipeCompactObject = mDataset.get(position);

        recipeName.setText(recipeCompactObject.recipeTitle);
        recipeTime.setText(recipeCompactObject.recipeTimeToCook);
        recipeServings.setText(recipeCompactObject.recipeServings);
        recipeBio.setText(recipeCompactObject.recipeDescription);
        userName.setText(recipeCompactObject.recipePublisher);

        // load recipe image
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(storageReference.child(recipeCompactObject.recipeMainPhotoPath))
                .into(recipeImage);

        // load user image
        final String profilePhotoPath = recipeCompactObject.recipePublisherPhotoPath;
        if (profilePhotoPath != null) {
            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(storageReference.child(profilePhotoPath))
                    .into(userPic);
        } else { // load with default image
            userPic.setImageResource(R.drawable.kermit_cooking);
        }

        // onclick recipe
        recipeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Individual_Recipe.class);
                intent.putExtra("ID", recipeCompactObject.recipeId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

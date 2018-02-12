package com.example.randyhe.cookpad;

/**
 * Created by Asus on 2/5/2018.
 */

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
        import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;

        import android.os.Bundle;
        import android.support.v4.app.Fragment;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageButton;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.TextView;
        import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.internal.zzbco.NULL;

public class exploreFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static feedFragment newInstance() {
        feedFragment fragment = new feedFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_explore, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        final Context c = getActivity();

        LinearLayout feed = (LinearLayout) getView().findViewById(R.id.recipesScrollView);

        final View a = getLayoutInflater().inflate(R.layout.explore_recipe, null);

        db.collection("recipes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int i = 0;
                            for (DocumentSnapshot document : task.getResult()) {
                                String image = document.getString("mainPhotoStoragePath");
                                if(image != null)
                                {
                                    if(i == 0)
                                    {
                                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(image);

                                        ImageView imageView = (ImageView) a.findViewById(R.id.recipePic1);

                                        Glide.with(c /* context */)
                                                .using(new FirebaseImageLoader())
                                                .load(storageReference)
                                                .into(imageView);
                                        i++;
                                    }
                                    else if(i == 1)
                                    {
                                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(image);

                                        ImageView imageView = (ImageView) a.findViewById(R.id.recipePic2);

                                        Glide.with(c /* context */)
                                                .using(new FirebaseImageLoader())
                                                .load(storageReference)
                                                .into(imageView);
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        feed.addView(a);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
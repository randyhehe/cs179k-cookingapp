package com.example.randyhe.cookpad;

/**
 * Created by Asus on 2/5/2018.
 */

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import java.util.ArrayList;
import java.util.Vector;

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
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(c).build();
        ImageLoader.getInstance().init(config);

        final Vector<View> listPic = new Vector<View>();
        LinearLayout feed = (LinearLayout) getView().findViewById(R.id.recipesScrollView);

        final ArrayList<String> imgUrls = new ArrayList<>();

        db.collection("recipes").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            final GridView g = (GridView) getView().findViewById(R.id.grid);
                            final GridImageAdapter gia = new GridImageAdapter(c,R.layout.grid_imageview,"",imgUrls);
                            g.setAdapter(gia);
                            for (DocumentSnapshot document : task.getResult()) {
                                String image = document.getString("imagePath");
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(image);
                                imgUrls.add(image);
                                g.setAdapter(gia);
                            }
                        }

                    }
                });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
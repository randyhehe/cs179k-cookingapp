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
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class exploreFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = fbStorage.getReference();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int tempCounter;


    public static exploreFragment newInstance() {
        exploreFragment fragment = new exploreFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_explore, container, false);
    }

    @Override
    public void onRefresh() {
        getData();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                getData();
            }
        });
    }

    private void getData() {
        mSwipeRefreshLayout.setRefreshing(true);
        final Context c = getActivity();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(c).build();
        ImageLoader.getInstance().init(config);

        final Vector<View> listPic = new Vector<View>();
        LinearLayout feed = (LinearLayout) getView().findViewById(R.id.recipesScrollView);

        final ArrayList<String> imgUrls = new ArrayList<>();
        final ArrayList<String> recipeId = new ArrayList<>();

        db.collection("recipes").get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful())
                    {
                        final GridView g = (GridView) getView().findViewById(R.id.grid);
                        View a = getLayoutInflater().inflate(R.layout.grid_imageview, null);

                        tempCounter = task.getResult().size();
                        for (final DocumentSnapshot document : task.getResult()) {
                            String path = document.getString("mainPhotoStoragePath");
                            storageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imgUrls.add(uri.toString());
                                    recipeId.add(document.getId());

                                    if (--tempCounter == 0) {
                                        final GridImageAdapter gia = new GridImageAdapter(c,R.layout.grid_imageview,"",imgUrls,recipeId);
                                        g.setAdapter(gia);
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
                                }
                            });
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
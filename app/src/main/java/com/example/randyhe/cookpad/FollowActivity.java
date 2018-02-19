package com.example.randyhe.cookpad;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by mcast on 2/18/2018.
 */

public class FollowActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentFirebaseUser = auth.getCurrentUser();

    private CircleImageView profilePic;
    private TextView tvUsername;
    private TextView tvRecipeNum;
    private TextView tvFollowersNum;
    private Button followBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_followlist);
        final LinearLayout feed = (LinearLayout) findViewById(R.id.followfeed);
//
//        View followItem = getLayoutInflater().inflate(R.layout.snippet_follow_listitem, null);
//        feed.addView(followItem);

        loadList(feed);
    }


    private void setupViews(View view) {
        tvUsername = (TextView) view.findViewById(R.id.tvUsername);
        tvRecipeNum = (TextView) view.findViewById(R.id.recipeNum);
        tvFollowersNum = (TextView) view.findViewById(R.id.followersNum);
        profilePic = (CircleImageView) view.findViewById(R.id.profilePic);
        followBtn = (Button) view.findViewById(R.id.followBtn);
    }

    private void loadList(final LinearLayout feed) {
        String user = getIntent().getExtras().getString("ID");
        DocumentReference docRef = db.collection("users").document(user);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    Map<String, Object> data = documentSnapshot.getData();

                    List<HashMap<String, Object>> followers = (ArrayList<HashMap<String, Object>>) data.get("followers");

                    for(int i = 0; i < followers.size(); i++) {
                        loadItem( (String) followers.get(i).get("uID"), feed);
                    }
                } else {
                    /* Document doesn't exist */
                }
            }
        });
    }

    private void loadItem(String userId, final LinearLayout feed) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    Map<String, Object> data = documentSnapshot.getData();

                    List<HashMap<String, Object>> followers = (ArrayList<HashMap<String, Object>>) data.get("followers");
                    List<HashMap<String, Object>> recipes = (ArrayList<HashMap<String, Object>>) data.get("recipes");
                    int followersNum = followers.size();
                    int recipesNum = recipes.size();

                    View followItem = getLayoutInflater().inflate(R.layout.snippet_follow_listitem, null);
                    setupViews(followItem);

                    tvUsername.setText((String) data.get("username"));
                    tvRecipeNum.setText(String.valueOf(recipesNum) + " recipes");
                    tvFollowersNum.setText(String.valueOf(followersNum) + " followers");
                    followBtn.setText("Following");

                    feed.addView(followItem);

                } else {
                    /* Document doesn't exist */
                }
            }
        });
    }

}

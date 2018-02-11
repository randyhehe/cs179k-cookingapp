package com.example.randyhe.cookpad;

import android.net.Uri;

import com.google.firebase.firestore.Exclude;

/**
 * Created by randyhe on 2/5/18.
 */

public class Method {
    public String instruction;
    public String storagePath;
    @Exclude public Uri photoUri;

    public Method(String instruction, Uri photoUri) {
        this.instruction = instruction;
        this.photoUri = photoUri;
        storagePath = null;
    }
}

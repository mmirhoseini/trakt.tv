package com.mirhoseini.trakttv.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mirhoseini.trakttv.TraktApplication;
import com.mirhoseini.trakttv.di.component.ApplicationComponent;

/**
 * Created by Mohsen on 19/07/16.
 */

public abstract class BaseActivity extends AppCompatActivity {
    protected FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        injectDependencies(TraktApplication.getComponent());

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // can be used for general purpose in all Activities of Application

    }

    protected abstract void injectDependencies(ApplicationComponent component);

}

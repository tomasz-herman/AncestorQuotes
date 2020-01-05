package com.therman.ancestorquotes;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import java.util.Objects;

public class AboutActivity extends AppCompatActivity {

    TextView tvAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        tvAbout = findViewById(R.id.tvAbout);
        tvAbout.setMovementMethod(LinkMovementMethod.getInstance());
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.about));

    }
}

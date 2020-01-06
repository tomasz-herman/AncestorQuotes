package com.therman.ancestorquotes;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import java.util.Objects;

public class AboutActivity extends AppCompatActivity {

    TextView tvAbout1, tvAbout2, tvAbout3, tvAbout4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        tvAbout1 = findViewById(R.id.tvAbout1);
        tvAbout1.setMovementMethod(LinkMovementMethod.getInstance());
        tvAbout2 = findViewById(R.id.tvAbout2);
        tvAbout2.setMovementMethod(LinkMovementMethod.getInstance());
        tvAbout3 = findViewById(R.id.tvAbout3);
        tvAbout3.setMovementMethod(LinkMovementMethod.getInstance());
        tvAbout4 = findViewById(R.id.tvAbout4);
        tvAbout4.setMovementMethod(LinkMovementMethod.getInstance());
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.about));

    }
}

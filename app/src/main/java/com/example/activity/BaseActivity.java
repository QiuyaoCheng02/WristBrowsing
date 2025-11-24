package com.example.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        FloatingActionButton fabTree = findViewById(R.id.fabTree);
        fabTree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the Tree Activity
                Log.d("BaseActivity", "FloatingActionButton clicked");

                Intent intent = new Intent(BaseActivity.this, treeActivity.class);
                startActivity(intent);
            }
        });
    }
}

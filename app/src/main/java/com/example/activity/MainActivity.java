package com.example.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import com.example.activity.databinding.ActivityMainBinding;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import fragment.searchFragment;


public class MainActivity extends BaseActivity {
    //private Button treeButton;
    private Button connectButton;
    private FloatingActionButton treeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = findViewById(R.id.connectButton);
        treeButton=findViewById(R.id.fabTree);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里执行跳转到 ClientActivity 的操作
                Intent clientServiceIntent = new Intent(MainActivity.this, PhoneConnectionService.class);
                startService(clientServiceIntent);
            }
        });

        treeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里执行跳转到 ClientActivity 的操作
                Intent treeIntent = new Intent(MainActivity.this, treeActivity.class);
                startActivity(treeIntent);
            }
        });

        // Load SearchFragment into the container
        if (savedInstanceState == null) {
            searchFragment searchFragment = new searchFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.search_fragment_container, searchFragment);
            fragmentTransaction.commit();
        }

    }

}
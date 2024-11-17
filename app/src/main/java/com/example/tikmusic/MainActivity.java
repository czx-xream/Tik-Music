package com.example.tikmusic;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ExploreFragment())
                .commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_explore) {
                //item.setIcon(R.drawable.ic_headphone_select);
                selectedFragment = new ExploreFragment();
            } else if (item.getItemId() == R.id.nav_playmusic) {
                //item.setIcon(R.drawable.ic_music_play_select);
                selectedFragment = new PlayMusicFragment();
            } else if (item.getItemId() == R.id.nav_userinfo) {
                //item.setIcon(R.drawable.ic_user_select);
                selectedFragment = new UserInfoFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

    }
}

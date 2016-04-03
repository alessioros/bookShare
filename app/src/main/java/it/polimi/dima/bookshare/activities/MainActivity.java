package it.polimi.dima.bookshare.activities;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.Profile;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.fragments.HomeFragment;
import it.polimi.dima.bookshare.fragments.LibraryFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG="MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("BookShare");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(getIntent().getExtras()!=null){

            Bundle extras = getIntent().getExtras();

            if(extras.get("redirect").equals("library")){

                Fragment fragment = LibraryFragment.newInstance();

                getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();

                getSupportActionBar().setTitle("Library");

            }
        }else{

            Fragment fragment = HomeFragment.newInstance();

            getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();

            getSupportActionBar().setTitle("BookShare");

            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(MainActivity.this, R.color.zxing_transparent)));

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        ComponentName cn = new ComponentName(this, SearchActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));


        Profile userProfile = Profile.getCurrentProfile();

        TextView username = (TextView) findViewById(R.id.username);

        Typeface aller = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");

        username.setTypeface(aller);
        username.setText(userProfile.getName());

        CircularImageView userImage = (CircularImageView) findViewById(R.id.userImage);

        try {

            Uri imageUri = userProfile.getProfilePictureUri(300, 300);

            Picasso.with(getApplicationContext()).load(imageUri).into(userImage);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {

            Fragment fragment = HomeFragment.newInstance();

            getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();

            getSupportActionBar().setTitle("Home");

        } else if (id == R.id.library) {

            Fragment fragment = LibraryFragment.newInstance();

            getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();

            getSupportActionBar().setTitle("Library");

        } else if (id == R.id.settings) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}

package it.polimi.dima.bookshare.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerTask;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerType;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private ManageUser manageUser;
    private User user;
    private TextView askLocation;
    private TextView rememberChange;
    private LatLng user_city;
    private Button confirmB;
    private Button changeB;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String languageToLoad = "en_US";
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;

        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Verify Location");
        setSupportActionBar(toolbar);


        manageUser = new ManageUser(MapsActivity.this);

        user = manageUser.getUser();

        askLocation = (TextView) findViewById(R.id.ask_location);
        rememberChange = (TextView) findViewById(R.id.remember);
        changeB = (Button) findViewById(R.id.find_location_button);
        confirmB = (Button) findViewById(R.id.confirm_button);

        Typeface aller = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");
        askLocation.setTypeface(aller);
        rememberChange.setTypeface(aller);
        changeB.setTypeface(aller);
        confirmB.setTypeface(aller);

        confirmB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(i);

                new DynamoDBManagerTask(MapsActivity.this, user).execute(DynamoDBManagerType.INSERT_USER);
                manageUser.saveUser(user);
                manageUser.setRegistered(true);

                MapsActivity.this.finish();
            }
        });

        changeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(MapsActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);

                } catch (GooglePlayServicesRepairableException e) {

                } catch (GooglePlayServicesNotAvailableException e) {

                }
            }
        });

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();

        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

            }

            @Override
            public void onError(Status status) {

                Log.i(TAG, "An error occurred: " + status);
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String fromSet = "";

        try {
            fromSet = getIntent().getExtras().getString("from_settings", null);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        // if the user has specified a city on facebook or not
        if (user.getCity() != null && fromSet.equals("")) {

            confirmB.setVisibility(View.VISIBLE);
            askLocation.setText(getResources().getText(R.string.ask_loc_text));

            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.US);

            try {
                List<Address> addresses = geocoder.getFromLocationName(user.getCity(), 1);

                user_city = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());

                marker = mMap.addMarker(new MarkerOptions().position(user_city).title(user.getCity() + "," + user.getCountry()));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user_city, 10.0f));

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (fromSet.equals("yes")) {

            getSupportActionBar().setTitle(getResources().getString(R.string.change_loc));
            askLocation.setText(getResources().getText(R.string.ask_loc_change));
            confirmB.setText(getResources().getText(R.string.confirm_loc));
            changeB.setText(getResources().getText(R.string.change_loc));
            rememberChange.setVisibility(View.INVISIBLE);

            LatLng userLatLng = new LatLng(user.getLatitude(), user.getLongitude());

            marker = mMap.addMarker(new MarkerOptions().position(userLatLng).title(user.getCity() + "," + user.getCountry()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 10.0f));

        } else {

            askLocation.setText(getResources().getText(R.string.ask_loc_search));
            confirmB.setVisibility(View.INVISIBLE);
            changeB.setText(getResources().getText(R.string.search_loc));
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.US);

                try {
                    List<Address> addresses = geocoder.getFromLocation(place.getLatLng().latitude, place.getLatLng().longitude, 1);

                    user.setCity(addresses.get(0).getLocality());
                    user.setCountry(addresses.get(0).getCountryName());

                } catch (IOException e) {
                    e.printStackTrace();
                }

                user.setLongitude(place.getLatLng().longitude);
                user.setLatitude(place.getLatLng().latitude);

                try {
                    marker.remove();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                marker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(user.getCity() + "," + user.getCountry()));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 10.0f));
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                confirmB.setVisibility(View.VISIBLE);
                askLocation.setText(getResources().getText(R.string.ask_loc_text));

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);

                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}

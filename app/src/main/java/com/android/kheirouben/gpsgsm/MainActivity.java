package com.android.kheirouben.gpsgsm;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
//import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
//import org.osmdroid.views.MapView;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
//import org.osmdroid.views.overlay.Marker;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    public MapView mapView;
    private MapController mapController;
    public static final GeoPoint ALGER = new GeoPoint(36.7525989, 3.055752);
    public static final GeoPoint AinSalah = new GeoPoint(27.192777, 2.485146);
    public GeoPoint imHere;
    ArrayList<OverlayItem> anotherOverlayItemArray;
    ArrayList<OverlayItem> mMyLocationOverlay;
    public Button localize;
    LocationManager locationManager;
    static Context ctx;
    private LocationTracker locationTracker;
    private Location mLocation;
    double lat, lon;
    static GeoPoint tPerson;
    static GeoPoint tPerson1;
    IMapController mapViewController;
    FloatingActionButton fab;
    FloatingActionButton fab2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplicationContext();
        //Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);
        hideActionbar();
        initMap(5, AinSalah);

        /*
        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
        */

        locationTracker = new LocationTracker(getApplicationContext());
        mLocation = locationTracker.getLocation();

        if (mLocation != null) {
            lat = mLocation.getLatitude();
            lon = mLocation.getLongitude();

            imHere = new GeoPoint(lat, lon);
            //System.out.print("im here" + lat);
            plotMarker(imHere);
        }


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getIntent().getExtras() != null) {
                    String sms = getIntent().getStringExtra("msgBody");
                    tPerson = smsToGeoPoint(sms);
                    plotMarker(tPerson);
                    mapViewController.animateTo(tPerson);
                } else toast("No data");


            }
        });

        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //startActivity(new Intent(MainActivity.this, SmsViewer.class));
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.activity_sms_viewer);
                dialog.setTitle("Inbox:");

                ListView lv = (ListView) dialog.findViewById(R.id.Smslist);
                if (inbox() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, inbox());
                    lv.setAdapter(adapter);
                }
                dialog.show();
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                        String selection = arg0.getItemAtPosition(position).toString();
                        //toast(selection.substring(21));

                        if (selection.contains("gps")) {
                            tPerson1 = smsToGeoPoint(selection.substring(21));
                            plotMarker(tPerson1);
                            mapViewController.animateTo(tPerson1);
                            dialog.dismiss();
                        }
                    }
                });
            }

        });


    }


    public static void toast(String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
        //plotMarker((smsToGeoPoint(msg));
    }

    /*
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

    }
*/
    public void plotMarker(GeoPoint geoPoint) {
        anotherOverlayItemArray = new ArrayList<OverlayItem>();
        anotherOverlayItemArray.add(new OverlayItem("*", null, geoPoint));
        ItemizedIconOverlay<OverlayItem> anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(this, anotherOverlayItemArray, null);
        mapView.getOverlays().add(anotherItemizedIconOverlay);
        ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(this);
        mapView.getOverlays().add(myScaleBarOverlay);
        mapView.invalidate();
    }

    private void initMap(int zoom, GeoPoint center) {
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(false);

        mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
        mapViewController = mapView.getController();
        mapViewController.setZoom(zoom);
        mapViewController.setCenter(center);

    }

    private void hideActionbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    public void requestPosition() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:0669255398"));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(callIntent);
    }

    public static GeoPoint smsToGeoPoint(String smsBody) {

        // gps,36.7525989,3.055752
        String[] latlong = smsBody.split(",");
        GeoPoint trackedPerson = new GeoPoint(Double.parseDouble(latlong[1]), Double.parseDouble(latlong[2]));

        return trackedPerson;
    }

    public ArrayList<String> inbox() {
        ArrayList<String> sms = new ArrayList<String>();
        Uri uri = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(uri, new String[]{"_id", "address", "date", "body"}, null, null, null);
        cursor.moveToFirst();
        int i = 0;
        while (cursor.moveToNext() || i <= 5) {
            String num = cursor.getString(1);
            String body = cursor.getString(3);
            sms.add(" " + num + "\n Sms :" + body + "\n");
            i++;
        }
        return sms;
    }


    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(SmsListener);
    }
}

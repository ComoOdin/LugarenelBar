package com.matic.lugarenelbar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.matic.lugarenelbar.com.matic.lugarenelbar.utils.BackendComunication;
import com.matic.lugarenelbar.com.matic.lugarenelbar.utils.Constants;
import com.matic.lugarenelbar.com.matic.lugarenelbar.utils.MapWrapperLayout;
import com.matic.lugarenelbar.com.matic.lugarenelbar.utils.OnInfoWindowElemTouchListener;

import java.util.LinkedList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    private ViewGroup myContentsView;

    private static final int GPS_CODE = 100;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    private ImageView ico;
    private GoogleMap mapa;
    private SupportMapFragment apmap;
    private Marker markerPosicionActual;
    private Button infoButton;
    private OnInfoWindowElemTouchListener infoButtonListener;

    private TextView lbl_pd_bareCercanos;
    private TextView lbl_pd_message;
    private FloatingActionButton fabinfo;
    private FloatingActionButton fabmilocation;
    private ProgressDialog pd = null;
    private LinearLayout layoutInfo;
    private LinearLayout layoutradiobutton;
    private RadioButton rbmenora1000;
    private RadioButton rbmayora1000;
    private MenuItem actionfiltro;

    Animation animationfadein;

    List<Bar> listaBares;

    double current_latitud = 0.0;
    double current_longitud = 0.0;
    int distancia;
    double lat_old = -31.4227466;
    double long_old = -64.1848051;
    static final Double RadioH = 6371.00;
    int item_selection = 1;
    int valor_rango = 1000;
    int rango_select = 1;
    String name;
    int markerImg = 0;
    private static int markerMask = R.layout.custom_view_marker_milocation;


    //variables para infowindowsadapert(infomarcador)
    String snippettel = "";
    String estado = "";

    //variables para el logo
    private Bitmap loadedImage;
    private String imageHttpAddress = "http://jonsegador.com/wp-content/apezzg.png";

    //para obtener la ubicacion
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    //boolean para saber si hace falta hacer el primer zoom o si no hay que seguir moviendo la camara
    private boolean cameraMoved = false;

    //para el boton Borrar en los infoWindow
    private MapWrapperLayout mapWrapperLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        configGPS();

        /*Bundle extras = getIntent().getExtras();
        procesarDatosDeBares(extras);*/


        lbl_pd_bareCercanos = (TextView) findViewById(R.id.lbl_pb_baresCercanos);
        lbl_pd_message = (TextView) findViewById(R.id.lbl_pb_message);
        layoutInfo = (LinearLayout) findViewById(R.id.layout_view_info);
        layoutradiobutton = (LinearLayout) findViewById(R.id.linearLayout_radiobutton);
        rbmayora1000 = (RadioButton) findViewById(R.id.rb_mayora1000);
        rbmenora1000 = (RadioButton) findViewById(R.id.rb_menora1000);
        rbmenora1000.setChecked(true);

        actionfiltro = (MenuItem) findViewById(R.id.action_filtro);

        animationfadein = AnimationUtils.loadAnimation(this, R.anim.fade_in);


        //GoogleMaps
        cargarGoogleMaps();


        lbl_pd_bareCercanos.setVisibility(View.INVISIBLE);
        lbl_pd_message.setVisibility(View.INVISIBLE);


        //FLOATING BUTTON INFO
        fabinfo = (FloatingActionButton) findViewById(R.id.fab_info);
        fabinfo.setVisibility(View.INVISIBLE);
        fabinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Snackbar.make(view, "BUSCAR", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                mostrarOcultarInfo();
            }
        });


        fabmilocation = (FloatingActionButton) findViewById(R.id.fab_milocation);
        fabmilocation.setVisibility(View.INVISIBLE);
        fabmilocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                myLocation();
            }
        });


/*
        //CUANDO LE HAGO CLICK A UN MARCADOR ROJO O VERDE
        mapa.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //Cuando toco el marcador seteo las  nuevas coordenadas para calcular las distancia
                lat_old = marker.getPosition().latitude;
                long_old = marker.getPosition().longitude;
                name = marker.getTitle();
                ocultarLayouts();

                centerWindowsInfo(marker);
                return true;


            }

        });
*/

        //TODO muestra info del bar
/*
        mapa.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(MainActivity.this, "ABRE INFORMACION DEL BAR", Toast.LENGTH_SHORT).show();
            }
        });
*/

        mapa.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                ocultarLayouts();
            }
        });


        //Para registrarse en gcm
        BackendComunication.getInstance().registerInBackground(this);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Bundle extras = intent.getExtras();
        if (null == extras) return;

        procesarDatosDeBares(extras);

    }

    public void borrarDato(View view) {
        String nombre = "";

        BackendComunication.getInstance().borrarBar(nombre);
    }


    /**
     * Procesa los datos de bares, extrayendolos de los extras (Bundle) de un Intent y guardandolos
     * en una variable de la clase
     *
     * @param extras el Bundle de un Intent
     */
    private void procesarDatosDeBares(Bundle extras) {
        if (null == extras) return;
        String baresString = extras.getString("bares");

        if (null == baresString) {
            return;
        }


        // Si llegamos aca, hay datos

        String mensaje = "";

        listaBares = new LinkedList<>();
        String[] bares = baresString.split(Constants.OBJECT_SEPARATOR);
        for (String s : bares) {
            String[] barString = s.split(Constants.FIELD_SEPARATOR);
            Bar nuevoBar = new Bar(Integer.parseInt(barString[0]), barString[1], barString[2], barString[3], barString[4], barString[5], Integer.parseInt(barString[6]), Double.parseDouble(barString[7]), Double.parseDouble(barString[8]), barString[9], null, Integer.parseInt(barString[11]));
            listaBares.add(nuevoBar);
            mensaje += nuevoBar.getNombreFantasia() + ", ";
        }

        // Este codigo es solo para mostrar que llegaron datos
        mensaje = "Llegaron datos de " + listaBares.size() + " bares, son: " + mensaje;

        ico = (ImageView) myContentsView.findViewById(R.id.ico_bar);

        createAllMarkers();


        Log.i(Constants.TAG, mensaje);

    }


    public void borraDato(View view) {
        //ESTE METODO ES EL DEL BOTON QUE DEBERIA LLAMAR AL BORRAR BARES DEL BACKEND COMUNICACION
    }


    public void cargarGoogleMaps() {
        //Google maps Settings
        apmap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_container);


        apmap.getView().setVisibility(View.INVISIBLE);
        mapa = apmap.getMap();
        //Establecer tipo de mapa y activo mi ubicacion
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        myContentsView = (ViewGroup) getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        this.infoButton = (Button) myContentsView.findViewById(R.id.button);

        // MapWrapperLayout initialization
        // 39 - default marker height
        // 20 - offset between the default InfoWindow bottom edge and it's content bottom edge
        mapWrapperLayout.init(mapa, getPixelsFromDp(this, 39 + 20));

        // Setting custom OnTouchListener which deals with the pressed state
        // so it shows up
        this.infoButtonListener = new OnInfoWindowElemTouchListener(infoButton,
                getResources().getDrawable(R.drawable.abc_list_pressed_holo_dark),
                getResources().getDrawable(R.drawable.abc_list_pressed_holo_light)) {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                // variable local final para poder acceder al valor de marker desde dentro de la
                // clase anonima. Si no es final no se puede acceder desde una clase anonima.
                final Marker finalMarker = marker;


                //BackendComunication.getInstance().borrarBar(finalMarker.getPosition());


                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Confirmación");
                builder.setMessage("¿Está seguro que desea eliminar el bar?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                BackendComunication.getInstance().borrarBar(finalMarker.getPosition());
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        };
        this.infoButton.setOnTouchListener(infoButtonListener);

        mapa.setInfoWindowAdapter(new MyInfoWindowAdapter());
    }


    //NOTA: pedir los permisos en oncreate y luego ejecutar el config, VER COMO HACER ESO
    public void configGPS() {


        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled) {
            // we have access to PlayServices, but user has disabled location visibility --> alert him
            dialogActivarGPS();

        }


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


        //el codigo viejo de camu:

        /*
        //permisos
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }


        pd = ProgressDialog.show(this, getString(R.string.pd_titulo), getString(R.string.pd_message));

        LocationManager mLocationManager;
        LocationListener mLocationListener;
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new MyLocationListener();

        Criteria criteria = new Criteria();
        String provider = mLocationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (mLocationManager.isProviderEnabled("network") == false && mLocationManager.isProviderEnabled("gps") == false) {

            dialogActivarGPS();
            mLocationManager.requestLocationUpdates(mLocationManager.NETWORK_PROVIDER, 30000, 50, mLocationListener);

            if (pd.isShowing()) {
                pd.dismiss();
            }
        } else if (mLocationManager.isProviderEnabled("gps") == true) {
            mLocationManager.requestLocationUpdates(mLocationManager.GPS_PROVIDER, 30000, 50, mLocationListener);
            Toast.makeText(this, "Esta usando GPS", Toast.LENGTH_SHORT).show();

            Location lastKnownLocation = mLocationManager.getLastKnownLocation(mLocationManager.GPS_PROVIDER);
            if (null != lastKnownLocation) {
                current_latitud = lastKnownLocation.getLatitude();
                current_longitud = lastKnownLocation.getLongitude();
            } else {
                lastKnownLocation = mLocationManager.getLastKnownLocation(mLocationManager.NETWORK_PROVIDER);
                if (null != lastKnownLocation) {
                    current_latitud = lastKnownLocation.getLatitude();
                    current_longitud = lastKnownLocation.getLongitude();
                }
            }
        } else if (mLocationManager.isProviderEnabled("network") == true) {
            mLocationManager.requestLocationUpdates(mLocationManager.NETWORK_PROVIDER, 30000, 50, mLocationListener);
            Toast.makeText(this, "Esta usando network", Toast.LENGTH_SHORT).show();

            Location lastKnownLocation = mLocationManager.getLastKnownLocation(mLocationManager.NETWORK_PROVIDER);
            if (null != lastKnownLocation) {
                current_latitud = lastKnownLocation.getLatitude();
                current_longitud = lastKnownLocation.getLongitude();
            }
        } else {
            mLocationManager.requestLocationUpdates(provider, 3000, 10, mLocationListener);
            Toast.makeText(this, "Esta usando " + provider, Toast.LENGTH_SHORT).show();

            Location lastKnownLocation = mLocationManager.getLastKnownLocation(provider);
            if (null != lastKnownLocation) {
                current_latitud = lastKnownLocation.getLatitude();
                current_longitud = lastKnownLocation.getLongitude();
            }

        }

*/
    }

/*
    public void updateMapa(Location location) {

        double dLatitude = location.getLatitude();
        double dLongitude = location.getLongitude();
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dLatitude, dLongitude), 14.0f));
        //remuevo el marcador anterior antes de agregar el nuevo
        if (null != markerPosicionActual) {
            markerPosicionActual.remove();
        }

        createMyLocationMarker();

        if (pd.isShowing()) {
            pd.dismiss();
        }

        apmap.getView().setVisibility(View.VISIBLE);
        //fabinfo.setVisibility(View.VISIBLE); lo dejo invisible por ahora
        fabmilocation.setVisibility(View.VISIBLE);

        Bundle extras = getIntent().getExtras();
        procesarDatosDeBares(extras);


    }*/

    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
            mGoogleApiClient.disconnect();
        }
        //boolean para saber si hace falta hacer el primer zoom o si no hay que seguir moviendo la camara
        cameraMoved = false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            handleNewLocation(location);
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(current_latitud, current_longitud), 14.0f));
        }


        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);


    }

    private void handleNewLocation(Location location) {

        current_latitud = location.getLatitude();
        current_longitud = location.getLongitude();

        createMyLocationMarker();

        apmap.getView().setVisibility(View.VISIBLE);
        //fabinfo.setVisibility(View.VISIBLE); lo dejo invisible por ahora
        fabmilocation.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(Constants.TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);


    }

    /*
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(MainActivity.this, getString(R.string.gpsActivado), Toast.LENGTH_SHORT).show();
        lbl_pd_message.setVisibility(View.INVISIBLE);
        configGPS();
    }

    @Override
    public void onProviderDisabled(String provider) {

        //if (provider.equals("gps") && provider.equals("network")) {
        //    Toast.makeText(MainActivity.this, getString(R.string.gpsDesactivado), Toast.LENGTH_SHORT).show(); }

        dialogActivarGPS();


    }
    */

    /*
        public class MyLocationListener implements LocationListener {

            @Override
            public void onLocationChanged(Location loc) {
                // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
                // debido a la deteccion de un cambio de ubicacion
                current_latitud = loc.getLatitude();
                current_longitud = loc.getLongitude();
                updateMapa(loc);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(MainActivity.this, getString(R.string.gpsActivado), Toast.LENGTH_SHORT).show();
                lbl_pd_message.setVisibility(View.INVISIBLE);
                configGPS();
            }

            @Override
            public void onProviderDisabled(String provider) {

                //if (provider.equals("gps") && provider.equals("network")) {
                //    Toast.makeText(MainActivity.this, getString(R.string.gpsDesactivado), Toast.LENGTH_SHORT).show(); }

                dialogActivarGPS();


            }

        }
    */
    //evento para crear un alertdialog que te lleva a activar el gps si esta desactivado
    public void dialogActivarGPS() {
        final AlertDialog.Builder abuilder = new AlertDialog.Builder(MainActivity.this);
        abuilder.setMessage(getString(R.string.dialog_gps_message)).setCancelable(false)
                .setPositiveButton(getString(R.string.pd_si), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //POR EL LADO DEL SI

                        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //startActivity(intent);
                        startActivityForResult(intent, GPS_CODE);
                        dialog.cancel();

                    }

                })
                .setNegativeButton(getString(R.string.pd_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();


                        if (apmap.getView().getVisibility() == View.INVISIBLE) {
                            lbl_pd_message.setVisibility(View.VISIBLE);

                            lbl_pd_message.setText("No se pudo obtener su ubicación");
                        }

                    }
                });

        AlertDialog alert = abuilder.create();
        alert.setTitle(getString(R.string.dialog_gps_titulo));
        alert.show();


    }


    public double CalcularDistancias(double lat1, double long1, double lat2, double long2) {
        double radio = RadioH;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLong = Math.toRadians(long2 - long1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLong / 2) * Math.sin(dLong / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return radio * c;

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        } else if (layoutInfo.getVisibility() == View.VISIBLE) {

            layoutInfo.setVisibility(View.INVISIBLE);
            layoutInfo.clearAnimation();
        } else if (layoutradiobutton.getVisibility() == View.VISIBLE) {

            layoutradiobutton.setVisibility(View.INVISIBLE);
            layoutradiobutton.clearAnimation();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_referencia) {
            ocultarLayouts();
            mostrarOcultarInfo();
            return true;
        }

        if (id == R.id.action_rango) {

            layoutInfo.setVisibility(View.INVISIBLE);
            layoutInfo.clearAnimation();

            if (layoutradiobutton.getVisibility() == View.INVISIBLE) {
                layoutradiobutton.setVisibility(View.VISIBLE);
                layoutradiobutton.startAnimation(animationfadein);

            } else {
                layoutradiobutton.setVisibility(View.INVISIBLE);
                layoutradiobutton.clearAnimation();
            }

        }


        if (id == R.id.action_filtro) {

            layoutInfo.setVisibility(View.INVISIBLE);
            layoutInfo.clearAnimation();
            layoutradiobutton.setVisibility(View.INVISIBLE);
            layoutradiobutton.clearAnimation();

            if (item_selection == 1) {
                createAvailableMarkers();

                Toast.makeText(MainActivity.this, getString(R.string.str_availableBares), Toast.LENGTH_SHORT).show();

                item.setIcon(R.drawable.ic_action_filter_selected);
                item_selection = 2;

            } else {

                createAllMarkers();
                Toast.makeText(MainActivity.this, getString(R.string.str_allBares), Toast.LENGTH_SHORT).show();

                item.setIcon(R.drawable.ic_action_filter_outline);
                item_selection = 1;
            }


            // return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.bares_cercanos) {
            Intent act = new Intent(this, MainActivity.class);
            startActivity(act);
            finish();

            // Handle the camera action
        } else if (id == R.id.buscar_bares) {

            //ver xq no anda
            Intent act = new Intent(this, ListadoBaresActivity.class);
            startActivity(act);
            //finish();


        } else if (id == R.id.suscribirse) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/html");
            intent.putExtra(Intent.EXTRA_EMAIL, "lugarenelbar@gmail.com");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Modificación en datos de bar");
            startActivity(Intent.createChooser(intent, "Enviar Email"));


        } else if (id == R.id.admin) {

            Intent act = new Intent(this, LoginActivity.class);
            startActivity(act);


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    configGPS();


                } else {
                    Toast.makeText(MainActivity.this, "PERMISO RECHAZADO, NO VA A PODER OBTENER SU UBICACIÓN.\n" +
                            "Si desea activar nuevamente los permisos diríjase a administración de aplicaciones.", Toast.LENGTH_LONG).show();
                }

                return;
            }


        }
    }


    //CLASE QUE MODIFICA LA INFORMACION DEL MARKER
    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

/*
        MyInfoWindowAdapter() {
            myContentsView = (ViewGroup)getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }
        */


        @Override
        public View getInfoContents(Marker marker) {
            //setInfoBares(marker);
            //ico.setImageResource(marker.getIcon());
            Typeface myfont = Typeface.createFromAsset(getAssets(), "Anjasmoro.ttf");
            TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
            tvTitle.setText(marker.getTitle());
            tvTitle.setTypeface(myfont);
            TextView tvSnippet_dire = ((TextView) myContentsView.findViewById(R.id.snippet_dire));
            tvSnippet_dire.setText(marker.getSnippet());
            TextView tv_snippet_tel = (TextView) myContentsView.findViewById(R.id.snippet_tel);
            tv_snippet_tel.setText(snippettel);
            TextView tv_snippet_distancia = (TextView) myContentsView.findViewById(R.id.snippet_distancia);
            tv_snippet_distancia.setText(distancia + " mts.");
            TextView tv_snippet_estado = (TextView) myContentsView.findViewById(R.id.snippet_estado);
            tv_snippet_estado.setText(estado);

            infoButtonListener.setMarker(marker);

            // We must call this to set the current marker and infoWindow references
            // to the MapWrapperLayout
            mapWrapperLayout.setMarkerWithInfoWindow(marker, myContentsView);

            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

    }


    //CUSTOM MARKER VIEW - ESTOS DOS METODOS SON PARA LOS MARCADORES DISPONIBLE Y NO DISP
    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(markerImg, null);

        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.profile_image);
        markerImageView.setImageResource(resId);

        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }


    public void mostrarOcultarInfo() {
        layoutradiobutton.setVisibility(View.INVISIBLE);
        layoutradiobutton.clearAnimation();

        if (layoutInfo.getVisibility() == View.VISIBLE) {
            //layoutInfo.startAnimation(animationfadein);
            layoutInfo.setVisibility(View.INVISIBLE);
            layoutInfo.clearAnimation();


        } else {

            layoutInfo.startAnimation(animationfadein);
            layoutInfo.setVisibility(View.VISIBLE);
        }

    }

    public void eventoRadiobuttons(View view) {

        switch (view.getId()) {

            case R.id.rb_menora1000:
                valor_rango = 1000;
                if (item_selection == 1) {
                    createAllMarkers();
                } else {
                    createAvailableMarkers();
                }

                rango_select = 1;
                break;

            case R.id.rb_mayora1000:

                valor_rango = 5000;

                if (item_selection == 1) {
                    createAllMarkers();
                } else {
                    createAvailableMarkers();
                }

                rango_select = 2;
                break;

        }

        createMyLocationMarker();

        layoutradiobutton.setVisibility(View.INVISIBLE);
        layoutradiobutton.clearAnimation();


    }


    public void mostrarBaresDisponibles() {
        //DISPONIBLES


        if (((int) Math.rint(CalcularDistancias(current_latitud, current_longitud, -31.4272182, -64.1866663) * 1000)) <= valor_rango) {
            mapa.addMarker(new MarkerOptions()
                    .position(new LatLng(-31.4272182, -64.1866663))
                    .title("Blow Club Pub & Restaurant")
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.logo_default))));
        }


    }


    //este metodo es para ocultar las ventanas emergentes de info y radiobutton
    public void ocultarLayouts() {
        if (layoutInfo.getVisibility() == View.VISIBLE) {

            layoutInfo.setVisibility(View.INVISIBLE);
            layoutInfo.clearAnimation();
        } else if (layoutradiobutton.getVisibility() == View.VISIBLE) {

            layoutradiobutton.setVisibility(View.INVISIBLE);
            layoutradiobutton.clearAnimation();
        }
    }

    public void createAllMarkers() {


        mapa.clear();
        createMyLocationMarker();
        for (Bar bar : listaBares) {

            //si la cantidad de lugares es !=0 , el marcador va a ser de color verde, sino rojo(es para el metodo: getMarkerBitmapFromView)
            if (bar.getMesasLibres() != 0) {
                markerImg = R.layout.custom_view_marker_disp;

            } else {
                markerImg = R.layout.custom_view_marker_nodisp;

            }

            distancia = ((int) Math.rint(CalcularDistancias(current_latitud, current_longitud, bar.getLat(), bar.getLon()) * 1000));

            if (distancia <= valor_rango) {
                mapa.addMarker(new MarkerOptions()
                        .position(new LatLng(bar.getLat(), bar.getLon()))
                        .title(bar.getNombreFantasia())
                        .snippet("Dirección: " + bar.getCalle() + ", " + bar.getNroCalle() + ", " + bar.getCiudad() + ", " + bar.getPais() + "\n\nTeléfono: " + bar.getTelefono() + "\n\nCantidad mesas: " + bar.getMesasLibres())
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.logo_default))));

                ico.setImageResource(R.drawable.logo_default);
                //pedir url del logo, uso el marker.icon
            }//fin del if del rango

            Log.i(Constants.TAG, "bar: " + bar.getNombreFantasia() + " distancia: " + distancia + "current lat: " + current_latitud);
        }
    }

    public void createAvailableMarkers() {

        mapa.clear();
        createMyLocationMarker();
        for (Bar bar : listaBares) {

            //si la cantidad de lugares es !=0 , el marcador va a ser de color verde, sino rojo(es para el metodo: getMarkerBitmapFromView)
            if (bar.getMesasLibres() != 0) {
                distancia = ((int) Math.rint(CalcularDistancias(current_latitud, current_longitud, bar.getLat(), bar.getLon()) * 1000));

                if (distancia <= valor_rango) {
                    markerImg = R.layout.custom_view_marker_disp;
                    mapa.addMarker(new MarkerOptions()
                            .position(new LatLng(bar.getLat(), bar.getLon()))
                            .title(bar.getNombreFantasia())
                            .snippet("Dirección: " + bar.getCalle() + ", " + bar.getNroCalle() + ", " + bar.getCiudad() + ", " + bar.getPais() + "\n\nTeléfono: " + bar.getTelefono() + "\n\nCantidad mesas: " + bar.getMesasLibres())
                            .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.logo_default))));

                    ico.setImageResource(R.drawable.logo_default);
                }

            }

            //pedir url del logo, uso el marker.icon
        }
    }

    public void createMyLocationMarker() {
        LatLng latLng = new LatLng(current_latitud, current_longitud);

        markerImg = R.layout.custom_view_marker_milocation;

        //primero remuevo el marker viejo
        if (null != markerPosicionActual) {
            markerPosicionActual.remove();
        }

        //crear el marker de posicion actual y agregarlo
        markerPosicionActual = mapa.addMarker(new MarkerOptions()
                .position(latLng)
                .title(getString(R.string.str_marker_currentLocation))
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.logo_milocation))));

        //para que no se muestre infoWindow en el marker de posicion actual, y la camara se mueva
        //al centro del infoWindow de los otros markers
        mapa.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {


                //centerWindowsInfo(marker);

                if (!marker.equals(markerPosicionActual)) {
                    int yMatrix = 600, xMatrix = 5;

                    DisplayMetrics metrics1 = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics1);
                    switch (metrics1.densityDpi) {
                        case DisplayMetrics.DENSITY_LOW:
                            yMatrix = 80;
                            xMatrix = 20;
                            break;
                        case DisplayMetrics.DENSITY_MEDIUM:
                            yMatrix = 100;
                            xMatrix = 25;
                            break;
                        case DisplayMetrics.DENSITY_HIGH:
                            yMatrix = 150;
                            xMatrix = 30;
                            break;
                        case DisplayMetrics.DENSITY_XHIGH:
                            yMatrix = 200;
                            xMatrix = 20;
                            break;
                        case DisplayMetrics.DENSITY_XXHIGH:
                            yMatrix = 500;
                            xMatrix = 1;
                            //Toast.makeText(MainActivity.this, "usa xxhigh", Toast.LENGTH_SHORT).show();
                            break;
                    }

                    Projection projection = mapa.getProjection();
                    LatLng latLng = marker.getPosition();
                    Point point = projection.toScreenLocation(latLng);
                    Point point2 = new Point(point.x + xMatrix, point.y - yMatrix);

                    LatLng point3 = projection.fromScreenLocation(point2);
                    CameraUpdate zoom1 = CameraUpdateFactory.newLatLng(point3);
                    mapa.animateCamera(zoom1);

                    marker.showInfoWindow();
                } else {
                    mapa.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                }
                return true;
            }
        });


        //mover la camara
        if (!cameraMoved) {
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0f));
            cameraMoved = true;
        }

    }

    public void centerWindowsInfo(Marker marker) {
        int yMatrix = 600, xMatrix = 5;

        DisplayMetrics metrics1 = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics1);
        switch (metrics1.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                yMatrix = 80;
                xMatrix = 20;
                Toast.makeText(MainActivity.this, "usa low", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                yMatrix = 100;
                xMatrix = 25;
                Toast.makeText(MainActivity.this, "usa medium", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_HIGH:
                yMatrix = 150;
                xMatrix = 30;
                Toast.makeText(MainActivity.this, "usa high", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                yMatrix = 200;
                xMatrix = 20;
                Toast.makeText(MainActivity.this, "usa xhigh", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                yMatrix = 500;
                xMatrix = 1;
                Toast.makeText(MainActivity.this, "usa xxhigh", Toast.LENGTH_SHORT).show();
                break;
        }

        Projection projection = mapa.getProjection();
        LatLng latLng = marker.getPosition();
        Point point = projection.toScreenLocation(latLng);
        Point point2 = new Point(point.x + xMatrix, point.y - yMatrix);

        LatLng point3 = projection.fromScreenLocation(point2);
        CameraUpdate zoom1 = CameraUpdateFactory.newLatLng(point3);
        //mapa.animateCamera(zoom1);
        //marker.showInfoWindow();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_CODE:
                cargarGoogleMaps();
                Log.i(Constants.TAG, "hay " + listaBares.size() + " bares en la lista en memoria");
                createAllMarkers();

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (location != null) {
                    handleNewLocation(location);
                    mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(current_latitud, current_longitud), 14.0f));
                }

                break;


        }
    }


    public void myLocation() {
        /*
        double lat = 0.0, lon = 0.0;
        LocationManager mLocationManager;
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = mLocationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (mLocationManager.isProviderEnabled("network") == false && mLocationManager.isProviderEnabled("gps") == false) {
            Toast.makeText(MainActivity.this, "GPS está desactivado ", Toast.LENGTH_SHORT).show();


        } else if (mLocationManager.isProviderEnabled("network") == true) {
            Location location = mLocationManager.getLastKnownLocation(mLocationManager.NETWORK_PROVIDER);
            lat = location.getLatitude();
            lon = location.getLongitude();

        } else {
            Location location = mLocationManager.getLastKnownLocation(provider);
            lat = location.getLatitude();
            lon = location.getLongitude();

        }
        */

        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(current_latitud, current_longitud), 14.0f));


    }


    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

}

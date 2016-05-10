package com.matic.lugarenelbar;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.model.LatLng;
import com.matic.lugarenelbar.com.matic.lugarenelbar.utils.BackendComunication;
import com.matic.lugarenelbar.com.matic.lugarenelbar.utils.Constants;

import android.location.Address;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class NuevoBarActivity extends AppCompatActivity implements LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FloatingActionButton fabconfirm;
    private EditText mNombre_legal;
    private EditText mNombre_fantasia;
    private EditText mCalle;
    private EditText mNumeroCalle;
    private EditText mCiudad;
    private EditText mPais;
    private EditText mTelefono;

    private double mlatitud = 0.0;
    private double mlongitud = 0.0;

    private double current_latitud = 0.0;
    private double current_longitud = 0.0;

    private ProgressDialog pd;

    //variables para subir logo
    private String APP_DIRECTORY = "LugarenelBar/";
    private String MEDIA_DIRECTORY = APP_DIRECTORY + "media";
    private String TEMPORAL_PICTURE_NAME = "temporal.jpg";
    private final int PHOTO_CODE = 100;
    private final int SELECT_LOGO = 200;

    private ImageView imageView;
    private Button btnUpload_logo;

    //formateo de texto
    private String txtCalle = "";
    private String txtNumero = "";

    //AlertDialog definido aca para llamar dismiss en onDestroy y evitar WindowLeaked
    AlertDialog alert;


    GoogleCloudMessaging gcm;
    Context context;
    String regid;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_bar);
        //activar boton back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mNombre_legal = (EditText) findViewById(R.id.nombre_legal_bar);
        mNombre_fantasia = (EditText) findViewById(R.id.nombre_fantasia_bar);
        mCalle = (EditText) findViewById(R.id.calle);
        mNumeroCalle = (EditText) findViewById(R.id.numero_calle);
        mCiudad = (EditText) findViewById(R.id.ciudad);
        mPais = (EditText) findViewById(R.id.pais);
        mTelefono = (EditText) findViewById(R.id.telefono);
        imageView = (ImageView) findViewById(R.id.imageView_logo);

        btnUpload_logo = (Button) findViewById(R.id.btn_uploadlogo);
        btnUpload_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = {"Tomar Foto", "Elegir de Galeria", "Cancelar"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(NuevoBarActivity.this);
                builder.setTitle("Elija su opción: ");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int seleccion) {
                        if (options[seleccion] == "Tomar Foto") {
                            openCamara();
                        } else if (options[seleccion] == "Elegir de Galeria") {

                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Selecciona imagen"), SELECT_LOGO);

                        } else {
                            dialog.cancel();
                            dialog.dismiss();
                        }
                    }

                });

                builder.show();
            }
        });


        fabconfirm = (FloatingActionButton) findViewById(R.id.fab_confirm);
        fabconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                attemptConfirm();

            }
        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void guardarDatos() {


        getLatLon();

        int id = 0;
        String nombreLegal = mNombre_legal.getText().toString();
        String nombreFantasia = mNombre_fantasia.getText().toString();
        String calle = mCalle.getText().toString();
        int nro = Integer.parseInt(mNumeroCalle.getText().toString());
        String ciudad = mCiudad.getText().toString();
        String pais = mPais.getText().toString();
        double lat = mlatitud;
        double lon = mlongitud;
        String tel = mTelefono.getText().toString();
        String urlLogo = null;
        int mesas = 0;

        Bar bar = new Bar(id, nombreLegal, nombreFantasia, pais, ciudad, calle, nro, lat, lon, tel, urlLogo, mesas);

        BackendComunication.getInstance().enviarNuevoBar(bar);

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "NuevoBar Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.matic.lugarenelbar/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "NuevoBar Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.matic.lugarenelbar/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    /**
     * Tarea asíncrona para comunicarse con el servidor mediante Sockets
     */
    /*
    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";

        MyClientTask(String addr, int port) {
            dstAddress = addr;
            dstPort = port;
        }

        private PrintWriter printwriter;
        private InputStreamReader inputStreamReader;
        private BufferedReader bufferedReader;

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;

            try {
                Log.i(Constants.TAG, "arrancando socket");
                socket = new Socket(dstAddress, dstPort);

                DataInputStream din = new DataInputStream(socket.getInputStream());
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());

                dout.writeUTF("hello from client");

                response = "sent to server ";

                String msg = din.readUTF();
                response += msg;

                socket.close();

                Log.i(Constants.TAG, "fin socket. respuesta:" + response);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = e.toString();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(Constants.TAG, "Respuesta socket: " + response);
            super.onPostExecute(result);
        }

    }
    */

    public void openCamara() {

        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        file.mkdirs();

        String path = Environment.getExternalStorageDirectory() + File.separator +
                MEDIA_DIRECTORY + File.separator + TEMPORAL_PICTURE_NAME;


        File newFile = new File(path);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
        startActivityForResult(intent, PHOTO_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PHOTO_CODE:

                String dir = Environment.getExternalStorageDirectory() + File.separator +
                        MEDIA_DIRECTORY + File.separator + TEMPORAL_PICTURE_NAME;
                decodeBitMap(dir);
             /*if (requestCode==RESULT_OK) {
                String dir=Environment.getExternalStorageDirectory()+ File.separator+
                        MEDIA_DIRECTORY+File.separator+TEMPORAL_PICTURE_NAME;
                decodeBitMap(dir);
            }*/
                break;

            case SELECT_LOGO:
                Uri path = data.getData();
                imageView.setImageURI(path);
                /*
                if (requestCode==RESULT_OK)
                {
                    Uri path=data.getData();
                    imageView.setImageURI(path);
                }
                else{Toast.makeText(this,"hubo algun error \n"+resultCode+"datos: \n"+data,Toast.LENGTH_SHORT).show()}*/
                break;


        }
    }

    private void decodeBitMap(String dir) {
        Bitmap bm;
        bm = BitmapFactory.decodeFile(dir);
        imageView.setImageBitmap(bm);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Bundle extras = intent.getExtras();
        if (null == extras) return;


        procesarRespuestaInsercionBar(extras);

    }

    private void procesarRespuestaInsercionBar(Bundle extras) {
        if (null == extras) return;

        //variable local final para poder acceder desde clases anonimas
        final Bundle finalExtras = extras;

        String respuesta = extras.getString("respuestaInsercion");
        if (null == respuesta) {
            return;
        }

        pd = ProgressDialog.show(this, "", getString(R.string.pd_message_saveSuccessful));
        //Toast.makeText(NuevoBarActivity.this, respuesta, Toast.LENGTH_SHORT).show();

        if (Constants.BAR_INSERTION_OK_MESSAGE.equals(respuesta)) {
            pd.dismiss();

            /*
            //Obtener ip y puerto para comunicarse con el servidor mediante Sockets
            String ip = finalExtras.getString("ip");
            int port = Integer.parseInt(finalExtras.getString("port"));

            //Iniciar comunicacion con el servidor mediante Sockets, para mandar imagen
            MyClientTask myClientTask = new MyClientTask(ip, port);
            myClientTask.execute();
*/

            final AlertDialog.Builder abuilder = new AlertDialog.Builder(this);
            abuilder.setMessage(getString(R.string.message_saveSuccessful)).setCancelable(false)
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent();
                            intent.setClass(getApplicationContext(), MainActivity.class);
                            intent.putExtra("saveOK", "OK");
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Log.i(Constants.TAG, "Se guardó correctamente y manda un intent al mainActivity");
                            getApplicationContext().startActivity(intent);


                            finish();
                        }
                    });

            alert = abuilder.create();
            alert.setTitle(getString(R.string.titulo_saveSuccessful));
            alert.show();


        } else if (Constants.BAR_INSERTION_ERROR_MESSAGE.equals(respuesta)) {
            pd.dismiss();

            final AlertDialog.Builder abuilder = new AlertDialog.Builder(this);
            abuilder.setMessage(getString(R.string.message_saveFailed)).setCancelable(false)
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

            alert = abuilder.create();
            alert.setTitle(getString(R.string.titulo_saveFailed));
            alert.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != alert) {
            alert.dismiss();
        }
    }

    //evento del boton
    public void miLocation(View view) {
        //pd = ProgressDialog.show(this, "Cargando ubicacion", "Espere por favor...");
        obtenerMilocation();
    }

    public void obtenerMilocation() {

        double lat = 0.0, lon = 0.0;
        LocationManager mLocationManager;
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = mLocationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (mLocationManager.isProviderEnabled("network") == false && mLocationManager.isProviderEnabled("gps") == false) {
            Toast.makeText(this, "GPS está desactivado ", Toast.LENGTH_SHORT).show();

        } else if (mLocationManager.isProviderEnabled("network") == true) {
            Location location = mLocationManager.getLastKnownLocation(mLocationManager.NETWORK_PROVIDER);
            lat = location.getLatitude();
            lon = location.getLongitude();

        } else {
            Location location = mLocationManager.getLastKnownLocation(provider);
            lat = location.getLatitude();
            lon = location.getLongitude();

        }

        //Toast.makeText(NuevoBarActivity.this, "ULTIMA POSICION CONOCIDA\nLatitud: "+lat+ "\nLongitud: "+lon, Toast.LENGTH_SHORT).show();
        //locationManager.requestLocationUpdates(provider, 20000, 0, this);
        getAddress(lat, lon);
    }

    public void getLatLon() {


        String calle = mCalle.getText().toString();
        String nro = mNumeroCalle.getText().toString();
        String ciudad = mCiudad.getText().toString();
        String pais = mPais.getText().toString();
        String location = calle + "" + nro + "," + ciudad + "," + pais;
        List<Address> addressList = null;

        Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
        try {
            addressList = geo.getFromLocationName(location, 1);
        } catch (IOException ex) {
            ex.getMessage();

        }

        Address address = addressList.get(0);
        LatLng latlon = new LatLng(address.getLatitude(), address.getLongitude());

        mlatitud = address.getLatitude();
        mlongitud = address.getLongitude();

        //Toast.makeText(this, "COORDENADAS CALLE:" + latlon, Toast.LENGTH_SHORT).show();


    }


    public void getAddress(double latitude, double longitude) {


        try {

            Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
            if (addresses.isEmpty()) {
                Toast.makeText(NuevoBarActivity.this, "esperando ubicacion", Toast.LENGTH_SHORT).show();
            } else {
                if (addresses.size() > 0) {
                    clearTxt();
                    String cadena = addresses.get(0).getAddressLine(0);
                    formatearTexto(cadena);
                    mCiudad.setText(addresses.get(0).getLocality());
                    mPais.setText(addresses.get(0).getCountryName());

                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }


    }

    private static boolean isNumeric(char caracter) {
        try {
            Integer.parseInt(String.valueOf(caracter));
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public void formatearTexto(String cadena) {

        char caracter;
        for (int i = 0; i < cadena.length(); i++) {
            caracter = cadena.charAt(i);
            if (isNumeric(caracter)) {
                txtNumero += caracter;
            } else {
                txtCalle += caracter;
            }
        }


        mCalle.setText(txtCalle);
        mNumeroCalle.setText(txtNumero);
        txtNumero = "";
        txtCalle = "";

    }

    private void clearTxt() {
        mCalle.setText("");
        mNumeroCalle.setText("");
        mCiudad.setText("");
        mPais.setText("");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "PERMISO ACEPTADO", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "PERMISO RECHAZADO", Toast.LENGTH_SHORT).show();
                }

                return;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void attemptConfirm() {

        // Reset errors.
        mNombre_fantasia.setError(null);
        mNombre_legal.setError(null);
        mCalle.setError(null);
        mNumeroCalle.setError(null);
        mTelefono.setError(null);
        mCiudad.setError(null);
        mPais.setError(null);

        // Store values at the time of the login attempt.
        String nlegal = mNombre_legal.getText().toString();
        String nfantasia = mNombre_fantasia.getText().toString();
        String ncalle = mCalle.getText().toString();
        String nnumcalle = mNumeroCalle.getText().toString();
        String ntel = mTelefono.getText().toString();
        String nciudad = mCiudad.getText().toString();
        String npais = mPais.getText().toString();

        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(nlegal)) {
            mNombre_legal.setError(getString(R.string.error_field_required));
            focusView = mNombre_legal;
            cancel = true;
        }


        if (TextUtils.isEmpty(nfantasia)) {
            mNombre_fantasia.setError(getString(R.string.error_field_required));
            focusView = mNombre_fantasia;
            cancel = true;
        }

        if (TextUtils.isEmpty(ncalle)) {
            mCalle.setError(getString(R.string.error_field_required));
            focusView = mCalle;
            cancel = true;
        }
        if (TextUtils.isEmpty(nnumcalle)) {
            mNumeroCalle.setError(getString(R.string.error_field_required));
            focusView = mNumeroCalle;
            cancel = true;
        }
        if (TextUtils.isEmpty(ntel)) {
            mTelefono.setError(getString(R.string.error_field_required));
            focusView = mTelefono;
            cancel = true;
        }
        if (TextUtils.isEmpty(nciudad)) {
            mCiudad.setError(getString(R.string.error_field_required));
            focusView = mCiudad;
            cancel = true;
        }
        if (TextUtils.isEmpty(npais)) {
            mPais.setError(getString(R.string.error_field_required));
            focusView = mPais;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            getLatLon();
            /*** SI GUARDA CORRECTAMENTE, DEBE MOSTRAR UN MENSAJE (DIALOG) Y CERRAR LA ACTIVIDAD ***/
            guardarDatos();
        }

    }


    @Override
    public void onLocationChanged(Location loc) {
        current_latitud = loc.getLatitude();
        current_longitud = loc.getLongitude();

        if (pd.isShowing()) {
            pd.dismiss();
            //Toast.makeText(NuevoBarActivity.this, "Latitud: " + current_latitud + "\nLongitud: " + current_longitud, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(NuevoBarActivity.this, "GPS ACTIVADO", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals("gps") || provider.equals("network")) {
            Toast.makeText(NuevoBarActivity.this, "GPS DESACTIVADO", Toast.LENGTH_SHORT).show();
        }


    }


}

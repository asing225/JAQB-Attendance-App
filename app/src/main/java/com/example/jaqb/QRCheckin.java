package com.example.jaqb;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class QRCheckin extends AppCompatActivity implements LocationListener {

    private static final double ALLOWED_DISTANCE = 50;
    SurfaceView surfaceView;
    CameraSource cameraSource;
    BarcodeDetector barcodeDetector;
    TextView textView;
    EditText editText;
    LocationManager locationManager;
    String data;
    Integer codeFound = 0;
    double Long;
    double Lat;
    String checkIn = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcheckin);

        editText = findViewById(R.id.codeArea);

        surfaceView = findViewById(R.id.cameraView);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE).build();

        CameraSource.Builder builder = new CameraSource.Builder(this, barcodeDetector);
        builder.setRequestedPreviewSize(640, 480);
        builder.setAutoFocusEnabled(true);
        cameraSource = builder.build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(holder);
                } catch(IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>()   {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                
                if(qrCodes.size()!=0) {

                    Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(100);
                    codeFound++;
                    barcodeDetector.setProcessor(null);
                    try {
                        checkIn = checkValues();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    runActivity(qrCodes, codeFound);
                }
            }
        });


    }

    public void codeButtonOnClick( View view) throws ParseException {
        //Not yet implemented

        Intent intent = new Intent(QRCheckin.this, IncompleteActivity.class);
        intent.putExtra("data", editText.getText().toString());
        startActivity(intent);
    }

    public String checkValues() throws ParseException {
        //Implement all the checks (location, class and time) from database here

        /*Location Check starts*/
        checkLocation();

        //Change to get long from DB function
        double randLong = generateRandomLoc(180);
        double randLat = generateRandomLoc(90);
        //****

        boolean distOk = checkDist(randLat, randLong);
        /*Location Check ends*/

        /*Time Check starts*/
        boolean timeOk = checkTime();
        /*Time Check ends*/

        if (timeOk && distOk) {
            return "CheckIn";
        } else if(timeOk && !distOk) {
            return "Get close & try again";
        } else if(!timeOk && distOk) {
            return "Too early/Too late";
        } else {
            return "Please checkin before class within 15 mins of strt time";
        }
    }

    private boolean checkTime() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String dateInString = "22-01-2015 10:20:56";
        Date date = sdf.parse(dateInString);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        long classTime = calendar.getTimeInMillis();

        calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();

        if(Math.abs(classTime - currentTime) == 1000*60*15)
            return true;
        return false;

    }

    private boolean checkDist(double randLat, double randLong) {
        final int R = 6371;

        double latDist = Math.toRadians(Lat - randLat);
        double longDist = Math.toRadians(Long - randLong);
        double a = Math.sin(latDist/2) * Math.sin(latDist/2)
                + Math.cos(Math.toRadians(randLat)) * Math.cos(Math.toRadians(Lat))
                + Math.sin(longDist/2) * Math.sin(longDist/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = R*c*1000;

        if(dist <=  ALLOWED_DISTANCE) {
            return true;
        }
        return false;
    }

    //convert to get Long & Lat from DB
    private double generateRandomLoc(int i) {
        double num = (Math.random()*i);
        double neg = Math.floor(Math.random());
        if(neg == 0) {
            num *= -1;
        }
        return num;
    }
    //***

    public boolean checkLocation(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        try {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        assert location != null;
        onLocationChanged(location);

        /* Get Location from database and compare */
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        Long = location.getLongitude();
        Lat = location.getLatitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void runActivity(SparseArray<Barcode> qrCodes, Integer codefound) {
        if(codefound == 1){
            Intent intent = new Intent(QRCheckin.this, IncompleteActivity.class);
            intent.putExtra("data", qrCodes.valueAt(0).displayValue);
            finish();
            startActivity(intent);
            codeFound = 0;
        }
    }
}

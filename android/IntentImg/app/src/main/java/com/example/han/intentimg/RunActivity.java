package com.example.han.intentimg;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by han on 2018-09-01.
 */

public class RunActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    String uploadFilePath=null;
    String uploadFileName=null;

    private GoogleMap mMap;
    int number=0;
    double longitude=0, latitude=0;
    ProgressDialog dialog=null;
    String uploadServerUri ="http://164.125.34.209:3003/upload";
    int serverResponseCode = 0;

    int RE_number=0;
    List<String> RE_name, RE_url, RE_address;
    HashMap< String,HashMap<String,String> > RE;
    HashMap<String, List<String>> RE_U;
    String currentLocationAddress=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.run_activity);

        RE_address=new ArrayList<String>();
        RE_name=new ArrayList<String>();
        RE_url=new ArrayList<String>();

        RE=new HashMap<String, HashMap<String, String>>();
        RE_U=new HashMap<String, List<String>>();

        Intent intent=new Intent(this.getIntent());
        uploadFilePath=intent.getStringExtra("FilePath");
        uploadFileName=intent.getStringExtra("FileName");
        latitude=Double.parseDouble(intent.getStringExtra("lat"));
        longitude=Double.parseDouble(intent.getStringExtra("lon"));


        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Geocoder geo=new Geocoder(this, Locale.KOREA);
        List<Address> add;

        try {
            Log.e("now","now is "+latitude);
            add= geo.getFromLocation(latitude,longitude,1);
            if(add!=null && add.size()>0){
                currentLocationAddress=add.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(uploadFilePath!=null) {
            dialog=ProgressDialog.show(RunActivity.this, "", "유사 여행지를 찾고 있어요 !", true);
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {

                            }
                        });
                        uploadFile(uploadFilePath);

                    }
                }).start();
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }finally {


            }
            return ;
        }

    }


    public int uploadFile(String sourceFileUri){
        String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 5*1024*1024;
        File sourceFile = new File(sourceFileUri);

        if(true){
            if(!sourceFile.isFile()){
                dialog.dismiss();
                Log.e("uploadFile", "Source file not exist: " + uploadFilePath + "" + uploadFileName);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
                return 0;
            }
            else{
                try {
                    FileInputStream fileInputStream=new FileInputStream(sourceFile);
                    URL url=new URL(uploadServerUri);

                    // open a HTTP connection to the URL
                    conn=(HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // allow inputs
                    conn.setDoOutput(true); // allow outputs
                    conn.setUseCaches(false); // don't user a cached copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", fileName);

                    dos=new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition:form-data; name=\"" + "longitude" + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(String.valueOf(longitude));
                    dos.writeBytes(lineEnd);

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition:form-data; name=\"" + "latitude" + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(String.valueOf(latitude));
                    dos.writeBytes(lineEnd);


                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    // create a buffer of maximum size
                    bytesAvailable=fileInputStream.available();
                    bufferSize=Math.min(bytesAvailable, maxBufferSize);
                    buffer=new byte[bufferSize];

                    // read file and write it into form
                    bytesRead=fileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable=fileInputStream.available();
                        bufferSize=Math.min(bytesAvailable, maxBufferSize);
                        bytesRead=fileInputStream.read(buffer, 0, bufferSize);
                    }

                    // send multipart form data necessary after file data
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // responses from the server (code and message)
                    serverResponseCode=conn.getResponseCode();
                    String serverResponseMessage=conn.getResponseMessage();

                    ConnectionUtils res=new ConnectionUtils();
                    res.receiveResponse(conn);

                    if(serverResponseCode==200){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RunActivity.this,"Complete!",Toast.LENGTH_SHORT).show();

                                for(int i=0;i<RE_number;++i){
                                    AddMarker(getLocationFromAddress(getApplicationContext(),RE_address.get(i)),String.valueOf(i+1)+". "+RE_name.get(i),RE_address.get(i));
                                }

                            }
                        });
                    }//end of response

                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                }catch (MalformedURLException ex) {
                    dialog.dismiss();
                    ex.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RunActivity.this, "MalformedURLException",Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                } catch (Exception e) {
                    dialog.dismiss();
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RunActivity.this, "Got Exception : see logcat", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.e("Upload file to svr Exp", "Exception: " + e.getMessage(), e);
                }

                dialog.dismiss();
                return serverResponseCode;

            }
        }

        return 0;
    }

    public void AddMarker(LatLng latLng, String title, String under ){
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(title);
        markerOptions.snippet(under);

        BitmapDrawable bitmapDrawable =(BitmapDrawable)getResources().getDrawable(R.drawable.one);
        Bitmap b=bitmapDrawable.getBitmap();
        Bitmap smallMarker=Bitmap.createScaledBitmap(b,200,200,false);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

        mMap.addMarker(markerOptions).showInfoWindow();
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder=new Geocoder(context);
        List<Address> address;
        LatLng p1=null;

        try {
            address=coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location=address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1=new LatLng(location.getLatitude(), location.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p1;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.getTitle().equals("you are here!")){
            Log.e("here","here");
            return false;
        }

        String go_name=marker.getTitle().substring(3);
        HashMap<String,String> temp=RE.get(go_name);
        Intent intent=new Intent(RunActivity.this,SliderActivity.class);
        intent.putExtra("name",go_name);
        intent.putExtra("address",marker.getSnippet());
        intent.putExtra("imgpath",uploadFilePath);
        intent.putStringArrayListExtra("url",(ArrayList<String> )RE_U.get(go_name));

        List<String> position=new ArrayList<String>();
        position.add(String.valueOf(latitude));
        position.add(String.valueOf(longitude));
        position.add(String.valueOf(marker.getPosition().latitude));
        position.add(String.valueOf(marker.getPosition().longitude));
        position.add(currentLocationAddress );

        intent.putStringArrayListExtra("position",(ArrayList<String> ) position);
        startActivity(intent);

        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        LatLng now=getLocationFromAddress(this,currentLocationAddress);

        MarkerOptions markerOptions= new MarkerOptions();
        markerOptions.position(now)
                .title("you are here!");

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));


        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(now));
        mMap.setOnMarkerClickListener(this);
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(6);
        mMap.animateCamera(zoom);
    }


    public class ConnectionUtils {
        public  void receiveResponse(HttpURLConnection conn)
                throws IOException {
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            // retrieve the response from server
            InputStream is = null;

            try {
                is = conn.getInputStream();
                convertInputStreamToString(is);

            } catch (IOException e) {
                throw e;
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        private  void convertInputStreamToString(InputStream inputStream) throws IOException {
            RE_url.clear(); RE_address.clear(); RE_name.clear();

            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            RE_number=Integer.parseInt(bufferedReader.readLine());

            for (int i=0;i<RE_number; ++i){
                int second_RE_number=Integer.parseInt(bufferedReader.readLine());

                String re_name= bufferedReader.readLine();
                String re_address=bufferedReader.readLine();
                String re_url=bufferedReader.readLine();

                HashMap<String,String> temp=new HashMap<String, String>();
                List<String> R_u=new ArrayList<String>();

                String number=String.valueOf(i+1);

                temp.put("number",number);
                temp.put("name",re_name);
                temp.put("address",re_address);
                temp.put("url",re_url);

                RE.put(re_name,temp);

                RE_name.add(re_name); RE_address.add(re_address); RE_url.add(re_url);
                R_u.add(re_url);

                for(int j=0;j<second_RE_number-1;++j){
                    R_u.add(bufferedReader.readLine());

                }

                RE_U.put(re_name,R_u);
            }

            bufferedReader.close(); inputStream.close();

        }
    }

}
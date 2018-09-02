package com.gmail.peurocs4.duck;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;


import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.SocialObject;
import com.kakao.util.helper.log.Logger;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.LocationTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    int RE_NUM=0;
    double latitude=0, longitude=0;

    private ImageView imageView1;
    int serverResponseCode = 0;
    ProgressDialog dialog = null;
    String uploadServerUri = null;

    // File Path
    static String uploadFilePath;
    String uploadFileName = "";

    final int REQ_CODE_SELECT_IMAGE=100;

    List<SliderLayout> sliderLayout;
    List< HashMap<String, String> > RE;
    List<TextView> tv_name, tv_address;
    List<Button> RE_btn;
    List<Button> search_btn;
    List<String> RE_name;
    List<String >RE_url;
    List<String > RE_address;
    List<Space> space;

    Drawable oldImg=null;
    private static final int MY_PERMISSION_REQUEST_LOCATION = 0;
    private static final int PICK_FROM_GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestLocation();

        imageView1=(ImageView) findViewById(R.id.imageView);
        oldImg=imageView1.getDrawable();

        uploadServerUri="http://164.125.34.209:3003/upload"; // 성혜: 164.125.34.209 // 192.168.203.128

        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryPermission();
//                Intent intent=new Intent(Intent.ACTION_PICK);
//                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
//                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
//                imageView1.setPadding(0,0,0,0);
            }
        });


        if (android.os.Build.VERSION.SDK_INT > 9) { //oncreate 에서 바로 쓰레드돌릴려고 임시방편으로 넣어둔소스

            StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

        }
    } // end oncreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                // something to do


                if(imageView1.getDrawable()==oldImg){
                    Toast.makeText(getApplicationContext(),"Insert new img!",Toast.LENGTH_SHORT).show();
                    return false;
                }

                //Toast.makeText(this, "upload", Toast.LENGTH_SHORT).show();
                dialog=ProgressDialog.show(MainActivity.this, "", "Uploading file...", true);

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
                    makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }finally {


                }
                return true;
            default:
                return true;
        }
    }


    @Override
    protected  void onActivityResult(int requestCode, int resultCode , Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQ_CODE_SELECT_IMAGE){
            if(resultCode==Activity.RESULT_OK){
                try{
                    String name_Str=getImageNameToUri(data.getData());
                    Bitmap image_bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    imageView1.setImageBitmap(image_bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public String getImageNameToUri(Uri data){
        String[] proj={MediaStore.Images.Media.DATA};
        Cursor cursor=managedQuery(data, proj, null,null,null);
        int column_index=cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String imgPath=cursor.getString(column_index);
        String imgName=imgPath.substring(imgPath.lastIndexOf("/")+1);
        uploadFilePath=imgPath;
        uploadFileName=imgName;
        return imgName;
    }


    public int uploadFile(String sourceFileUri) {

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

        sliderLayout=new ArrayList<SliderLayout>();
        tv_address=new ArrayList<TextView>();
        tv_name=new ArrayList<TextView>();
        RE_btn=new ArrayList<Button>();
        search_btn=new ArrayList<Button>();
        space=new ArrayList<Space>();
        RE_name=new ArrayList<String>();
        RE_url=new ArrayList<String>();
        RE_address=new ArrayList<String>();

        sliderLayout.clear();
        tv_address.clear();
        tv_name.clear();
        RE_btn.clear();
        search_btn.clear();
        space.clear();
        RE_name.clear();
        RE_url.clear();
        RE_address.clear();

        if( mLocationListener!=null){
        if (!sourceFile.isFile()) {
            dialog.dismiss();
            Log.e("uploadFile", "Source file not exist: " + uploadFilePath + "" + uploadFileName);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
            return 0;
        } else {
            try {
                // open a URL connection to the servlet
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


                if (serverResponseCode == 200) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            String msg="File Upload Completed.\n\n See uploaded file here : \n\n" + uploadFileName;
                            makeText(MainActivity.this, "File Upload Complete.", LENGTH_SHORT).show();


                            for (int i=0; i < 3; ++i) {


                                Log.e("inum","i is .."+i);
                                int resid=getResources().getIdentifier("slider" + String.valueOf(i), "id", getPackageName());
                                sliderLayout.add((SliderLayout) findViewById(resid));
                                sliderLayout.get(i).removeAllSliders();
                                sliderLayout.get(i).setVisibility(View.VISIBLE);
                                sliderLayout.get(i).stopAutoCycle();

                                int namesid=getResources().getIdentifier("name_text" + String.valueOf(i), "id", getPackageName());
                                int addressid=getResources().getIdentifier("address_text" + String.valueOf(i), "id", getPackageName());
                                int btnesid=getResources().getIdentifier("btn"+String.valueOf(i), "id", getPackageName());
                                int searbtnid=getResources().getIdentifier("bbtn"+String.valueOf(i), "id", getPackageName());
                                int spacesid=getResources().getIdentifier("space"+String.valueOf(i),"id",getPackageName());
                                Log.e("id","btn id "+ searbtnid+ " space id : "+spacesid);
                                RE_btn.add((Button)findViewById(btnesid));
                                search_btn.add((Button)findViewById(searbtnid));
                                tv_address.add((TextView) findViewById(addressid));
                                tv_name.add((TextView)findViewById(namesid));
                                space.add((Space)findViewById(spacesid));

                                if(i<RE_NUM)
                                {
                                final String re_name=RE_name.get(i);
                                final String re_address=RE_address.get(i);
                                final String re_url=RE_url.get(i);

                                tv_name.get(i).setText(re_name);
                                tv_name.get(i).setVisibility(View.VISIBLE);
                                tv_address.get(i).setText(re_address);
                                tv_address.get(i).setVisibility(View.VISIBLE);
                                space.get(i).setVisibility(View.VISIBLE);

                                search_btn.get(i).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String uri="https://m.search.naver.com/search.naver?ie=utf8&where=nexearch&query="+re_name;
                                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                        startActivity(intent);
                                    }
                                });
                                search_btn.get(i).setVisibility(View.VISIBLE);

                                RE_btn.get(i).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        LocationTemplate params=LocationTemplate.newBuilder(String.valueOf(re_address),
                                                ContentObject.newBuilder(String.valueOf(re_name),
                                                        String.valueOf(re_url),
                                                        LinkObject.newBuilder()
                                                                .setWebUrl(re_url)
                                                                .setMobileWebUrl("https://developers.kakao.com")
                                                                .build())
                                                        .setDescrption("Aboard in Korea")
                                                        .build())
                                                .setAddressTitle(String.valueOf(re_name))
                                                .build();

                                        KakaoLinkService.getInstance().sendDefault(getApplicationContext(), params, new ResponseCallback<KakaoLinkResponse>() {
                                            @Override
                                            public void onFailure(ErrorResult errorResult) {
                                                Logger.e(errorResult.toString());
                                            }

                                            @Override
                                            public void onSuccess(KakaoLinkResponse result) {

                                            }
                                        });
                                    }
                                });

                                RE_btn.get(i).setVisibility(View.VISIBLE);
                                TextSliderView t=new TextSliderView(MainActivity.this);
                                t.description(" ")
                                        .image(re_url)
                                        .setScaleType(BaseSliderView.ScaleType.Fit)
                                        .setOnSliderClickListener(MainActivity.this);
                                t.bundle(new Bundle());
                                sliderLayout.get(i).addSlider(t);

                                for (String name : RE.get(i).keySet()) {
                                    TextSliderView textSliderView=new TextSliderView(MainActivity.this);

                                    textSliderView.description(" ")
                                            .image(RE.get(i).get(name))
                                            .setScaleType(BaseSliderView.ScaleType.Fit)
                                            .setOnSliderClickListener(MainActivity.this);

                                    textSliderView.bundle(new Bundle());

                                    sliderLayout.get(i).addSlider(textSliderView);
                                }
                            }
                            else{
                                    RE_btn.get(i).setVisibility(View.GONE);
                                    search_btn.get(i).setVisibility(View.GONE);
                                    tv_address.get(i).setVisibility(View.GONE);
                                    tv_name.get(i).setVisibility(View.GONE);
                                    sliderLayout.get(i).setVisibility(View.GONE);
                                    space.get(i).setVisibility(View.GONE);
                                }
                        }
                        }
                    });
                }

                // close the streams
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (MalformedURLException ex) {
                dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        makeText(MainActivity.this, "MalformedURLException", LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {
                dialog.dismiss();
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        makeText(MainActivity.this, "Got Exception : see logcat", LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to svr Exp", "Exception: " + e.getMessage(), e);
            }


            dialog.dismiss();

            return serverResponseCode;
        }

        } // end else block
        return 0;
    }//end upload function

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

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

        private  void convertInputStreamToString(InputStream inputStream) throws IOException{
            RE=new ArrayList<HashMap<String, String>>();
            RE.clear();
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line = "";

            RE_NUM=Integer.parseInt(bufferedReader.readLine());
            Log.e("result","num is "+RE_NUM);

            for (int i=0; i<RE_NUM;++i){

                HashMap<String, String > newH=new HashMap<String, String>();
                int second_RE_NUM=Integer.parseInt(bufferedReader.readLine());

                String RE_NAME=bufferedReader.readLine();
                String RE_ADDRESS=bufferedReader.readLine();
                String RE_URL=bufferedReader.readLine();
                RE_name.add(RE_NAME);
                RE_address.add(RE_ADDRESS);
                RE_url.add(RE_URL);
//                newH.put(RE_URL,RE_URL);

                for (int j=0;j<second_RE_NUM-1;++j){
                    String _RE_URL=bufferedReader.readLine();
                    newH.put(_RE_URL, _RE_URL);
                    Log.e("get url", "get url ... " + _RE_URL);
                }
                RE.add(newH);

            }


            bufferedReader.close();
            inputStream.close();

        }
    }


    public void galleryPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
        }else {
            Intent intent=new Intent(Intent.ACTION_PICK);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
            imageView1.setPadding(0, 0, 0, 0);

        }
    }


    public void requestLocation() {
        final LocationManager lm=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);}

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                    100, // 통지사이의 최소 시간간격 (miliSecond)
                    1, // 통지사이의 최소 변경거리 (m)
                    mLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                    100, // 통지사이의 최소 시간간격 (miliSecond)
                    1, // 통지사이의 최소 변경거리 (m)
                    mLocationListener);

        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == MY_PERMISSION_REQUEST_LOCATION){
            requestLocation();

        }

        switch (requestCode) {
            case PICK_FROM_GALLERY:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
                } else {
                    //do something like displaying a message that he didn`t allow the app to access gallery and you wont be able to let him select from gallery
                }
                break;

            case MY_PERMISSION_REQUEST_LOCATION:
                requestLocation();
                break;
        }
    }


    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            Log.d("test", "onLocationChanged, location:" + location);
            longitude = location.getLongitude(); //경도
            latitude = location.getLatitude();   //위도
            //Toast.makeText(getApplicationContext(),String.valueOf(longitude)+" "+String.valueOf(latitude), Toast.LENGTH_SHORT).show();
        }
        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }

    };

}


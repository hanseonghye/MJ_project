package com.example.han.intentimg;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.LocationTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.helper.log.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by han on 2018-09-01.
 */

public class SliderActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener {
    TextView tv_name, tv_address;
    SliderLayout sliderLayout;
    String Url, name, address;
    ArrayList<String> position;
    String uploadFilePath;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slider);
        imageView=(ImageView)findViewById(R.id.T_imgview);

        tv_name=(TextView)findViewById(R.id.name);
        tv_address=(TextView)findViewById(R.id.address);
        sliderLayout=(SliderLayout)findViewById(R.id.slider);
        sliderLayout.stopAutoCycle();

        Intent intent=new Intent(this.getIntent());

        address=intent.getStringExtra("address");
        name=intent.getStringExtra("name");
        uploadFilePath=intent.getStringExtra("imgpath");

        File img=new File(uploadFilePath);
        if(img.exists()){
            Bitmap bitmap=BitmapFactory.decodeFile(img.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        }

        tv_name.setText(name);
        tv_address.setText(address);

        ArrayList<String> url=getIntent().getStringArrayListExtra("url");
        position=getIntent().getStringArrayListExtra("position");

        Url=url.get(0);

        for(int i=0;i<url.size();++i){
            TextSliderView textSliderView=new TextSliderView(SliderActivity.this);
            textSliderView.description(" ")
                    .image(url.get(i))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(SliderActivity.this);
            textSliderView.bundle(new Bundle());
            sliderLayout.addSlider(textSliderView);
        }


    }

    public void findload(View view){
        String load_url="http://m.map.naver.com/route.nhn?menu=route&sname="+position.get(4)+"&sx="+position.get(1)+"&sy="+position.get(0)+"&ename="+name+
                "&ex="+position.get(3)+"&ey="+position.get(2)+"&pathType=0&showMap=true";
        Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(load_url));
        startActivity(intent);

    }

    public void search(View view){
        String search_uri="https://m.search.naver.com/search.naver?ie=utf8&where=nexearch&query="+name;
        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(search_uri));
        startActivity(intent);
    }

    public void share(View view){
        LocationTemplate params=LocationTemplate.newBuilder(String.valueOf(address),
                ContentObject.newBuilder(String.valueOf(name),
                        String.valueOf(Url),
                        LinkObject.newBuilder()
                                .setWebUrl(Url)
                                .setMobileWebUrl("https://developers.kakao.com")
                                .build())
                        .setDescrption("Aboard in Korea")
                        .build())
                .setAddressTitle(String.valueOf(name))
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

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }
}

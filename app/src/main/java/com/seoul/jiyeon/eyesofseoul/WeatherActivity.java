package com.seoul.jiyeon.eyesofseoul;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class WeatherActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    TextView wText, wTemp, tvDate;
    Document doc = null;
    RelativeLayout layout;
    ImageView wIcon;
    TextToSpeech tts;
    GestureDetector gd = null;

    String sDate = "";
    String sMonth = "";
    String tmp = "";
    String weather = "";
    String hour = "";

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gd.onTouchEvent(event);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        layout = findViewById(R.id.rl1);
        wTemp = findViewById(R.id.wTemp);
        wText = findViewById(R.id.wText);
        wIcon = findViewById(R.id.wIcon);


        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat curMonth = new SimpleDateFormat("MM");
        SimpleDateFormat curDate = new SimpleDateFormat("dd");

        sMonth = curMonth.format(date);
        sDate = curDate.format(date);


        GetXMLTask task = new GetXMLTask();
        task.execute("http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=1171056100");

        tts = new TextToSpeech(this,this);
        permissionCheck();

        gd = new GestureDetector(layout.getContext(), new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
                startActivity(intent);
                finish();

                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Intent intent = new Intent(getApplicationContext(), WeatherActivity2.class);
                startActivity(intent);
                finish();

                return super.onSingleTapConfirmed(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);
                startActivity(intent);
                finish();
                super.onLongPress(e);
            }
        });


    }


    private void permissionCheck() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
        }
    }
    @Override
    public void onInit(int i) {
        String isIntroduce = sMonth +"월 " +sDate+"일 " + hour +"시. 서울 날씨는 "+ weather + ". 온도는 "+ tmp +"도 입니다. 내일 날씨 예보를 들으시려면 화면을 한 번," +
                " 다시 들으시려면 화면을 길게 터치해주세요. 초기 메뉴로 돌아가시려면 화면을 두 번 터치해주세요.";
        tts.speak(isIntroduce, TextToSpeech.QUEUE_FLUSH,null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }
    private class GetXMLTask extends AsyncTask<String,Void,Document> {
        @Override
        protected Document doInBackground(String... strings) {
            URL url;
            try{
                url = new URL(strings[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();
            }catch (Exception e){
                Toast.makeText(getBaseContext(), "Parsing Error", Toast.LENGTH_SHORT).show();
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document document) {

            float ftemp;
            int itemp;
            String day="";
            NodeList nodeList = doc.getElementsByTagName("data");

            for(int i=0; i<nodeList.getLength();i++){

                Node node = nodeList.item(i);

                Element element = (Element) node;

                if(i==0){
                    NodeList nameList = element.getElementsByTagName("temp");
                    Element nameElement = (Element)nameList.item(0);
                    nameList = nameElement.getChildNodes();
                    NodeList dayList = element.getElementsByTagName("day");
                    day = dayList.item(0).getChildNodes().item(0).getNodeValue();

                    if(day.equals("1")){
                        int date;
                        int month;
                        if(sMonth=="1"||sMonth=="3"||sMonth=="5"||sMonth=="7"||sMonth=="8"||sMonth=="10"||sMonth=="12"){
                            if(sDate=="31"){
                                sDate="1";
                                month = Integer.parseInt(sMonth);
                                month += 1;
                                sDate = Integer.toString(month);
                            }else{
                                date = Integer.parseInt(sDate);
                                date += 1;
                                sDate = Integer.toString(date);
                            }
                        }else if(sMonth=="4"||sMonth=="6"||sMonth=="9"||sMonth=="11"){
                            if(sDate=="30"){
                                sDate="1";
                                month = Integer.parseInt(sMonth);
                                month += 1;
                                sDate = Integer.toString(month);
                            }else{
                                date = Integer.parseInt(sDate);
                                date += 1;
                                sDate = Integer.toString(date);
                            }
                        }else if(sMonth == "2"){
                            if(sDate=="28"){
                                sDate="1";
                                sMonth = "3";
                            }else{
                                date = Integer.parseInt(sDate);
                                date += 1;
                                sDate = Integer.toString(date);
                            }
                        }

                    }

                    tmp = ((Node)nameList.item(0)).getNodeValue();
                    ftemp = Float.parseFloat(tmp);
                    itemp = (int)ftemp;
                    tmp = Integer.toString(itemp)+"º";

                    NodeList hourList = element.getElementsByTagName("hour");
                    hour = hourList.item(0).getChildNodes().item(0).getNodeValue();

                    NodeList weatherList = element.getElementsByTagName("wfKor");
                    weather = weatherList.item(0).getChildNodes().item(0).getNodeValue();

                }
            } //end of for

            wTemp.setText(tmp);
            wText.setText(weather);

            if(weather.equals("맑음")){
                wIcon.setImageResource(R.drawable.sunny);
            }else if(weather.equals("구름 조금") || weather.equals("구름 많음")) {
                wIcon.setImageResource(R.drawable.suncloud);
            }else if(weather.equals("흐림")){
                wIcon.setImageResource(R.drawable.cloudy);
            }else if(weather.equals("비")){
                wIcon.setImageResource(R.drawable.rainy);
            }

            super.onPostExecute(document);
        }
    }
}

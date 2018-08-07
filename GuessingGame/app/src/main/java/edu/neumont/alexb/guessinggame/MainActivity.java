package edu.neumont.alexb.guessinggame;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    int correctGuess, incorrectGuess;
    ArrayList<String> titles;
    ArrayList<Bitmap> imageBitmaps;
    ArrayList<String> usedTitles;
    DownloadTask myDownload;
    String correctTitle;
    Random gen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usedTitles = new ArrayList<>();
        gen = new Random();
        correctGuess = 0;
        incorrectGuess = 0;
        String url = null;
        try {
            url = "https://www.pcauthority.com.au/news/top-10-computer-games-of-all-time-170181";
            myDownload = new DownloadTask();
            myDownload.ctx = this;
            myDownload.execute(url);
            Toast.makeText(this, "End Of onCreate", Toast.LENGTH_SHORT).show();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }
////android.graphics.BitmapFactory.decodeStream(url) <------ MAKE BITMAP FROM URL
    private void setUpGame(){
        ImageView imageView = (ImageView)findViewById(R.id.GameImage);
        if(usedTitles.isEmpty()) {
            int indexForImageAndTitle =gen.nextInt(titles.size());
            String usedTitle = titles.get(indexForImageAndTitle);
            int buttonNum = gen.nextInt(4)+1;
            placeTitleOnButton(buttonNum, usedTitle);
            correctTitle = usedTitle;
            usedTitles.add(usedTitle);
            try{
                imageView.setImageBitmap(imageBitmaps.get(indexForImageAndTitle));
            } catch(Exception e) {
                e.printStackTrace();
            }
            for(int i=1;i<5;i++) {
                if(i!=buttonNum) {
                    String nextTitle = titles.get(gen.nextInt(titles.size()));
                    while(nextTitle.equals(usedTitle)) {
                        nextTitle = titles.get(gen.nextInt(titles.size()));
                    }
                    placeTitleOnButton(i, nextTitle);
                }
            }
        } else {
            int index = gen.nextInt(titles.size())+1;
            int buttonNum = gen.nextInt(4)+1;
            String title = titles.get(index);
            while(usedTitles.contains(title)) {//
                index = gen.nextInt(4)+1;
                title = titles.get(index);
            }
            correctTitle = title;
            usedTitles.add(title);
            placeTitleOnButton(buttonNum, title);
            try {
                imageView.setImageBitmap(imageBitmaps.get(index));
            } catch(Exception e) {
                e.printStackTrace();
            }
            for(int i=1;i<5;i++) {
                if(i!=buttonNum) {
                    String nextTitle = titles.get(gen.nextInt(titles.size()));
                    while(nextTitle.equals(title)) {
                        nextTitle = titles.get(gen.nextInt(titles.size()));
                    }
                    placeTitleOnButton(i, nextTitle);
                }
            }
        }
    }

    private void placeTitleOnButton(int buttonNum, String usedTitle) {
        Button but1 = (Button)findViewById(R.id.TitleOneButton);
        Button but2 = (Button)findViewById(R.id.TitleTwoButton);
        Button but3 = (Button)findViewById(R.id.TitleThreeButton);
        Button but4 = (Button)findViewById(R.id.TitleFourButton);
        switch(buttonNum) {//places initial title
            case 1:
                but1.setText(usedTitle);
                break;
            case 2:
                but2.setText(usedTitle);
                break;
            case 3:
                but3.setText(usedTitle);
                break;
            case 4:
                but4.setText(usedTitle);
                break;
        }
    }

    public void buttonOnClick(android.view.View v) {
        Button b = (Button) v;
        String guessedTitle = b.getText().toString();
        if(checkGuess(guessedTitle)) {
            correctGuess++;
            setUpGame();
        } else {
            incorrectGuess++;
        }
    }

    private boolean checkGuess(String title) {
        return correctTitle.equals(title);
    }
    private void Pass(android.util.Pair<ArrayList<String>, ArrayList<Bitmap>> input){
        titles = input.first;
        imageBitmaps = input.second;
        setUpGame();
    }

    private class DownloadTask extends AsyncTask<String, Void, android.util.Pair<ArrayList<String>, ArrayList<Bitmap>>> {
        public android.content.Context ctx;
        @Override
        protected android.util.Pair<ArrayList<String>, ArrayList<Bitmap>> doInBackground(String... urls){
           ArrayList<String> gameTitles = new ArrayList<>();
           ArrayList<String> imageURLs = new ArrayList<>();
           ArrayList<Bitmap> imageBitmaps = new ArrayList<>();
           try {
               org.jsoup.nodes.Document doc = Jsoup.connect(urls[0]).get();
               Elements boldTitles = doc.body().getElementsByTag("b");
               for (Element e : boldTitles){
                   if(e.ownText().matches("(\\d+. +(\\s|\\S)+)")){
                       gameTitles.add(java.util.regex.Pattern.compile("(\\d+. +)").split(e.ownText())[1]);
                   }
               }
               Elements images = doc.body().getElementsByTag("img");
               ArrayList<String> tempArrList = new ArrayList<>();
               for (Element e : images){
                   tempArrList.add(e.attributes().get("src"));
               }
               for(int i=9;i<19;i++) {
                    imageURLs.add(tempArrList.get(i));
               }
               for (String s : imageURLs){
                   imageBitmaps.add(android.graphics.BitmapFactory.decodeStream(new URL(s).openStream()));
               }

           } catch(Exception e) {

               Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
           }
           return new android.util.Pair<>(gameTitles,imageBitmaps);
        }
        @Override
        protected void onPostExecute(android.util.Pair<ArrayList<String>, ArrayList<Bitmap>> result){
            super.onPostExecute(result);
            Pass(result);
        }
    }

}

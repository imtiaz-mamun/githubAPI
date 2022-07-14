package com.example.apilist;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailsActivity extends AppCompatActivity {
    TextView id,des,l_up;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        id = (TextView) findViewById(R.id.listId);
        des = (TextView) findViewById(R.id.listDes);
        l_up = (TextView) findViewById(R.id.lastUpdate);
        imageView = (ImageView) findViewById(R.id.imageView2);
        String newString, description;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                newString= null;
            } else {
                newString = extras.getString("OWNER");
                description = extras.getString("DESCRIPTION");
                String[] separated = newString.split("__");
                id.setText("Owner's name: "+separated[0]);
                des.setText("Repository's description: "+description);
                String[] dateTime = separated[2].split("T");
                String[] date = dateTime[0].split("-");
                String time = dateTime[1].replace("Z", "");
                String[] nTime = time.split(":");
                l_up.setText("Last Update Date: "+date[1]+"-"+date[2]+"-"+date[0]+" "+nTime[0]+":"+nTime[2]);
                new DownloadImageTask((ImageView) findViewById(R.id.imageView2))
                        .execute(separated[1]);

            }
        } else {
            newString= (String) savedInstanceState.getSerializable("OWNER");
            description = (String) savedInstanceState.getSerializable("DESCRIPTION");
        }


    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
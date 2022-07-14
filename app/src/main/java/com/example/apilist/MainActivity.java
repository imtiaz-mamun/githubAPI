package com.example.apilist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private static String data_url = "https://api.github.com/search/repositories?q=Android&sort=stars&order=desc";
    private static String data_url_date = "https://api.github.com/search/repositories?q=Android&sort=updated_at&order=desc";
    ListView numbersListView;
    ArrayList<NumbersView> arrayList = new ArrayList<NumbersView>();
    NumbersViewAdapter numbersArrayAdapter;
    String filterSelected;
    Spinner spinnerS;
    ArrayAdapter<String> dataAdapterR;
    String fileName = "localJson.txt";
    FileOutputStream outputStream;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        numbersListView = (ListView) findViewById(R.id.listView);
        spinnerS = (Spinner) findViewById(R.id.curName);
        spinnerS.setOnItemSelectedListener(this);
        List<String> allReceiveMethods = new ArrayList<String>();
        allReceiveMethods.add("Stars");
        allReceiveMethods.add("Update Time");
        dataAdapterR = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, allReceiveMethods);
        dataAdapterR.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerS.setAdapter(dataAdapterR);

    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        filterSelected = parent.getItemAtPosition(position).toString();
        if (filterSelected.equals("Stars")) {
            try {
                if(isNetworkConnected()==true && isConnected()==true){
                    new JsonTask().execute(data_url);
                }
                else{
                    File path = this.getFilesDir();
                    Toast.makeText(MainActivity.this, "No Internet!", Toast.LENGTH_LONG).show();
                    new JsonTaskLocal().execute(String.valueOf(path));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                if(isNetworkConnected()==true && isConnected()==true){
                    new JsonTask().execute(data_url_date);
                }
                else{
                    File path = this.getFilesDir();
                    Toast.makeText(MainActivity.this, "No Internet!", Toast.LENGTH_LONG).show();
                    new JsonTaskLocal().execute(String.valueOf(path));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    private class JsonTaskLocal extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {

            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(new
                        File(getFilesDir()+File.separator+fileName)));
                String read;
                StringBuilder builder = new StringBuilder("");

                while((read = bufferedReader.readLine()) != null){
                    builder.append(read);
                }
                return builder.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject mainObject = null;
            try {
                arrayList.clear();
                mainObject = new JSONObject(result);
                JSONArray array = (JSONArray) mainObject.get("items");
                BufferedWriter bufferedWriter = null;
                try {
                    bufferedWriter = new BufferedWriter(new FileWriter(new
                            File(getFilesDir()+File.separator+fileName)));
                    bufferedWriter.write(mainObject.toString());
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int j = 0;
                for (j = 0; j < array.length(); j++) {
                    JSONObject childObject = array.getJSONObject(j);
                    JSONObject ownerObject = childObject.getJSONObject("owner");
                    String name = ownerObject.getString("login");
                    String avatar = ownerObject.getString("avatar_url");
                    String last_update = childObject.getString("updated_at");
                    String hidden_text = name+"__"+avatar+"__"+last_update;
                    String description = childObject.getString("description");
                    arrayList.add(new NumbersView(R.drawable.github, hidden_text, description));
                }
                numbersArrayAdapter = new NumbersViewAdapter(getApplicationContext(), arrayList);
                numbersListView.setAdapter(numbersArrayAdapter);
                numbersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        Intent i = new Intent(MainActivity.this, DetailsActivity.class);
                        TextView txt_own = (TextView) v.findViewById(R.id.textView1);
                        String o = txt_own.getText().toString();
                        TextView txt_des = (TextView) v.findViewById(R.id.textView2);
                        String d = txt_des.getText().toString();
                        i.putExtra("OWNER", o);
                        i.putExtra("DESCRIPTION", d);
                        startActivity(i);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject mainObject = null;
            try {
                arrayList.clear();
                mainObject = new JSONObject(result);
                JSONArray array = (JSONArray) mainObject.get("items");
                BufferedWriter bufferedWriter = null;
                try {
                    bufferedWriter = new BufferedWriter(new FileWriter(new
                            File(getFilesDir()+File.separator+fileName)));
                    bufferedWriter.write(mainObject.toString());
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int j = 0;
                for (j = 0; j < array.length(); j++) {
                    JSONObject childObject = array.getJSONObject(j);
                    JSONObject ownerObject = childObject.getJSONObject("owner");
                    String name = ownerObject.getString("login");
                    String avatar = ownerObject.getString("avatar_url");
                    String last_update = childObject.getString("updated_at");
                    String hidden_text = name+"__"+avatar+"__"+last_update;
                    String description = childObject.getString("description");
                    arrayList.add(new NumbersView(R.drawable.github, hidden_text, description));
                }
                numbersArrayAdapter = new NumbersViewAdapter(getApplicationContext(), arrayList);
                numbersListView.setAdapter(numbersArrayAdapter);
                numbersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        Intent i = new Intent(MainActivity.this, DetailsActivity.class);
                        TextView txt_own = (TextView) v.findViewById(R.id.textView1);
                        String o = txt_own.getText().toString();
                        TextView txt_des = (TextView) v.findViewById(R.id.textView2);
                        String d = txt_des.getText().toString();
                        i.putExtra("OWNER", o);
                        i.putExtra("DESCRIPTION", d);
                        startActivity(i);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    private boolean isConnected() throws InterruptedException, IOException {
        String command = "ping -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }
    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
    //saveJson
    public void saveJson(String data) {
        String str = data;
        SharedPreferences sharedPref = getSharedPreferences("appData", Context.MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor prefEditor = getSharedPreferences("appData", Context.MODE_WORLD_WRITEABLE).edit();
        prefEditor.putString("json", str);
        prefEditor.commit();
    }//end of saveJson()
}
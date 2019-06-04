package com.meet.phonebook.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.meet.phonebook.Misc.DatabaseHelper;
import com.meet.phonebook.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

import static com.meet.phonebook.Misc.Constants.DATABASE_NAME;
import static com.meet.phonebook.Misc.Constants.DATABASE_URL;
import static com.meet.phonebook.Misc.Constants.IS_DATABASE_UPDATED;
import static com.meet.phonebook.Misc.Constants.SHARED_PREFS;

public class UpdateActivity extends AppCompatActivity {

    String dataUrl;
    TextView textView;
    ProgressBar progressBar;
    Button button;

    SharedPreferences sharedPreferences;
    DownloadTask downloadTask;
    boolean isUpdateInitiated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        dataUrl = Objects.requireNonNull(getIntent().getExtras()).getString("dataUrl");

        textView = findViewById(R.id.updateData_text);
        progressBar = findViewById(R.id.updateData_progress);
        button = findViewById(R.id.updateData_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    isUpdateInitiated = true;
                    downloadTask = new DownloadTask();
                    downloadTask.execute(dataUrl);
                } else {
                    Toast.makeText(UpdateActivity.this, "Turn on internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                URLConnection urlConnection = url.openConnection();
                urlConnection.connect();

                int lenghtOfFile = urlConnection.getContentLength();
                Log.e("File length", lenghtOfFile + "");

                if (lenghtOfFile <= 0) {
                    cancel(true);
                    return null;
                }
                InputStream inputStream = urlConnection.getInputStream();
                OutputStream outputStream = new FileOutputStream(getPath("database_new.db"));

                byte data[] = new byte[4096];
                int count;
                int total = 0;
                while ((count = inputStream.read(data)) != -1) {
                    if (isCancelled()) {
                        inputStream.close();
                        return null;
                    }
                    total += count;

                    if (lenghtOfFile > 0)
                        publishProgress((int) (total * 100 / lenghtOfFile));
                    outputStream.write(data, 0, count);
                }
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DATABASE_URL, dataUrl);
            editor.putBoolean(IS_DATABASE_UPDATED, false).apply();
//            finishAffinity();
//            System.exit(0);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Starting download");

            textView.setText("Updating database");
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String file_url) {
            File file = new File(getPath("database_new.db"));
            File oldFile = new File(getPath(DATABASE_NAME));
            if (oldFile.exists()) {
                oldFile.delete();
                System.out.println("delete database file.");
            }
            file.renameTo(oldFile);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DATABASE_URL, null);
            editor.putBoolean(IS_DATABASE_UPDATED, true).apply();

            Intent intent = new Intent(UpdateActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
            System.out.println("Downloaded");
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) UpdateActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if ((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()))
                return true;
            else return false;
        } else
            return false;
    }

    private String getPath(String databaseName) {
        String db_path = UpdateActivity.this.getApplicationInfo().dataDir;
        File file = new File(db_path, "databases");
        return file.getAbsolutePath() + "/" + databaseName;
    }
}

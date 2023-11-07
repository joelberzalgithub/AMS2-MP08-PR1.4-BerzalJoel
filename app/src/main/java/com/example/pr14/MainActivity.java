package com.example.pr14;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    // Model = Taula d'items': utilitzem ArrayList
    ArrayList<String> items;
    // Enter que assignarà un número als nous items que s'introduiran a l'ArrayList
    int numItem = 3;
    // ArrayAdapter serà l'intermediari amb la ListView
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialitzem model
        items = new ArrayList<String>();
        // Afegim alguns exemples
        items.add("Item 1");
        items.add("Item 2");
        items.add("Item 3");

        // Inicialitzem l'ArrayAdapter amb el layout pertinent
        adapter = new ArrayAdapter<String>(this, R.layout.list_item, items) {
            @SuppressLint("SetTextI18n")
            @NonNull
            @Override
            public View getView(int pos, View convertView, @NonNull ViewGroup container) {

                // GetView ens construeix el layout i hi "pinta" els valors de l'element en la posició pos
                if (convertView == null) {
                    // Inicialitzem l'element la View amb el seu layout
                    convertView = getLayoutInflater().inflate(R.layout.list_item, container, false);
                }
                // "Pintem" els valors (també quan es refresca)
                ((TextView) convertView.findViewById(R.id.item)).setText(Objects.requireNonNull(getItem(pos)));
                return convertView;
            }
        };

        // Busquem la ListView i li endollem l'ArrayAdapter
        ListView lv =  findViewById(R.id.listView);
        lv.setAdapter(adapter);

        Button button = findViewById(R.id.button);
        TextView textView = findViewById(R.id.textView);
        ImageView imageView = findViewById(R.id.imageView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Utilitzem un Executor per gestionar la tasca en segon pla
                Executor executor = Executors.newSingleThreadExecutor();;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        final String result_1 = getDataFromUrl("https://api.myip.com");
                        final Bitmap result_2 = getBitmapFromUrl("https://randomfox.ca/images/122.jpg");
                        // Utilitzem un Handler per actualitzar el TextView
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {

                                if (result_1 != null) {
                                    // Mostrem les dades per la consola de debug
                                    Log.i("NetworkData", result_1);
                                    // Actualitzem el text del TextView amb les dades rebudes.
                                    textView.setText("Dades rebudes:\n" + result_1);
                                } else {
                                    // Si hi ha hagut un error, el mostrem en la consola de debug
                                    Log.e("NetworkData", "Failed to fetch data");
                                }

                                if (result_2 != null) {
                                    // Mostrem la imatge en la ImageView
                                    imageView.setImageBitmap(result_2);
                                } else {
                                    // Si hi ha hagut un error, el mostrem en la consola de debug
                                    Log.e("NetworkData", "Failed to download image");
                                }
                                // Afegim un item nou a l'ArrayList d'items
                                numItem++;
                                items.add("Item " + numItem);
                                // Notifiquem l'adapter dels canvis al model
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        });
    }
    String error = "";
    private String getDataFromUrl(String demoIdUrl) {
        String result = null;
        int resCode;
        InputStream in;
        try {
            HttpsURLConnection httpsConn = (HttpsURLConnection) new URL(demoIdUrl).openConnection();
            httpsConn.setAllowUserInteraction(false);
            httpsConn.setInstanceFollowRedirects(true);
            httpsConn.setRequestMethod("GET");
            httpsConn.connect();
            resCode = httpsConn.getResponseCode();

            if (resCode == HttpURLConnection.HTTP_OK) {

                in = httpsConn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1), 8);
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                in.close();
                result = sb.toString();
            } else {
                error += resCode;
            }
        } catch (IOException e) {
            Log.e("Error", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
        }
        return result;
    }
    private Bitmap getBitmapFromUrl(String urlDisplay) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(new URL(urlDisplay).openStream());
        } catch (Exception e) {
            Log.e("Error", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
        }
        return bitmap;
    }
}

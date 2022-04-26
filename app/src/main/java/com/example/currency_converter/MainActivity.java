package com.example.currency_converter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ListView lvMain = null;
    final String LOG_TAG = "myLogs";
    final String SAVED_JSON = "Saved_json";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String url = "https://www.cbr-xml-daily.ru/daily_json.js";
        ArrayList<Valuta> Valute = new ArrayList<>();
        lvMain = findViewById(R.id.lvMain);

        new GetData().execute(url);
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        String savedText = sPref.getString(SAVED_JSON,"");
        try {
            JSONObject jValute = new JSONObject(savedText);
            JSONArray Keys = jValute.names();
            for (int i=0; i<Keys.length();i++) {
                String key = Keys.getString(i);
                JSONObject jO = jValute.getJSONObject(key);
                String jName = jO.getString("Name");
                Double jValue = jO.getDouble("Value");
                Valute.add(new Valuta(jName,jValue));
            }

            ArrayAdapter<Valuta> valuteArrayAdapter = new ArrayAdapter<Valuta>(MainActivity.this,R.layout.custom_item,R.id.valuta_value,Valute) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    super.getView(position, convertView, parent);
                    Valuta currentValuta = Valute.get(position);
                    //вызываем .inflate для ускорения работы программы
                    if (convertView == null) {
                        convertView = getLayoutInflater()
                                .inflate(R.layout.custom_item,null, false);
                    }
                    //прописываем ViewHolhder для оптимизации скорости выполнения метода
                    ViewHolder viewHolder = new ViewHolder();
                    viewHolder.valuteName = (TextView)convertView.findViewById(R.id.valuta_name);
                    viewHolder.valuteValue = (TextView)convertView.findViewById(R.id.valuta_value);
                    convertView.setTag(viewHolder);
                    TextView valuteName = ((ViewHolder)convertView.getTag()).valuteName;
                    TextView valuteValue = ((ViewHolder)convertView.getTag()).valuteValue;

                    valuteName.setText(currentValuta.name);
                    String value = String.format("%.2f",currentValuta.value);
                    valuteValue.setText(value);
                    return convertView;
                }
            };
            lvMain.setAdapter(valuteArrayAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        lvMain.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        Log.d(LOG_TAG,"ItemClick: position =" + position + ", id:" + id );
                        Intent intent = new Intent(MainActivity.this, change_quantity.class);
                        intent.putExtra("Name",Valute.get(position).name);
                        intent.putExtra("Value",Valute.get(position).value);
                        startActivity(intent);
                    }
                }
        );
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, change_quantity.class);
    }

    private class GetData extends AsyncTask<String,String,String> {
    /* *
    * Отдельный поток для загрузки и обработки json файла
    * */
        @Override
        protected String doInBackground(String... Strings) {
            //метод загрузки и считывания json
            HttpURLConnection conn = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(Strings[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                InputStream inputStream = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                try {
                    if (conn != null)
                        conn.disconnect();
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //Преобразовываем полученный json объект, формируем arrayList, переписываем ArrayAdapter
            try {
                JSONObject jO = new JSONObject(result);
                SharedPreferences sPref = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = sPref.edit();
                editor.putString(SAVED_JSON,jO.getJSONObject("Valute").toString());
                editor.apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    static class Valuta {
        String name;
        Double value;

        public Valuta (String name, Double value) {
            this.name = name;
            this.value = value;
        }
    }
    static class ViewHolder {
        TextView valuteName;
        TextView valuteValue;
    }
}
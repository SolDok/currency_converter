package com.example.currency_converter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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

public class MainActivity extends AppCompatActivity {

    TextView tv = null;
    ListView lv = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String url = "https://www.cbr-xml-daily.ru/daily_json.js";
        lv = findViewById(R.id.lv);
        new GetData().execute(url);

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
                JSONObject jObject = new JSONObject(result);
                JSONObject jValute = jObject.getJSONObject("Valute");
                JSONArray Keys = jValute.names();
                ArrayList<Valute> Valutes = new ArrayList<>();

                for (int i=0; i<Keys.length();i++) {
                    String key = Keys.getString(i);
                    JSONObject jO = jValute.getJSONObject(key);
                    String jName = jO.getString("Name");
                    Double jValue = jO.getDouble("Value");
                    Valutes.add(new Valute(jName,jValue));
                }

                ArrayAdapter<Valute> valuteArrayAdapter = new ArrayAdapter<Valute>(MainActivity.this,R.layout.custom_item,R.id.valute_value,Valutes) {
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        super.getView(position, convertView, parent);
                        Valute currentValute = Valutes.get(position);
                        //вызываем .inflate для ускорения работы программы
                        if (convertView == null) {
                            convertView = getLayoutInflater()
                                    .inflate(R.layout.custom_item,null, false);
                        }
                        //прописываем ViewHolhder для оптимизации скорости выполнения метода
                        ViewHolder viewHolder = new ViewHolder();
                        viewHolder.valuteName = (TextView)convertView.findViewById(R.id.valute_name);
                        viewHolder.valuteValue = (TextView)convertView.findViewById(R.id.valute_value);
                        convertView.setTag(viewHolder);
                        TextView valuteName = ((ViewHolder)convertView.getTag()).valuteName;
                        TextView valuteValue = ((ViewHolder)convertView.getTag()).valuteValue;

                        valuteName.setText(currentValute.name);
                        valuteValue.setText(currentValute.value.toString());
                        return convertView;
                    }
                };
                lv.setAdapter(valuteArrayAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    static class Valute {
        String name;
        Double value;

        public Valute (String name, Double value) {
            this.name = name;
            this.value = value;
        }
    }
    static class ViewHolder {
        TextView valuteName;
        TextView valuteValue;
    }
}
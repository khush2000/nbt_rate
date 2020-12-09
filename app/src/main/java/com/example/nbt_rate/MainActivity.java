package com.example.nbt_rate;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView date_text, cnh_field, usd_field, eur_field, rub_field, date_start, rate_result;
    MainActivity activity;
    Spinner code_spinner;
    Button table_btn;
    ArrayList<Massive> massive = new ArrayList<>();
    String result = "";
    private int mYear, mMonth, mDay;
    private int position_code;
    String code1 = "840";
    String code2 = "978";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        //Initialization..
        date_text = findViewById(R.id.date_text);
        cnh_field = findViewById(R.id.cnh_field);
        usd_field = findViewById(R.id.usd_field);
        eur_field = findViewById(R.id.eur_field );
        rub_field = findViewById(R.id.rub_field);
        date_start = findViewById(R.id.date_start);
        code_spinner = findViewById(R.id.code_spinner);
        rate_result = findViewById(R.id.rate_result);
        table_btn = findViewById(R.id.table_btn);
        activity = this;

        //Title field..
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.my_title);

        //RSS request..
        FeedRequest req = new FeedRequest();
        req.activity = activity;
        req.execute();

        //Date..
        try {
            Date currentDate = new Date();
            long dnum = currentDate.getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateText = dateFormat.format(dnum);

            @SuppressLint("SimpleDateFormat") Date jud = new SimpleDateFormat("yyyy-MM-dd").parse(dateText);
            String month = DateFormat.getDateInstance(SimpleDateFormat.LONG, new Locale("ru")).format(jud);
            date_text.setText(month);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Load feed..
        SharedPreferences prefs = getSharedPreferences("data", 0);
        RSS_parse(prefs.getString("rss_feed", ""));

        //Date selection..
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("dd.MM.yyyy").format(c.getTime());
        date_start.setText(date);

        Calendar first_date = Calendar.getInstance();
        first_date.add(Calendar.MONTH, -12);
        final long date_result = first_date.getTimeInMillis();

        date_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rate_result.setText("");
                DatePickerDialog dpd = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                c.set(year, month, day);
                                @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("dd.MM.yyyy").format(c.getTime());
                                date_start.setText(date);

                                mYear = c.get(Calendar.YEAR);
                                mMonth = c.get(Calendar.MONTH);
                                mDay = c.get(Calendar.DAY_OF_MONTH);

                                getHtmlFromWeb(date_start.getText().toString(), code1, code2);
                            }
                        }, mYear, mMonth, mDay);

                dpd.getDatePicker().setMinDate(date_result);
                Calendar a = Calendar.getInstance();
                a.add(Calendar.DAY_OF_MONTH,0);
                dpd.getDatePicker().setMaxDate(a.getTimeInMillis());
                dpd.show();
            }
        });

        //Code spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.code_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        code_spinner.setAdapter(adapter);

        code_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ((TextView)parentView.getChildAt(0)).setTextColor(Color.parseColor("#00695C"));
                ((TextView)parentView.getChildAt(0)).setTextSize(15);
                position_code = code_spinner.getSelectedItemPosition();
                Log.i("Position", String.valueOf(position_code));

                switch(position_code) {
                    case 0:
                        code1 = "840";
                        code2 = "978";
                        break;
                    case 1:
                        code1 = "978";
                        code2 = "156";
                        break;
                    case 2:
                        code1 = "156";
                        code2 = "756";
                        break;
                    case 3:
                        code1 = "756";
                        code2 = "810";
                        break;
                    case 4:
                        code1 = "810";
                        code2 = "860";
                        break;
                    case 5:
                        code1 = "860";
                        code2 = "417";
                        break;
                    case 6:
                        code1 = "417";
                        code2 = "398";
                        break;
                    case 7:
                        code1 = "398";
                        code2 = "933";
                        break;
                    case 8:
                        code1 = "933";
                        code2 = "364";
                        break;
                    case 9:
                        code1 = "364";
                        code2 = "971";
                        break;
                    case 10:
                        code1 = "971";
                        code2 = "586";
                        break;
                    case 11:
                        code1 = "586";
                        code2 = "949";
                        break;
                    case 12:
                        code1 = "949";
                        code2 = "934";
                        break;
                    case 13:
                        code1 = "934";
                        code2 = "826";
                        break;
                    case 14:
                        code1 = "826";
                        code2 = "036";
                        break;
                    case 15:
                        code1 = "036";
                        code2 = "208";
                        break;
                    case 16:
                        code1 = "208";
                        code2 = "352";
                        break;
                    case 17:
                        code1 = "352";
                        code2 = "124";
                        break;
                    case 18:
                        code1 = "124";
                        code2 = "414";
                        break;
                    case 19:
                        code1 = "414";
                        code2 = "578";
                        break;
                    case 20:
                        code1 = "578";
                        code2 = "702";
                        break;
                    case 21:
                        code1 = "702";
                        code2 = "752";
                        break;
                    case 22:
                        code1 = "752";
                        code2 = "392";
                        break;
                    case 23:
                        code1 = "392";
                        code2 = "944";
                        break;
                    case 24:
                        code1 = "944";
                        code2 = "051";
                        break;
                    case 25:
                        code1 = "051";
                        code2 = "981";
                        break;
                    case 26:
                        code1 = "981";
                        code2 = "498";
                        break;
                    case 27:
                        code1 = "498";
                        code2 = "980";
                        break;
                    case 28:
                        code1 = "980";
                        code2 = "784";
                        break;
                    case 29:
                        code1 = "784";
                        code2 = "682";
                        break;
                    case 30:
                        code1 = "682";
                        code2 = "356";
                        break;
                    case 31:
                        code1 = "356";
                        code2 = "985";
                        break;
                    case 32:
                        code1 = "985";
                        code2 = "458";
                        break;
                    case 33:
                        code1 = "458";
                        code2 = "764";
                        break;
                    case 34:
                        code1 = "764";
                        code2 = "444";
                        break;
                    default:
                        rate_result.setText("");
                }
                getHtmlFromWeb(date_start.getText().toString(), code1, code2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                rate_result.setText("");
            }
        });

        table_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), TableActivity.class));
            }
        });
    }

    public void RSS_parse(String json){
        Log.e("Parse", json);
        if (json != null) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject fromJson = array.getJSONObject(i);
                    String content = fromJson.getString("title");
                    Massive mes = new Massive();
                    mes.code  = content.split("\\|")[1].substring(16);
                    mes.name  = content.split("\\|")[0].substring(6);
                    mes.rate  = content.split("\\|")[3].substring(7);
                    massive.add(mes);
                }

                cnh_field.setText(massive.get(2).rate.substring(0, massive.get(2).rate.length()-1));
                usd_field.setText(massive.get(0).rate.substring(0, massive.get(0).rate.length()-1));
                eur_field.setText(massive.get(1).rate.substring(0, massive.get(1).rate.length()-1));
                rub_field.setText(massive.get(4).rate.substring(0, massive.get(4).rate.length()-1));

            } catch (final JSONException e) {
                Log.e("Debug", "Incorrect json file");
            }
        }
    }

    private void getHtmlFromWeb(final String new_date, final String c_code, final String c_code2) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder stringBuilder = new StringBuilder();
                try {
                    Document doc = Jsoup.connect("http://nbt.tj/ru/kurs/kurs.php?date="+new_date).get();
                    Elements table = doc.select("table[id=myTable]");
                    //Log.e("WEB33", table.toString());
                    SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("data", 0).edit();
                    editor.putString("html_table", table.toString());
                    editor.apply();

                    for (Element link : table) {
                        stringBuilder.append("\n").append(link.text());
                    }
                } catch (Exception e) {
                    //stringBuilder.append("Error : ").append(e.getMessage()).append("\n");
                    Log.e("WEB", e.toString());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String pre_result = stringBuilder.toString();
                        List<String> code_massive = new ArrayList<>();

                        if(!pre_result.equals("")) {
                            pre_result = pre_result.substring(60);
                            pre_result = pre_result.substring(0, pre_result.length() - 77);
                            pre_result = pre_result +" 30 444";
                            boolean check_code = pre_result.contains(c_code);
                            if(check_code){
                                for(int i = 0; i < pre_result.split(" ").length; i++){
                                    code_massive.add(pre_result.split(" ")[i]);
                                }
                                Log.e("OTVET", pre_result);
                                Log.e("WEB", code_massive.get(code_massive.indexOf(c_code)));
                                Log.e("WEB2", code_massive.get(code_massive.indexOf(c_code2)));

                                StringBuilder str = new StringBuilder();
                                int b = code_massive.indexOf(c_code2) - code_massive.indexOf(c_code)-2;
                                for(int i = 1; i < b; i++) {
                                    int a = code_massive.indexOf(c_code)+i;
                                    str.append(code_massive.get(a)+" ");
                                }
                                String text1 = String.valueOf(str);
                                int sec_index = code_massive.indexOf(c_code2)-2;
                                result = text1 + "= " + code_massive.get(sec_index) + " Сомони";
                                Log.e("OTVET2", text1);
                                rate_result.setText(result);
                            }
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onBackPressed()
    {
    }
}
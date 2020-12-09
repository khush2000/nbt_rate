package com.example.nbt_rate;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import fr.arnaudguyon.xmltojsonlib.XmlToJson;

public class FeedRequest extends AsyncTask<Void, Void, Void> {

    @SuppressLint("StaticFieldLeak")
    public MainActivity activity;
    private String myResponse;

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        String fromUrl = "https://nbt.tj/ru/kurs/rss.php";
        //Log.e("Debug", fromUrl);
        HttpClient client = new DefaultHttpClient();
        if (fromUrl.toLowerCase().contains("https://")) {
            KeyStore trustStore = null;
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
            try {
                assert trustStore != null;
                trustStore.load(null, null);
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            SSLSocketFactory sf = null;
            try {
                sf = new HTTPSHandler(trustStore);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            }
            assert sf != null;
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            client = new DefaultHttpClient(ccm, params);
        }

        HttpGet request = new HttpGet();
        try {
            request.setURI(new URI(fromUrl));
            HttpResponse response = client.execute(request);

            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            //BufferedReader reader = new BufferedReader(new InputStreamReader(is, "ISO_8859_1"), 8);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");

            String resString = sb.toString();
            is.close();

            //Log.e("Debug", resString);
            XmlToJson xmlToJson = new XmlToJson.Builder(resString).build();
            myResponse = xmlToJson.toString();

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (myResponse != null) {
            Log.e("Debug", myResponse);
            try{
                JSONObject jObject1 = new JSONObject(myResponse);
                String json1 = jObject1.getString("rss");

                JSONObject jObject2 = new JSONObject(json1);
                String json2 = jObject2.getString("channel");

                JSONObject jObject3 = new JSONObject(json2);
                String json3 = jObject3.getString("item");
                //Log.e("Debug", json3);

                //Save feed..
                SharedPreferences.Editor editor = activity.getApplicationContext().getSharedPreferences("data", 0).edit();
                editor.putString("rss_feed", json3);
                editor.apply();

                activity.RSS_parse(json3);

            }catch (Exception ex){
                Log.e("Debug", ex.toString());
            }
        } else {
            Toast.makeText(activity.getBaseContext(), "Server is not responding!", Toast.LENGTH_SHORT).show();
        }
    }
}

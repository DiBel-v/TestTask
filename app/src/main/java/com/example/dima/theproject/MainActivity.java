package com.example.dima.theproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.dima.theproject.models.ModelRockets;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView rocketsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rocketsList = (ListView) findViewById(R.id.rockets);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);

    }

    public class JSONread extends AsyncTask<String,String,List<ModelRockets>>{

        String data ="";
        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader=null;

        @Override
        protected List<ModelRockets> doInBackground(String... params) {

            try {
                URL urlJson = new URL(params[0]);
                httpURLConnection = ( HttpURLConnection)urlJson.openConnection();
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";

                while((line=bufferedReader.readLine())!=null){
                    data=data + line;
                }

                JSONArray jsonArray = new JSONArray(data);
                SimpleDateFormat dateFormat = new SimpleDateFormat();

                List<ModelRockets> modelRocketsList = new ArrayList<>();
                Date dateT = new Date();

                for(int i=0;i<jsonArray.length();i++){
                    JSONObject mainObject = (JSONObject) jsonArray.get(i);
                    ModelRockets modelRockets = new ModelRockets();
                    JSONObject rocketInfo = mainObject.getJSONObject("rocket");
                    JSONObject links = mainObject.getJSONObject("links");

                    dateT.setTime(mainObject.getInt("launch_date_unix"));

                    modelRockets.setUnixDate("Launch Unix date: " + dateFormat.format(dateT));
                    modelRockets.setNameOfRockets(rocketInfo.getString("rocket_name"));
                    modelRockets.setDetails(mainObject.getString("details"));
                    modelRockets.setImage(links.getString("mission_patch"));


                    modelRocketsList.add(modelRockets);
                }

                return modelRocketsList;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }   finally {
                if(httpURLConnection!=null){
                    httpURLConnection.disconnect();
                }
                try {
                    if(bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ModelRockets> result) {
            super.onPostExecute(result);
            RocketAdapter adapter = new RocketAdapter(getApplicationContext(),R.layout.row,result);
            rocketsList.setAdapter(adapter);
        }
    }


    public class RocketAdapter extends ArrayAdapter{

        private int resource;
        public List<ModelRockets> modelRocketsList;
        private LayoutInflater inflater;

        public RocketAdapter(Context context, int resource, List<ModelRockets> objects) {
            super(context, resource, objects);
            modelRocketsList = objects;
            this.resource=resource;
            inflater=(LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position,View convertView, ViewGroup parent){

            ViewHolder holder = null;

            if(convertView==null){
                holder = new ViewHolder();
                convertView = inflater.inflate(resource, null);
                holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                holder.rocketName = (TextView) convertView.findViewById(R.id.rocketName);
                holder.details = (TextView) convertView.findViewById(R.id.details);
                holder.dateUnix = (TextView) convertView.findViewById(R.id.dateUnix);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }




            final ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar2);

            ImageLoader.getInstance().displayImage(modelRocketsList.get(position).getImage(), holder.imageView, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    progressBar.setVisibility(View.GONE);
                }
            });

            holder.rocketName.setText(modelRocketsList.get(position).getNameOfRockets());
            holder.details.setText(modelRocketsList.get(position).getDetails());
            holder.dateUnix.setText(modelRocketsList.get(position).getUnixDate());
            return convertView;
        }

        class ViewHolder{
            private ImageView imageView;
            private TextView rocketName;
            private TextView details;
            private TextView dateUnix;

        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.action_refresh){
            new JSONread().execute("https://api.spacexdata.com/v2/launches?launch_year=2017");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

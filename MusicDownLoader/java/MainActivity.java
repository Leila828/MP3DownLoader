package com.example.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
Context context;
 MyAsyncTask myTask;
 ConditionVariable mCondition;
 MediaPlayer mediaPlayer;
    ImageButton pause;
    ImageButton play;
    ImageButton stop;
  private static int REQUEST_CODE=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText url=(EditText) findViewById(R.id.edit);
        final Button Download=(Button) findViewById(R.id.buttonD);
     final   ImageButton pause=(ImageButton) findViewById(R.id.button2);
        final ImageButton play=(ImageButton) findViewById(R.id.button1);
        final ImageButton stop=(ImageButton) findViewById(R.id.button3);
        mediaPlayer=new MediaPlayer();
        myTask=new MyAsyncTask(context);
        pause.setVisibility(View.GONE);
        play.setVisibility(View.GONE);
        stop.setVisibility(View.GONE);

        //i added this lines just because the permission in the manifest didn't work
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, REQUEST_CODE);

      final boolean[] test={false};
      Download.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Uri uri=Uri.parse(url.getText().toString());
               Toast.makeText(MainActivity.this, url.getText().toString(), Toast.LENGTH_SHORT).show();
               myTask.execute(uri);

          }
      });

      play.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View v) {
          if (test[0]==false){
            try {
               mediaPlayer.setDataSource(Environment.getExternalStorageDirectory()+ "/Download" +"/mysong.mp3");
               mediaPlayer.prepare();

               test[0]=true;
            } catch (IOException e) {
               e.printStackTrace();
//               Toast.makeText(context, "Song not Found", Toast.LENGTH_SHORT).show();
            }
          }
           mediaPlayer.start();
           pause.setVisibility(View.VISIBLE);
           stop.setVisibility(View.VISIBLE);
           play.setVisibility(View.GONE);
       }

     });
      pause.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              mediaPlayer.pause();
              pause.setVisibility(View.GONE);
              stop.setVisibility(View.VISIBLE);
              play.setVisibility(View.VISIBLE);
          }
      });
      stop.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              mediaPlayer.stop();
              mediaPlayer.reset();
              play.setVisibility(View.VISIBLE);
              pause.setVisibility(View.GONE);
              stop.setVisibility(View.GONE);
              test[0]=false;
          }
      });


    }
    public class MyAsyncTask extends AsyncTask<Uri,Integer,Integer>{
private Context context;
final ImageButton play=(ImageButton) findViewById(R.id.button1);

public MyAsyncTask(Context context){this.context=context;};

        @Override
        protected Integer doInBackground(Uri... uris) {
            int count=uris.length;
           DownLoadData(uris[0]);
            return count;
        }
        @Override
        protected void onPostExecute(Integer count){
            Toast.makeText(MainActivity.this, count+" File DownLoaded", Toast.LENGTH_SHORT).show();
            play.setVisibility(View.VISIBLE);
        }
    }

    private void DownLoadData(Uri uris) {

        DownloadManager downloadManager=(DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request=new DownloadManager.Request(uris);
        request.setTitle("My Song");
        request.setDescription("Downloading . . . .");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"mysong.mp3");

        final long downloadId=downloadManager.enqueue(request);
        mCondition = new ConditionVariable(false);

        IntentFilter  filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == reference) {
                    mCondition.open(); }
            }
        };
        getApplicationContext().registerReceiver(receiver, filter);
        mCondition.block();
    }

}

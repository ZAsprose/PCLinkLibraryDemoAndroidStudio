package com.taidoc.pclinklibrary.demo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.taidoc.pclinklibrary.demo.util.GuiUtils;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by XieDugu on 2016/1/15.
 */
public class NoSensorMySQLHW extends Activity {

    private Button submit;
    private EditText threshold;
    private TextView weighttext;
    private TextView hwoutput;

    private String thresholdstr;
    private String weightstr;
    private int thresholdint;
    private int weightint;
    private String hwdb;

    public java.sql.Connection pcon;
    public Session session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getIntent().getExtras();
        String weight = "11";
        weight = bundle.getString("weight");
        Log.i("Intent", weight);
        setContentView(R.layout.hello_world_proj_without_sensor);
        findViews();
        /*
        new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... integers) {
                try{
                    sshconnect();
                    dbconnect();
                }catch(Exception e){
                    Log.i("onCreate", "onCreate failure...");
                }
                return null;
            }
        }.execute(1);*/
        setListeners(weight);
        init();
        weighttext.setText(weight);
    }
    /**
     * 設定此Activity會用到的View
     */
    private void findViews() {
        submit = (Button) findViewById(R.id.ButSubmithw);
        threshold = (EditText) findViewById(R.id.thresholdhw);
        weighttext = (TextView) findViewById(R.id.weighthw);
        hwoutput = (TextView) findViewById(R.id.texthw);
    }

    // ssh link
    public void sshconnect(){
        session = null;
        ChannelExec openChannel = null;
        JSch jsch = new JSch();
        try {
            session = jsch.getSession("zzz847", "murphy.wot.eecs.northwestern.edu");//user, host
            session.setPassword("Duguxie@93531asprose");//password
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            session.setPortForwardingL(3306, "127.0.0.1", 3306);
            Log.i("SSH", "ssh link success");
        } catch (JSchException e) {
            Log.i("SSH", "ssh link fail...");
        }
    }

    // mysql link
    public java.sql.Connection dbconnect(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Log.i("Driver", "driver success");
        } catch (Exception ex) {
            Log.i("Driver", "driver failure...");
        }
        try {
            pcon = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mhealthplay?autoReconnect=true", "mhealth", "mhealth");
                    //(Connection) DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mhealth?user=mhealth&password=mhealth&useUnicode=true&characterEncoding=UTF-8play&autoReconnect=true&failOverReadOnly=false&maxReconnects=10");
            if(pcon!=null) {
                Log.i("mysql", "mysql connection success");
            }
        } catch (SQLException e) {
            Log.i("mysql", "mysql connection failure...");
        }
        return pcon;
    }

    // mysql insert
    public int dbinsert(int t, int w, String h){
        if (pcon != null) {
            Statement stmt = null;
            try {
                Log.i("mysql", "mysql con not null");
                stmt = pcon.createStatement();
                int result = stmt.executeUpdate("insert into zzjhw " +
                        "VALUES(null," + t + "," + w + ",'" + h + "')");
                Log.i("mysql", "mysql insert " + result);
                stmt.close();
                return result;
            } catch (SQLException e) {
                Log.i("mysql", e.getErrorCode() + e.getMessage());
                e.printStackTrace();
                Log.i("mysql", "mysql insert failure...");
                return -1;
            }
        } else {
            Log.i("mysql", "mysql connection not set...");
            return -1;
        }
    }

    // helloworld
    public int all(int t, int w, String h){
        try{
            sshconnect();
            dbconnect();
            dbinsert(t,w,h);
            return 1;
        }catch(Exception e){
            Log.i("onCreate", "onCreate failure...");
            return -1;
        }

    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        /*try {

        } catch (SQLException e) {
            e.printStackTrace();
        }*/
        GuiUtils.goToPCLinkLibraryHomeActivity(NoSensorMySQLHW.this);
    }

    private void setListeners(final String weight) {
        submit.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //get the input
                thresholdstr = threshold.getText().toString();
                Log.i("onClick", weight);
                weightstr = weight;
                //judge if legal input
                if (thresholdstr != null && weightstr != null &&
                        thresholdstr != "" && weightstr != "") {
                    Pattern pattern = Pattern.compile("[0-9]*");
                    Matcher thre = pattern.matcher(thresholdstr);
                    Matcher wei = pattern.matcher(weightstr);
                    if (thre.matches() && wei.matches()) {
                        thresholdint = Integer.parseInt(thresholdstr);
                        weightint = Integer.parseInt(weightstr);
                        if (weightint > thresholdint)
                            hwdb = "hello";
                        else
                            hwdb = "world";
                        //send message to mysql
                        new AsyncTask<Integer, Void, Void>() {
                            @Override
                            protected Void doInBackground(Integer... integers) {
                                try{
                                    sshconnect();
                                    dbconnect();
                                    dbinsert(thresholdint, weightint, hwdb);
                                    pcon.close();
                                }catch(Exception e){
                                    Log.i("onCreate", "onCreate failure...");
                                }
                                return null;
                            }
                        }.execute(1);
                        hwoutput.setText("Data input success!\n" + hwdb);
                    } else {
                        hwoutput.setText("wrong input!");
                    }
                } else {
                    hwoutput.setText("Input empty!");
                }
            }
        });
    }

    private void init() {}
}

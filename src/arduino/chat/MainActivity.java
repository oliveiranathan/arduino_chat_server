package arduino.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bEntrar = (Button)findViewById(R.id.bEntrar);

        bEntrar.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {

                Logger.getAnonymousLogger().log(Level.SEVERE, "onClick" );

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Logger.getAnonymousLogger().log(Level.SEVERE, "run" );

                EditText etNick = (EditText)findViewById(R.id.etNick);

                String nick  = etNick.getText().toString();

                try
                {
                	Logger.getAnonymousLogger().log(Level.SEVERE, "tentando..." );
                    Socket sCliente = new Socket( "192.168.0.104", 6666);
                    
                    
                    PrintWriter pw = new PrintWriter( sCliente.getOutputStream() );
                    
                    pw.print( "##nick:"+nick+"\n" );
                    pw.flush();
                    Logger.getAnonymousLogger().log(Level.SEVERE, "enviado..." );
                    //pw.close();

                    BufferedReader br = new BufferedReader( new InputStreamReader( sCliente.getInputStream() ));

                    String resp = br.readLine();
                    sCliente.close();
                    Logger.getAnonymousLogger().log(Level.SEVERE, "foi" );

                    if (resp.equals("##ok"))
                    {
                        Intent intent = new Intent( getApplicationContext(), ChatActivity.class );

                        startActivity(intent);
                    }

                    else
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Esse usuário já existe.", Toast.LENGTH_LONG).show();
                            }
                        });

                    }


                } catch (IOException e)
                {
                    e.printStackTrace();
                }

                    }
                }).start();

            }
        });
    }


}

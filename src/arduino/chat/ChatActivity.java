package arduino.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Ghost on 20/10/13.
 */
public class ChatActivity extends Activity {
	private String server = "192.168.0.104";
	private int porta = 6667;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		// thread do brodcast
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					DatagramSocket socket = new DatagramSocket(porta);
					socket.setBroadcast(true);
					DatagramPacket packet;
					byte[] buff;
					final TextView tChat = (TextView) findViewById(R.id.textChat);

					while (true) {
						Logger.getAnonymousLogger().log(Level.SEVERE, "VAIIII");
						buff = new byte[256];
						packet = new DatagramPacket(buff, buff.length);

						socket.receive(packet);

						final String msg = new String(packet.getData());
						Logger.getAnonymousLogger().log(Level.SEVERE,
								"recebida <- " + msg);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								tChat.append(msg + "\n");
							}
						});

					}
				} catch (IOException ex) {

				}
			}
		}).start();

		Button bEnviar = (Button) findViewById(R.id.bEnviar);

		bEnviar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						final EditText etChat = (EditText) findViewById(R.id.etChat);
						byte[] buff = etChat.getText().toString().getBytes();

						try {
							Logger.getAnonymousLogger().log(Level.SEVERE,
									"Mensagem -> " + new String(buff));
							DatagramPacket packet = new DatagramPacket(buff,
									buff.length, InetAddress.getByName(server),
									porta + 1);

							DatagramSocket socket = new DatagramSocket(
									porta + 1);
							socket.send(packet);

							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									etChat.setText("");
								}
							});

							socket.close();
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (SocketException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();

			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		sairChat();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		sairChat();
		onDestroy();
	}
	
	private void sairChat(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Socket sCliente;
				
				try {
					sCliente = new Socket( "192.168.0.104", 6666);
					
					PrintWriter pw = new PrintWriter( sCliente.getOutputStream() );
	                
	                pw.print( "##saindo:\0" );
	                pw.flush();
	                
	                sCliente.close();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

}

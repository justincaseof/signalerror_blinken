package vu.de.signalerror.tcpproxy;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.BoxLayout;

import org.apache.log4j.PropertyConfigurator;

public class TcpProxyGUI extends Frame implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private Panel panel_Main = new Panel();
	private Panel panel_Buttons = new Panel();

	private Panel panel_listenIPandPort = new Panel();
	private Panel panel_targetIPandPort = new Panel();

	private TextField textField_listenIPandPort = new TextField("127.0.0.1:3200");
	private TextField textField_targetIPandPort = new TextField("192.168.1.2:80");

	private Label label_listenIPandPort = new Label("Listen IP and Port");
	private Label label_targetIPandPort = new Label("Target IP and Port");

	private HashMap<Integer, ServerThread> servers;

	private Button cmd(String caption)
	{
		Button cmd = new Button(caption);
		cmd.addActionListener(this);
		return cmd;
	}

	public TcpProxyGUI()
	{
		super("JCO Test");

		servers = new HashMap<Integer, ServerThread>();

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});

		setupLayout();

		this.setVisible(true);

		connect("127.0.0.1:3200", "192.168.1.2:80");
		connect("127.0.0.1:8000", "10.55.10.48:8000");

	}

	private void setupLayout()
	{
		// set layout
		LayoutManager layoutManager = new BoxLayout(panel_Main, BoxLayout.Y_AXIS);
		panel_Main.setLayout(layoutManager);

		// set same size for all labels
		label_listenIPandPort.setMinimumSize(new Dimension(100, 50));
		label_targetIPandPort.setMinimumSize(new Dimension(100, 50));

		// adjust textField size
		textField_listenIPandPort.setColumns(40);
		textField_targetIPandPort.setColumns(40);

		LayoutManager gridLayout = new GridLayout();

		// adjust orientation
		panel_listenIPandPort.setLayout(gridLayout);
		panel_targetIPandPort.setLayout(gridLayout);

		panel_listenIPandPort.setMaximumSize(new Dimension(600, 30));
		panel_targetIPandPort.setMaximumSize(new Dimension(600, 30));

		/* listen */
		panel_listenIPandPort.add(label_listenIPandPort);
		panel_listenIPandPort.add(textField_listenIPandPort);
		panel_Main.add(panel_listenIPandPort);

		/* target */
		panel_targetIPandPort.add(label_targetIPandPort);
		panel_targetIPandPort.add(textField_targetIPandPort);
		panel_Main.add(panel_targetIPandPort);

		/* buttons */
		panel_Buttons.add(cmd("Add"));
		panel_Main.add(panel_Buttons);

		add(panel_Main);
		setSize(500, 300);
	}

	/**
	 * Button-Events
	 **/
	public void actionPerformed(ActionEvent e)
	{
		String caption = e.getActionCommand();

		if (caption.equals("Add"))
			connect(textField_listenIPandPort.getText(), textField_targetIPandPort.getText());

	}

	private void connect(String listenIPandPort, String targetIPandPort)
	{
		try
		{
			System.out.println(listenIPandPort + " --> " + targetIPandPort);

			int colon_listen = listenIPandPort.indexOf(":");
			String listenIP = listenIPandPort.substring(0, colon_listen);
			Integer listenPort = Integer.valueOf(listenIPandPort.substring(colon_listen + 1));
			if (servers.containsKey(listenPort))
			{
				ServerThread server = servers.get(listenPort);
				server.closeServer();
			}
			ServerSocket listenSocket = new ServerSocket(listenPort);

			int colon_target = targetIPandPort.indexOf(":");
			String targetIP = targetIPandPort.substring(0, colon_target);
			Integer targetPort = Integer.valueOf(targetIPandPort.substring(colon_target + 1));
			Socket targetSocket = new Socket(targetIP, targetPort);

			ServerThread newServer = new ServerThread(listenSocket, targetSocket);
			newServer.start();
			servers.put(listenPort, newServer);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		try
		{
			File config = new File("C:\\log4j.properties");
			Properties props = new Properties();
			props.load(config.toURL().openStream());
			PropertyConfigurator.configure(props);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

		new TcpProxyGUI();
	}

	private class ServerThread extends Thread
	{
		private ServerSocket listenServerSocket;
		private Socket target;
		private boolean running = true;

		public ServerThread(ServerSocket listenServerSocket, Socket target)
		{
			this.listenServerSocket = listenServerSocket;
			this.target = target;
		}

		@Override
		public void run()
		{
//			while(true)
//			{
//				try
//				{
//					boolean bREQ = false;
//					InputStream in = null;
//					OutputStream pOutputStream = null;
//					InputStream pIutputStream = null;
//					OutputStream outputStream = null;
//					Socket socket = null;
//					int byteCheckFl = 0;
//					int intPrevCounter = 0;
//					int intCounter = 0;
//					
//					try
//					{
//						socket = listenServerSocket.accept();
//						in = socket.getInputStream();
//						pOutputStream = target.getOutputStream();
//						
//						while ((intCounter = in.read()) != -1)
//						{
//							if (intCounter == -1)
//								break;
//							if (!bREQ)
//								bREQ = true;
//							pOutputStream.write(intCounter);
//							if (byteCheckFl > 0)
//							{
//								break;
//							}
//							if (intCounter == 13 && intPrevCounter == 10)
//								byteCheckFl++;
//							intPrevCounter = intCounter;
//						}
//					}
//					catch (Exception e)
//					{
//						if (!bREQ)
//						{
//							continue;
//						}
//					}
//		
//					pOutputStream.flush();
//					pIutputStream = target.getInputStream();
//					outputStream = socket.getOutputStream();
//					try
//					{
//						while ((intCounter = pIutputStream.read()) != -1)
//						{
//							outputStream.write(intCounter);
//						}
//					}
//					catch (Exception e)
//					{
//					}
//					outputStream.flush();
//				}
//				catch (Exception e)
//				{
//					e.printStackTrace();
//				}
//			}
			try
			{
					System.err.println("Waiting for connection.");
					Socket listenSocket = listenServerSocket.accept();
					InputStream is1 = null;
					OutputStream os1 = null;
					InputStream is2 = null;
					OutputStream os2 = null;

					while(running)
					{
						// // ## ## \\\\
						// System.err.println("REQUEST:\n-------\n");
						is1 = listenSocket.getInputStream();
						os1 = target.getOutputStream();

						int c1 = -1;
						
//						while ( (c1=is1.read())!=-1 )
						while (is1.available() > 0)
						{
							c1 = is1.read();
							os1.write((byte) c1);
							System.err.print((char) c1);
							if (c1 == '\n')
							{
								os1.flush();
							}
						}

						// // ## ## \\\\
						// System.out.println("RESPONSE:\n---------\n");
						is2 = target.getInputStream();
						os2 = listenSocket.getOutputStream();

						int c2 = -1;
//						while ( (c2=is2.read())!=-1 )
						while (is2.available() > 0)
						{
							c2 = is2.read();
							os2.write((byte) c2);
							System.out.print((char) c2);
							if (c2 == '\n')
							{
								os2.flush();
							}
						}
					}
					if (is1 != null)
						is1.close();
					if (is2 != null)
						is2.close();
					if (os1 != null)
						os1.close();
					if (os2 != null)
						os2.close();
					System.err.println("Closed Server.");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		public void closeServer()
		{
			try
			{
				running = false;
				listenServerSocket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	};

}

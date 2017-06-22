package FactoryTester;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import protocol.ComPackage;
import protocol.RxAnalyse;


public class MainFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static DatagramSocket CommSocket = null;
	private static final int CommPort = 6000;
	private static final String CommIP = "192.168.4.1";

	private static ComPackage rxData = new ComPackage();

//	private JLabel pic_lab = null;
	private JPanel VolPanel = new JPanel();
	private JPanel VelPanel = new JPanel();
	private JPanel EurPanel = new JPanel();
	private JPanel InitRetPanel = new JPanel();
	private JTextField VoltText = new JTextField(5);
	private JTextField VelXText = new JTextField(5);
	private JTextField VelYText = new JTextField(5);
	private JTextField PitchText = new JTextField(5);
	private JTextField RollText = new JTextField(5);

	private JLabel IMUSta = new JLabel();
	private JLabel BAROSta = new JLabel();
	private JLabel MTDSta = new JLabel();
	private JLabel FLOWSta = new JLabel();
	private JLabel TOFSta = new JLabel();

	public MainFrame() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
//				pic_lab = new JLabel();
//				pic_lab.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("cha.png")).getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT)));
//				pic_lab.setPreferredSize(new Dimension(50, 50));
//				add(pic_lab);

				VolPanel.add(new JLabel("voltage: "));
				VolPanel.add(VoltText);
				VoltText.setEditable(false);

				VelPanel.add(new JLabel("Vel_X: "));
				VelPanel.add(VelXText);
				VelPanel.add(new JLabel("Vel_Y: "));
				VelPanel.add(VelYText);

				EurPanel.add(new JLabel("Pitch: "));
				EurPanel.add(PitchText);
				EurPanel.add(new JLabel("Roll: "));
				EurPanel.add(RollText);

				IMUSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("wait_s.gif")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
				BAROSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("wait_s.gif")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
				MTDSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("wait_s.gif")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
				FLOWSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("wait_s.gif")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
				TOFSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("wait_s.gif")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
				InitRetPanel.add(new JLabel("IMU: "));
				InitRetPanel.add(IMUSta);
				InitRetPanel.add(new JLabel("BARO: "));
				InitRetPanel.add(BAROSta);
				InitRetPanel.add(new JLabel("MTD: "));
				InitRetPanel.add(MTDSta);
				InitRetPanel.add(new JLabel("FLOW: "));
				InitRetPanel.add(FLOWSta);
				InitRetPanel.add(new JLabel("TOF: "));
				InitRetPanel.add(TOFSta);

				add(VolPanel);
				add(VelPanel);
				add(EurPanel);
				add(InitRetPanel);

				setLayout(new GridLayout(4, 1));
				Toolkit tool = getToolkit();
				setIconImage(tool.getImage(MainFrame.class.getResource("FactoryTest.png")));

				setResizable(false);
				setTitle("kyChu.FactoryTester");
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				addWindowListener(wl);
				setSize(400, 150);
				setLocationRelativeTo(null);
				setVisible(true);
			}
		});
		try {
			CommSocket = new DatagramSocket(CommPort);
		} catch (SocketException e) {
			JOptionPane.showMessageDialog(null, e, "error!", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		new Thread(new UpgradeRxThread()).start();
		new Thread(new UpgradeTxThread()).start();
		new Thread(new SignalTestThread()).start();
	}

	private class UpgradeRxThread implements Runnable {
		public void run() {
			while(true) {
				byte[] data = new byte[100];
				DatagramPacket packet = new DatagramPacket(data, 0, data.length);
				try {
					CommSocket.receive(packet);
					byte[] recData = packet.getData();
					for(int i = 0; i < packet.getLength(); i ++)
						RxAnalyse.rx_decode(recData[i]);
					if(RxAnalyse.GotNewPackage()) {
						GotResponseFlag = true;
						synchronized(new String("")) {//unnecessary (copy).
							try {
								rxData = (ComPackage) RxAnalyse.RecPackage.PackageCopy();
								if(rxData.type == ComPackage.TYPE_FC_Response) {
									if((rxData.rData[1] & 0x01) == 0x01) {
										IMUSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("cha.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
									} else {
										IMUSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("dui.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
									}
									if((rxData.rData[1] & 0x02) == 0x02) {
										BAROSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("cha.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
									} else {
										BAROSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("dui.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
									}
									if((rxData.rData[1] & 0x08) == 0x08) {
										MTDSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("cha.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
									} else {
										MTDSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("dui.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
									}
									if((rxData.rData[2] & 0x08) == 0x08) {
										TOFSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("cha.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
									} else {
										if((rxData.rData[2] & 0x04) == 0x04) {
											TOFSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("warning.jpg")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
										} else {
											TOFSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("dui.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
										}
									}
									if((rxData.rData[2] & 0x20) == 0x20) {
										FLOWSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("cha.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
									} else {
										if((rxData.rData[2] & 0x10) == 0x10) {
											FLOWSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("warning.jpg")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
										} else {
											FLOWSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("dui.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
										}
									}
									int val = rxData.rData[14] & 0xFF;
									VoltText.setText("" + ((float)val + 640.0)/71.0);
									PitchText.setText("" + rxData.readoutFloat(5));
									RollText.setText("" + rxData.readoutFloat(9));
									VelXText.setText("" + rxData.readoutFloat(19));
									VelYText.setText("" + rxData.readoutFloat(23));
								}
							} catch (CloneNotSupportedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private class UpgradeTxThread implements Runnable {
		public void run() {
			while(true) {
				String SendBuffer = "test string.";
				DatagramPacket packet = new DatagramPacket(SendBuffer.getBytes(), 0, SendBuffer.length(), new InetSocketAddress(CommIP, CommPort));
				try {
					CommSocket.send(packet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					TimeUnit.MILLISECONDS.sleep(100);//100ms
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.err.println("Interrupted");
				}
			}
		}
	}

	private static boolean GotResponseFlag = false;
	private static int SignalLostCnt = 0;
	private class SignalTestThread implements Runnable{
		public void run() {
			while(true) {
				if(GotResponseFlag == false) {
					if(SignalLostCnt < 20)
						SignalLostCnt ++;
					else {
						SignalLostCnt = 0;
						IMUSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("wait_s.gif")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
						BAROSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("wait_s.gif")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
						MTDSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("wait_s.gif")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
						FLOWSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("wait_s.gif")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
						TOFSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("wait_s.gif")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
					}
				} else {
					SignalLostCnt = 0;
					GotResponseFlag = false;
				}
				try {
					TimeUnit.MILLISECONDS.sleep(50);//50ms loop.
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.err.println("Interrupted");
				}
			}
		}
	}

	WindowAdapter wl = new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	};

	public static void main(String[] args) {
		new MainFrame();
	}
}

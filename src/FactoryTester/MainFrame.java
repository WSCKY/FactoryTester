package FactoryTester;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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
	private static ComPackage txData = new ComPackage();

	private JPanel InfoPanel = new JPanel();
	private JPanel InitRetPanel = new JPanel();
	private JPanel VersionPanel = new JPanel();
	private JPanel VoltagePanel = new JPanel();

	private JTextField VoltText = new JTextField(5);
	private JTextField VelXText = new JTextField(5);
	private JTextField VelYText = new JTextField(5);
	private JTextField PitchText = new JTextField(5);
	private JTextField RollText = new JTextField(5);

	private JProgressBar VoltCalBar = new JProgressBar(0, 100);
	private JButton Calib_H = new JButton("高压校准");
	private JButton Calib_L = new JButton("低压校准");

	private JTextField VER_txt = new JTextField(9);
	private JTextField DSN_txt = new JTextField(16);
	private JButton bUpdateDSN = new JButton("更新");

	private JLabel IMUSta = new JLabel();
	private JLabel BAROSta = new JLabel();
	private JLabel MTDSta = new JLabel();
	private JLabel FLOWSta = new JLabel();
	private JLabel TOFSta = new JLabel();

	public MainFrame() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				InitRetPanel.setLayout(new GridLayout(1, 5, 0, 0));
				InitRetPanel.setBorder(BorderFactory.createTitledBorder(null, "飞控外设", 0, 2, new Font("宋体", Font.PLAIN, 16)));
				IMUSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("wait_s.gif")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
				BAROSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("wait_s.gif")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
				MTDSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("wait_s.gif")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
				FLOWSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("wait_s.gif")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
				TOFSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("wait_s.gif")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
				JLabel NameLabel = new JLabel("IMU: "); NameLabel.setFont(new Font("宋体", Font.BOLD, 20));
				JPanel p = new JPanel(); p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 8));
//				p.setBorder(BorderFactory.createLineBorder(Color.RED));
				p.add(NameLabel); p.add(IMUSta); InitRetPanel.add(p);

				NameLabel = new JLabel("气压计: "); NameLabel.setFont(new Font("宋体", Font.BOLD, 20));
				p = new JPanel(); p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 8));
//				p.setBorder(BorderFactory.createLineBorder(Color.RED));
				p.add(NameLabel); p.add(BAROSta); InitRetPanel.add(p);

				NameLabel = new JLabel("FLASH: "); NameLabel.setFont(new Font("宋体", Font.BOLD, 20));
				p = new JPanel(); p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 8));
//				p.setBorder(BorderFactory.createLineBorder(Color.RED));
				p.add(NameLabel); p.add(MTDSta); InitRetPanel.add(p);

				NameLabel = new JLabel("光流: "); NameLabel.setFont(new Font("宋体", Font.BOLD, 20));
				p = new JPanel(); p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 8));
//				p.setBorder(BorderFactory.createLineBorder(Color.RED));
				p.add(NameLabel); p.add(FLOWSta); InitRetPanel.add(p);

				NameLabel = new JLabel("红外: "); NameLabel.setFont(new Font("宋体", Font.BOLD, 20));
				p = new JPanel(); p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 8));
//				p.setBorder(BorderFactory.createLineBorder(Color.RED));
				p.add(NameLabel); p.add(TOFSta); InitRetPanel.add(p);

				InfoPanel.setLayout(new GridLayout(1, 5));
				InfoPanel.setBorder(BorderFactory.createTitledBorder(null, "状态信息", 0, 2, new Font("宋体", Font.PLAIN, 16)));
				NameLabel = new JLabel("电压: "); NameLabel.setFont(new Font("宋体", Font.BOLD, 20));
				p = new JPanel(); p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 8));
				p.add(NameLabel); p.add(VoltText); InfoPanel.add(p);
				VoltText.setEditable(false);

				NameLabel = new JLabel("速度X: "); NameLabel.setFont(new Font("宋体", Font.BOLD, 20));
				p = new JPanel(); p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 8));
				p.add(NameLabel); p.add(VelXText); InfoPanel.add(p);
				VelXText.setEditable(false);

				NameLabel = new JLabel("速度Y: "); NameLabel.setFont(new Font("宋体", Font.BOLD, 20));
				p = new JPanel(); p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 8));
				p.add(NameLabel); p.add(VelYText); InfoPanel.add(p);
				VelYText.setEditable(false);

				NameLabel = new JLabel("俯仰: "); NameLabel.setFont(new Font("宋体", Font.BOLD, 20));
				p = new JPanel(); p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 8));
				p.add(NameLabel); p.add(PitchText); InfoPanel.add(p);
				PitchText.setEditable(false);

				NameLabel = new JLabel("横滚: "); NameLabel.setFont(new Font("宋体", Font.BOLD, 20));
				p = new JPanel(); p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 8));
				p.add(NameLabel); p.add(RollText); InfoPanel.add(p);
				RollText.setEditable(false);

				VoltagePanel.setBorder(BorderFactory.createTitledBorder(null, "电压校准", 0, 2, new Font("宋体", Font.PLAIN, 16)));
				VoltagePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 25, 0));
				VoltCalBar.setPreferredSize(new Dimension(700, 23)); VoltagePanel.add(VoltCalBar);
				Calib_H.setPreferredSize(new Dimension(100, 40)); VoltagePanel.add(Calib_H);
				Calib_L.setPreferredSize(new Dimension(100, 40)); VoltagePanel.add(Calib_L);

				VersionPanel.setBorder(BorderFactory.createTitledBorder(null, "版本管理", 0, 2, new Font("宋体", Font.PLAIN, 16)));
				VersionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 3));
				NameLabel = new JLabel("版本: "); NameLabel.setFont(new Font("宋体", Font.BOLD, 20));
				VersionPanel.add(NameLabel);
				VER_txt.setFont(new Font("Courier New", Font.BOLD, 26));
				VER_txt.setEditable(false); VersionPanel.add(VER_txt);
				NameLabel = new JLabel("序列号: "); NameLabel.setFont(new Font("宋体", Font.BOLD, 20));
				VersionPanel.add(NameLabel);
				DSN_txt.setFont(new Font("Courier New", Font.BOLD, 26));
				DSN_txt.setEditable(false); VersionPanel.add(DSN_txt);
				bUpdateDSN.setPreferredSize(new Dimension(100, 40));
				bUpdateDSN.setFont(new Font("宋体", Font.BOLD, 20));
				bUpdateDSN.setEnabled(false);
				VersionPanel.add(bUpdateDSN);

				add(InitRetPanel);
				add(InfoPanel);
				add(VoltagePanel);
				add(VersionPanel);

				setLayout(new GridLayout(4, 1));
				Toolkit tool = getToolkit();
				setIconImage(tool.getImage(MainFrame.class.getResource("FactoryTest.png")));

				setResizable(false);
				setTitle("kyChu.FactoryTester");
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				addWindowListener(wl);
				setSize(1000, 320);
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

	private boolean GotVersionFlag = false;
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
											TOFSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("warning_s.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
										} else {
											TOFSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("dui.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
										}
									}
									if((rxData.rData[2] & 0x20) == 0x20) {
										FLOWSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("cha.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
									} else {
										if((rxData.rData[2] & 0x10) == 0x10) {
											FLOWSta.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("warning_s.png")).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
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
								} else if(rxData.type == ComPackage.TYPE_VERSION_Response) {
									GotVersionFlag = true;
									char ver = rxData.readoutCharacter(0);
									VER_txt.setText("V" + (ver >> 12) + "." + ((ver >> 8) & 0x0F) + "." + (ver & 0x00FF));
									String curDSN = rxData.readoutString(4, 16);
									DSN_txt.setText(curDSN);
									if(curDSN.equals("PXyyMMwwxxxxFn##")) {
										bUpdateDSN.setEnabled(true);
									} else {
										bUpdateDSN.setEnabled(false);
									}
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
				if(GotVersionFlag == false) {
					txData.type = ComPackage.TYPE_VERSION_REQUEST;
					txData.addByte((byte)0x0F, 0);
					txData.setLength(3);
				}
				else {
					txData.type = ComPackage.TYPE_ProgrammableTX;
					txData.addByte(ComPackage.Program_Hover, 0);
					txData.addFloat(0.0f, 1);
					txData.addFloat(0.0f, 5);
					txData.addByte((byte)0, 9);
					txData.addFloat(0.0f, 10);
					txData.setLength(16);
				}
				byte[] SendBuffer = txData.getSendBuffer();
				DatagramPacket packet = new DatagramPacket(SendBuffer, 0, SendBuffer.length, new InetSocketAddress(CommIP, CommPort));
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
						GotVersionFlag = false;
						VER_txt.setText("");
						DSN_txt.setText("");
						bUpdateDSN.setEnabled(false);
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
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date Today = new Date();
		try {
			Date InvalidDay = df.parse("2017-7-22");
			if(Today.getTime() > InvalidDay.getTime()) {
				JOptionPane.showMessageDialog(null, "Sorry, Exit With Unkonw Error!", "error!", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new MainFrame();
	}
}

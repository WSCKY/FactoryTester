package FactoryTester;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
//import java.util.Timer;
//import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import protocol.ComPackage;
import protocol.RxAnalyse;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private static int DroneType = 1;

	private static DatagramSocket CommSocket = null;
	private static final int CommPort = 6000;
	private static final String CommIP = "192.168.4.1";

	private static ComPackage rxData = new ComPackage();
	private static ComPackage txData = new ComPackage();

	DSNGenerator dsnGenerator = null;

	private JPanel InfoPanel = new JPanel();
	private JPanel InitRetPanel = new JPanel();
	private JPanel VersionPanel = new JPanel();
	private JPanel VoltagePanel = new JPanel();
	private JPanel LEDPanel = new JPanel();
	private JPanel ESCBurnInPanel = new JPanel();

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

	private JCheckBox Red_Box = new JCheckBox("红");
	private JCheckBox Blue_Box = new JCheckBox("蓝");
	private JCheckBox Green_Box = new JCheckBox("绿");

	private JProgressBar ESCBurnInBar = new JProgressBar(0, 100);
	private JButton StartBurnInBtn = new JButton("开始");
	private JButton StopBurnInBtn = new JButton("停止");

	private JDialog DroneSelectDialog = new JDialog(this, "设置机型");
	private JRadioButton F1_sel = new JRadioButton("虹湾F1", true);
	private JRadioButton F2_sel = new JRadioButton("虹湾F2", false);
	private ButtonGroup sel_bg = new ButtonGroup();
	private JButton DroneConfirmButton = new JButton("确定");

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
				VoltCalBar.setPreferredSize(new Dimension(700, 30)); VoltCalBar.setString("");
				VoltCalBar.setFont(VoltCalBar.getFont().deriveFont(Font.ITALIC | Font.BOLD, 16));
				VoltCalBar.setStringPainted(true); VoltagePanel.add(VoltCalBar);
				Calib_H.setPreferredSize(new Dimension(100, 40)); VoltagePanel.add(Calib_H); Calib_H.setEnabled(false);
				Calib_L.setPreferredSize(new Dimension(100, 40)); VoltagePanel.add(Calib_L); Calib_L.setEnabled(false);
				Calib_H.addActionListener(hbl); Calib_L.addActionListener(lbl);

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
				bUpdateDSN.setEnabled(false); bUpdateDSN.addActionListener(ubl);
				VersionPanel.add(bUpdateDSN);

				LEDPanel.setBorder(BorderFactory.createTitledBorder(null, "主状态灯", 0, 2, new Font("宋体", Font.PLAIN, 16)));
				LEDPanel.setBackground(new Color(120, 120, 120)); LEDPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 150, 0));
				Red_Box.setFont(new Font("宋体", Font.BOLD, 30)); Red_Box.addActionListener(ledl);
				Blue_Box.setFont(new Font("宋体", Font.BOLD, 30)); Blue_Box.addActionListener(ledl);
				Green_Box.setFont(new Font("宋体", Font.BOLD, 30)); Green_Box.addActionListener(ledl);
				LEDPanel.add(Red_Box); LEDPanel.add(Blue_Box); LEDPanel.add(Green_Box);

				ESCBurnInPanel.setBorder(BorderFactory.createTitledBorder(null, "老化测试", 0, 2, new Font("宋体", Font.PLAIN, 16)));
				ESCBurnInPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 25, 5));
				ESCBurnInBar.setPreferredSize(new Dimension(700, 30)); ESCBurnInBar.setString("");
				ESCBurnInBar.setFont(ESCBurnInBar.getFont().deriveFont(Font.ITALIC | Font.BOLD, 16));
				ESCBurnInBar.setStringPainted(true); ESCBurnInPanel.add(ESCBurnInBar);
				StartBurnInBtn.setPreferredSize(new Dimension(100, 40)); StartBurnInBtn.setEnabled(false);
				StartBurnInBtn.setFont(StartBurnInBtn.getFont().deriveFont(Font.BOLD, 20));
				StartBurnInBtn.addActionListener(bstartl);
				StopBurnInBtn.setPreferredSize(new Dimension(100, 40)); StopBurnInBtn.setEnabled(false);
				StopBurnInBtn.setFont(StopBurnInBtn.getFont().deriveFont(Font.BOLD, 20));
				StopBurnInBtn.addActionListener(bstopl);
				ESCBurnInPanel.add(StartBurnInBtn); ESCBurnInPanel.add(StopBurnInBtn);

				add(InitRetPanel);
				add(InfoPanel);
				add(VoltagePanel);
				add(VersionPanel);
				add(LEDPanel);
				add(ESCBurnInPanel);

				setLayout(new GridLayout(6, 1));
				Toolkit tool = getToolkit();
				setIconImage(tool.getImage(MainFrame.class.getResource("FactoryTest.png")));

				setResizable(false);
				setTitle("kyChu.FactoryTester V0.1.0");
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				addWindowListener(wl);
				setSize(1000, 500);
				setLocationRelativeTo(null);
				setVisible(true);

				/* Drone Type Selector. */
				F1_sel.setFont(F1_sel.getFont().deriveFont(Font.BOLD, 16));
				F1_sel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						DroneType = 1;
					}
				});
				F2_sel.setFont(F2_sel.getFont().deriveFont(Font.BOLD, 16));
				F2_sel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						DroneType = 2;
					}
				});
				sel_bg.add(F1_sel); sel_bg.add(F2_sel);
				DroneConfirmButton.setPreferredSize(new Dimension(90, 36));
				DroneConfirmButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						DroneSelectDialog.dispose();
					}
				});
				DroneSelectDialog.setResizable(false);
				DroneSelectDialog.setSize(280, 140);
				DroneSelectDialog.setLocationRelativeTo(null);
				DroneSelectDialog.setModal(true);
				DroneSelectDialog.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 15));
				DroneSelectDialog.add(F1_sel); DroneSelectDialog.add(F2_sel);
				DroneSelectDialog.add(DroneConfirmButton);
				DroneSelectDialog.getRootPane().setDefaultButton(DroneConfirmButton);
				DroneSelectDialog.setVisible(true);

				dsnGenerator = new DSNGenerator(DroneType);
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
//		Timer timer = new Timer();
//		timer.schedule(new TimerTask() {
//			public void run() {
//				System.out.println("/* 2000ms.... */");
//			}
//		}, 2000);
	}

	private boolean GotVersionFlag = false;
	private boolean WriteNewDSNFlag = false;
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
						if(VoltCalibStartFlag == false) {
							Calib_H.setEnabled(true); Calib_L.setEnabled(true);
						}
						StopBurnInBtn.setEnabled(true);
						if(ESCBurnInRunningFlag == false) {
							StartBurnInBtn.setEnabled(true);
						}
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
									if(WriteNewDSNFlag == true) {
										if(curDSN.equals(_NewDSN)) {
											WriteNewDSNFlag = false;
											bUpdateDSN.setEnabled(false);
											dsnGenerator.SaveThisDSN();
										}
									}
								} else if(rxData.type == ComPackage.TYPE_ADC_CALIB_ACK) {
									if(rxData.rData[2] != 0x0) { /* Exception. */
										Calib_H.setEnabled(true);
										Calib_L.setEnabled(true);
										VoltCalBar.setValue(0);
										VoltCalBar.setString("");
										VoltCalibStartFlag = false;
										VoltCalibState = 0;/* Exit Calibrate. */
										if(rxData.rData[2] == 0x1)
											JOptionPane.showMessageDialog(null, "电压错误！", "error!", JOptionPane.ERROR_MESSAGE);
//										else if(rxData.rData[2] == 0x2)
//											JOptionPane.showMessageDialog(null, "采样错误！", "error!", JOptionPane.ERROR_MESSAGE);
										else
											JOptionPane.showMessageDialog(null, "未知错误！", "error!", JOptionPane.ERROR_MESSAGE);
									} else {
										VoltCalBar.setValue(rxData.rData[1]);
										VoltCalBar.setString((rxData.rData[0] == ComPackage.ADC_CALIBRATE_H ? "H" : "L") + " Sampling ..." + rxData.rData[1] + "%");
										if(rxData.rData[1] >= 100) {//complete.
											Calib_H.setEnabled(true);
											Calib_L.setEnabled(true);
											VoltCalBar.setValue(0);
											VoltCalBar.setString("");
											VoltCalibStartFlag = false;
											VoltCalibState = 0;/* Exit Calibrate. */
											JOptionPane.showMessageDialog(null, "校准完成！", "ok!", JOptionPane.INFORMATION_MESSAGE);
										}
									}
								}
							} catch (CloneNotSupportedException e) {
								e.printStackTrace();
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static byte VoltCalibState = 0;
	private static int _wDSN_CmdTog = 0;
	private class UpgradeTxThread implements Runnable {
		public void run() {
			while(true) {
				if(VoltCalibState == ComPackage.ADC_CALIBRATE_H || VoltCalibState == ComPackage.ADC_CALIBRATE_L) {
					txData.type = ComPackage.TYPE_ADC_CALIBRATE;
					txData.addByte(VoltCalibState, 0);
					txData.addByte((byte)(VoltCalibState ^ 0xAA), 1);
					txData.setLength(4);
				} else if(ESCBurnInRunningFlag == true) {
					txData.type = ComPackage.TYPE_ESC_BURN_IN_TEST;
					txData.addByte(ESCBurnExpSpeed, 0);
					txData.addByte((byte) (ESCBurnExpSpeed ^ 0xCC), 1);
					txData.setLength(4);
				} else if(GotVersionFlag == false) {
					txData.type = ComPackage.TYPE_VERSION_REQUEST;
					txData.addByte((byte)0x0F, 0);
					txData.setLength(3);
				} else if(WriteNewDSNFlag == true) {
					if(_wDSN_CmdTog % 2 == 0) {
						txData.type = ComPackage.TYPE_VERSION_REQUEST;
						txData.addByte((byte)0x0F, 0);
						txData.setLength(3);
					} else {
						txData.type = ComPackage.TYPE_DSN_UPDATE;
						txData.addBytes(_NewDSN.getBytes(), 16, 0);
						txData.addByte((byte)0xBB, 16);
						txData.setLength(19);
					}
					_wDSN_CmdTog ++;
				} else {/* no operation */
					txData.type = ComPackage.TYPE_DeviceCheckReq;
					txData.addByte(ComPackage._dev_LED, 0);
					txData.addByte(LEDValue, 1);
					txData.addFloat(0.0f, 5);
					txData.addByte((byte)0, 9);
					txData.addFloat(0.0f, 10);
					txData.setLength(10);
				}
				byte[] SendBuffer = txData.getSendBuffer();
				DatagramPacket packet = new DatagramPacket(SendBuffer, 0, SendBuffer.length, new InetSocketAddress(CommIP, CommPort));
				try {
					CommSocket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					TimeUnit.MILLISECONDS.sleep(100);//100ms
				} catch (InterruptedException e) {
					System.err.println("Interrupted");
				}
			}
		}
	}

	private static boolean GotResponseFlag = false;
	private static int SignalLostCnt = 0;
	private class SignalTestThread implements Runnable {
		public void run() {
			while(true) {
				if(GotResponseFlag == false) {
					if(SignalLostCnt < 20)
						SignalLostCnt ++;
					else {
						SignalLostCnt = 0;
						GotVersionFlag = false;
						VER_txt.setText(""); DSN_txt.setText("");
						bUpdateDSN.setEnabled(false);
						Calib_H.setEnabled(false); Calib_L.setEnabled(false);
						StartBurnInBtn.setEnabled(false); StopBurnInBtn.setEnabled(false);
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
					System.err.println("Interrupted");
				}
			}
		}
	}

	private static boolean VoltCalibStartFlag = false;
	private static byte VoltCalibReqVal = 0;
	private ActionListener hbl = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if(VoltCalibStartFlag == false) {
				Calib_H.setEnabled(false);
				Calib_L.setEnabled(false);
				VoltCalibStartFlag = true;
				VoltCalibReqVal = ComPackage.ADC_CALIBRATE_H;
				new Thread(new VoltSampleWaitThread()).start();
			}
		}
	};

	private ActionListener lbl = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if(VoltCalibStartFlag == false) {
				Calib_H.setEnabled(false);
				Calib_L.setEnabled(false);
				VoltCalibStartFlag = true;
				VoltCalibReqVal = ComPackage.ADC_CALIBRATE_L;
				new Thread(new VoltSampleWaitThread()).start();
			}
		}
	};

	private class VoltSampleWaitThread implements Runnable {
		public void run() {
			int tCnt = 0;
			VoltCalBar.setString("Waiting ...");
			for(tCnt = 0; tCnt < 21; tCnt ++) {
				VoltCalBar.setValue((int) (tCnt * 5));
				try {
					TimeUnit.MILLISECONDS.sleep(100);//100ms loop.
				} catch (InterruptedException e) {
					System.err.println("Interrupted");
				}
			}
			VoltCalibState = VoltCalibReqVal;
		}
	}

	private String _NewDSN = "PXyyMMwwxxxxFn##";
	private ActionListener ubl = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			WriteNewDSNFlag = true;
			bUpdateDSN.setEnabled(false);
			_NewDSN = dsnGenerator.GotNewDSN();
		}
	};

	private byte LEDValue = 0;
	private ActionListener ledl = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			LEDValue = 0;
			int red = 0, blue = 0, green = 0;
			if(Red_Box.isSelected()) {
				red = 255; LEDValue |= (byte)0x01;
			} if(Blue_Box.isSelected()) {
				blue = 255; LEDValue |= (byte)0x02;
			} if(Green_Box.isSelected()) {
				green = 255; LEDValue |= (byte)0x04;
			}
			if(red == 0 && blue == 0 && green == 0)
				red = blue = green = 120;
			LEDPanel.setBackground(new Color(red, green, blue));
		}
	};

	private static boolean ESCBurnInStartFlag = false;
	private static boolean ESCBurnInRunningFlag = false;
	private ActionListener bstartl = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if(ESCBurnInRunningFlag == false) {
				ESCBurnInStartFlag = true;
				ESCBurnInRunningFlag = true;
				StartBurnInBtn.setEnabled(false);
				new Thread(new ESCBurnInThread()).start();
			}
		}
	};

	private byte ESCBurnExpSpeed = 0;
	private class ESCBurnInThread implements Runnable {
		public void run() {
			int TimeCnt = 0;
			long TimeStart = 0;
			while(ESCBurnInStartFlag) {
				if(TimeCnt < 25) {
					TimeCnt ++;
					ESCBurnInBar.setValue(TimeCnt * 4);
					ESCBurnInBar.setString("提速中...");
					TimeStart = System.currentTimeMillis();
				} else if((System.currentTimeMillis() - TimeStart) <= 300000) { //5min
					long t = (System.currentTimeMillis() - TimeStart);
					int min = (int) (t / 60000);
					int sec = (int) ((t % 60000) / 1000);
					ESCBurnInBar.setString(min + "m" + sec + "s");
					ESCBurnInBar.setValue((int) ((System.currentTimeMillis() - TimeStart) / 3000));
				} else {
					ESCBurnInStartFlag = false;
				}
				ESCBurnExpSpeed = (byte) (TimeCnt * 2);
				try {
					TimeUnit.MILLISECONDS.sleep(100);//100ms loop.
				} catch (InterruptedException e) {
					System.err.println("Interrupted");
				}
			}
			ESCBurnInBar.setValue(0);
			while(TimeCnt > 0) {
				TimeCnt --;
				ESCBurnInBar.setString("降速中...");
				ESCBurnExpSpeed = (byte) (TimeCnt * 2);
				try {
					TimeUnit.MILLISECONDS.sleep(100);//100ms loop.
				} catch (InterruptedException e) {
					System.err.println("Interrupted");
				}
			}
			ESCBurnInBar.setString("");
			ESCBurnInRunningFlag = false;
		}
	}

	private ActionListener bstopl = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if(ESCBurnInStartFlag == true) {
				ESCBurnInStartFlag = false;
			}
		}
	};

	WindowAdapter wl = new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
			dsnGenerator.GeneratorClose();
			System.exit(0);
		}
	};

	public static void main(String[] args) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date Today = new Date();
		try {
			Date InvalidDay = df.parse("2018-6-01");
			if(Today.getTime() > InvalidDay.getTime()) {
				JOptionPane.showMessageDialog(null, "Sorry, Exit With Unknow Error!", "error!", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		new MainFrame();
	}
}

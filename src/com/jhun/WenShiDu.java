package com.jhun;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * 串口无线温湿度上位机
 * By:江汉大学物理与信息工程学院
 * @author 陈精华
 */
public class WenShiDu extends JFrame implements ActionListener, SerialPortEventListener {
	
	/**
	 * 自动生成serialVersionUID
	 */
	private static final long serialVersionUID = -6266266021599123514L;
	int WIDTH = 400, HEIGHT = 300;
	JComboBox<?> cbportname, cbdatabits, cbparitybits, cbbaudrate, cbstopbits;
	JButton btsearch, btopen, btclose;
	JTextField tfcurrentport, tf13, tf23, tf33, tf43, tf16, tf26, tf36, tf46;
	JLabel lbstatusbar;
	String portname, databits, paritybits, buadrate, stopbits;
	List<String> portlist = null;
	CommPortIdentifier portid = null;
	SerialPort serialport = null;
	OutputStream outputstream = null;
	InputStream inputstream = null;
	int receviedcount = 0;
	
	/**
     * 构造函数
     */
	public WenShiDu() {		
		super("江汉大学物信学院温湿度上位机");
		setSize(WIDTH, HEIGHT);
		setLocationRelativeTo(null);
		Image icon = null;
		try {
			InputStream inputstream = getClass().getResourceAsStream("/resource/rs232.png");
			icon = ImageIO.read(inputstream);
		} catch (IOException e) {
			showErrorMsg(e.getMessage());
		}
		setIconImage(icon);
		setResizable(false);
		scanSerialPorts();
		initComponents();
		setComponentsEnable(true);
		setSerialPortsPara();
		lbstatusbar.setText(setStatusBarPara());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
		
	/**
	 * 初始化各组件
	 */
	public void initComponents() {
		//字体
		Font lbfont = new Font("微软雅黑", Font.TRUETYPE_FONT, 15);
		Font btfont = new Font("黑体", Font.TRUETYPE_FONT, 11);
		Font tffont = new Font("微软雅黑", Font.TRUETYPE_FONT, 12);
		Font bigfont = new Font("微软雅黑", Font.TRUETYPE_FONT, 18);
		
		//北面板
		JPanel pnnorth = new JPanel();
		pnnorth.setLayout(new GridLayout(3, 5));

		//各Label
		JLabel lbportname = new JLabel("串口号：");
		JLabel lbdatabits = new JLabel("数据位：");
		JLabel lbpartitybits = new JLabel("检验位：");
		JLabel lbbaudrate = new JLabel("波特率：");
		JLabel lbstopbits = new JLabel("停止位：");
		JLabel lbcurrentport = new JLabel("当前串口：");
		lbportname.setFont(lbfont);
		lbdatabits.setFont(lbfont);
		lbpartitybits.setFont(lbfont);
		lbbaudrate.setFont(lbfont);
		lbstopbits.setFont(lbfont);
		lbcurrentport.setFont(lbfont);
		lbportname.setHorizontalAlignment(SwingConstants.CENTER);
		lbdatabits.setHorizontalAlignment(SwingConstants.CENTER);
		lbpartitybits.setHorizontalAlignment(SwingConstants.CENTER);
		lbbaudrate.setHorizontalAlignment(SwingConstants.CENTER);
		lbstopbits.setHorizontalAlignment(SwingConstants.CENTER);
		lbcurrentport.setHorizontalAlignment(SwingConstants.CENTER);
		//各ComboBox
		cbportname = new JComboBox<String>(portlist.toArray(new String[0]));
		tfcurrentport = new JTextField();
		tfcurrentport.setFont(tffont);
		tfcurrentport.setEditable(false);
		tfcurrentport.setText(portlist.toArray(new String[0])[0]);
		cbdatabits = new JComboBox<Integer>(new Integer[]{5, 6, 7, 8});
		cbparitybits = new JComboBox<String>(new String[]{"NONE","ODD","EVEN","MARK","SPACE"});
		cbbaudrate = new JComboBox<Integer>(new Integer[]{300,600,1200,2400,4800,9600,19200,38400,43000,56000,57600,115200});
		cbstopbits = new JComboBox<String>(new String[]{"1","2","1.5"});
		cbdatabits.setSelectedIndex(3);
		cbparitybits.setSelectedIndex(0);
		cbbaudrate.setSelectedIndex(5);
		cbstopbits.setSelectedIndex(0);
		//各Button
		btsearch = new JButton("扫描端口");
		btopen = new JButton("打开端口");
		btclose = new JButton("关闭端口");
		btsearch.setFont(btfont);
		btopen.setFont(btfont);
		btclose.setFont(btfont);
		
		//将各组件添加到北面板中
		pnnorth.add(lbportname);
		pnnorth.add(cbportname);
		pnnorth.add(lbbaudrate);
		pnnorth.add(cbbaudrate);
		pnnorth.add(btsearch);
		pnnorth.add(lbdatabits);
		pnnorth.add(cbdatabits);
		pnnorth.add(lbstopbits);
		pnnorth.add(cbstopbits);
		pnnorth.add(btopen);
		pnnorth.add(lbpartitybits);
		pnnorth.add(cbparitybits);
		pnnorth.add(lbcurrentport);
		pnnorth.add(tfcurrentport);
		pnnorth.add(btclose);
		
		//中面板
		JPanel pncenter = new JPanel();
		pncenter.setLayout(new GridLayout(4, 7));

		JLabel lb11 = new JLabel("点1:");
		JLabel lb21 = new JLabel("点2:");
		JLabel lb31 = new JLabel("点3:");
		JLabel lb41 = new JLabel("点4:");
		JLabel lb12 = new JLabel("温度：");
		JLabel lb22 = new JLabel("温度：");
		JLabel lb32 = new JLabel("温度：");
		JLabel lb42 = new JLabel("温度：");
		JLabel lb14 = new JLabel("℃");
		JLabel lb24 = new JLabel("℃");
		JLabel lb34 = new JLabel("℃");
		JLabel lb44 = new JLabel("℃");		
		JLabel lb15 = new JLabel("湿度：");
		JLabel lb25 = new JLabel("湿度：");
		JLabel lb35 = new JLabel("湿度：");
		JLabel lb45 = new JLabel("湿度：");		
		JLabel lb17 = new JLabel("%");
		JLabel lb27 = new JLabel("%");
		JLabel lb37 = new JLabel("%");
		JLabel lb47 = new JLabel("%");
	
		tf13 = new JTextField();
		tf23 = new JTextField();
		tf33 = new JTextField();
		tf43 = new JTextField();
		tf16 = new JTextField();
		tf26 = new JTextField();
		tf36 = new JTextField();
		tf46 = new JTextField();
		
		tf13.setEditable(false);
		tf23.setEditable(false);
		tf33.setEditable(false);
		tf43.setEditable(false);
		tf16.setEditable(false);
		tf26.setEditable(false);
		tf36.setEditable(false);
		tf46.setEditable(false);
		
		lb11.setFont(bigfont);
		lb12.setFont(bigfont);
		tf13.setFont(bigfont);
		lb14.setFont(bigfont);
		lb15.setFont(bigfont);
		tf16.setFont(bigfont);
		lb17.setFont(bigfont);
		lb21.setFont(bigfont);
		lb22.setFont(bigfont);
		tf23.setFont(bigfont);
		lb24.setFont(bigfont);
		lb25.setFont(bigfont);
		tf26.setFont(bigfont);
		lb27.setFont(bigfont);
		lb31.setFont(bigfont);
		lb32.setFont(bigfont);
		tf33.setFont(bigfont);
		lb34.setFont(bigfont);
		lb35.setFont(bigfont);
		tf36.setFont(bigfont);
		lb37.setFont(bigfont);
		lb41.setFont(bigfont);
		lb42.setFont(bigfont);
		tf43.setFont(bigfont);
		lb44.setFont(bigfont);
		lb45.setFont(bigfont);
		tf46.setFont(bigfont);
		lb47.setFont(bigfont);
		
		lb11.setHorizontalAlignment(SwingConstants.CENTER);
		lb21.setHorizontalAlignment(SwingConstants.CENTER);
		lb31.setHorizontalAlignment(SwingConstants.CENTER);
		lb41.setHorizontalAlignment(SwingConstants.CENTER);
		lb12.setHorizontalAlignment(SwingConstants.RIGHT);
		lb22.setHorizontalAlignment(SwingConstants.RIGHT);
		lb32.setHorizontalAlignment(SwingConstants.RIGHT);
		lb42.setHorizontalAlignment(SwingConstants.RIGHT);
		lb14.setHorizontalAlignment(SwingConstants.CENTER);
		lb24.setHorizontalAlignment(SwingConstants.CENTER);
		lb34.setHorizontalAlignment(SwingConstants.CENTER);
		lb44.setHorizontalAlignment(SwingConstants.CENTER);
		lb15.setHorizontalAlignment(SwingConstants.RIGHT);
		lb25.setHorizontalAlignment(SwingConstants.RIGHT);
		lb35.setHorizontalAlignment(SwingConstants.RIGHT);
		lb45.setHorizontalAlignment(SwingConstants.RIGHT);
		lb17.setHorizontalAlignment(SwingConstants.CENTER);
		lb27.setHorizontalAlignment(SwingConstants.CENTER);
		lb37.setHorizontalAlignment(SwingConstants.CENTER);
		lb47.setHorizontalAlignment(SwingConstants.CENTER);
		tf13.setHorizontalAlignment(4);
		tf23.setHorizontalAlignment(4);
		tf33.setHorizontalAlignment(4);
		tf43.setHorizontalAlignment(4);
		tf16.setHorizontalAlignment(4);
		tf26.setHorizontalAlignment(4);
		tf36.setHorizontalAlignment(4);
		tf46.setHorizontalAlignment(4);
		
		pncenter.add(lb11);
		pncenter.add(lb12);
		pncenter.add(tf13);
		pncenter.add(lb14);
		pncenter.add(lb15);
		pncenter.add(tf16);
		pncenter.add(lb17);
		pncenter.add(lb21);
		pncenter.add(lb22);
		pncenter.add(tf23);
		pncenter.add(lb24);
		pncenter.add(lb25);
		pncenter.add(tf26);
		pncenter.add(lb27);
		pncenter.add(lb31);
		pncenter.add(lb32);
		pncenter.add(tf33);
		pncenter.add(lb34);
		pncenter.add(lb35);
		pncenter.add(tf36);
		pncenter.add(lb37);
		pncenter.add(lb41);
		pncenter.add(lb42);
		pncenter.add(tf43);
		pncenter.add(lb44);
		pncenter.add(lb45);
		pncenter.add(tf46);
		pncenter.add(lb47);
		
		
		//状态栏
		lbstatusbar = new JLabel();
		lbstatusbar.setOpaque(true);
		
		//获取主窗体的容器,并将以上三面板以北、中、南的布局整合
		JPanel pncontent = (JPanel)getContentPane();
		pncontent.setLayout(new BorderLayout());
		pncontent.setBorder(new EmptyBorder(0, 0, 0, 0));
		pncontent.add(pnnorth, BorderLayout.NORTH);
		pncontent.add(pncenter, BorderLayout.CENTER);
		pncontent.add(lbstatusbar, BorderLayout.SOUTH);
		
		//为各组件添加监听
		cbportname.addActionListener(this);
		cbdatabits.addActionListener(this);
		cbparitybits.addActionListener(this);
		cbbaudrate.addActionListener(this);
		cbstopbits.addActionListener(this);
		btsearch.addActionListener(this);
		btopen.addActionListener(this);
		btclose.addActionListener(this);
	}
	
	/**
	 * 扫描本机的所有串口
	 */
	public void scanSerialPorts() {
		portlist = new ArrayList<String>();
		Enumeration<?> en = CommPortIdentifier.getPortIdentifiers();
		while(en.hasMoreElements()){
			CommPortIdentifier portid = (CommPortIdentifier)en.nextElement();
			if(portid.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				String name = portid.getName();
				if(!portlist.contains(name)) 
					portlist.add(name);
			}
		}
		if(null == portlist || portlist.isEmpty()) {
			showErrorMsg("未找到可用的串行端口，程序将关闭！");
			System.exit(0);
		}
	}

	/**
	 * 打开选定的串口
	 */
	public void openSerialPort() {
		//定义一个portid，即要打开的端口
		try {
			portid = CommPortIdentifier.getPortIdentifier(portname);
		} catch (NoSuchPortException e) {
			showErrorMsg("打开失败");
			setComponentsEnable(true);
			return;
		}
		
		//打开端口portid
		try {
			serialport = (SerialPort)portid.open("WenShiDu", 2000);
		} catch (PortInUseException e) {
			showErrorMsg("打开失败");
			setComponentsEnable(true);
			return;
		}
	
		//设置打开的串口参数
		int data = Integer.parseInt(this.databits);
		int parity = cbparitybits.getSelectedIndex();
		int stop = cbstopbits.getSelectedIndex()+1;
		int rate = Integer.parseInt(this.buadrate);
		try {
			serialport.setSerialPortParams(rate, data, stop, parity);
		} catch (UnsupportedCommOperationException e) {
			showErrorMsg("设置串口参数失败");
			setComponentsEnable(true);
			return;
		}
		
		//打开选定端口的输入输出流
		try {
			outputstream = serialport.getOutputStream();
			inputstream = serialport.getInputStream();
		} catch (IOException e) {
			showErrorMsg("接收数据异常！");
			return;
		}
		
		//给端口添加监视
		try {
			serialport.addEventListener(this);
		} catch (TooManyListenersException e) {
			showErrorMsg("接收数据异常！");
			return;
		}
		
		//设置端口允许接收数据
		serialport.notifyOnDataAvailable(true);
		
		lbstatusbar.setText("打开端口"+portname+"成功");
	}
	
	/**
	 * 关闭当前打开的端口
	 */
	public void closeSerialPort() {	
		try {
			if(outputstream != null)
				outputstream.close();
			if(serialport != null)
				serialport.close();
			outputstream = null;
			serialport = null;
			receviedcount = 0;
			lbstatusbar.setText("关闭端口"+portname+"成功");
			tf13.setText("");
			tf23.setText("");
			tf33.setText("");
			tf43.setText("");
			tf16.setText("");
			tf26.setText("");
			tf36.setText("");
			tf46.setText("");
		} catch (IOException e) {
			showErrorMsg(e.getMessage());
		}
	}
	
	/**
	 * 改变串口参数
	 */
	public void setSerialPortsPara() {
		portname = cbportname.getSelectedItem().toString();
		databits = cbdatabits.getSelectedItem().toString();
		paritybits = cbparitybits.getSelectedItem().toString();
		buadrate = cbbaudrate.getSelectedItem().toString();
		stopbits = cbstopbits.getSelectedItem().toString();
	}
	
	/**
	 * 改变状态栏信息
	 * @return String - 状态栏上显示的信息
	 */
	public String setStatusBarPara() {
		StringBuffer strbuff = new StringBuffer("当前串口:");
		strbuff.append(portname);
		strbuff.append("，数据位:");
		strbuff.append(databits);
		strbuff.append("，停止位:");
		strbuff.append(stopbits);
		strbuff.append("，校验位:");
		strbuff.append(paritybits);
		strbuff.append("，波特率:");
		strbuff.append(buadrate);
		return strbuff.toString();
	}

	/**
	 * 改变组件是否Enable
	 * @param able 是否使能
	 */
	public void setComponentsEnable(boolean abled) {
		cbportname.setEnabled(abled);
		cbdatabits.setEnabled(abled);
		cbparitybits.setEnabled(abled);
		cbstopbits.setEnabled(abled);
		cbbaudrate.setEnabled(abled);
		btsearch.setEnabled(abled);
		btopen.setEnabled(abled);
		btclose.setEnabled(!abled);
	}
	
	/**
	 * 界面组件动作监听
	 * @param e 界面动作ActionEvent
	 */
    @Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == cbportname
				|| e.getSource() == cbdatabits
				|| e.getSource() == cbparitybits
				|| e.getSource() == cbbaudrate
				|| e.getSource() == cbstopbits) {
			setSerialPortsPara();
			tfcurrentport.setText(portname);
			lbstatusbar.setText(setStatusBarPara());
		}
		if(e.getSource() == btsearch) {
			setSerialPortsPara();
			scanSerialPorts();
			lbstatusbar.setText(setStatusBarPara());
			showErrorMsg("扫描完成！端口"+portlist+"可用");
		}
		if(e.getSource() == btopen) {
			setComponentsEnable(false);
			openSerialPort();
		}
		if(e.getSource() == btclose) {
			setComponentsEnable(true);
			closeSerialPort();
		}
	}
	
	/**
	 * 串口事件监听
	 * @param e 串口事件SerialPortEvent
	 */
    @Override
	public void serialEvent(SerialPortEvent e) {
		switch (e.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE: {
				byte[] receivebuffer = new byte[50];
				try {
					while (inputstream.available() > 0) {
						inputstream.read(receivebuffer);
						String str = new String(receivebuffer).trim();
						String wendu = str.substring(0, str.indexOf(","))
								.replaceFirst("^-0*", "-").replaceFirst("^-\\.", "-0.")
								.replaceFirst("^0*", "").replaceFirst("^\\.", "0.");
						if(str.substring(0, str.indexOf(",")).equals("FFFFF"))
							wendu = "离线";
						String shidu = str.substring(str.indexOf(",")+1,str.indexOf(":")).replaceFirst("^0*", "");
						if(str.substring(str.indexOf(",")+1,str.indexOf(":")).equals("FF"))
							shidu = "离线";
						String spotNo = str.substring(str.indexOf(":")+1,str.length());
						int no = Integer.parseInt(spotNo);
						switch (no) {
							case 0:{
								tf13.setText(wendu);
								tf16.setText(shidu);
								break;
							}
							case 1:{
								tf23.setText(wendu);
								tf26.setText(shidu);
								break;
							}
							case 2:{
								tf33.setText(wendu);
								tf36.setText(shidu);
								break;
							}
							case 3:{
								tf43.setText(wendu);
								tf46.setText(shidu);
								break;
							}	
							default:break;
						}
						receviedcount++;
						lbstatusbar.setText("接收: "+receviedcount);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			default: break;
		}
	}
	
	/**
	 * 显示错误信息
	 * @param msg 错误信息
	 */
	public void showErrorMsg(String msg) {
		JOptionPane.showMessageDialog(this, msg);
	}

	/**
	 * 主函数，程序入口
	 * @param args 主函数参数
	 */
	public static void main(String[] args) {
		new WenShiDu();
	}
}

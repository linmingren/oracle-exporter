package export;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalIconFactory.FolderIcon16;
import javax.swing.text.DefaultCaret;

import org.apache.log4j.Logger;

public class MainPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1388494243022621849L;

	private final Logger		logger	= Logger.getLogger(MainPanel.class);
	
	JPanel fieldPanel = new JPanel();
	JPanel sharedPanel = new JPanel();
	JPanel oraclePanel = new JPanel();
	JPanel buttonPanel = new JPanel();
	
	JTextField sidField = new JTextField(Config.getSID());
	JTextField userField = new JTextField(Config.getUser());
	JPasswordField pwdField = new JPasswordField(Config.getPwd());
	JTextField dumpFileField = new JTextField(Config.getDir());
	JTextField sharedUserField = new JTextField(Config.getSharedUser());
	JPasswordField sharedPwdField = new JPasswordField(Config.getSharedPwd());
	
	
	JTextArea  logArea = new JTextArea(5,20);
	boolean firstLogging = true;
	
	
	private void updateTextArea(final String text) {
		if (firstLogging) {
			firstLogging = false;
			logArea.setText("");
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				logArea.append(text);
			}
		});
	}

	private void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		//System.setErr(new PrintStream(out, true));
	}
	  
	  
	public void init() {
		fieldPanel.setLayout(new BorderLayout());
		oraclePanel.setBorder(BorderFactory.createTitledBorder("Cracle����"));
		sharedPanel.setBorder(BorderFactory.createTitledBorder("����Ŀ¼����"));
		fieldPanel.add(oraclePanel,BorderLayout.NORTH);
		fieldPanel.add(sharedPanel,BorderLayout.CENTER);
		
		JScrollPane sp = new JScrollPane(logArea);
		logArea.setText("������������������ı�����");
		logArea.setEditable(false);
		DefaultCaret caret = (DefaultCaret)logArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		//logArea.setEnabled(false);

	    redirectSystemStreams();

		
		this.setLayout(new BorderLayout(10,10));
		
		add(fieldPanel, BorderLayout.NORTH);
		add(sp, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		
		GridBagLayout experimentLayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = 30;
		c.weightx = 1;
		oraclePanel.setLayout(experimentLayout);
		sharedPanel.setLayout(experimentLayout);
		JLabel sidLabel = new JLabel("Oracle SID");
		JLabel userLabel = new JLabel("Oracle�û���");
		JLabel pwdLabel = new JLabel("Oracle����");
		JLabel dumpFileLabel = new JLabel("�����ļ�Ŀ¼");
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets= new Insets(10,0,0,0);
		c.ipadx = 10;
		c.weightx = 0.0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		oraclePanel.add(sidLabel,c);
	    c.weightx = 1;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 2;
		oraclePanel.add(sidField,c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.0;
		c.gridwidth = 1;
		oraclePanel.add(userLabel,c);
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1.0;
		c.gridwidth = 2;
		oraclePanel.add(userField,c);
		
		
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0.0;
		c.gridwidth = 1;
		oraclePanel.add(pwdLabel,c);
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 1.0;
		c.gridwidth = 2;
		pwdField.setEchoChar('*');
		oraclePanel.add(pwdField,c);
		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;
		c.gridwidth = 1;
		sharedPanel.add(dumpFileLabel,c);
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1.0;
		c.gridwidth = 1;
		sharedPanel.add(dumpFileField,c);
		dumpFileField.setToolTipText(dumpFileField.getText());

		dumpFileField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				dumpFileField.setToolTipText(dumpFileField.getText());
			}
		});

		 
		 
		c.weightx = 0.0;
		c.gridx = 3;
		JButton fileChooseButton = new JButton("ѡ��Ŀ¼", new FolderIcon16());
		
		fileChooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectDir();
			}
		});
		
		sharedPanel.add(fileChooseButton,c);
		
		
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.0;
		c.gridwidth = 1;
		sharedPanel.add(new JLabel("�û���"),c);
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1.0;
		c.gridwidth = 2;
		sharedPanel.add(sharedUserField,c);
		
		
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0.0;
		c.gridwidth = 1;
		sharedPanel.add(new JLabel("����"),c);
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 1.0;
		c.gridwidth = 2;
		pwdField.setEchoChar('*');
		sharedPanel.add(sharedPwdField,c);
		
		
		JButton exportButton = new JButton("����");
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				export();
			}
		});
		
		JButton importButton = new JButton("����");
		importButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				importTables();
			}
		});
		
		JButton clearLogButton = new JButton("�����־���");
		clearLogButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logArea.setText("");
			}
		});
		
		buttonPanel.add(importButton);
		buttonPanel.add(exportButton);
		buttonPanel.add(clearLogButton);
	}
	
	private void selectDir() {
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.showOpenDialog(this);
		
		if (fc.getSelectedFile() != null) {
			this.dumpFileField.setText(fc.getSelectedFile().getAbsolutePath());
		}
	}
	
	private void saveConf() {
		//save to properties file
    	Config.setSID(sidField.getText().trim());
    	Config.setUser(userField.getText().trim());
    	Config.setPwd(pwdField.getText().trim());
    	Config.setDir(dumpFileField.getText().trim());
    	Config.setSharedUser(sharedUserField.getText());
    	Config.setSharedPwd(sharedPwdField.getText().trim());
	}
	
	private void export() {
		saveConf();
		
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				Sqlplus sqlplus = new Sqlplus(userField.getText().trim(),pwdField.getText().trim(), sidField.getText().trim());
				int ret = 0;
				
				File f = new File(dumpFileField.getText().trim());
				if (!f.exists()) {
					f.mkdir();
				}
				
				sqlplus.setPhase("Export");
		    	try {
					ret = sqlplus.createDir(dumpFileField.getText().trim());
					if (ret == 0 ) {
			    		logger.info("���õ���Ŀ¼�ɹ�");
			    	} else {
			    		logger.info("���õ���Ŀ¼ʧ��");
			    	}
				} catch (IOException e) {
					logger.error("���õ���Ŀ¼ʧ��",e);
				}
		    	
		    	if (ret != 0) {
		    		return;
		    	}
		    	
		    	try {
					Config.save();
				} catch (IOException e) {
					logger.error("�޷�����������Ϣ",e);
				}
		    	
		    	ret = sqlplus.exportAllTables();
		    	
		    	if (ret == 0 ) {
		    		logger.info("�������ݳɹ�");
		    	} else {
		    		logger.info("��������ʧ��");
		    	}
				
			}
			
		});
		t.start();
	}
	
	private void importTables() {
		saveConf();

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				Sqlplus sqlplus = new Sqlplus(userField.getText().trim(),
						pwdField.getText().trim(), sidField.getText().trim());

				sqlplus.setPhase("Import");
				try {
					sqlplus.createDir(dumpFileField.getText().trim());
				} catch (IOException e) {
					logger.error("�޷�����Ŀ¼", e);
				}

				int ret = sqlplus.importAllTables();
				if (ret == 0) {
					logger.info("�������ݳɹ�");
				} else {
					logger.info("��������ʧ��");
				}
			}
		});

		t.start();
	}
}

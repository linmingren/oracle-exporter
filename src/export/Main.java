package export;



import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;


public class Main {

	private final static Logger		logger	= Logger.getLogger(Main.class);
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		URL xmlFileUrl = Sqlplus.class.getResource("/conf/Log4jConfiguration.xml");
		DOMConfigurator.configure(xmlFileUrl);
		
		Config.load();
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			logger.info("�޷�ʹ��ϵͳ���",e);
		
		}
		
		if (args.length >0) {
			if (args[1].equals("-export")) {
				export();
			} else {
				importTables();
			}
		} else {
			showGUI();
		}
	}
	
	
	private static void export() {

		Sqlplus sqlplus = new Sqlplus(Config.getUser(), Config.getPwd(), Config.getSID());
		int ret = 0;

		sqlplus.setPhase("Export");
		File f = new File(Config.getDir());
		if (!f.exists()) {
			f.mkdir();
		}

		try {
			ret = sqlplus.createDir(Config.getDir());
			if (ret == 0) {
				logger.info("���õ���Ŀ¼�ɹ�");
			} else {
				logger.info("���õ���Ŀ¼ʧ��");
			}
		} catch (IOException e) {
			logger.error("���õ���Ŀ¼ʧ��", e);
		}

		if (ret != 0) {
			return;
		}

		ret = sqlplus.exportAllTables();

		if (ret == 0) {
			logger.info("�������ݳɹ�");
		} else {
			logger.info("��������ʧ��");
		}
	}
		
	

	private  static void importTables() {
		int ret = 0;
		Sqlplus sqlplus = new Sqlplus(Config.getUser(), Config.getPwd(),
				Config.getSID());
		sqlplus.setPhase("Import");
		try {
			ret = sqlplus.createDir(Config.getDir());
			if (ret != 0) {
				logger.error("�޷�����Ŀ¼:  " + ret);
				return;
			}
		} catch (IOException e) {
			logger.error("�޷�����Ŀ¼", e);
			return;
		}

		ret = sqlplus.importAllTables();
		if (ret == 0) {
			logger.info("�������ݳɹ�");
		} else {
			logger.info("��������ʧ��");
		}
	}
		
	
	private static void showGUI() {
		JFrame f = new JFrame("���ݵ��뵼������");

		MainPanel mainPanel = new MainPanel();
		mainPanel.init();

		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		f.setContentPane(mainPanel);
		
		f.setIconImage(new ImageIcon(f.getClass().getResource("/export/frame.png")).getImage());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setMinimumSize(new Dimension(300, 300));
		f.setPreferredSize(new Dimension(500, 600));
		f.setLocationRelativeTo(null);
		f.pack();

		f.setVisible(true);
	}

}

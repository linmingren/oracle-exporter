package export;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class Config extends Properties {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1243926726265770289L;
	private static Config c = new Config();
	
	static {
		c.setProperty("directory", "export");
	}
	
	public static String getUser() {
		return c.getProperty("user");
	}
	
	public static void setUser(String user) {
		c.setProperty("user", user);
	}
	
	public static String getSharedUser() {
		return c.getProperty("sharedUser");
	}
	
	public static void setSharedUser(String user) {
		c.setProperty("sharedUser", user);
	}
	
	public static String getSharedPwd() {
		return c.getProperty("sharedPwd");
	}
	
	public static void setSharedPwd(String user) {
		c.setProperty("sharedPwd", user);
	}
	
	public static String getPwd() {
		return c.getProperty("password");
	}
	
	public static void setPwd(String user) {
		c.setProperty("password", user);
	}
	
	public static String getSID() {
		return c.getProperty("sid");
	}
	
	public static void setSID(String user) {
		c.setProperty("sid", user);
	}
	
	public static String getDir() {
		return c.getProperty("directory");
	}
	
	public static void setDir(String user) {
		c.setProperty("directory", user);
	}
	

	public static void save() throws IOException {
		FileOutputStream output = new FileOutputStream("conf/config.properties");
		String plainTextPwd = Config.getPwd();
		String plainTextSharedPwd = Config.getSharedPwd();
		
		Config.setPwd(Base64.encode(plainTextPwd.getBytes()));
		Config.setSharedPwd(Base64.encode(plainTextSharedPwd.getBytes()));
		
		c.store(output, null);
		
		Config.setPwd(plainTextPwd);
		Config.setSharedPwd(plainTextSharedPwd);
	}

	public static void load() {
		FileInputStream output;
		try {
			output = new FileInputStream("conf/config.properties");
			c.load(output);
			
			if (Config.getPwd() != null) {
				byte[] bts = Base64.decode(Config.getPwd().getBytes());
				Config.setPwd(new String(bts));
			}

			if (Config.getSharedPwd() != null) {
				byte[] plainTextSharedPwd = Base64.decode(Config.getSharedPwd()
						.getBytes());
				Config.setSharedPwd(new String(plainTextSharedPwd));
			}

		} catch (FileNotFoundException e) {
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Base64DecodingException e) {
		}
		
	}
}

package export;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbSession;

public class SharedFile {
	NtlmPasswordAuthentication auth;
	private final Logger		logger	= Logger.getLogger(SharedFile.class);
	
	public SharedFile(String user, String password) {
		if (user.contains("\\")) {
			user = user.replace("\\", ";");

			auth = new NtlmPasswordAuthentication(user + ":" + password);
		} else if (user.contains("/")) {
			user = user.replace("/", ";");

			auth = new NtlmPasswordAuthentication(user + ":" + password);
		} else {
			auth = new NtlmPasswordAuthentication(null, user, password);
		}
	}
	
	public void logon() throws SmbException, UnknownHostException {
		SmbSession.logon(UniAddress.getByName("10.60.1.109"), auth);
	}

	public void copyToRemote(String fromFile, String remoteUrl) throws Exception {
		InputStream in = null;
		OutputStream out = null;
		try {
			File localFile = new File(fromFile);
			
			if (!localFile.exists()) {
				logger.error("本地文件不存在 " + fromFile);
				return;
			}
			
			String fileName = localFile.getName();
			remoteUrl = remoteUrl.replace("\\", "/");
			SmbFile remoteFile = new SmbFile("smb:" +remoteUrl + "/" + fileName, auth);
			in = new BufferedInputStream(new FileInputStream(localFile));
			out = new BufferedOutputStream(new SmbFileOutputStream(remoteFile));
			byte[] buffer = new byte[1024 * 1024];
			while ((in.read(buffer)) != -1) {
				out.write(buffer);
				buffer = new byte[1024];
			}
		}  finally {
			try {
				if (out != null) {
				    out.close();
				}
				
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	/*
	 * @remoteUrl 比如//10.60.1.109/cvexs/test.txt
	 */
	public void copyToLocal(String remoteUrl, String localDir) throws Exception {
		InputStream in = null;
		OutputStream out = null;
		try {
			remoteUrl = remoteUrl.replace("\\", "/");
			SmbFile remoteFile = new SmbFile("smb:" + remoteUrl,auth);

			remoteFile.connect();
			if (!remoteFile.exists()) {
				logger.error("导出文件在远程目录不存在: " + remoteUrl);
				return;
			}
			
			String fileName = remoteFile.getName();
			File localFile = new File(localDir + File.separator + fileName);
			in = new BufferedInputStream(new SmbFileInputStream(remoteFile));
			out = new BufferedOutputStream(new FileOutputStream(localFile));
			byte[] buffer = new byte[1024];
			while (in.read(buffer) != -1) {
				out.write(buffer);
				buffer = new byte[1024];
			}
		} finally {
			try {
				if (out != null) {
					out.close();
					in.close();
				}
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	public boolean exists(String filePath) {
		SmbFile f;
		filePath = filePath.replace("\\", "/");
		try {
			f = new SmbFile("smb:" + filePath, auth);
			return f.exists();
		} catch (Exception e) {
			logger.error(e);;
		}
		
		return false;
	}
	
	/*public String[] listAllFileNames(String dir) throws MalformedURLException, SmbException {
		SmbFile folder = new SmbFile(dir, auth);
		for (SmbFile f : folder.listFiles()){
			if (f.getName())
		}
	}*/

	public static void main(String[] args) throws Exception {
		URL xmlFileUrl = Sqlplus.class.getResource("/conf/Log4jConfiguration.xml");
		DOMConfigurator.configure(xmlFileUrl);
		SharedFile sf = new SharedFile("arrs\\dlin","2wsx!QA");
		
       // sf.copyToRemote("C:\\Shared\\Export\\./export/export-2014-08-26.imp", "//10.60.1.109/cvexs");
       // sf.copyToLocal("\\\\10.60.1.109\\cvexs\\test.TXT","C:\\Shared\\Export\\./export");
        System.out.println("file exists: " + sf.exists("\\\\10.60.1.109\\cvexs\\export-2014-08-26.imp"));
	}

}

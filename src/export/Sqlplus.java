package export;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class Sqlplus {
	
	private final Logger		logger	= Logger.getLogger(Sqlplus.class);
	
    private String user;
    private String password;
    private String sid;
   
    
    private static String script = "drop directory innotech_oracle_export_folder; \n create directory innotech_oracle_export_folder as '{dir}'; \n quit; \n/";
    private static String defaultCreateDirScript = "drop directory innotech_oracle_export_folder; \n create directory innotech_oracle_export_folder as '{appPath}/export'; \n quit; \n/";
    
    private static String createDirCommand = "{user}/{password}@{sid}";
    private static String dummyUserCommand ;
    private static String directoryParam = "DIRECTORY=innotech_oracle_export_folder";
    private static String dumpFileParam = "DUMPFILE=export-{currentdate}.imp";
    private static String schemasParam = "SCHEMAS={user}";
    private static String contentParam = "CONTENT=ALL";
    private static String replaceTableParam = "TABLE_EXISTS_ACTION=REPLACE";
    
    private String phase = "Export";
    
    
    public Sqlplus(String user, String password, String sid) {
    	this.user = user;
    	this.password = password;
    	this.sid = sid;
    }
    
    public int createDir(String dir) throws IOException {
    	if (dir.startsWith("\\")) {
    		return createRemoteDir(dir);
    	} else {
    		return createLocalDir(dir);
    	}
    }
    
    private int createLocalDir(String dir) throws IOException {
    	
    	String s;
    	if (!dir.contains(":")) {
    		//相对路径,没有盘符
    		String appPath = new File(".").getAbsolutePath();
    		s = defaultCreateDirScript.replace("{appPath}", appPath);
    		renameOldDumpFileIfExists(appPath + "/export");
    	} else {
    		s = script.replace("{dir}", dir);
    		renameOldDumpFileIfExists(dir);
    	}
    	
    	
    	//write to test.sql
    	Files.write(Paths.get("./conf/export.sql"), s.getBytes());
    	
    	String cmd = createDirCommand.replace("{user}", user);
    	dummyUserCommand = cmd.replace("{password}", "***");
    	cmd = cmd.replace("{password}", password);
    	dummyUserCommand = dummyUserCommand.replace("{sid}", sid);
    	cmd = cmd.replace("{sid}", sid);
    	
    	return runScript("sqlplus",cmd,"@conf/export.sql");
    }
    
    private int createRemoteDir(String dir) throws IOException {
    	//先保存到当前目录的export下面
    	String appPath = new File(".").getAbsolutePath();
    	String s = defaultCreateDirScript.replace("{appPath}", appPath);
    	
    	
    	renameOldDumpFileIfExists(appPath + "/export");
    	
    	//write to test.sql
    	Files.write(Paths.get("./conf/export.sql"), s.getBytes());
    	
    	String cmd = createDirCommand.replace("{user}", user);
    	dummyUserCommand = cmd.replace("{password}", "***");
    	cmd = cmd.replace("{password}", password);
    	dummyUserCommand = dummyUserCommand.replace("{sid}", sid);
    	cmd = cmd.replace("{sid}", sid);
    	
    	return runScript("sqlplus",cmd,"@conf/export.sql");
    }
    
    private void renameOldDumpFileIfExists(String fileDir) {
    	
    	if (phase.equalsIgnoreCase("Import")) {
    		return;
    	}
    	
    	
    	SimpleDateFormat fileNameDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	String currentDateString = fileNameDateFormat.format(new Date(System.currentTimeMillis()));
    	String fileName = "export-" + currentDateString + ".imp";
    	
    	String filePath = fileDir + "/" + fileName;
    	
    	File f = new File(filePath);
    	File backupFile = new File(filePath + ".copy");
    	if (backupFile.exists()) {
    		backupFile.delete();
    	}
    	
    	if (f.exists()) {
    		f.renameTo(new File(filePath + ".copy"));
    	}
    }
    
    public int exportAllTables() {
    	phase = "Export";
    	
    	String cmd = createDirCommand.replace("{user}", user);
    	dummyUserCommand = cmd.replace("{password}", "***");
    	cmd = cmd.replace("{password}", password);
    	dummyUserCommand = dummyUserCommand.replace("{sid}", sid);
    	cmd = cmd.replace("{sid}", sid);
    	
    	
    	List<String>scriptParamList = new ArrayList<String>();
    	scriptParamList.add("expdp");
    	scriptParamList.add(cmd);
    	scriptParamList.add(directoryParam);
    	
    	SimpleDateFormat fileNameDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	String currentDateString = fileNameDateFormat.format(new Date(System.currentTimeMillis()));
    	String fileName = dumpFileParam.replace("{currentdate}", currentDateString);
    	
    	dumpFileParam.replace("{currentdate}", currentDateString);
    	
    	
    	scriptParamList.add(fileName);
    	scriptParamList.add(schemasParam.replace("{user}", user));
    	scriptParamList.add(contentParam);
    	
    	
    	if ( runScript(scriptParamList.toArray(new String[0])) != 0 ) {
    		return -1;
    	}
    	
        return copyToRemoteServer();
    }
    
	private int copyToRemoteServer() {
		if (!Config.getDir().startsWith("\\") && !Config.getDir().startsWith("//")) {
			return 0;
		}
		
		SimpleDateFormat fileNameDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String currentDateString = fileNameDateFormat.format(new Date(System
				.currentTimeMillis()));
		// 然后把export目录下的文件复制到远程目录
		try {
			SharedFile sf = new SharedFile(Config.getSharedUser(),
					Config.getSharedPwd());
			String appPath = new File(".").getAbsolutePath();
			sf.copyToRemote(appPath + "/export/export-" + currentDateString
					+ ".imp", Config.getDir());

		} catch (Exception e) {
			logger.error("无法把导出文件复制到远程目录", e);
			return -1;
		}
		return 0;
	}
	
	private int copyToLocalFolder() {
		if (!Config.getDir().startsWith("\\") && !Config.getDir().startsWith("//")) {
			return 0;
		}
		
		SimpleDateFormat fileNameDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String currentDateString = fileNameDateFormat.format(new Date(System
				.currentTimeMillis()));
		String yesterdayDateString = fileNameDateFormat.format(new Date(System
				.currentTimeMillis() - 60 * 60 * 24 * 1000));
		
		// 把远程目录下的文件复制到export目录下.
		try {
			SharedFile sf = new SharedFile(Config.getSharedUser(),
					Config.getSharedPwd());
			String appPath = new File(".").getAbsolutePath();
			
			if (sf.exists(Config.getDir() + "/export-" + currentDateString + ".imp") ) {
				//导出当天的
				sf.copyToLocal(Config.getDir() + "/export-" + currentDateString + ".imp",appPath + "/export");
			} else if (sf.exists(Config.getDir() + "/export-" + yesterdayDateString + ".imp")){
				//否则昨天的,
				sf.copyToLocal(Config.getDir() + "/export-" + yesterdayDateString + ".imp",appPath + "/export");
				File f = new File(appPath + "/export/export-" + yesterdayDateString + ".imp");
				if (f.exists()) {
					f.renameTo(new File(appPath + "/export/export-" + currentDateString + ".imp"));
				}
			}
			

		} catch (Exception e) {
			logger.error("无法把文件复制到本地", e);
			return -1;
		}
		return 0;
	}
    
    public int importAllTables() {
    	
    	if (copyToLocalFolder() != 0 ) {
    		return -1;
    	}
    	
    	phase = "Import";
    	String cmd = createDirCommand.replace("{user}", user);
    	dummyUserCommand = cmd.replace("{password}", "***");
    	cmd = cmd.replace("{password}", password);
    	dummyUserCommand = dummyUserCommand.replace("{sid}", sid);
    	cmd = cmd.replace("{sid}", sid);
    	
    	List<String>scriptParamList = new ArrayList<String>();
    	scriptParamList.add("impdp");
    	scriptParamList.add(cmd);
    	scriptParamList.add(directoryParam);
    	
    	SimpleDateFormat fileNameDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	String currentDateString = fileNameDateFormat.format(new Date(System.currentTimeMillis()));
    	
    	scriptParamList.add(dumpFileParam.replace("{currentdate}", currentDateString));
    	scriptParamList.add(schemasParam.replace("{user}", user));
    	scriptParamList.add(contentParam);
    	scriptParamList.add(replaceTableParam);
    	
    	
    	return runScript(scriptParamList.toArray(new String[0]));
    }
    
    
    public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public int runScript(final String ...command)
	{
		ProcessBuilder pb = new ProcessBuilder(command);

		String cmd = new String();
		int i = 0;
		for (String s: command) {
			if (i++ == 1) {
			   s= dummyUserCommand;
			}
			
			cmd += s + " ";
		}
		
		
		//logger.re
		logger.info("开始执行命令:  " + cmd);
		try
		{
			final Process p = pb.start();
			
			final InputStream err = p.getErrorStream();
			final InputStream in = p.getInputStream();
			
			new Thread(new Runnable(){

				@Override
				public void run()
				{
					BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
					BufferedReader errReader = new BufferedReader(new InputStreamReader(err));

					logger.info("进程 " + command[0] + " 的输出开始" );
					String line = null;
					try
					{
						while((line = inReader.readLine()) != null )
						{
							logger.info(line);
							if (line.contains("ORA-01017")) {
								p.destroy();
								break;
							}
						}
							
					}
					catch(IOException e)
					{
						logger.error("无法获取进程 " + command[0] + " 的标准输出",e);
					}
					
					try
					{
						while((line = errReader.readLine()) != null )
							logger.info(line);
					}
					catch(IOException e)
					{
						logger.error("无法获取进程 " + command[0] + " 的标准输出",e);
					}
					
					logger.info("进程 " + command[0] + " 的输出结束 " );
				}
				
			}).start();
			
			return p.waitFor();
		}
		catch(Exception e)
		{
			logger.error("运行外部进程失败", e);
		}
		
		return 1;
	}
    
    public static void main(String[] args) throws IOException {
    	
    	URL xmlFileUrl = Sqlplus.class.getResource("/conf/Log4jConfiguration.xml");
		DOMConfigurator.configure(xmlFileUrl);
		
    	Sqlplus sqlplus = new Sqlplus("hr","bigband","dlin1");
    	//sqlplus.createDir("c:/test");
    	System.out.println("exit code: " + sqlplus.exportAllTables());
    }
}

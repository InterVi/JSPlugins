package ru.intervi.jsplugins;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import ru.intervi.jsplugins.api.PluginListener;
import ru.intervi.jsplugins.manager.PluginManager;

public class Main {
	private Main() {}
	
	public static Logger LOGGER = Logger.getLogger("JSPlugins");
	private static String HELP[] = {
			"Basic commands:",
			"exit - unloading all plugins and exiting this program",
			"load /path/to/plugin.jar - load plugin",
			"unload PluginName - unload plugin",
			"reload PluginName - reload plugin (call unload and load)",
			"plugins - list loaded plugins",
			"info PluginName - information of plugin",
			"cmds PluginName - commands in plugin",
			"help - this help"
	};
	
	public static void main(String[] args) {
		ConsoleHandler chandler = new ConsoleHandler();
		chandler.setFormatter(new LogFormatter());
		LOGGER.addHandler(chandler);
		LOGGER.setUseParentHandlers(false);
		System.setErr(new PrintStream(new LogOut(true), true));
		System.setOut(new PrintStream(new LogOut(false), true));
		Scanner in = new Scanner(System.in);
		try {
			if (args == null || args.length == 0 || !args[0].equalsIgnoreCase("nolog")) {
				String path = new File(new Main().getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getAbsolutePath();
				FileHandler fhandler = new FileHandler(path + File.separatorChar + "jsplugins.log", true);
				fhandler.setFormatter(new LogFormatter());
				LOGGER.addHandler(fhandler);
			}
			
			LOGGER.warning("=========== STARTING ===========");
			PluginManager manager = new PluginManager(new Main());
			if (new File("./plugins").isDirectory()) {
				LOGGER.info("Loading plugins from ./plugins...");
				for (File file : new File("./plugins").listFiles()) {
					if (!file.isFile() || !file.getName().substring(file.getName().lastIndexOf('.')+1).equalsIgnoreCase("jar")) continue;
					manager.loadPlugin(file);
				}
			}
			
			Loop: while(true) { try {
				String mess = in.nextLine().trim();
				String cmd = null;
				String argv[] = null;
				int ind = mess.indexOf(' ');
				if (ind == -1) cmd = mess;
				else {
					cmd = mess.substring(0, ind).trim();
					mess = mess.substring(ind).trim();
					argv = mess.split(" ");
				}
				switch(cmd.toLowerCase()) {
				case "exit":
					LOGGER.warning("Unloading plugins...");
					for (PluginListener p : manager.getPlugins()) manager.unloadPlugin(p);
					LOGGER.warning("Exiting...");
					break Loop;
				case "load":
					if (argv == null) {
						LOGGER.warning("Invalid path! Use: load /path/to/plugin.jar");
						break;
					}
					manager.loadPlugin(argv[0]);
					break;
				case "unload":
					if (argv == null || !manager.hasPlugin(argv[0])) {
						LOGGER.warning("Invalid name! Use: unload PluginName");
						break;
					}
					manager.unloadPlugin(argv[0]);
					break;
				case "reload":
					if (argv == null || !manager.hasPlugin(argv[0])) {
						LOGGER.warning("Invalid name! Use: reload PluginName");
						break;
					}
					manager.reloadPlugin(argv[0]);
					break;
				case "plugins":
					if (manager.isEmpty()) {
						LOGGER.info("Not loaded plugins.");
						break;
					}
					String pllist = "Plugins(" + manager.size() + "): ";
					for (PluginListener p : manager.getPlugins()) pllist += p.getName() + ", ";
					LOGGER.info(pllist.substring(0, pllist.length()-2));
					break;
				case "info":
					if (argv == null || !manager.hasPlugin(argv[0])) {
						LOGGER.warning("Invalid name! Use: info PluginName");
						break;
					}
					PluginListener pl = manager.getPluginFromName(argv[0]);
					LOGGER.info("===== " + pl.getName() + ": \n" + pl.getDescription() + "\nVersion: " + pl.getVersion() + "\nAuthor: " + pl.getAuthor() +
							"\nE-mail: " + pl.getAuthorEmail() + "\nURL: " + pl.getAuthorURL() + "\n==========");
					break;
				case "cmds":
					if (argv == null || !manager.hasPlugin(argv[0])) {
						LOGGER.warning("Invalid name! Use: cmds PluginName");
						break;
					}
					Map<String, String> cmap = manager.getPluginFromName(argv[0]).getCommands();
					if (cmap == null) {
						LOGGER.info("Not commands.");
						break;
					}
					String[] cinfo = new String[cmap.size()];
					int ci = 0;
					for (Entry<String, String> entry : cmap.entrySet()) {
						cinfo[ci] = entry.getKey() + ": " + entry.getValue();
						ci++;
					}
					LOGGER.info("=== Commands in " + argv[0] + ':');
					for (String s : cinfo) LOGGER.info(s);
					LOGGER.info("==================");
					break;
				case "help":
					LOGGER.info("==================");
					for (String s : HELP) LOGGER.info(s);
					LOGGER.info("==================");
					break;
				default:
					String out = manager.sendCommand(cmd, argv);
					if (out == null) LOGGER.info("null");
					else LOGGER.info(out);
				}
			} catch(Exception e) {e.printStackTrace();}}
		} catch(Exception e) {e.printStackTrace();} finally {in.close();}
	}
	
	public static class LogFormatter extends Formatter {
		@Override
        public String format(LogRecord record) {
            SimpleDateFormat logTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(record.getMillis());
            //[INFO] 03-12-2017 15:32:32 -> ...
            return "[" + record.getLevel() + "] " + logTime.format(cal.getTime()) + " -> " + record.getMessage() + "\n";
        }
	}
	
	/**
	 * перенаправление системных потоков вывода в логгер
	 */
	public static class LogOut extends OutputStream {
		public LogOut(boolean err) {
			ERR = err;
		}
		
		private final boolean ERR;;
		private StringBuffer buf = new StringBuffer();
		private final String SEP = System.getProperty("line.separator");
		
		@Override
		public void write(int b) {
			char ch = (char) b;
			buf.append(ch);
			if (buf.substring(buf.length()-SEP.length()).equals(SEP)) {
				String s = buf.toString().trim();
				if (ERR) LOGGER.warning(s);
				else LOGGER.info(s);
				buf.setLength(0);
			}
		}
		
		@Override
		public void write(byte[] b) {
			for (byte by : b) {
				char ch = (char) by;
				buf.append(ch);
				if (buf.substring(buf.length()-SEP.length()).equals(SEP)) {
					String s = buf.toString().trim();
					if (ERR) LOGGER.warning(s);
					else LOGGER.info(s);
					buf.setLength(0);
				}
			}
		}
	}
}
package ru.intervi.jsplugins.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import ru.intervi.jsplugins.Main;
import ru.intervi.jsplugins.api.InvalidPluginException;
import ru.intervi.jsplugins.api.PluginListener;

public class PluginManager {
	public PluginManager() {
		STANDALONE = false;
	}
	
	public PluginManager(Main main) {
		STANDALONE = true;
	}
	
	private ConcurrentHashMap<String, PluginListener> map = new ConcurrentHashMap<String, PluginListener>();
	private ConcurrentHashMap<String, String> cmdsMap = new ConcurrentHashMap<String, String>();
	private final boolean STANDALONE;
	
	public void loadPlugin(File path) throws NullPointerException, IOException, InvalidPluginException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (!path.isFile()) throw new NullPointerException("not found: " + path.getAbsolutePath());
		URLClassLoader loader = new URLClassLoader(new URL[] {path.toURI().toURL()});
		try {
			InputStream stream = loader.getResourceAsStream("path.txt");
			if (stream == null) throw new NullPointerException("path.txt not found");
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String mainPath = reader.readLine().trim();
			reader.close();
			if (mainPath == null || mainPath.isEmpty()) throw new InvalidPluginException("broken path.txt");
			Class<?> cls = loader.loadClass(mainPath);
			PluginListener plugin = (PluginListener) cls.newInstance();
			plugin.onLoad(this);
			map.put(plugin.getName(), plugin);
			if (plugin.getCommands() != null) for (String cmd : plugin.getCommands().values()) cmdsMap.put(cmd, plugin.getName());
			for (PluginListener p : map.values()) p.onLoadedNewPlugin(plugin);
			if (STANDALONE) Main.LOGGER.warning(plugin.getName() + " loaded");
		} finally {loader.close();}
	}
	
	public void loadPlugin(String path) throws NullPointerException, IOException, InvalidPluginException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		loadPlugin(new File(path));
	}
	
	public void reloadPlugin(PluginListener plugin) throws NullPointerException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, InvalidPluginException {
		File path = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		unloadPlugin(plugin);
		loadPlugin(path);
	}
	
	public void reloadPlugin(String name) throws NullPointerException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, InvalidPluginException {
		reloadPlugin(map.get(name));
	}
	
	public void unloadPlugin(PluginListener plugin) {
		plugin.onUnload();
		for (PluginListener p : map.values()) p.onUnloadPlugin(plugin);
		map.remove(plugin.getName());
		if (plugin.getCommands() != null) for (String cmd : plugin.getCommands().values()) cmdsMap.remove(cmd);
		if (STANDALONE) Main.LOGGER.warning(plugin.getName() + " unloaded");
	}
	
	public void unloadPlugin(String name) {
		unloadPlugin(map.get(name));
	}
	
	public boolean isValidPlugin(File path) {
		return true;
	}
	
	public boolean isValidPlugin(String path) {
		return isValidPlugin(new File(path));
	}
	
	public boolean hasPlugin(PluginListener plugin) {
		return map.containsValue(plugin);
	}
	
	public boolean hasPlugin(String name) {
		return map.containsKey(name);
	}
	
	public PluginListener getPluginFromName(String name) {
		return map.get(name);
	}
	
	public Collection<PluginListener> getPlugins() {
		return map.values();
	}
	
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	public int size() {
		return map.size();
	}
	
	public static File getPluginsFolder() {
		return new File('.' + File.separatorChar + "plugins");
	}
	
	public File getJarPath() {
		return new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
	}
	
	public String sendCommand(String message, String[] args) {
		return map.get(cmdsMap.get(message)).onCommand(message, args);
	}
}
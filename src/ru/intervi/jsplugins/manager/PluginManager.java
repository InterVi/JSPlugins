package ru.intervi.jsplugins.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import ru.intervi.jsplugins.Main;
import ru.intervi.jsplugins.api.InvalidPluginException;
import ru.intervi.jsplugins.api.PluginListener;

/**
 * управление плагинами
 */
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
	
	/**
	 * загрузить плагин
	 * @param path
	 * @throws NullPointerException
	 * @throws IOException
	 * @throws InvalidPluginException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @return загруженный плагин
	 */
	public PluginListener loadPlugin(File path) throws NullPointerException, IOException, InvalidPluginException, ClassNotFoundException, InstantiationException, IllegalAccessException {
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
			return plugin;
		} finally {loader.close();}
	}
	
	/**
	 * загрузить плагин
	 * @param path
	 * @throws NullPointerException
	 * @throws IOException
	 * @throws InvalidPluginException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @return загруженный плагин
	 */
	public PluginListener loadPlugin(String path) throws NullPointerException, IOException, InvalidPluginException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		return loadPlugin(new File(path));
	}
	
	/**
	 * перезагрузить плагин
	 * @param plugin
	 * @throws NullPointerException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws InvalidPluginException
	 * @return перезагруженный плагин
	 */
	public PluginListener reloadPlugin(PluginListener plugin) throws NullPointerException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, InvalidPluginException {
		File path = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		unloadPlugin(plugin);
		return loadPlugin(path);
	}
	
	/**
	 * перезагрузить плагин
	 * @param name
	 * @throws NullPointerException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws InvalidPluginException
	 * @return перезагруженный плагин
	 */
	public PluginListener reloadPlugin(String name) throws NullPointerException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, InvalidPluginException {
		return reloadPlugin(map.get(name));
	}
	
	/**
	 * выгрузить плагин
	 * @param plugin
	 */
	public void unloadPlugin(PluginListener plugin) {
		plugin.onUnload();
		for (PluginListener p : map.values()) p.onUnloadPlugin(plugin);
		map.remove(plugin.getName());
		if (plugin.getCommands() != null) for (String cmd : plugin.getCommands().values()) cmdsMap.remove(cmd);
		if (STANDALONE) Main.LOGGER.warning(plugin.getName() + " unloaded");
	}
	
	/**
	 * выгрузить плагин
	 * @param name
	 */
	public void unloadPlugin(String name) {
		unloadPlugin(map.get(name));
	}
	
	/**
	 * проверка плагина на валидность
	 * @param path
	 * @return
	 */
	public boolean isValidPlugin(File path) {
		return true; //будет написано позже
	}
	
	/**
	 * проверка плагина на валидность
	 * @param path
	 * @return
	 */
	public boolean isValidPlugin(String path) {
		return isValidPlugin(new File(path));
	}
	
	/**
	 * проверка, загружен ли плагин
	 * @param plugin
	 * @return true если да
	 */
	public boolean hasPlugin(PluginListener plugin) {
		return map.containsValue(plugin);
	}
	
	/**
	 * проверка, загружен ли плагин
	 * @param name
	 * @return true если да
	 */
	public boolean hasPlugin(String name) {
		return map.containsKey(name);
	}
	
	/**
	 * получить плагин по названию
	 * @param name
	 * @return
	 */
	public PluginListener getPluginFromName(String name) {
		return map.get(name);
	}
	
	/**
	 * получить все плагины
	 * @return
	 */
	public Collection<PluginListener> getPlugins() {
		return Collections.unmodifiableCollection(map.values());
	}
	
	/**
	 * проверка, есть ли загруженные плагины
	 * @return true если да
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	/**
	 * количество загруженных плагинов
	 * @return
	 */
	public int size() {
		return map.size();
	}
	
	/**
	 * получить директорию с плагинами
	 * @return
	 */
	public static File getPluginsFolder() {
		return new File('.' + File.separatorChar + "plugins");
	}
	
	/**
	 * получить JAR-файл библиотеки
	 * @return
	 */
	public File getJarPath() {
		return new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
	}
	
	/**
	 * отправить команду
	 * @param message команда
	 * @param args аргументы
	 * @return вывод пользователю
	 */
	public String sendCommand(String message, String[] args) {
		return map.get(cmdsMap.get(message)).onCommand(message, args);
	}
}
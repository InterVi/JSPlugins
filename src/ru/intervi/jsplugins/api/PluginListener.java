package ru.intervi.jsplugins.api;

import java.util.List;
import java.util.Map;

import ru.intervi.jsplugins.manager.PluginManager;

/**
 * интерфейс плагина
 */
public interface PluginListener {
	/** название плагина */
	public String getName();
	/** версия плагина */
	public int getVersion();
	/** имя автора */
	public String getAuthor();
	/** почта автора */
	public String getAuthorEmail();
	/** страница плагина или автора */
	public String getAuthorURL();
	/** описание плагина */
	public String getDescription();
	/**
	 * событие загрузки плагина
	 * @param manager
	 */
	public void onLoad(PluginManager manager);
	/**
	 * событие загрузки другого плагина
	 * @param plugin
	 */
	public void onLoadedNewPlugin(PluginListener plugin);
	/**
	 * событие выгрузки другого плагина
	 * @param plugin
	 */
	public void onUnloadPlugin(PluginListener plugin);
	/** событие выгрузки плагина */
	public void onUnload();
	/**
	 * обработка команды
	 * @param message команда
	 * @param args аргументы
	 * @return вывод пользователю
	 */
	public String onCommand(String message, String[] args);
	/**
	 * команды плагина
	 * @return ключ - команда, значение - описание
	 */
	public Map<String, String> getCommands();
	/**
	 * универсальный метод для взаимодействия плагинов между собой
	 * @param data
	 * @return
	 */
	public List<? extends Object> communicate(List<? extends Object> data);
}
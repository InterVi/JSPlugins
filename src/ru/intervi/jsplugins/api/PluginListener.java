package ru.intervi.jsplugins.api;

import java.util.List;
import java.util.Map;

import ru.intervi.jsplugins.manager.PluginManager;

public interface PluginListener {
	public String getName();
	public int getVersion();
	public String getAuthor();
	public String getAuthorEmail();
	public String getAuthorURL();
	public String getDescription();
	public void onLoad(PluginManager manager);
	public void onLoadedNewPlugin(PluginListener plugin);
	public void onUnloadPlugin(PluginListener plugin);
	public void onUnload();
	public String onCommand(String message, String[] args);
	public Map<String, String> getCommands();
	public List<? extends Object> communicate(List<? extends Object> data);
}
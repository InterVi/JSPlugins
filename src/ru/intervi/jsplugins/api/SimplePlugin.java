package ru.intervi.jsplugins.api;

import java.util.List;
import java.util.Map;

import ru.intervi.jsplugins.manager.PluginManager;

/**
 * класс-заглушка, чтобы не реализовывать не нужные методы
 */
public class SimplePlugin implements PluginListener {
	public String getName() {
		return "SimplePlugin";
	}
	
	public int getVersion() {
		return 0;
	}
	
	public String getAuthor() {
		return "-";
	}
	
	public String getAuthorEmail() {
		return "-";
	}
	
	public String getAuthorURL() {
		return "-";
	}
	
	public String getDescription() {
		return "-";
	}
	
	public void onLoad(PluginManager manager) {
		
	}
	
	public void onLoadedNewPlugin(PluginListener plugin) {
		
	}
	
	public void onUnloadPlugin(PluginListener plugin) {
		
	}
	
	public void onUnload() {
		
	}
	
	public String onCommand(String message, String[] args) {
		return null;
	}
	
	public Map<String, String> getCommands() {
		return null;
	}
	
	public List<? extends Object> communicate(List<? extends Object> data) {
		return null;
	}
}
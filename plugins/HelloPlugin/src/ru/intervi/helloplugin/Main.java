package ru.intervi.helloplugin;

import java.util.logging.Logger;

import ru.intervi.jsplugins.api.SimplePlugin;
import ru.intervi.jsplugins.manager.PluginManager;

public class Main extends SimplePlugin {
	@Override
	public String getName() {
		return "HelloPlugin";
	}
	
	@Override
	public int getVersion() {
		return 1;
	}
	
	@Override
	public String getAuthor() {
		return "InterVi";
	}
	
	@Override
	public String getAuthorEmail() {
		return "intervionly@gmail.com";
	}
	
	@Override
	public String getAuthorURL() {
		return "https://github.com/InterVi";
	}
	
	@Override
	public String getDescription() {
		return "Easy example plugin.";
	}
	
	@Override
	public void onLoad(PluginManager manager) {
		Logger.getLogger("JSPlugins").info("Hello! I loaded!");
	}
	
	@Override
	public void onUnload() {
		Logger.getLogger("JSPlugins").info("I will unloaded...");
	}
}
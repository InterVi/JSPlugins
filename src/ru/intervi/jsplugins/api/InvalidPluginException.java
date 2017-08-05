package ru.intervi.jsplugins.api;

public class InvalidPluginException extends Exception {
	public InvalidPluginException(String message) {
		MESSAGE = message;
	}
	
	private static final long serialVersionUID = 4433168136729928210L;
	private final String MESSAGE;
	
	public String getMessage() {
		return MESSAGE;
	}
}
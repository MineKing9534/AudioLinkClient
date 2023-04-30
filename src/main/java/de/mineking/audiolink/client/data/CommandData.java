package de.mineking.audiolink.client.data;

import java.util.Map;

public class CommandData {
	public final String command;
	public final Map<String, Object> args;

	public CommandData(String command, Map<String, Object> args) {
		this.command = command;
		this.args = args;
	}
}

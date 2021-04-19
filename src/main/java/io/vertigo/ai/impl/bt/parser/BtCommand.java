package io.vertigo.ai.impl.bt.parser;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import io.vertigo.core.lang.Assertion;

public class BtCommand {
	public enum CommandType {
		START_COMPOSITE,
		END_COMPOSITE,
		STANDARD
	}

	private final String commandName;
	private final List<String> commandArgs;
	private final CommandType type;

	private BtCommand(final String commandName, final List<String> commandArgs, final CommandType type) {
		this.commandName = commandName;
		this.commandArgs = commandArgs;
		this.type = type;
	}

	public static BtCommand of(final String commandName, final List<String> commandArgs, final CommandType type) {
		return new BtCommand(commandName, commandArgs, type);
	}

	/**
	 * @return the commandName
	 */
	public String getCommandName() {
		return commandName;
	}

	/**
	 * @return the commandArgs
	 */
	public List<String> getCommandArgs() {
		return commandArgs;
	}

	/**
	 * @return the type
	 */
	public CommandType getType() {
		return type;
	}

	/**
	 * Get Nth parameter, String value.
	 *
	 * @param idx argument position
	 * @return argument value
	 */
	public String getStringParam(final int idx) {
		Assertion.check()
				.isTrue(idx < commandArgs.size(), "Missing parameter n°{0} on '{1}'.", idx, commandName);
		//--
		return commandArgs.get(idx);
	}

	/**
	 * Get optionally the Nth parameter, String value.
	 *
	 * @param idx argument position
	 * @return argument value if exists
	 */
	public Optional<String> getOptStringParam(final int idx) {
		if (idx < commandArgs.size()) {
			return Optional.of(commandArgs.get(idx));
		}
		return Optional.empty();
	}

	/**
	 * Get all parameters from the Nth parameter and after, String value.
	 *
	 * @param idx start position
	 * @return argument all values
	 */
	public String[] getRemainingStringParam(final int idx) {
		if (idx >= commandArgs.size()) {
			return new String[0];
		}
		return commandArgs.subList(idx, commandArgs.size()).toArray(String[]::new);
	}

	/**
	 * Get Nth parameter, int value.
	 *
	 * @param idx argument position
	 * @return argument value
	 */
	public int getIntParam(final int idx) {
		Assertion.check()
				.isTrue(idx < commandArgs.size(), "Missing parameter n°{0} on '{1}'.", idx, commandName);
		//--
		return Integer.valueOf(commandArgs.get(idx));
	}

	/**
	 * Get optionally the Nth parameter, int value.
	 *
	 * @param idx argument position
	 * @return argument value if exists
	 */
	public OptionalInt getOptIntParam(final int idx) {
		if (idx < commandArgs.size()) {
			return OptionalInt.of(getIntParam(idx));
		}
		return OptionalInt.empty();
	}
}

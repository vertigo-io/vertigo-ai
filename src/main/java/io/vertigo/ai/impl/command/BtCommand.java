package io.vertigo.ai.impl.command;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import io.vertigo.core.lang.Assertion;

/**
 * Generic representation of a command. 
 * All args are String and we don't know if this command is valid.
 *
 * @author skerdudou
 */
public class BtCommand {
	public enum CommandType {
		START_COMPOSITE, //
		END_COMPOSITE, // why start and end type.. start or end should be another property
		STANDARD
	}

	private final String name;
	private final List<String> args;
	private final CommandType type;

	private BtCommand(final String commandName, final List<String> commandArgs, final CommandType type) {
		Assertion.check()
				.isNotBlank(commandName)
				.isNotNull(commandArgs)
				.isNotNull(type);
		//---
		this.name = commandName;
		this.args = commandArgs;
		this.type = type;
	}

	public static BtCommand of(final String commandName, final List<String> commandArgs, final CommandType type) {
		return new BtCommand(commandName, commandArgs, type);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the args
	 */
	public List<String> getArgs() {
		return args;
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
				.isTrue(idx < args.size(), "Missing parameter n°{0} on '{1}'.", idx, name);
		//--
		return args.get(idx);
	}

	/**
	 * Get optionally the Nth parameter, String value.
	 *
	 * @param idx argument position
	 * @return argument value if exists
	 */
	public Optional<String> getOptStringParam(final int idx) {
		if (idx < args.size()) {
			return Optional.of(args.get(idx));
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
		if (idx >= args.size()) {
			return new String[0];
		}
		return args.subList(idx, args.size()).toArray(String[]::new);
	}

	/**
	 * Get Nth parameter, int value.
	 *
	 * @param idx argument position
	 * @return argument value
	 */
	public int getIntParam(final int idx) {
		Assertion.check()
				.isTrue(idx < args.size(), "Missing parameter n°{0} on '{1}'.", idx, name);
		//--
		return Integer.valueOf(args.get(idx));
	}

	/**
	 * Get optionally the Nth parameter, int value.
	 *
	 * @param idx argument position
	 * @return argument value if exists
	 */
	public OptionalInt getOptIntParam(final int idx) {
		if (idx < args.size()) {
			return OptionalInt.of(getIntParam(idx));
		}
		return OptionalInt.empty();
	}
}

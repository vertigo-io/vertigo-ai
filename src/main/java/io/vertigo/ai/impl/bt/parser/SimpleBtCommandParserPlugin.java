package io.vertigo.ai.impl.bt.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.vertigo.ai.bt.BTNode;
import io.vertigo.ai.bt.BtNodeProvider;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;

/**
 * Class pattern for a ParserPlugin with a simple command registration.
 *
 * @author skerdudou
 * @param <P> Type of the BtNodeProvider
 */
public abstract class SimpleBtCommandParserPlugin<P extends BtNodeProvider> implements BtCommandParserPlugin<P> {

	private final Map<String, CommandResolver<P>> basicCommands = new HashMap<>();
	private final Map<String, CommandResolver<P>> compositeCommands = new HashMap<>();

	public SimpleBtCommandParserPlugin() {
		init();
	}

	/**
	 * Register commands with either registerBasicCommand, registerCompositeCommand or registerCommands.
	 */
	protected abstract void init(); // prevent empty concrete classes

	@Override
	public final Optional<Function<P, BTNode>> parse(final BtCommand command, final List<BTNode> childs) {
		Assertion.check()
				.isNotNull(command)
				.isNotNull(childs);
		//--
		switch (command.getType()) {
			case STANDARD:
				Assertion.check()
						.isTrue(childs.isEmpty(), "Standard commands dont expect childs");
				// --
				return Optional.ofNullable(basicCommands.get(command.getCommandName()))
						.map(cmd -> cmd.getCommandConverter().apply(command, Collections.emptyList()));
			case START_COMPOSITE:
				return Optional.ofNullable(compositeCommands.get(command.getCommandName()))
						.map(cmd -> cmd.getCommandConverter().apply(command, childs));
			default:
				throw new VSystemException("Parser plugin don't handle {0} command types", command.getType());
		}
	}

	/**
	 * Register a function to convert a basic command to a node provider.
	 *
	 * @param name the name of the command
	 * @param commandConverter the function to convert a command and a nodeProvider into a node
	 */
	protected final void registerBasicCommand(final String name, final BiFunction<BtCommand, P, BTNode> commandConverter) {
		Assertion.check()
				.isNotBlank(name)
				.isNotNull(commandConverter);
		//--
		basicCommands.put(name, CommandResolver.of(name, c -> p -> commandConverter.apply(c, p)));
	}

	/**
	 * Register a function to convert a composite command to a node provider.
	 *
	 * @param name the name of the command
	 * @param commandConverter the function to convert a command, a nodeProvider and a list of child nodes into a node
	 */
	protected final void registerCompositeCommand(final String name, final TriFunction<BtCommand, P, List<BTNode>, BTNode> commandConverter) {
		Assertion.check()
				.isNotBlank(name)
				.isNotNull(commandConverter);
		//--
		compositeCommands.put(name, CommandResolver.ofComposite(name, (c, l) -> (p -> commandConverter.apply(c, p, l))));
	}

	/**
	 * Register a function to convert a basic command to a node.
	 * Here stateless means, no need for BtNodeProvider object.
	 *
	 * @param name the name of the command
	 * @param commandConverter function to convert a command into a node
	 */
	protected final void registerStatelessBasicCommand(final String name, final Function<BtCommand, BTNode> commandConverter) {
		registerBasicCommand(name, (c, p) -> commandConverter.apply(c));
	}

	/**
	 * Register a function to convert a composite command to a node .
	 * Here stateless means, no need for BtNodeProvider object.
	 *
	 * @param name the name of the command
	 * @param commandConverter function to convert a command and a list of child nodes into a node
	 */
	protected final void registerStatelessCompositeCommand(final String name, final BiFunction<BtCommand, List<BTNode>, BTNode> commandConverter) {
		registerCompositeCommand(name, (c, p, l) -> commandConverter.apply(c, l));
	}

	/**
	 * Register a list of pre-build CommandResolver.
	 *
	 * @param commands list of commands to register
	 */
	protected final void registerCommands(final Iterable<CommandResolver<P>> commands) {
		Assertion.check()
				.isNotNull(commands);
		//--
		for (final CommandResolver<P> command : commands) {
			if (command.isComposite()) {
				compositeCommands.put(command.getName(), command);
			} else {
				basicCommands.put(command.getName(), command);
			}
		}
	}

	@FunctionalInterface
	public interface TriFunction<T, U, V, R> {
		R apply(T t, U u, V v);
	}
}

package io.vertigo.ai.impl.bt.parser;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.vertigo.ai.bt.BTNode;
import io.vertigo.ai.bt.BtNodeProvider;

public final class CommandResolver<P extends BtNodeProvider> {

	private final String name;
	private final BiFunction<BtCommand, List<BTNode>, Function<P, BTNode>> commandConverter;
	private final boolean isComposite;

	private CommandResolver(final String name, final BiFunction<BtCommand, List<BTNode>, Function<P, BTNode>> commandConverter, final boolean isComposite) {
		this.name = name;
		this.commandConverter = commandConverter;
		this.isComposite = isComposite;
	}

	public static <P extends BtNodeProvider> CommandResolver<P> of(final String name, final Function<BtCommand, Function<P, BTNode>> commandConverter) {
		return new CommandResolver<>(name, (c, l) -> commandConverter.apply(c), false);
	}

	public static <P extends BtNodeProvider> CommandResolver<P> ofComposite(final String name, final BiFunction<BtCommand, List<BTNode>, Function<P, BTNode>> commandConverter) {
		return new CommandResolver<>(name, commandConverter, true);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the commandConverter
	 */
	public BiFunction<BtCommand, List<BTNode>, Function<P, BTNode>> getCommandConverter() {
		return commandConverter;
	}

	public boolean isComposite() {
		return isComposite;
	}

}

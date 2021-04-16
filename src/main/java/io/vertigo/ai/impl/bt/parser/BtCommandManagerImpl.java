package io.vertigo.ai.impl.bt.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.ai.bt.BTNode;
import io.vertigo.ai.bt.BtNodeProvider;
import io.vertigo.ai.bt.parser.BtCommandManager;
import io.vertigo.ai.impl.bt.parser.BtCommand.CommandType;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;

public class BtCommandManagerImpl implements BtCommandManager {

	private final List<BtTextParserPlugin<?>> plugins;

	@Inject
	public BtCommandManagerImpl(final List<BtTextParserPlugin<?>> plugins) {
		this.plugins = plugins;
	}

	@Override
	public Function<List<Object>, BTNode> parse(final String text) {
		final var commands = doParseText(text);

		return pluginParameters -> {
			final var pluginNodeProviders = resolvePluginsNodeProvider(pluginParameters);
			return parseCommands(commands, pluginNodeProviders);
		};
	}

	private List<BtCommand> doParseText(final String text) {
		// TODO
		return Collections.emptyList();
	}

	private Map<BtTextParserPlugin<?>, BtNodeProvider> resolvePluginsNodeProvider(final List<Object> pluginParameters) {
		return plugins.stream()
				.collect(Collectors.toMap(p -> p, p -> p.getNodeProvider(pluginParameters)));
	}

	private BTNode parseCommands(final List<BtCommand> commands, final Map<BtTextParserPlugin<?>, BtNodeProvider> pluginNodeProviders) {
		Assertion.check()
				.isNotNull(commands)
				.isTrue(commands.size() > 1, "No command provided")
				.isTrue(CommandType.START_COMPOSITE.equals(commands.get(0).getType()), "Root level only accepts composite nodes");
		//--
		final Deque<BtCommand> compositeStack = new ArrayDeque<>();
		final Deque<List<BTNode>> stdNodesStack = new ArrayDeque<>();

		BTNode rootNode = null;
		//final List<BTNode> childs
		final Iterator<BtCommand> it = commands.iterator();
		while (it.hasNext()) {
			final BtCommand command = it.next();

			switch (command.getType()) {
				case START_COMPOSITE:
					compositeStack.add(command);
					stdNodesStack.add(new ArrayList<BTNode>());
					break;
				case STANDARD:
					stdNodesStack.getFirst().add(doParseCommand(command, Collections.emptyList(), pluginNodeProviders));
					break;
				case END_COMPOSITE:
					Assertion.check()
							.isFalse(compositeStack.isEmpty(), "Cannot end composite '{0}', nothing is actually opened", command.getCommandName())
							.isTrue(compositeStack.getFirst().getCommandName().equals(command.getCommandName()),
									"Cannot close '{0}', currently on '{1}'",
									command.getCommandName(), compositeStack.getFirst().getCommandName());
					//--
					final BtCommand cmd = compositeStack.peekFirst();
					final BTNode compositeNode = doParseCommand(cmd, stdNodesStack.peekFirst(), pluginNodeProviders);

					if (stdNodesStack.isEmpty()) {
						// we close the root composite
						rootNode = compositeNode;
						break;
					} else {
						stdNodesStack.getFirst().add(compositeNode); // add composite node to previous node list
					}
					break;
				default:
					throw new VSystemException("Unknown command type {0}", command.getType());
			}
		}

		Assertion.check()
				.isFalse(it.hasNext(), "Commands after root node is not supported.")
				.isNotNull(rootNode);

		return rootNode;
	}

	private BTNode doParseCommand(final BtCommand command, final List<BTNode> childs, final Map<BtTextParserPlugin<?>, BtNodeProvider> pluginNodeProviders) {
		return plugins.stream()
				.map(p -> getOptNode(command, childs, p, pluginNodeProviders.get(p)))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseThrow(() -> new VSystemException("No plugin found to handle '{0}' command.", command.getCommandName()));
	}

	private <T extends BtNodeProvider> Optional<BTNode> getOptNode(final BtCommand command, final List<BTNode> childs, final BtTextParserPlugin<T> plugin, final BtNodeProvider provider) {
		return plugin.parse(command, childs)
				.map(f -> f.apply((T) provider)); // dirty cast, BtNodeProvider type enforced by construction of the map (coherent with plugin)
	}

}

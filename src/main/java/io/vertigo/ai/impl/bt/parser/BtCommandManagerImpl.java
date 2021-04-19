package io.vertigo.ai.impl.bt.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.ai.bt.BTNode;
import io.vertigo.ai.bt.BtNodeProvider;
import io.vertigo.ai.bt.parser.BtCommandManager;
import io.vertigo.ai.impl.bt.parser.BtCommand.CommandType;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;

public class BtCommandManagerImpl implements BtCommandManager {

	private final List<BtCommandParserPlugin<?>> plugins;

	@Inject
	public BtCommandManagerImpl(final List<BtCommandParserPlugin<?>> plugins) {
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
		return text.lines()
				.map(this::stripComment)
				.map(String::strip)
				.filter(s -> !s.isEmpty())
				.map(this::doParseLine)
				.collect(Collectors.toList());
	}

	private String stripComment(final String in) {
		final int commentIndex = in.indexOf("--");
		return in.substring(0, commentIndex == -1 ? in.length() : commentIndex);
	}

	private BtCommand doParseLine(final String line) {
		final var pattern = Pattern.compile("^((begin|end)\\s+)?([A-Za-z0-9:]+)(\\s+(.*))?$");
		final Matcher matcher = pattern.matcher(line);

		if (!matcher.matches()) {
			throw new VSystemException("Malformed line '{0}'", line);
		}

		final String type = matcher.group(2);
		final String command = matcher.group(3);
		final String args = matcher.group(5);

		CommandType commandType;
		if (type.equals("begin")) {
			commandType = CommandType.START_COMPOSITE;
		} else if (type.equals("end")) {
			commandType = CommandType.END_COMPOSITE;
		} else {
			commandType = CommandType.STANDARD;
		}

		return BtCommand.of(command, resolveArgs(args), commandType);
	}

	private List<String> resolveArgs(final String args) {
		if (args == null) {
			return Collections.emptyList();
		}

		final var out = new ArrayList<String>();

		boolean wasQuoted = false;
		boolean isQuoted = false;
		boolean isEscaping = false;
		StringBuilder curentArg = new StringBuilder();

		for (final char c : args.toCharArray()) {
			if (wasQuoted) {
				Assertion.check()
						.isTrue(Character.isWhitespace(c), "Text is not allowed just after quotes, please add a space. '{0}'", args);
				// consume next character after end quote
				wasQuoted = false;
			} else if (!isEscaping && c == '"') {
				Assertion.check()
						.isTrue(curentArg.length() == 0 || isQuoted, "Quotes are only allowed around text or escaped inside quotes. '{0}'", args);

				// Quote handling
				if (curentArg.length() == 0) {
					isQuoted = true;
				} else {
					isQuoted = false;
					wasQuoted = true;
					out.add(curentArg.toString());
					curentArg = new StringBuilder();
				}
			} else {
				isEscaping = isQuoted && !isEscaping && c == '\\';

				if ((isQuoted && !isEscaping) || !Character.isWhitespace(c)) {
					curentArg.append(c);
				}

				if (!isQuoted && Character.isWhitespace(c) && curentArg.length() > 0) {
					out.add(curentArg.toString());
					curentArg = new StringBuilder();
				}
			}
		}

		Assertion.check()
				.isFalse(isQuoted, "End quote not found. '{0}'", args);

		// final arg
		if (curentArg.length() > 0) {
			out.add(curentArg.toString());
		}

		return out;
	}

	private Map<BtCommandParserPlugin<?>, BtNodeProvider> resolvePluginsNodeProvider(final List<Object> pluginParameters) {
		final Map<BtCommandParserPlugin<?>, BtNodeProvider> map = new HashMap<>();
		for (final BtCommandParserPlugin<?> plugin : plugins) {
			map.put(plugin, plugin.getNodeProvider(pluginParameters));
		}
		return map;
	}

	private BTNode parseCommands(final List<BtCommand> commands, final Map<BtCommandParserPlugin<?>, BtNodeProvider> pluginNodeProviders) {
		Assertion.check()
				.isNotNull(commands)
				.isTrue(commands.size() > 1, "No command provided")
				.isTrue(CommandType.START_COMPOSITE.equals(commands.get(0).getType()), "Root level only accepts composite nodes");
		//--
		final Deque<BtCommand> compositeStack = new ArrayDeque<>();
		final Deque<List<BTNode>> stdNodesStack = new ArrayDeque<>();

		BTNode rootNode = null;
		final Iterator<BtCommand> it = commands.iterator();
		while (it.hasNext() && rootNode == null) {
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
					final BtCommand cmd = compositeStack.pop();
					final BTNode compositeNode = doParseCommand(cmd, stdNodesStack.pop(), pluginNodeProviders);

					if (compositeStack.isEmpty()) {
						// we close the root composite
						rootNode = compositeNode;
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
				.isTrue(compositeStack.isEmpty(), "Node '{0}' not ended.", compositeStack.isEmpty() ? "" : compositeStack.getFirst().getCommandName())
				.isNotNull(rootNode);

		return rootNode;
	}

	private BTNode doParseCommand(final BtCommand command, final List<BTNode> childs, final Map<BtCommandParserPlugin<?>, BtNodeProvider> pluginNodeProviders) {
		return plugins.stream()
				.map(p -> getOptNode(command, childs, p, pluginNodeProviders.get(p)))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseThrow(() -> new VSystemException("No plugin found to handle '{0}' command.", command.getCommandName()));
	}

	private <T extends BtNodeProvider> Optional<BTNode> getOptNode(final BtCommand command, final List<BTNode> childs, final BtCommandParserPlugin<T> plugin, final BtNodeProvider provider) {
		return plugin.parse(command, childs)
				.map(f -> f.apply((T) provider)); // dirty cast, BtNodeProvider type enforced by construction of the map (coherent with plugin)
	}

}

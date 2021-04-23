package io.vertigo.ai.impl.command;

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

import io.vertigo.ai.bt.BTNode;
import io.vertigo.ai.bt.BTNodes;
import io.vertigo.ai.command.BtCommandManager;
import io.vertigo.ai.impl.command.BtCommand.CommandType;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;

/**
 * Implementation of the BtCommandManager.
 * It uses BtCommandParserDefinitions to get the necessary code to actually convert a line of text into an instance of BTNode.
 * A BtCommandParserDefinition is created via the static building methods provided in the BtCommandParserDefinition class and then registered in the app DefinitionSpace via a DefinitionProvider.
 * Multiple definitions can be provided for the same command name. The last registered will be the used one.
 *
 * @author skerdudou, mlaroche
 */
public class BtCommandManagerImpl implements BtCommandManager, SimpleDefinitionProvider, Activeable {
	private final Map<String, BtCommandParserDefinition> commandParsers = new HashMap<>();

	@Override
	public void start() {
		Node.getNode().getDefinitionSpace().getAll(BtCommandParserDefinition.class).stream()
				.forEach(btCommandParser -> commandParsers.put(btCommandParser.getCommandName(), btCommandParser));
	}

	@Override
	public void stop() {
		// nothing

	}

	/**
	 * Registers the parsers for the core commandParsers needed to build any BTNode
	 */
	@Override
	public List<BtCommandParserDefinition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return List.of(
				BtCommandParserDefinition.statelessBasicCommand("succeed", c -> BTNodes.succeed()),
				BtCommandParserDefinition.statelessBasicCommand("fail", c -> BTNodes.fail()),
				BtCommandParserDefinition.statelessBasicCommand("running", c -> BTNodes.running()),
				BtCommandParserDefinition.statelessCompositeCommand("sequence", (c, l) -> BTNodes.sequence(l)),
				BtCommandParserDefinition.statelessCompositeCommand("selector", (c, l) -> BTNodes.selector(l)),
				BtCommandParserDefinition.statelessCompositeCommand("try", (c, l) -> BTNodes.doTry(c.getIntParam(0), l)),
				BtCommandParserDefinition.statelessCompositeCommand("loop", (c, l) -> {
					final var optionalInt = c.getOptIntParam(0);
					if (optionalInt.isPresent()) {
						return BTNodes.loop(optionalInt.getAsInt(), l);
					}
					return BTNodes.loop(l);
				}));
	}

	@Override
	public Function<List<Object>, BTNode> parse(final String text) {
		final var commands = doParseText(text);

		return parserParameters -> parseCommands(commands, parserParameters);
	}

	private static List<BtCommand> doParseText(final String text) {
		return text.lines()
				.map(BtCommandManagerImpl::stripComment)
				.map(String::strip)
				.filter(s -> !s.isEmpty())
				.map(BtCommandManagerImpl::doParseLine)
				.collect(Collectors.toList());
	}

	private static String stripComment(final String line) {
		final int commentIndex = line.indexOf("--");
		return commentIndex == -1
				? line
				: line.substring(0, commentIndex);
	}

	private static BtCommand doParseLine(final String line) {
		final var pattern = Pattern.compile("^((begin|end)\\s+)?([A-Za-z0-9:]+)(\\s+(.*))?$");
		final Matcher matcher = pattern.matcher(line);

		if (!matcher.matches()) {
			throw new VSystemException("Malformed line '{0}'", line);
		}

		final String type = matcher.group(2);
		final String command = matcher.group(3);
		final String args = matcher.group(5);

		CommandType commandType;
		if ("begin".equalsIgnoreCase(type)) {
			commandType = CommandType.START_COMPOSITE;
		} else if ("end".equalsIgnoreCase(type)) {
			commandType = CommandType.END_COMPOSITE;
		} else {
			commandType = CommandType.STANDARD;
		}

		return BtCommand.of(command, resolveArgs(args), commandType);
	}

	private static List<String> resolveArgs(final String args) {
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

	private BTNode parseCommands(final List<BtCommand> commands, final List<Object> params) {
		Assertion.check()
				.isNotNull(commands)
				.isFalse(commands.isEmpty(), "No command provided")
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
					compositeStack.push(command);
					stdNodesStack.push(new ArrayList<BTNode>());
					break;
				case STANDARD:
					stdNodesStack.getFirst().add(doParseCommand(command, Collections.emptyList(), params));
					break;
				case END_COMPOSITE:
					Assertion.check()
							.isFalse(compositeStack.isEmpty(), "Cannot end composite '{0}', nothing is actually opened", command.getName())
							.isTrue(compositeStack.getFirst().getName().equals(command.getName()),
									"Cannot close '{0}', currently on '{1}'",
									command.getName(), compositeStack.getFirst().getName());
					//--
					final BtCommand cmd = compositeStack.pop();
					final BTNode compositeNode = doParseCommand(cmd, stdNodesStack.pop(), params);

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
				.isTrue(compositeStack.isEmpty(), "Node '{0}' not ended.", compositeStack.isEmpty() ? "" : compositeStack.getFirst().getName())
				.isNotNull(rootNode);

		return rootNode;
	}

	private BTNode doParseCommand(final BtCommand command, final List<BTNode> childs, final List<Object> params) {
		Assertion.check()
				.isNotNull(command)
				.isNotNull(childs);
		//--
		final BtCommandParserDefinition commandParser = Optional.ofNullable(commandParsers.get(command.getName()))
				.orElseThrow(() -> new VSystemException("No parser found to handle {0} '{1}' command.", command.getType() == CommandType.STANDARD ? "standard" : "composite", command.getName()));

		switch (command.getType()) {
			case STANDARD:
				Assertion.check()
						.isTrue(childs.isEmpty(), "Standard commandParsers dont expect childs")
						.isTrue(commandParser.getCommandType() == CommandType.STANDARD, "The command parser is not for the correct type");
				return commandParser.getCommandEvaluator().apply(command, params, Collections.emptyList());
			case START_COMPOSITE:
				Assertion.check()
						.isTrue(commandParser.getCommandType() == CommandType.START_COMPOSITE, "The command parser is not for the correct type");
				return commandParser.getCommandEvaluator().apply(command, params, childs);
			default:
				throw new VSystemException("Unkown command type {0}", command.getType());
		}
	}
}

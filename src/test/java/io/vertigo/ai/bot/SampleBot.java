package io.vertigo.ai.bot;

import static io.vertigo.ai.bt.BTNodes.selector;
import static io.vertigo.ai.bt.BTNodes.sequence;
import static io.vertigo.ai.bt.BTNodes.succeed;

import java.util.Optional;
import java.util.Scanner;

import javax.inject.Inject;

import io.vertigo.ai.bb.BlackBoardManager;
import io.vertigo.ai.bot.BotResponse.BotStatus;
import io.vertigo.ai.bt.BTNode;
import io.vertigo.ai.bt.BTNodes;
import io.vertigo.ai.bt.BehaviorTreeManager;
import io.vertigo.ai.impl.bb.BlackBoardManagerImpl;
import io.vertigo.ai.impl.bot.BotManagerImpl;
import io.vertigo.ai.impl.bt.BehaviorTreeManagerImpl;
import io.vertigo.ai.plugins.bb.memory.MemoryBlackBoardStorePlugin;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.util.InjectorUtil;

public class SampleBot {

	@Inject
	private BotManager botManager;

	static BotEngine botEngine;

	public static void main(final String[] args) {
		try (AutoCloseableNode node = new AutoCloseableNode(buildNodeConfig())) {
			final SampleBot sampleBot = InjectorUtil.newInstance(SampleBot.class);
			sampleBot.runInConsole();

		}

	}

	void runTick() {
		botEngine = botManager.createBotEngine(BlackBoardManager.MAIN_STORE_NAME);
		final BTNode rootNode = sequence(
				botEngine.inputString("u/name", "Hello I'm Alan what is your name ?"),
				//intents
				main(),
				botEngine.display("bye bye {{u/name}}"));
		final BotResponse botResponse = botManager.runTick(rootNode, BlackBoardManager.MAIN_STORE_NAME, Optional.empty());
		System.out.println(botResponse.question);

	}

	void runInConsole() {
		// create a botEngine that is bound to a specific context
		botEngine = botManager.createBotEngine(BlackBoardManager.MAIN_STORE_NAME);
		// create or parse or retrieve the brain
		final BTNode rootNode = sequence(
				botEngine.inputString("u/name", "Hello I'm Alan what is your name ?"),
				//intents
				main(),
				botEngine.display("bye bye {{u/name}}"));

		final Scanner sc = new Scanner(System.in);

		// init conversation
		BotResponse botResponse = botManager.runTick(rootNode, BlackBoardManager.MAIN_STORE_NAME, Optional.empty());
		// run the rest
		while (botResponse.botStatus != BotStatus.Ended) {
			System.out.println(">>running *************************");
			System.out.println(botResponse.question);
			final var userResponse = Optional.of(sc.nextLine());
			botResponse = botManager.runTick(rootNode, BlackBoardManager.MAIN_STORE_NAME, userResponse);
		}
		sc.close();
		System.out.println(">> end ***********************");
	}

	private static NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(new CommonsFeatures().build())// for transactions
				.addModule(
						ModuleConfig.builder("myModule")
								.addComponent(BlackBoardManager.class, BlackBoardManagerImpl.class)
								.addPlugin(MemoryBlackBoardStorePlugin.class)
								.addComponent(BehaviorTreeManager.class, BehaviorTreeManagerImpl.class)
								.addComponent(BotManager.class, BotManagerImpl.class)
								.build())
				.build();
	}

	private static BTNode main() {
		return selector(
				botEngine.eq("i/name", "X"),
				sequence(
						//botEngine.clear("i/*"),
						//botEngine.clear("rate/*"),
						//						botEngine.fulfill("i/name", "Hi {{u/name}} please select [W]eather, [T]icket, [G]ame or e[X]it ?", "W", "G", "T", "X"),
						botEngine.inputString("i/name", "Hi {{u/name}} please select [W]eather, [X]icket, [G]ame or e[X]it ?", "W", "G", "X"),
						selector(
								botEngine.fulfilled("i/done"),
								botEngine.doSwitch("i/name")
										.when("W", weather())
										.when("G", game())
										//								.when("T", ticket())
										.build()),
						rate(),
						botEngine.remove("i/*"),
						botEngine.remove("rate/*")));
	}

	private static BTNode weather() {
		return sequence(
				botEngine.inputString("w/city", "Please choose a city"),
				botEngine.display("It's sunny in {{w/city}} !"),
				botEngine.set("i/done", "ok"),
				botEngine.remove("w/*"));
	}

	//	private BTNode ticket() {
	//		return sequence(
	//				botEngine.display("You have chosen to book a ticket, I have some questions..."),
	//				botEngine.fulfill("t/return", "Do you want a return ticket  ? Y/N", "Y", "N"),
	//				botEngine.fulfill("t/from", "from ?"),
	//				botEngine.fulfill("t/to", "to ?"),
	//				botEngine.fulfill("t/count", "How many tickets ?",
	//						BTUtils.isInteger().and(s -> Integer.valueOf(s) > 0 && Integer.valueOf(s) < 10)),
	//				loopUntil(botEngine.eq("t/idx", "{{t/count}}"),
	//						botEngine.inc("t/idx"),
	//						botEngine.fulfill("t/{{t/idx}}/name", "What is the name of the {{t/idx}} person ?"),
	//						botEngine.display("The ticket {{t/idx}} is booked for {{t/{{t/idx}}/name}}")),
	//				botEngine.display("Thank you, your ticket from {{t/from}} to {{t/to}} for {{t/count}} persons will be sent..."),
	//				botEngine.clear("t/*"));
	//	}

	private static BTNode game() {
		return sequence(
				//first select a random number between 0 and 100
				selector(
						botEngine.fulfilledInteger("g/target"),
						sequence(
								botEngine.display("You have chosen to play !"),
								botEngine.display("{{u/name}}, you must find the number I have chosen between 0 and 100"),
								botEngine.set("g/target",
										Double.valueOf(Math.floor(Math.random() * 101)).intValue()))),
				//make your choice until having found the right number
				selector(
						botEngine.eqIntegerByValue("g/target", "g/choice"),
						sequence(
								botEngine.inputInteger("g/choice", "What is your choice ?"),
								botEngine.incr("g/rounds"),
								selector(
										sequence(
												botEngine.gtByValue("g/target", "g/choice"),
												botEngine.display("select up !"),
												botEngine.remove("g/choice"),
												BTNodes.running()),
										sequence(
												botEngine.ltByValue("g/target", "g/choice"),
												botEngine.display("select down !"),
												botEngine.remove("g/choice"),
												BTNodes.running()),
										succeed()))),
				//The right number has been found
				botEngine.display("Bravo {{u/name}} you have found the right number {{g/target}} in {{g/rounds}} rounds"),
				botEngine.set("i/done", "ok"),
				botEngine.remove("g/*"));
	}

	private static BTNode rate() {
		return sequence(
				botEngine.inputString("rate/rating", "Please rate the response [0, 1, 2, 3, 4, 5]", "0", "1", "2", "3", "4", "5"),
				botEngine.display("You have rated {{rate/rating}}"),
				botEngine.remove("rate/*"));
	}
}

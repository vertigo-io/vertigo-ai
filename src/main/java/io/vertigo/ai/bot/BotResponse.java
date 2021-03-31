package io.vertigo.ai.bot;

public class BotResponse {

	public enum BotStatus {
		Talking, Ended;
	}

	public final BotStatus botStatus;
	public final String question;

	public static final BotResponse BOT_RESPONSE_END = new BotResponse(BotStatus.Ended, null);

	private BotResponse(final BotStatus botStatus, final String question) {
		this.botStatus = botStatus;
		this.question = question;
	}

	public static BotResponse talk(final String question) {
		return new BotResponse(BotStatus.Talking, question);
	}

}

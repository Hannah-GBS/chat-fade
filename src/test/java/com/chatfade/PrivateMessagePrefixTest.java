package com.chatfade;

import net.runelite.api.ChatMessageType;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Covers issue #20. For PRIVATECHATOUT the name field holds the recipient rather than the
 * local player, so without a direction prefix both directions render identically.
 */
public class PrivateMessagePrefixTest
{
	private static String prefix(String sender, ChatMessageType type)
	{
		return ChatFadePlugin.applyPrivateMessagePrefix(sender, type, true);
	}

	@Test
	public void incomingPrivateMessageIsPrefixedFrom()
	{
		assertEquals("From Zezima", prefix("Zezima", ChatMessageType.PRIVATECHAT));
	}

	@Test
	public void outgoingPrivateMessageIsPrefixedTo()
	{
		assertEquals("To Zezima", prefix("Zezima", ChatMessageType.PRIVATECHATOUT));
	}

	@Test
	public void moderatorPrivateMessageIsPrefixedFrom()
	{
		assertEquals("From Mod Ash", prefix("Mod Ash", ChatMessageType.MODPRIVATECHAT));
	}

	@Test
	public void directionsAreDistinguishable()
	{
		// The actual point of the issue: the two directions must not render the same.
		String in = prefix("Zezima", ChatMessageType.PRIVATECHAT);
		String out = prefix("Zezima", ChatMessageType.PRIVATECHATOUT);

		org.junit.Assert.assertNotEquals(in, out);
	}

	// ── Types that must not be touched ──────────────────────

	@Test
	public void publicChatIsNotPrefixed()
	{
		assertEquals("Zezima", prefix("Zezima", ChatMessageType.PUBLICCHAT));
	}

	@Test
	public void loginLogoutNotificationIsNotPrefixed()
	{
		// Grouped under Private Messages for visibility, but not directional.
		assertEquals("Zezima", prefix("Zezima", ChatMessageType.LOGINLOGOUTNOTIFICATION));
	}

	@Test
	public void clanChatIsNotPrefixed()
	{
		assertEquals("Zezima", prefix("Zezima", ChatMessageType.CLAN_CHAT));
	}

	@Test
	public void npcDialogueIsNotPrefixed()
	{
		assertEquals("Hans", prefix("Hans", ChatMessageType.DIALOG));
	}

	// ── Toggle ──────────────────────────────────────────────

	@Test
	public void toggleOffLeavesEveryTypeUnchanged()
	{
		assertEquals("Zezima",
			ChatFadePlugin.applyPrivateMessagePrefix("Zezima", ChatMessageType.PRIVATECHAT, false));
		assertEquals("Zezima",
			ChatFadePlugin.applyPrivateMessagePrefix("Zezima", ChatMessageType.PRIVATECHATOUT, false));
	}

	// ── Channel name prefix ─────────────────────────────────

	@Test
	public void prefixesTheChannelNameInBrackets()
	{
		assertEquals("[Valence] Bob", ChatFadePlugin.applyChannelPrefix("Bob", "Valence"));
	}

	@Test
	public void leavesSenderAloneWhenThereIsNoChannel()
	{
		// Only clan and friends chat carry a channel; every other type has none.
		assertEquals("Bob", ChatFadePlugin.applyChannelPrefix("Bob", null));
		assertEquals("Bob", ChatFadePlugin.applyChannelPrefix("Bob", ""));
	}

	@Test
	public void unescapesChannelNames()
	{
		// Channel names go through the same escaping as any other game text.
		assertEquals("[Bob@s clan] Bob",
			ChatFadePlugin.applyChannelPrefix("Bob", "Bob<at>s clan"));
	}

	@Test
	public void composesWithThePmDirectionPrefix()
	{
		// The two prefixes are independent; a PM has no channel, but the ordering must be
		// stable if both ever applied.
		String withDirection = ChatFadePlugin.applyPrivateMessagePrefix(
			"Bob", ChatMessageType.PRIVATECHAT, true);

		assertEquals("From Bob", ChatFadePlugin.applyChannelPrefix(withDirection, null));
	}
}

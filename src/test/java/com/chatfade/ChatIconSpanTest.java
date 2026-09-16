package com.chatfade;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.IntFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Inline chat icons. Emoji, clan and friends chat rank badges, ironman icons and mod crowns
 * all arrive as {@code <img=N>} indexes into one shared client array, so a single path
 * covers every one of them.
 */
public class ChatIconSpanTest
{
	private static final Color FALLBACK = new Color(100, 200, 255);

	private static final BufferedImage ICON_A = new BufferedImage(11, 11, BufferedImage.TYPE_INT_ARGB);
	private static final BufferedImage ICON_B = new BufferedImage(11, 11, BufferedImage.TYPE_INT_ARGB);

	/** Stands in for the client's mod icon array. */
	private static final IntFunction<BufferedImage> RESOLVER = index ->
	{
		switch (index)
		{
			case 1:
				return ICON_A;
			case 2:
				return ICON_B;
			default:
				return null;
		}
	};

	private static String textOf(List<ColorSpan> spans)
	{
		StringBuilder sb = new StringBuilder();
		spans.forEach(s -> sb.append(s.getText()));
		return sb.toString();
	}

	// ── Icons alone are enough to need spans ────────────────

	@Test
	public void producesSpansForAMessageWithOnlyAnIcon()
	{
		// No colour markup at all, so before icons this returned null and rendered as
		// plain text with the tag stripped.
		List<ColorSpan> spans = ChatFadePlugin.parseColorSpans("hi <img=1> there", FALLBACK, RESOLVER);

		assertNotNull(spans);
		assertEquals(3, spans.size());
		assertEquals("hi ", spans.get(0).getText());
		assertTrue(spans.get(1).isIcon());
		assertSame(ICON_A, spans.get(1).getImage());
		assertEquals(" there", spans.get(2).getText());
	}

	@Test
	public void iconSpansCarryNoText()
	{
		// Reassembling spans must still yield exactly what the overlay would draw as text.
		List<ColorSpan> spans = ChatFadePlugin.parseColorSpans("a<img=1>b", FALLBACK, RESOLVER);

		assertEquals("ab", textOf(spans));
	}

	@Test
	public void handlesSeveralIconsInOneMessage()
	{
		List<ColorSpan> spans = ChatFadePlugin.parseColorSpans("<img=1><img=2>go", FALLBACK, RESOLVER);

		assertNotNull(spans);
		assertEquals(3, spans.size());
		assertSame(ICON_A, spans.get(0).getImage());
		assertSame(ICON_B, spans.get(1).getImage());
		assertEquals("go", spans.get(2).getText());
	}

	@Test
	public void combinesIconsWithColourMarkup()
	{
		List<ColorSpan> spans = ChatFadePlugin.parseColorSpans(
			"<col=ff0000>red <img=1> still red</col>", FALLBACK, RESOLVER);

		assertNotNull(spans);
		assertEquals("red  still red", textOf(spans));
		assertEquals(new Color(0xFF0000), spans.get(0).getColor());
		assertTrue(spans.get(1).isIcon());
		assertEquals(new Color(0xFF0000), spans.get(2).getColor());
	}

	// ── Unresolvable and disabled ───────────────────────────

	@Test
	public void dropsIconsWhenNoResolverIsSupplied()
	{
		// The option is off: behaviour must match what it was before icons existed.
		assertNull(ChatFadePlugin.parseColorSpans("hi <img=1> there", FALLBACK, null));
	}

	@Test
	public void dropsIconsTheClientCannotResolve()
	{
		// An index past the end of the array, or a sprite that will not convert.
		assertNull(ChatFadePlugin.parseColorSpans("hi <img=99> there", FALLBACK, RESOLVER));
	}

	@Test
	public void ignoresMalformedIconTags()
	{
		assertNull(ChatFadePlugin.parseColorSpans("hi <img=> there", FALLBACK, RESOLVER));
		assertNull(ChatFadePlugin.parseColorSpans("hi <imgx=1> there", FALLBACK, RESOLVER));
	}

	// ── Independence from the colour option ─────────────────

	@Test
	public void dropSpanColoursKeepsIconsAndFlattensText()
	{
		// "Preserve In-Game Colors" off must still draw icons, with text at the per-type
		// colour rather than whatever the message asked for.
		List<ColorSpan> spans = ChatFadePlugin.parseColorSpans(
			"<col=ff0000>red <img=1></col>", FALLBACK, RESOLVER);
		List<ColorSpan> flattened = ChatFadePlugin.dropSpanColours(spans, FALLBACK);

		assertNotNull(flattened);
		assertEquals("red ", flattened.get(0).getText());
		assertEquals(FALLBACK, flattened.get(0).getColor());
		assertTrue(flattened.get(1).isIcon());
	}

	@Test
	public void dropSpanColoursReturnsNullWhenThereIsNothingToDraw()
	{
		// Colour-only spans with no icons collapse back to plain text rendering.
		List<ColorSpan> spans = ChatFadePlugin.parseColorSpans(
			"<col=ff0000>just red</col>", FALLBACK, RESOLVER);

		assertNotNull(spans);
		assertNull(ChatFadePlugin.dropSpanColours(spans, FALLBACK));
		assertNull(ChatFadePlugin.dropSpanColours(null, FALLBACK));
	}

	// ── Text spans are unaffected ───────────────────────────

	@Test
	public void plainTextIsStillNotWorthSpans()
	{
		assertNull(ChatFadePlugin.parseColorSpans("no markup here", FALLBACK, RESOLVER));
	}

	@Test
	public void escapedCharactersStillSurviveAlongsideIcons()
	{
		List<ColorSpan> spans = ChatFadePlugin.parseColorSpans(
			"<at>bob<at> <img=1>", FALLBACK, RESOLVER);

		assertNotNull(spans);
		assertEquals("@bob@ ", textOf(spans));
		assertTrue(spans.get(spans.size() - 1).isIcon());
	}

	@Test
	public void iconSpansAreNotFilteredOutAsEmpty()
	{
		// Icon spans deliberately hold empty text; the empty-span cleanup must skip them.
		List<ColorSpan> spans = ChatFadePlugin.parseColorSpans("<img=1>", FALLBACK, RESOLVER);

		assertNotNull(spans);
		assertEquals(1, spans.size());
		assertTrue(spans.get(0).isIcon());
		assertFalse(spans.isEmpty());
	}
}

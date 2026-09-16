package com.chatfade;

import java.awt.Color;
import java.awt.image.BufferedImage;
import lombok.Value;

/**
 * A run of a message that renders as one unit: either coloured text, or an inline chat icon.
 *
 * <p>Icon spans carry an empty {@link #text} so that reassembling a message's spans still
 * produces exactly the text the overlay would otherwise draw.
 */
@Value
public class ColorSpan
{
	String text;
	Color color;

	/** Non-null when this span is an inline chat icon rather than text. */
	BufferedImage image;

	ColorSpan(String text, Color color)
	{
		this(text, color, null);
	}

	ColorSpan(String text, Color color, BufferedImage image)
	{
		this.text = text;
		this.color = color;
		this.image = image;
	}

	static ColorSpan icon(BufferedImage image)
	{
		return new ColorSpan("", null, image);
	}

	boolean isIcon()
	{
		return image != null;
	}
}

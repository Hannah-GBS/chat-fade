package com.chatfade;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import net.runelite.api.Client;
import net.runelite.api.IndexedSprite;

/**
 * Resolves the {@code <img=N>} references the game puts in chat text into drawable images.
 *
 * <p>Everything that appears as an inline chat icon — emoji registered by RuneLite's Emoji
 * plugin, clan and friends chat rank badges, mod crowns, ironman icons — is an index into
 * the single {@link Client#getModIcons()} array, so one lookup covers all of them.
 *
 * <p>Resolution happens at message ingest, which runs on the client thread. That keeps the
 * sprite array off the render path entirely and means each icon is converted once rather
 * than every frame while the message fades.
 */
class ChatIcons
{
	static final Pattern IMG_TAG = Pattern.compile("<img=(\\d+)>");

	/**
	 * Converted sprites, keyed by mod icon index. The array is rebuilt when plugins register
	 * icons, so entries are dropped whenever its length changes rather than kept forever.
	 */
	private final Map<Integer, BufferedImage> cache = new HashMap<>();
	private int cachedIconCount = -1;

	/**
	 * @param client the client, which must be accessed from the client thread
	 * @param index the {@code N} from an {@code <img=N>} tag
	 * @return the icon, or null when the index is out of range or the sprite is unusable
	 */
	BufferedImage resolve(Client client, int index)
	{
		if (client == null || index < 0)
		{
			return null;
		}

		IndexedSprite[] icons = client.getModIcons();
		if (icons == null || index >= icons.length)
		{
			return null;
		}

		if (icons.length != cachedIconCount)
		{
			// Another plugin registered icons and the array was reallocated; indices may
			// have shifted, so previously converted entries can no longer be trusted.
			cache.clear();
			cachedIconCount = icons.length;
		}

		BufferedImage cached = cache.get(index);
		if (cached != null)
		{
			return cached;
		}

		BufferedImage converted = toBufferedImage(icons[index]);
		if (converted != null)
		{
			cache.put(index, converted);
		}
		return converted;
	}

	void clear()
	{
		cache.clear();
		cachedIconCount = -1;
	}

	/**
	 * Converts a palette-indexed sprite to ARGB. Palette entry 0 is the sprite's
	 * transparent colour, so those pixels are left fully transparent.
	 */
	private static BufferedImage toBufferedImage(IndexedSprite sprite)
	{
		if (sprite == null)
		{
			return null;
		}

		byte[] pixels = sprite.getPixels();
		int[] palette = sprite.getPalette();
		int width = sprite.getWidth();
		int height = sprite.getHeight();

		if (pixels == null || palette == null || width <= 0 || height <= 0
			|| pixels.length < width * height)
		{
			return null;
		}

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int paletteIndex = pixels[y * width + x] & 0xFF;
				if (paletteIndex == 0 || paletteIndex >= palette.length)
				{
					continue;
				}
				image.setRGB(x, y, 0xFF000000 | palette[paletteIndex]);
			}
		}
		return image;
	}
}

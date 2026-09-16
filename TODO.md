# Chat Fade - TODO

## Completed
- **Typing input overlay** — Show typed text with blinking `>` when chatbox is collapsed
- **Command response display** — Commands like `!task`, `!kc` now show the actual response instead of raw command text
- **Per-type custom colors** — Customizable color pickers for each message category when "Use Default Colors" is off
- **Font picker** — Full font selection (family, size, bold, italic) via RuneLite's FontType config
- **Respect chat tab filters** — Messages hidden by the game's Filtered/Off tab states are also hidden from Chat Fade
- **Above-chatbox positioning** — When chatbox is open, overlay renders above the chatbox instead of overlapping it
- **Split PM avoidance** — Overlay dynamically repositions above any visible split private chat messages
- **Username colorization** — Player names render in a separate configurable color for readability
- **Dialog awareness** — Game dialogs requiring a response (e.g. High Alchemy warning) pin messages at full opacity and show an "Open chatbox to continue" prompt
- **NPC dialogue formatting** — DIALOG/MESBOX messages split on `|` so NPC name renders separately with its own configurable color (golden yellow by default)
- **Dialog prompt improvements** — Prompt is widget-driven (disappears immediately on dismiss); MESBOX fallback clears the instant the chatbox is opened; never shows while chatbox is open
- **CA_ID prefix stripping** — Combat Achievement clan messages have the `CA_ID:###` prefix stripped at ingest so only the human-readable text is displayed
- **Guest / GIM chat separation** — Clan, Guest Clan and Group Ironman chat have independent show toggles and colours (#19, thanks @TyboJones24)
- **`@name@` colour macros** — Jagex's older palette syntax (e.g. `@mes_hl_pur@`) is expanded via `Client.macroExpand` at ingest, so it renders in the game's own colours instead of appearing as literal text (#21)
- **Chat Filter integration fixed** — `chatFilterCheck` fires after `ChatMessage`, so blocked messages are now removed retroactively by message id (#11, #22, thanks @jarredgoddard)
- **Per-message ignore lists** — `Ignored Messages` (comma-separated fragments) and `Ignored Regex`, independent of the Chat Filter plugin (#11, #22)
- **PM direction** — Private messages are prefixed `From`/`To` so incoming and outgoing are distinguishable (#20)
- **Inline chat icons** — Emoji, clan/friends chat rank badges, ironman icons and mod crowns render in the overlay. `<img=N>` tags resolve against `client.getModIcons()`; rank badges are not in the message at all and are looked up from the channel
- **Channel names** — Optional `[Clan name]` prefix on clan and friends chat, drawn in the message type's colour
- **Loot value tiers** — Clan drop broadcasts and the player's own "Valuable drop" notifications colour the item and value by GE value, mirroring the Ground Items plugin's tiers (#24)
- **Collection log highlighting** — Clan collection log broadcasts colour the item name (#24)
- **Escaped character handling** — The game escapes printable characters as pseudo-tags (`<at>`, `<lt>`, `<gt>`); these are now restored instead of being stripped along with real markup, so a typed `@` survives
- **Chat Filter censor mode** — "Censor Words" rewrites text rather than blocking it, and only on the object stack; the overlay now picks that rewrite up instead of showing the uncensored original

## Backlog
- **`CA_ID:###` still appearing** — Combat Achievement messages sometimes render the raw `CA_ID:123` marker instead of the achievement name. Prefix stripping exists at ingest and handles the common form, so something is reaching the overlay by a path it does not cover — possibly a later rewrite of the message node, or a format the regex does not match. Needs a captured example of the raw message to diagnose.
- **Wrap long messages** — The overlay truncates at Max Message Width. Wrapping needs each message to occupy a variable number of lines, which changes how the stack's height and position are calculated, and requires breaking across spans (an icon is a span too). Open questions: whether the `[channel] badge name:` prefix repeats or indents on continuation lines, whether there is a per-message line cap, and whether Max Visible Messages should count lines instead of messages.

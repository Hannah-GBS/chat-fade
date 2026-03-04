# Chat Fade - TODO

## Completed
- **Typing input overlay** — Show typed text with blinking `>` when chatbox is collapsed

## Planned

### 1. Command response display
Chat commands like `!task`, `!kc`, `!lvl` show the raw command text in Chat Fade instead of the response detail (e.g., slayer task info). The command responses likely arrive as separate ChatMessage events after the initial command message — need to investigate whether we're capturing those response messages or only the user's input.

### 2. Per-type custom colors
When "Use Chat Type Colors" is unchecked, currently all messages use a single custom color. Instead, expose individual color pickers for each message category:
- Private chat
- Clan chat
- Game messages
- Healing/potion messages
- Boss/kill messages
- Broadcast messages
- Friends chat
- Trade messages
- Examine messages

This gives users full control over their Chat Fade color scheme.

### 3. Emoji rendering
Other plugins (e.g., Emoji plugin) render emoji shortcodes like `:rat:`, `:boy:` as actual images in chat. Chat Fade currently shows the raw shortcode text instead. Investigate whether we can hook into the emoji plugin's image rendering or parse the shortcodes ourselves to display the actual emoji sprites in the overlay.

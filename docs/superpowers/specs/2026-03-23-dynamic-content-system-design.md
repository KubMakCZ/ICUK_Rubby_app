# Dynamic Content System for Rubby Pepper Robot

**Date:** 2026-03-23
**Status:** Approved
**Branch:** `dynamic`

## Problem

The Rubby Pepper robot app at ICUK requires a developer to modify Java code, XML layouts, QiChat files, and string resources for every event. ICUK colleagues without programming experience cannot make content changes.

## Solution

A single Markdown file (`assets/content.md`) drives the entire app: tile count, tile titles, speech text, and optional images. No code changes needed per event.

## Configuration File Format

File: `app/src/main/assets/content.md`

```markdown
# Tile Title

Text that Rubby will speak for this tile.

![optional description](image_file.png)

# Another Tile

More speech text here.
```

Rules:
- Each `# Heading` = one tile (only `#` level 1 headings count)
- Text below heading = Rubby's speech for that tile
- `![desc](file.png)` = optional image (must exist in `assets/images/`)
- Order in file = order on screen
- Number of headings = number of tiles (max 10 supported via voice commands)
- Tile buttons display as "i. Title" (number auto-generated from order)
- Blank lines between paragraphs are collapsed into a single space for speech
- `##` subheadings, `**bold**`, and other Markdown formatting are stripped (treated as plain text)

## Architecture

### What stays unchanged
- `MainActivity.java` — entry point, Robot SDK lifecycle (modified for dynamic content)
- `LoadingFragment.java` — loading screen
- `SplashFragment.java` — idle screen (5 min timeout)
- `CountDownNoInteraction.java` — timeout timer
- `concepts.top` — Czech speech recognition concepts (~pozdrav, ~rozloucit, ~ukazat, ~restartik, ~confirmation)
- Bottom logos on main screen (ICUK, UJEP, region)
- Fade in/out animations

### New components
- **`TileItem.java`** — data class: `title` (String), `speechText` (String), `imageName` (String, nullable)
- **`ContentParser.java`** — reads `assets/content.md`, returns `List<TileItem>`
- **`TileAdapter.java`** — RecyclerView adapter for main menu grid (2 columns)
- **`DynamicScreenFragment.java`** — universal detail fragment, receives tile index via Bundle argument, displays title + optional image + "Opakovat řeč" and "Úvodní obrazovka" buttons
- **`fragment_dynamic_screen.xml`** — universal detail layout
- **`dynamic.top`** — template QiChat topic for tile speech (uses QiChat variable)
- Updated **`fragment_main.xml`** — RecyclerView replacing hardcoded buttons
- **`assets/images/`** — directory for optional tile images

### What gets removed
- `ScreenOneFragment.java` through `ScreenTenFragment.java` (all 10)
- `fragment_one.xml` through `fragment_ten.xml` (all 10)
- `screenone.top` through `screenten.top` (all per-screen .top files)
- `everthing.top` (unused)
- `VariableExecutor.java` (no longer needed)
- Static tile button definitions from `fragment_main.xml`

### QiChat handling — key design decision

**Constraint:** QiSDK 1.7.5 `TopicBuilder` only loads topics from compiled resources (`res/raw/`). Topics cannot be generated from strings at runtime. This fundamentally shapes the approach.

**Solution: Template topic + QiChat variable**

A single `dynamic.top` template topic in `res/raw/`:

```
topic: ~dynamic()

proposal: %init $tileSpeech
proposal: %say $tileSpeech

u:([~restartik ~rozloucit]) ~confirmation ^execute(FragmentExecutor, frag_main)
```

- `$tileSpeech` is a QiChat variable set at runtime before navigating to the "init" bookmark
- All tiles share this one topic — the variable value changes per tile
- Bookmark `%say` is used by "Opakovat řeč" button

**Navigation voice commands** stay hardcoded in `main.top` for numbers 1-10 (Czech variants). The `FragmentExecutor` validates the tile index — if a user says "šest" but only 5 tiles exist, nothing happens.

Updated `main.top` navigation rules:
```
u:({~ukazat} ["1" "jedna" ...]) ~confirmation ^execute(FragmentExecutor, frag_dynamic, 0)
u:({~ukazat} ["2" "dvě" ...]) ~confirmation ^execute(FragmentExecutor, frag_dynamic, 1)
...up to 10...
```

Global commands (jokes, greetings, weather) remain in `main.top` unchanged.

**Topic list loaded at startup:**
`["main", "dynamic", "concepts"]` — only 3 topics instead of 10+.

**Note:** Concepts defined in `concepts.top` (`~restartik`, `~rozloucit`, `~confirmation`, etc.) are globally available across all topics loaded in the same `QiChatbot`. The `dynamic.top` topic depends on these concepts — `concepts.top` must always be included in the topic list.

### FragmentExecutor changes

Current: hardcoded switch mapping `frag_screen_one` → `ScreenOneFragment`, etc.

New: `FragmentExecutor` detects `frag_dynamic` prefix, parses the second parameter as tile index, creates `DynamicScreenFragment` with index passed via `Bundle`:

```java
case "frag_dynamic":
    int index = Integer.parseInt(params.get(1).trim()); // .trim() needed — QiChat may add leading spaces to params
    if (index >= 0 && index < ma.getTileItems().size()) {
        fragment = DynamicScreenFragment.newInstance(index);
    } else {
        return; // invalid index, ignore
    }
    break;
```

### setFragment changes

Current: derives topic name from fragment class name (`DynamicScreenFragment` → `"dynamicscreen"` — wrong).

New: `setFragment` is overloaded. When called with `DynamicScreenFragment`:
1. Gets tile index from fragment's Bundle arguments
2. Sets `$tileSpeech` variable to `tileItems[index].speechText`
3. Navigates to bookmark "init" in topic "dynamic"

```java
public void setFragment(Fragment fragment) {
    if (fragment instanceof DynamicScreenFragment) {
        int index = fragment.getArguments().getInt("tile_index");
        TileItem tile = tileItems.get(index);
        // IMPORTANT: setQiVariable uses async().setValue() — must chain goToBookmark
        // AFTER the variable is set to avoid a race condition where the robot speaks
        // an empty or stale $tileSpeech value.
        currentChatBot.variables.get("tileSpeech").async().setValue(tile.getSpeechText())
            .andThenConsume(aVoid -> {
                currentChatBot.goToBookmarkNewTopic("init", "dynamic");
            });
    } else if (!(fragment instanceof LoadingFragment) && !(fragment instanceof SplashFragment)) {
        String topicName = fragment.getClass().getSimpleName().toLowerCase().replace("fragment", "");
        currentChatBot.goToBookmarkNewTopic("init", topicName);
    }
    // ... fragment transaction as before
}
```

**Note on QiChat variable registration:** The existing `ChatData.setupQiVariable()` reinitializes its internal `variables` map each time it is called. Since we need both `"qiVariable"` (legacy, if kept) and `"tileSpeech"`, we must use `setupQiVariables(List)` with both names at once, or refactor `setupQiVariable` to not clear the map. The recommended approach is to use the list version:
```java
englishChatBot.setupQiVariables(Arrays.asList("tileSpeech"));
```
(The old `"qiVariable"` is no longer needed since `VariableExecutor` is being removed.)

## Data Flow

### App startup
```
MainActivity.onCreate()
  └─ LoadingFragment
      └─ onRobotFocusGained()
          ├─ ContentParser reads assets/content.md
          │   └─ returns List<TileItem>, stored in MainActivity
          ├─ ChatData initializes QiChat:
          │   ├─ loads main.top (global + navigation commands 1-10)
          │   ├─ loads dynamic.top (template with $tileSpeech variable)
          │   ├─ loads concepts.top (Czech speech recognition)
          │   └─ sets up QiChat variable "tileSpeech"
          └─ displays MainFragment
```

### Tile interaction
```
MainFragment (RecyclerView grid, 2 columns)
  └─ click tile[i] OR voice command "number i"
      └─ FragmentExecutor validates index against tileItems.size()
          └─ DynamicScreenFragment(index=i)
              ├─ setFragment sets $tileSpeech = tileItems[i].speechText
              ├─ navigates to bookmark "init" in topic "dynamic"
              ├─ Rubby speaks the $tileSpeech content
              ├─ displays "i+1. tileItems[i].title" as heading
              ├─ if tileItems[i].imageName != null → loads image from assets/images/
              ├─ "Opakovat řeč" → goToBookmarkSameTopic("say")
              └─ "Úvodní obrazovka" → setFragment(new MainFragment())
```

## Main Screen Layout

- RecyclerView with GridLayoutManager (2 columns)
- Tiles auto-arranged: 2 per row, last row may have 1
- Scroll enabled as safety net for 10+ tiles
- Minimum tile height 48dp for touch usability
- Bottom logos remain hardcoded (ICUK, UJEP, region)

## Content Parser Details

- Simple line-by-line state machine (no external library dependency — safe for Android 6)
- Lines starting with `# ` start a new tile (only level-1 headings)
- All other text lines within a tile section are concatenated as speech text (blank lines collapsed to single space)
- `![...](...) ` pattern on its own line is parsed as optional image reference
- `##`, `**bold**`, `*italic*`, and other Markdown formatting are stripped to plain text
- Special QiChat characters in speech text (`$`, `~`, `^`, `[`, `]`) must be escaped or stripped by the parser to avoid breaking QiChat syntax
- Handles BOM markers and both `\r\n` and `\n` line endings
- Heading with no body text → tile exists but Rubby says nothing (empty speech)

## Error Handling

- `content.md` missing or empty → MainFragment shows message "Chybí soubor content.md" instead of tiles
- Image referenced in Markdown not found in `assets/images/` → image view hidden, tile works without it
- Voice command for non-existent tile (e.g., "šest" with 5 tiles) → `FragmentExecutor` ignores, no crash

## Colleague Workflow

To prepare Rubby for a new event:

1. Open Android Studio
2. Edit `app/src/main/assets/content.md`
3. Write headings and speech text in Markdown
4. (Optional) Add images to `app/src/main/assets/images/` and reference them as `![desc](filename.png)`
5. Build APK and deploy to robot
6. Done — no Java, XML, or QiChat editing needed

## Future Considerations

- **Online content source (variant B):** ContentParser is decoupled from file source. Future version could load from Google Docs/online file instead of local assets. Android 6 compatibility to be verified.
- **Multiple images per tile:** Current design supports one image. Could be extended later.
- **Logo customization:** Bottom logos are hardcoded for now, could be made dynamic later.

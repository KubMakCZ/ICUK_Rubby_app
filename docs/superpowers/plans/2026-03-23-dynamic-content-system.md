# Dynamic Content System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace 10 hardcoded screen fragments with a single dynamic content system driven by one Markdown file (`assets/content.md`).

**Architecture:** A `ContentParser` reads a Markdown file at startup and produces a list of `TileItem` objects. `MainFragment` displays tiles in a RecyclerView grid (2 columns). Clicking a tile opens `DynamicScreenFragment` which sets a QiChat variable `$tileSpeech` and navigates to a template topic bookmark so Rubby speaks the tile's text.

**Tech Stack:** Java, Android SDK 23+, QiSDK 1.7.5, AndroidX RecyclerView, ConstraintLayout

**Spec:** `docs/superpowers/specs/2026-03-23-dynamic-content-system-design.md`

---

## File Structure

### New files
| File | Responsibility |
|------|---------------|
| `app/src/main/java/.../TileItem.java` | Data class: title, speechText, imageName |
| `app/src/main/java/.../ContentParser.java` | Parses `content.md` into `List<TileItem>` |
| `app/src/main/java/.../TileAdapter.java` | RecyclerView adapter for main menu grid |
| `app/src/main/java/.../Fragments/DynamicScreenFragment.java` | Universal detail fragment |
| `app/src/main/res/layout/fragment_dynamic_screen.xml` | Layout for DynamicScreenFragment |
| `app/src/main/res/layout/item_tile.xml` | Layout for a single tile button in RecyclerView |
| `app/src/main/res/raw/dynamic.top` | QiChat template topic with `$tileSpeech` variable |
| `app/src/main/assets/content.md` | Default content file with current ICUK event tiles |
| `app/src/main/assets/images/` | Directory for optional tile images |

### Modified files
| File | Changes |
|------|---------|
| `app/build.gradle` | Add RecyclerView dependency |
| `app/src/main/java/.../MainActivity.java` | Store `List<TileItem>`, parse content, update topic list, overload `setFragment` for dynamic tiles, remove VariableExecutor |
| `app/src/main/java/.../Fragments/MainFragment.java` | Replace hardcoded buttons with RecyclerView |
| `app/src/main/java/.../Executors/FragmentExecutor.java` | Replace hardcoded switch with dynamic `frag_dynamic` handler |
| `app/src/main/java/.../Utils/ChatData.java` | Fix `setupQiVariable` to not reinitialize map |
| `app/src/main/res/layout/fragment_main.xml` | Replace 8 buttons with RecyclerView + logos |
| `app/src/main/res/raw/main.top` | Update navigation commands to use `frag_dynamic, N` |
| `app/src/main/res/values-cs/strings.xml` | Clean up unused string resources |
| `app/src/main/res/values/strings.xml` | Clean up unused string resources |

### Deleted files
| File | Reason |
|------|--------|
| `app/src/main/java/.../Fragments/Screen{One..Ten}Fragment.java` | Replaced by DynamicScreenFragment |
| `app/src/main/java/.../Executors/VariableExecutor.java` | No longer needed |
| `app/src/main/res/layout/fragment_{one..ten}.xml` | Replaced by fragment_dynamic_screen.xml |
| `app/src/main/res/raw/screen{one..ten}.top` | Replaced by dynamic.top |
| `app/src/main/res/raw/everthing.top` | Unused |

**Note:** All Java paths below use the prefix `app/src/main/java/com/softbankrobotics/pepperapptemplate/` abbreviated as `.../`.

---

### Task 1: Add RecyclerView dependency

**Files:**
- Modify: `app/build.gradle:49-61`

- [ ] **Step 1: Add RecyclerView to build.gradle**

In the `dependencies` block, add:
```groovy
implementation 'androidx.recyclerview:recyclerview:1.1.0'
```

Use version 1.1.0 for Android 6 (API 23) compatibility.

- [ ] **Step 2: Verify the change**

Run: `grep recyclerview app/build.gradle`
Expected: the line appears in dependencies block.

- [ ] **Step 3: Commit**

```bash
git add app/build.gradle
git commit -m "feat: add RecyclerView dependency for dynamic tile grid"
```

---

### Task 2: Create TileItem data class

**Files:**
- Create: `.../TileItem.java`

- [ ] **Step 1: Create TileItem.java**

```java
package com.softbankrobotics.pepperapptemplate;

public class TileItem {
    private final String title;
    private final String speechText;
    private final String imageName; // nullable

    public TileItem(String title, String speechText, String imageName) {
        this.title = title;
        this.speechText = speechText;
        this.imageName = imageName;
    }

    public String getTitle() { return title; }
    public String getSpeechText() { return speechText; }
    public String getImageName() { return imageName; }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/softbankrobotics/pepperapptemplate/TileItem.java
git commit -m "feat: add TileItem data class for dynamic content"
```

---

### Task 3: Create ContentParser

**Files:**
- Create: `.../ContentParser.java`

- [ ] **Step 1: Create ContentParser.java**

A line-by-line state machine that reads `content.md` from assets and produces `List<TileItem>`.

```java
package com.softbankrobotics.pepperapptemplate;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentParser {

    private static final String TAG = "MSI_ContentParser";
    private static final Pattern HEADING_PATTERN = Pattern.compile("^#\\s+(.+)$");
    private static final Pattern IMAGE_PATTERN = Pattern.compile("^!\\[.*?]\\((.+?)\\)$");
    // Characters that could break QiChat syntax
    private static final Pattern QICHAT_SPECIAL = Pattern.compile("[\\$~\\^\\[\\]%]");

    public static List<TileItem> parse(Context context) {
        List<TileItem> items = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("content.md");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String currentTitle = null;
            StringBuilder speechBuilder = new StringBuilder();
            String currentImage = null;
            String line;

            while ((line = reader.readLine()) != null) {
                // Strip BOM if present
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                line = line.trim();

                Matcher headingMatcher = HEADING_PATTERN.matcher(line);
                if (headingMatcher.matches()) {
                    // Save previous tile if exists
                    if (currentTitle != null) {
                        items.add(new TileItem(currentTitle,
                                sanitizeSpeech(speechBuilder.toString().trim()),
                                currentImage));
                    }
                    currentTitle = headingMatcher.group(1).trim();
                    speechBuilder = new StringBuilder();
                    currentImage = null;
                    continue;
                }

                if (currentTitle == null) continue; // skip lines before first heading

                Matcher imageMatcher = IMAGE_PATTERN.matcher(line);
                if (imageMatcher.matches()) {
                    currentImage = imageMatcher.group(1).trim();
                    continue;
                }

                // Skip ## subheadings, strip markdown formatting
                if (line.startsWith("##")) continue;

                // Strip bold/italic markers
                String cleanLine = line.replaceAll("\\*+", "").replaceAll("_+", "");

                if (!cleanLine.isEmpty()) {
                    if (speechBuilder.length() > 0) {
                        speechBuilder.append(" ");
                    }
                    speechBuilder.append(cleanLine);
                }
            }
            // Save last tile
            if (currentTitle != null) {
                items.add(new TileItem(currentTitle,
                        sanitizeSpeech(speechBuilder.toString().trim()),
                        currentImage));
            }
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to read content.md: " + e.getMessage());
        }
        return items;
    }

    private static String sanitizeSpeech(String text) {
        // Remove QiChat special characters that could break syntax
        return QICHAT_SPECIAL.matcher(text).replaceAll("");
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/softbankrobotics/pepperapptemplate/ContentParser.java
git commit -m "feat: add ContentParser to read tiles from assets/content.md"
```

---

### Task 4: Create default content.md and images directory

**Files:**
- Create: `app/src/main/assets/content.md`
- Create: `app/src/main/assets/images/.gitkeep`

- [ ] **Step 1: Create assets/content.md**

Migrate current ICUK event content from the existing `.top` files into Markdown format:

```markdown
# Já jsem Rubby

Ahoj, všichni! Já jsem Rubby Pepper. Vaše zvídavá, chytrá a přátelská robotka z Ústeckého kraje. A nebojte, nevychloubám se. Teda možná jen trošku. Jestli tě zajímá ICUK, naše služby a programy pro studenty, nebo chceš vědět víc o tom, co všechno ti může nabídnout Ústecký kraj, stačí si vybrat v menu a já ti hned všechno povím. Budu se těšit, že se potkáme na některé z akcí. Tak klikni, nebo se zeptej buď mně nebo kolegů, kteří jsou tady se mnou.

# Inovační centrum Ústeckého kraje

Icuk. Inovační centrum Ústeckého kraje neboli Icuk je skvělá organizace, protože podporuje vzdělávání, startupy, inovace, podnikání a vědu a výzkum. Rozvíjí region, hledá nové příležitosti pro kraj, a snaží se, aby lidé tady studovali, pracovali, podnikali a žili. Více o icuku najdete na webu icuk tečka cézet.

# Ústecký kraj

Ústecký kraj. Ahoj. Já jsem Rubby, malá zvídavá robotka a mám ráda Ústecký kraj.

# UJEP

UJEP je zkratka a znamená Univerzita Jana Evangelisty Purkyně v Ústí nad Labem.

# ICUK pro vysokoškoláky

ICUK pro vysokoškoláky.

# ICUK BOOTCAMP

Intenzivní inovační stáž přímo na icuku.

# Univerzitní inkubátor

Pokud máš nápad na vlastní podnikání.

# Marketing prakticky

Na fakultě sociálně ekonomické.
```

**IMPORTANT:** The speech text above is abbreviated. The implementer MUST migrate the full speech text from the existing `.top` files:
- For each `screen{one..eight}.top`, copy the text after `proposal: %init` into the corresponding `# Heading` section
- Collapse multi-line text into a single paragraph (remove line breaks, keep as continuous text)
- The parser will handle the rest (stripping QiChat-incompatible characters, etc.)

- [ ] **Step 2: Create images directory**

Create `app/src/main/assets/images/.gitkeep` (empty file to track the directory in git).

- [ ] **Step 3: Commit**

```bash
git add app/src/main/assets/content.md app/src/main/assets/images/.gitkeep
git commit -m "feat: add default content.md with current ICUK event tiles"
```

---

### Task 5: Create dynamic.top QiChat template

**Files:**
- Create: `app/src/main/res/raw/dynamic.top`

- [ ] **Step 1: Create dynamic.top**

```
topic: ~dynamic()

proposal: %init $tileSpeech
proposal: %say $tileSpeech

u:([~restartik ~rozloucit]) ~confirmation ^execute(FragmentExecutor, frag_main)
```

This is the template topic used by all dynamic tiles. The `$tileSpeech` QiChat variable is set before navigating to the `init` or `say` bookmarks.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/res/raw/dynamic.top
git commit -m "feat: add dynamic.top QiChat template topic"
```

---

### Task 6: Update main.top navigation commands

**Files:**
- Modify: `app/src/main/res/raw/main.top`

- [ ] **Step 1: Update main.top**

Replace the navigation commands to use `frag_dynamic, N` instead of `frag_screen_*`. Keep all global commands (greetings, jokes, weather) unchanged.

Updated `main.top`:

```
topic: ~main()

proposal: %init Ahoj! Jsem Rubby. Vyberte si téma kliknutím na obrazovku nebo řekněte číslo.

u:(~pozdrav) Zdravíčko! Vyberte si téma kliknutím na obrazovku nebo řekněte číslo.

u:({~ukazat} ["1" "jedna" "jedná" "číslo jedná" "jednička" "jedničku" "číslo jedna" "číslo 1" "Kdo jsi" "Kdopak jsi" "Kdo si" "Má úvodní řeč" "Úvodní slovo" "Předávám Úvodní slovo" "můžeš rubby" "máš uvodní slovo" "ruby máš úvodní slovo"]) ~confirmation ^execute(FragmentExecutor, frag_dynamic, 0)

u:({~ukazat} ["2" "dvě" "číslo dva" "číslo 2" "dvojku" "dvojka" "dva"]) ~confirmation ^execute(FragmentExecutor, frag_dynamic, 1)

u:({~ukazat} ["3" "tři" "číslo tři" "tří" "číslo tří" "číslo 3" "trojka" "trojku"]) ~confirmation ^execute(FragmentExecutor, frag_dynamic, 2)

u:({~ukazat} ["čtyři" "čtyří" "4" "číslo čtyři" "číslo čtyří" "číslo 4" "čtyřka" "čtyřku"]) ~confirmation ^execute(FragmentExecutor, frag_dynamic, 3)

u:({~ukazat} ["pět" "5" "číslo pět" "číslo 5" "pětku" "pětka"]) ~confirmation ^execute(FragmentExecutor, frag_dynamic, 4)

u:({~ukazat} ["šest" "6" "číslo šest" "číslo 6" "šestka" "šestku"]) ~confirmation ^execute(FragmentExecutor, frag_dynamic, 5)

u:({~ukazat} ["sedum" "sedům" "sedm" "7" "číslo sedum" "sedmička" "sedmičku" "číslo sedům" "číslo sedm" "číslo 7"]) ~confirmation ^execute(FragmentExecutor, frag_dynamic, 6)

u:({~ukazat} ["osum" "osům" "osm" "8" "číslo osum" "číslo osům" "číslo osm" "osmička" "osmičku" "číslo 8"]) ~confirmation ^execute(FragmentExecutor, frag_dynamic, 7)

u:({~ukazat} ["devět" "9" "číslo devět" "číslo 9" "devítka" "devítku"]) ~confirmation ^execute(FragmentExecutor, frag_dynamic, 8)

u:({~ukazat} ["deset" "10" "číslo deset" "číslo 10" "desítka" "desítku"]) ~confirmation ^execute(FragmentExecutor, frag_dynamic, 9)

u:([~restartik ~rozloucit]) ~confirmation ^execute(FragmentExecutor, frag_main)

u:(["řekni vtip" "řekni ftip" "sranda" "ftip" "vtip" "pobav mě" "pobav nás" "řekni něco vtipného"]) ^rand["A Bůh pravil: Noe, udělej zálohu, budu formátovat!" "Jak si tři programátoři vydělávají na živobytí? Jeden vytvoří virus, druhý antivirový program a třetí prostředí, ve kterém to všechno funguje." "Víte co dělá Windows na Měsíci? Padá šestkrát pomaleji." "Proč programátoři nosí brýle? Protože nevidí Sharp." "Jsem robot. Nemám emoce. Ale kdybych je měla... tak bych si teď zakoulela očima." "Člověk se ptá robota: Umíš mluvit česky? Robot odpoví: Ano. Ale stálo to dvě aktualizace a tři rebooty." "Kolik programátorů potřebuješ na výměnu žárovky? Žádného. To je problém hardwaru." "Proč skončil robot v terapii? Protože měl příliš mnoho nevyřešených výjimek." "Jaký je oblíbený tanec programátorů? Algoritmus." "Umělá inteligence jednou nahradí lidi. Já jsem tady. Zatím vás jen bavím. Zatím."] Ha. Ha. Ha.

u:(["jaké je počasí" "jaké je venku počasí" "jaké bude počasí" "co říká předpověď" "jak je venku" "prší venku" "svítí slunce"]) Bohužel ještě neumím číst z internetu, jaké je počasí. Ale moje senzory říkají, že tady uvnitř je příjemně teplo!
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/res/raw/main.top
git commit -m "feat: update main.top navigation to use frag_dynamic indices"
```

---

### Task 7: Fix ChatData.setupQiVariable to not reinitialize map

**Files:**
- Modify: `.../Utils/ChatData.java:218-234`

- [ ] **Step 1: Fix setupQiVariable**

The current `setupQiVariable(String)` method creates a new `HashMap` every call, wiping previously registered variables. Fix it to append instead:

Replace the `setupQiVariable` method (lines 231-234) with:

```java
public void setupQiVariable(String qiVariablesName) {
    if (variables == null) {
        variables = new HashMap<>();
    }
    variables.put(qiVariablesName, qiChatbot.variable(qiVariablesName));
}
```

Also fix `setupQiVariables` (lines 218-223) the same way:

```java
public void setupQiVariables(List<String> qiVariablesNames) {
    if (variables == null) {
        variables = new HashMap<>();
    }
    for (String qiVariableName : qiVariablesNames) {
        variables.put(qiVariableName, qiChatbot.variable(qiVariableName));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/softbankrobotics/pepperapptemplate/Utils/ChatData.java
git commit -m "fix: ChatData.setupQiVariable no longer wipes existing variables"
```

---

### Task 8: Create DynamicScreenFragment and its layout

**Files:**
- Create: `.../Fragments/DynamicScreenFragment.java`
- Create: `app/src/main/res/layout/fragment_dynamic_screen.xml`

- [ ] **Step 1: Create fragment_dynamic_screen.xml**

Based on the existing fragment_one.xml pattern but with a dynamic title, optional image, and standard buttons:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:id="@+id/dynamic_title"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:textColor="@color/black"
        android:textStyle="bold"
        android:textSize="40sp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <ImageView
        android:id="@+id/dynamic_image"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:scaleType="fitCenter"
        android:visibility="gone"
        app:layout_constraintTop_toBottomOf="@id/dynamic_title"
        app:layout_constraintBottom_toTopOf="@id/dynamic_say_button"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="16dp"
        android:layout_marginBottom="16dp"
        android:layout_marginStart="32dp"
        android:layout_marginEnd="32dp" />

    <Button
        android:id="@+id/dynamic_say_button"
        android:layout_width="450dp"
        android:layout_height="wrap_content"
        android:text="@string/saySomething"
        android:textAllCaps="false"
        android:textSize="40sp"
        android:textStyle="bold"
        android:paddingTop="80dp"
        android:paddingBottom="80dp"
        app:layout_constraintBottom_toTopOf="@id/dynamic_reset_button"
        app:layout_constraintTop_toBottomOf="@id/dynamic_image"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <Button
        android:id="@+id/dynamic_reset_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/reset"
        android:textAllCaps="false"
        android:textSize="30sp"
        android:textStyle="bold"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:layout_margin="10dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

When the image is `GONE`, the `dynamic_say_button` expands to fill the space between the title and the bottom button (same as current fragment_one.xml). When the image is `VISIBLE`, it sits between the title and the say button.

- [ ] **Step 2: Create DynamicScreenFragment.java**

```java
package com.softbankrobotics.pepperapptemplate.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.softbankrobotics.pepperapptemplate.MainActivity;
import com.softbankrobotics.pepperapptemplate.R;
import com.softbankrobotics.pepperapptemplate.TileItem;

import java.io.IOException;
import java.io.InputStream;

public class DynamicScreenFragment extends Fragment {

    private static final String TAG = "MSI_DynamicScreen";
    private static final String ARG_TILE_INDEX = "tile_index";
    private MainActivity ma;

    public static DynamicScreenFragment newInstance(int tileIndex) {
        DynamicScreenFragment fragment = new DynamicScreenFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TILE_INDEX, tileIndex);
        fragment.setArguments(args);
        return fragment;
    }

    public int getTileIndex() {
        return getArguments() != null ? getArguments().getInt(ARG_TILE_INDEX, 0) : 0;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        this.ma = (MainActivity) getActivity();
        if (ma != null) {
            Integer themeId = ma.getThemeId();
            if (themeId != null) {
                final Context contextThemeWrapper = new ContextThemeWrapper(ma, themeId);
                LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
                return localInflater.inflate(R.layout.fragment_dynamic_screen, container, false);
            } else {
                return inflater.inflate(R.layout.fragment_dynamic_screen, container, false);
            }
        } else {
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        int index = getTileIndex();
        if (ma.getTileItems() == null || index < 0 || index >= ma.getTileItems().size()) {
            Log.e(TAG, "Invalid tile index or missing content");
            ma.setFragment(new MainFragment());
            return;
        }
        TileItem tile = ma.getTileItems().get(index);

        // Set title with number prefix
        TextView title = view.findViewById(R.id.dynamic_title);
        title.setText((index + 1) + ". " + tile.getTitle());

        // Set optional image
        ImageView imageView = view.findViewById(R.id.dynamic_image);
        if (tile.getImageName() != null) {
            try {
                InputStream is = ma.getAssets().open("images/" + tile.getImageName());
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                Log.w(TAG, "Image not found: " + tile.getImageName());
                imageView.setVisibility(View.GONE);
            }
        } else {
            imageView.setVisibility(View.GONE);
        }

        // "Opakovat řeč" button
        view.findViewById(R.id.dynamic_say_button).setOnClickListener(v ->
                ma.getCurrentChatBot().goToBookmarkSameTopic("say"));

        // "Úvodní obrazovka" button
        view.findViewById(R.id.dynamic_reset_button).setOnClickListener(v ->
                ma.setFragment(new MainFragment()));
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/layout/fragment_dynamic_screen.xml app/src/main/java/com/softbankrobotics/pepperapptemplate/Fragments/DynamicScreenFragment.java
git commit -m "feat: add DynamicScreenFragment and its layout"
```

---

### Task 9: Create TileAdapter and item_tile layout

**Files:**
- Create: `.../TileAdapter.java`
- Create: `app/src/main/res/layout/item_tile.xml`

- [ ] **Step 1: Create item_tile.xml**

A single tile button for the RecyclerView grid:

```xml
<?xml version="1.0" encoding="utf-8"?>
<Button
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/tile_button"
    android:layout_width="match_parent"
    android:layout_height="60dp"
    android:layout_margin="2dp"
    android:textAppearance="@style/TextAppearance.AppCompat.Large"
    android:textSize="24sp" />
```

- [ ] **Step 2: Create TileAdapter.java**

```java
package com.softbankrobotics.pepperapptemplate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TileAdapter extends RecyclerView.Adapter<TileAdapter.TileViewHolder> {

    private final List<TileItem> items;
    private final OnTileClickListener listener;

    public interface OnTileClickListener {
        void onTileClick(int index);
    }

    public TileAdapter(List<TileItem> items, OnTileClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tile, parent, false);
        return new TileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TileViewHolder holder, int position) {
        TileItem item = items.get(position);
        holder.button.setText((position + 1) + ". " + item.getTitle());
        holder.button.setOnClickListener(v -> listener.onTileClick(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TileViewHolder extends RecyclerView.ViewHolder {
        Button button;

        TileViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.tile_button);
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/layout/item_tile.xml app/src/main/java/com/softbankrobotics/pepperapptemplate/TileAdapter.java
git commit -m "feat: add TileAdapter and item_tile layout for RecyclerView grid"
```

---

### Task 10: Update fragment_main.xml with RecyclerView

**Files:**
- Modify: `app/src/main/res/layout/fragment_main.xml`

- [ ] **Step 1: Replace fragment_main.xml**

Replace the entire file. Keep the header text, replace 8 hardcoded buttons with a RecyclerView, and keep the 3 logos at the bottom:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:id="@+id/main_text"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:text="@string/isMainFragment"
        android:textColor="@color/black"
        android:textSize="40sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/tile_recycler_view"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginTop="16dp"
        android:layout_marginBottom="8dp"
        app:layout_constraintTop_toBottomOf="@id/main_text"
        app:layout_constraintBottom_toTopOf="@id/imageView13"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <ImageView
        android:id="@+id/imageView13"
        android:layout_width="375dp"
        android:layout_height="144dp"
        android:layout_marginBottom="42dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/tile_recycler_view"
        app:layout_constraintVertical_bias="0.663"
        app:srcCompat="@drawable/logo_kraj_nove" />

    <ImageView
        android:id="@+id/imageView2"
        android:layout_width="409dp"
        android:layout_height="109dp"
        android:layout_marginStart="8dp"
        android:layout_marginBottom="8dp"
        android:contentDescription="@string/app_name"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toStartOf="@+id/imageView4"
        app:layout_constraintStart_toStartOf="parent"
        app:srcCompat="@drawable/logo_ujep" />

    <ImageView
        android:id="@+id/imageView4"
        android:layout_width="442dp"
        android:layout_height="88dp"
        android:layout_marginEnd="8dp"
        android:layout_marginBottom="8dp"
        android:cropToPadding="false"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@+id/imageView2"
        app:layout_constraintTop_toBottomOf="@+id/imageView13"
        app:layout_constraintVertical_bias="0.529"
        app:srcCompat="@drawable/icuk" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/res/layout/fragment_main.xml
git commit -m "feat: replace hardcoded buttons with RecyclerView in fragment_main"
```

---

### Task 11: Update MainFragment.java to use RecyclerView

**Files:**
- Modify: `.../Fragments/MainFragment.java`

- [ ] **Step 1: Rewrite MainFragment.java**

Replace the entire file:

```java
package com.softbankrobotics.pepperapptemplate.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.softbankrobotics.pepperapptemplate.MainActivity;
import com.softbankrobotics.pepperapptemplate.R;
import com.softbankrobotics.pepperapptemplate.TileAdapter;
import com.softbankrobotics.pepperapptemplate.TileItem;

import java.util.List;

public class MainFragment extends Fragment {

    private static final String TAG = "MSI_MainFragment";
    private MainActivity ma;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        int fragmentId = R.layout.fragment_main;
        this.ma = (MainActivity) getActivity();
        if (ma != null) {
            Integer themeId = ma.getThemeId();
            if (themeId != null) {
                final Context contextThemeWrapper = new ContextThemeWrapper(ma, themeId);
                LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
                return localInflater.inflate(fragmentId, container, false);
            } else {
                return inflater.inflate(fragmentId, container, false);
            }
        } else {
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        List<TileItem> tileItems = ma.getTileItems();

        RecyclerView recyclerView = view.findViewById(R.id.tile_recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        if (tileItems != null && !tileItems.isEmpty()) {
            TileAdapter adapter = new TileAdapter(tileItems, index -> {
                ma.setFragment(DynamicScreenFragment.newInstance(index));
            });
            recyclerView.setAdapter(adapter);
        } else {
            TextView mainText = view.findViewById(R.id.main_text);
            mainText.setText("Chybí soubor content.md nebo je prázdný.");
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/softbankrobotics/pepperapptemplate/Fragments/MainFragment.java
git commit -m "feat: MainFragment now uses RecyclerView with dynamic tiles"
```

---

### Task 12: Update FragmentExecutor for dynamic tiles

**Files:**
- Modify: `.../Executors/FragmentExecutor.java`

- [ ] **Step 1: Rewrite FragmentExecutor.java**

Replace the entire file. Remove all ScreenXFragment imports and switch cases, add `frag_dynamic` handler:

```java
package com.softbankrobotics.pepperapptemplate.Executors;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.softbankrobotics.pepperapptemplate.Fragments.DynamicScreenFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.MainFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.SplashFragment;
import com.softbankrobotics.pepperapptemplate.MainActivity;

import java.util.List;

public class FragmentExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private final String TAG = "MSI_FragmentExecutor";

    public FragmentExecutor(QiContext qiContext, MainActivity mainActivity) {
        super(qiContext);
        this.ma = mainActivity;
    }

    @Override
    public void runWith(List<String> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        String fragmentName = params.get(0).trim();
        Fragment fragment;
        Log.d(TAG, "fragmentName: " + fragmentName);

        switch (fragmentName) {
            case "frag_main":
                fragment = new MainFragment();
                break;
            case "frag_splash_screen":
                fragment = new SplashFragment();
                break;
            case "frag_dynamic":
                if (params.size() < 2) {
                    Log.e(TAG, "frag_dynamic requires tile index parameter");
                    return;
                }
                try {
                    int index = Integer.parseInt(params.get(1).trim());
                    if (index >= 0 && index < ma.getTileItems().size()) {
                        fragment = DynamicScreenFragment.newInstance(index);
                    } else {
                        Log.w(TAG, "Tile index out of range: " + index);
                        return;
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid tile index: " + params.get(1));
                    return;
                }
                break;
            default:
                Log.w(TAG, "Unknown fragment: " + fragmentName);
                fragment = new MainFragment();
        }
        ma.runOnUiThread(() -> ma.setFragment(fragment));
    }

    @Override
    public void stop() {
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/softbankrobotics/pepperapptemplate/Executors/FragmentExecutor.java
git commit -m "feat: FragmentExecutor handles dynamic tile indices"
```

---

### Task 13: Update MainActivity — the core integration

**Files:**
- Modify: `.../MainActivity.java`

This is the most critical task — it connects everything together.

- [ ] **Step 1: Rewrite MainActivity.java**

Key changes:
1. Add `List<TileItem> tileItems` field and `getTileItems()` accessor
2. Parse `content.md` in `onRobotFocusGained` before building ChatData
3. Update topic list to `["main", "dynamic", "concepts"]`
4. Remove VariableExecutor, only register FragmentExecutor
5. Setup `tileSpeech` QiChat variable
6. Overload `setFragment` to handle DynamicScreenFragment (set `$tileSpeech` variable, chain bookmark navigation)

```java
package com.softbankrobotics.pepperapptemplate;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.TopicStatus;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.softbankrobotics.pepperapptemplate.Executors.FragmentExecutor;
import com.softbankrobotics.pepperapptemplate.Fragments.DynamicScreenFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.LoadingFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.MainFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.SplashFragment;
import com.softbankrobotics.pepperapptemplate.Utils.ChatData;
import com.softbankrobotics.pepperapptemplate.Utils.CountDownNoInteraction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "MSI_MainActivity";
    private final List<String> topicNames = Arrays.asList("main", "dynamic", "concepts");
    private FragmentManager fragmentManager;
    private QiContext qiContext;
    private ChatData currentChatBot;
    private String currentFragment;
    private CountDownNoInteraction countDownNoInteraction;
    private HumanAwareness humanAwareness;
    private android.content.res.Configuration config;
    private Resources res;
    private Future<Void> chatFuture;
    private List<TileItem> tileItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getApplicationContext().getResources();
        config = res.getConfiguration();
        this.fragmentManager = getSupportFragmentManager();
        QiSDK.register(this, this);
        countDownNoInteraction = new CountDownNoInteraction(this, new SplashFragment(),
                300000, 100000);
        countDownNoInteraction.start();
        updateLocale("cs");
        setContentView(R.layout.activity_main);

        // Parse content at creation time (before robot focus)
        tileItems = ContentParser.parse(this);
        Log.d(TAG, "Parsed " + tileItems.size() + " tiles from content.md");
    }

    private void updateLocale(String strLocale) {
        Locale locale = new Locale(strLocale);
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.d(TAG, "onRobotFocusGained");
        this.qiContext = qiContext;
        currentChatBot = new ChatData(this, qiContext, new Locale("cs"), topicNames, true);

        Map<String, QiChatExecutor> executors = new HashMap<>();
        executors.put("FragmentExecutor", new FragmentExecutor(qiContext, this));
        currentChatBot.setupExecutors(executors);
        currentChatBot.setupQiVariable("tileSpeech");

        currentChatBot.chat.async().addOnStartedListener(() -> {
            runOnUiThread(() -> {
                setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
                setFragment(new MainFragment());
            });
        });
        currentChatBot.chat.async().addOnNormalReplyFoundForListener(input -> {
            countDownNoInteraction.reset();
        });
        chatFuture = currentChatBot.chat.async().run();
        humanAwareness = qiContext.getHumanAwareness();
        humanAwareness.async().addOnEngagedHumanChangedListener(engagedHuman -> {
            if (getFragment() instanceof SplashFragment) {
                if (engagedHuman != null) {
                    setFragment(new MainFragment());
                }
            } else {
                countDownNoInteraction.reset();
            }
        });
    }

    @Override
    public void onRobotFocusLost() {
        if (humanAwareness != null) {
            humanAwareness.async().removeAllOnEngagedHumanChangedListeners();
        }
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.d(TAG, "onRobotFocusRefused");
    }

    @Override
    protected void onDestroy() {
        countDownNoInteraction.cancel();
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        countDownNoInteraction.cancel();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY);
        this.setFragment(new LoadingFragment());
    }

    @Override
    public void onUserInteraction() {
        if (getFragment() instanceof SplashFragment) {
            setFragment(new MainFragment());
            countDownNoInteraction.start();
        } else {
            countDownNoInteraction.reset();
        }
    }

    public ChatData getCurrentChatBot() {
        return currentChatBot;
    }

    public QiContext getQiContext() {
        return qiContext;
    }

    public List<TileItem> getTileItems() {
        return tileItems;
    }

    public Integer getThemeId() {
        try {
            return getPackageManager().getActivityInfo(getComponentName(), 0).getThemeResource();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Fragment getFragment() {
        return fragmentManager.findFragmentByTag("currentFragment");
    }

    public void setFragment(Fragment fragment) {
        currentFragment = fragment.getClass().getSimpleName();

        if (fragment instanceof DynamicScreenFragment) {
            int index = fragment.getArguments().getInt("tile_index");
            TileItem tile = tileItems.get(index);
            // Chain: set variable THEN navigate to bookmark (avoid race condition)
            currentChatBot.variables.get("tileSpeech").async()
                    .setValue(tile.getSpeechText())
                    .andThenConsume(aVoid -> {
                        currentChatBot.goToBookmarkNewTopic("init", "dynamic");
                    });
        } else if (!(fragment instanceof LoadingFragment) && !(fragment instanceof SplashFragment)) {
            String topicName = currentFragment.toLowerCase().replace("fragment", "");
            currentChatBot.goToBookmarkNewTopic("init", topicName);
        }

        Log.d(TAG, "Transaction for fragment: " + currentFragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_fade_in_right, R.anim.exit_fade_out_left,
                R.anim.enter_fade_in_left, R.anim.exit_fade_out_right);
        transaction.replace(R.id.placeholder, fragment, "currentFragment");
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/softbankrobotics/pepperapptemplate/MainActivity.java
git commit -m "feat: MainActivity integrates dynamic content system"
```

---

### Task 14: Delete old static files

**Files:**
- Delete: All `Screen*Fragment.java` files (10 files)
- Delete: `VariableExecutor.java`
- Delete: All `fragment_{one..ten}.xml` layout files (10 files)
- Delete: All `screen*.top` files and `everthing.top` (11 files)

- [ ] **Step 1: Delete old Screen fragments and VariableExecutor**

```bash
git rm app/src/main/java/com/softbankrobotics/pepperapptemplate/Fragments/ScreenOneFragment.java app/src/main/java/com/softbankrobotics/pepperapptemplate/Fragments/ScreenTwoFragment.java app/src/main/java/com/softbankrobotics/pepperapptemplate/Fragments/ScreenThreeFragment.java app/src/main/java/com/softbankrobotics/pepperapptemplate/Fragments/ScreenFourFragment.java app/src/main/java/com/softbankrobotics/pepperapptemplate/Fragments/ScreenFiveFragment.java app/src/main/java/com/softbankrobotics/pepperapptemplate/Fragments/ScreenSixFragment.java app/src/main/java/com/softbankrobotics/pepperapptemplate/Fragments/ScreenSevenFragment.java app/src/main/java/com/softbankrobotics/pepperapptemplate/Fragments/ScreenEightFragment.java app/src/main/java/com/softbankrobotics/pepperapptemplate/Fragments/ScreenNineFragment.java app/src/main/java/com/softbankrobotics/pepperapptemplate/Fragments/ScreenTenFragment.java app/src/main/java/com/softbankrobotics/pepperapptemplate/Executors/VariableExecutor.java
```

- [ ] **Step 2: Delete old layouts**

```bash
git rm app/src/main/res/layout/fragment_one.xml app/src/main/res/layout/fragment_two.xml app/src/main/res/layout/fragment_three.xml app/src/main/res/layout/fragment_four.xml app/src/main/res/layout/fragment_five.xml app/src/main/res/layout/fragment_six.xml app/src/main/res/layout/fragment_seven.xml app/src/main/res/layout/fragment_eight.xml app/src/main/res/layout/fragment_nine.xml app/src/main/res/layout/fragment_ten.xml
```

- [ ] **Step 3: Delete old .top files**

Note: `screennine.top` and `screenten.top` exist on disk but were never loaded at runtime (missing from `topicNames` list). They are still deleted for cleanup.

```bash
git rm app/src/main/res/raw/screenone.top app/src/main/res/raw/screentwo.top app/src/main/res/raw/screenthree.top app/src/main/res/raw/screenfour.top app/src/main/res/raw/screenfive.top app/src/main/res/raw/screensix.top app/src/main/res/raw/screenseven.top app/src/main/res/raw/screeneight.top app/src/main/res/raw/screennine.top app/src/main/res/raw/screenten.top app/src/main/res/raw/everthing.top
```

- [ ] **Step 4: Commit**

```bash
git commit -m "refactor: remove old static fragments, layouts, topics, and VariableExecutor"
```

---

### Task 15: Clean up string resources

**Files:**
- Modify: `app/src/main/res/values-cs/strings.xml`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Clean values-cs/strings.xml**

Remove all the hardcoded fragment name strings (fragmentThree-fragmentTen) and fragment title strings (isFragmentThree-isFragmentTen). Keep only:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Rubby_DenKariery_2025</string>
    <string name="loading">Načítání</string>
    <string name="isMainFragment">Klikni na tlačítko nebo řekni číslo:</string>
    <string name="reset">Úvodní obrazovka</string>
    <string name="saySomething">Opakovat řeč</string>
</resources>
```

- [ ] **Step 2: Clean values/strings.xml**

Keep only the necessary defaults:

```xml
<resources>
    <string name="app_name">Rubby_DOD_DenKariery_0425</string>
    <string name="loading">Loading</string>
    <string name="isMainFragment">This is the main fragment</string>
    <string name="reset">Reset</string>
    <string name="saySomething">Say something</string>
</resources>
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/values-cs/strings.xml app/src/main/res/values/strings.xml
git commit -m "refactor: clean up unused string resources"
```

---

### Task 16: Build verification

- [ ] **Step 1: Run Gradle build**

Run: `./gradlew assembleDebug` from the project root.

Expected: BUILD SUCCESSFUL. If there are compile errors, fix them before proceeding.

Common issues to watch for:
- Missing imports (if any old fragment is still referenced somewhere)
- R.id references to deleted layout elements
- String resource references that were removed

- [ ] **Step 2: Verify APK is generated**

Check that `app/build/outputs/apk/debug/` contains the generated APK files.

- [ ] **Step 3: Commit if any fixes were needed**

```bash
git add -A
git commit -m "fix: resolve build issues from dynamic content migration"
```

---

### Task 17: Final push and verification

- [ ] **Step 1: Push to remote**

```bash
git push origin dynamic
```

- [ ] **Step 2: Verify branch on GitHub**

Confirm the `dynamic` branch has all commits.

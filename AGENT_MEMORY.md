# AGENT_MEMORY.md — Kontext pro AI asistenty

> Tento soubor slouží jako sdílená paměť mezi různými AI asistenty (Gemini, Claude, Hermes, atd.)
> pracujícími na tomto projektu. Přečti si ho **celý** než začneš cokoliv upravovat.

---

## O projektu

- **Název**: ICUK Rubby App
- **Typ**: Android aplikace pro humanoidního robota **Pepper** (SoftBank Robotics)
- **Robot se jmenuje**: **Rubby** — robotka (ženský rod, mluví česky)
- **Účel**: Interaktivní průvodce na akcích Inovačního centra Ústeckého kraje (ICUK)
- **Jazyk kódu**: Java
- **SDK**: QiSDK 1.7.5
- **Build**: Gradle 7.1.2, Android API 23–30
- **IDE**: Android Studio Bumblebee 2021.1.1 Patch 3 (novější verze nekompatibilní s Pepper SDK!)
- **JDK**: 8 (vyžadováno)

---

## Architektura — DŮLEŽITÉ MAPOVÁNÍ

### Hlavní menu → Fragment třídy → Layout → String → QiChat topic

Toto je **kritické** pochopit. Čísla se NESHODUJÍ — je tam posun o 2:

| Tlačítko v menu | String name | Fragment třída | Layout XML | Nadpis string | QiChat topic |
|---|---|---|---|---|---|
| 1. RUBBY | `fragmentThree` | `ScreenOneFragment` | `fragment_one.xml` | `isFragmentOne` | `screenone.top` |
| 2. ICUK | `fragmentFour` | `ScreenTwoFragment` | `fragment_two.xml` | `isFragmentTwo` | `screentwo.top` |
| 3. ÚSTECKÝ KRAJ | `fragmentFive` | `ScreenThreeFragment` | `fragment_three.xml` | `isFragmentThree` | `screenthree.top` |
| 4. UJEP | `fragmentSix` | `ScreenFourFragment` | `fragment_four.xml` | `isFragmentFour` | `screenfour.top` |
| 5. PRO ZŠ | `fragmentSeven` | `ScreenFiveFragment` | `fragment_five.xml` | `isFragmentFive` | `screenfive.top` |
| 6. PRO SŠ | `fragmentEight` | `ScreenSixFragment` | `fragment_six.xml` | `isFragmentSix` | `screensix.top` |
| 7. PRO VŠ | `fragmentNine` | `ScreenSevenFragment` | `fragment_seven.xml` | `isFragmentSeven` | `screenseven.top` |
| 8. STARTUPY | `fragmentTen` | `ScreenEightFragment` | `fragment_eight.xml` | `isFragmentEight` | `screeneight.top` |

> **Proč posun?** `Fragment 1` a `Fragment 2` byly původní demo fragmenty z Pepper SDK šablony.
> Tlačítka v hlavním menu jsou pojmenována `frag_three` až `frag_ten` (historicky),
> ale mapují na `ScreenOneFragment` až `ScreenEightFragment`.

### Nepoužívané fragmenty (záloha)
- `ScreenNineFragment` / `fragment_nine.xml` / `screennine.top` — existují, ale nejsou v menu
- `ScreenTenFragment` / `fragment_ten.xml` / `screenten.top` — existují, ale nejsou v menu

---

## QiChat systém

- Soubory `.top` jsou v `app/src/main/res/raw/`
- `main.top` — **vždy aktivní**, obsahuje navigaci, vtipy, počasí
- `concepts.top` — definice konceptů/synonym pro QiChat
- `screen*.top` — jeden soubor na každou obrazovku, obsahuje `proposal: %init` s textem co Rubby řekne
- Přepínání topicu je automatické: `MainActivity.setFragment()` odvodí název topicu z názvu třídy fragmentu

### Jak funguje `proposal: %init`
- Když se fragment zobrazí, automaticky se aktivuje odpovídající topic
- `proposal: %init` znamená, že Rubby tento text řekne hned po otevření obrazovky
- Bookmark `init` se používá pro tlačítko "Opakovat řeč" (`goToBookmarkSameTopic("init")`)

### Hlasové příkazy (main.top)
- Čísla 1–8 a jejich synonyma přepínají obrazovky
- Názvy témat (icuk, ujep, startupy, pro zš, pro sš, pro vš, ústecký kraj) také fungují
- `~restartik` / `~rozloucit` → návrat na hlavní menu
- Vtipy, počasí — viz `main.top`

---

## Co bylo naposledy změněno (září 2026)

### 1. Aktualizace textů podle dokumentu `RUBBY_texty 9_2026.txt`
- Všech 8 `.top` souborů přepsáno novými texty
- Nové dlaždice: RUBBY, ICUK, ÚSTECKÝ KRAJ, UJEP, PRO ZŠ, PRO SŠ, PRO VŠ, STARTUPY
- Staré dlaždice byly: Já jsem Rubby, ICUK, Ústecký kraj, UJEP, ICUK pro vysokoškoláky, ICUK BOOTCAMP, Univerzitní inkubátor, Marketing prakticky

### 2. Oprava kritického bugu: špatné mapování nadpisů na obrazovkách
- Stringy `isFragmentOne`–`isFragmentEight` opraveny, aby odpovídaly layoutu kde se skutečně zobrazují
- Dříve: `isFragmentThree`–`isFragmentTen` byly nastaveny pro Screen 1–8, ale layouty používají `isFragmentOne`–`isFragmentEight`

### 3. Oprava layoutů
- `fragment_one.xml` — odstraněno zbytečné tlačítko "Fragment 2"
- `fragment_two.xml` — nahrazen QiVariable demo layout standardním layoutem (title + say + icuk.cz + reset)
- `fragment_three.xml` — přidáno logo Ústeckého kraje (`logo_kraj_nove`)
- `fragment_four.xml` — opraveno "icuk.cz" → "ujep.cz", přidáno `logo_ujep`
- `fragment_five.xml` — odstraněno špatně umístěné `logo_kraj_nove` (PRO ZŠ)
- `fragment_six.xml` — odstraněno špatně umístěné `logo_ujep` (PRO SŠ)
- `fragment_eight.xml` — sjednoceny rozměry tlačítek (450×150 → 500×200), opraveno hardcoded "Opakovat řeč" → `@string/saySomething`

### 4. Oprava Java kódu
- `ScreenOneFragment.java` — odstraněn click listener na neexistující tlačítko `one_button_frag_two`
- `ScreenTwoFragment.java` — zjednodušen, odstraněn QiVariable demo kód
- `VariableExecutor.java` — odstraněn odkaz na `ScreenTwoFragment.setTextQiVariableValue()` (metoda už neexistuje)

### 5. Hlasové příkazy rozšířeny
- Do `main.top` přidána synonyma pro nové názvy dlaždic (icuk, ústecký kraj, ujep, pro zš/sš/vš, startupy)

---

## Struktura klíčových souborů

```
app/src/main/
├── java/com/.../pepperapptemplate/
│   ├── MainActivity.java              # Hlavní aktivita, QiSDK lifecycle, chat
│   ├── Utils/
│   │   ├── ChatData.java              # Správa QiChat témat, proměnných, bookmarků
│   │   └── CountDownNoInteraction.java # Časovač nečinnosti (5 min → SplashFragment)
│   ├── Executors/
│   │   ├── FragmentExecutor.java       # Přepínání obrazovek přes QiChat příkazy
│   │   └── VariableExecutor.java       # Aktualizace QiChat proměnných z dialogu
│   └── Fragments/
│       ├── SplashFragment.java         # Klidová obrazovka
│       ├── LoadingFragment.java        # Načítací obrazovka
│       ├── MainFragment.java           # Hlavní menu s 8 tlačítky
│       └── Screen[One-Eight]Fragment.java
│
├── res/
│   ├── layout/
│   │   ├── fragment_main.xml           # Layout hlavního menu
│   │   └── fragment_[one-eight].xml    # Layouty jednotlivých obrazovek
│   ├── raw/                            # QiChat .top soubory
│   ├── values/strings.xml              # Anglické fallback stringy
│   └── values-cs/strings.xml           # České stringy (primární)
```

---

## Jak přidat novou obrazovku

1. Vytvoř `ScreenNineFragment.java` (zkopíruj existující, uprav layout ID)
2. Vytvoř `fragment_nine.xml` s `isFragmentNine` stringem
3. Přidej `screennine.top` do `res/raw/` s textem pro Rubby
4. Přidej `"screennine"` do `topicNames` v `MainActivity.java`
5. Přidej tlačítko do `fragment_main.xml`
6. Přidej click listener v `MainFragment.java`
7. Přidej hlasový příkaz do `main.top`
8. Přidej case do `FragmentExecutor.java`
9. Přidej string pro tlačítko a nadpis do obou `strings.xml`

---

## Plánované úpravy (TODO)

- [ ] Kompletní redesign UI — moderní vzhled ve stylu ICUK brandingu
- [ ] Aktuální aplikace vypadá jako stará Android aplikace — potřebuje nový design

---

## Poznámky pro AI asistenty

- **NIKDY neměň mapování MainFragment.java** pokud přesně nevíš co děláš — posun čísel je záměrný
- **Čeština v .top souborech** — QiChat engine čte texty nahlas, proto se píše foneticky (např. "tečka cézet" místo ".cz")
- **Stringy `isFragment*`** se zobrazují jako nadpis na tabletu po rozkliknutí dlaždice
- **Stringy `fragment*`** se zobrazují jako text na tlačítkách v hlavním menu
- **Build vyžaduje JDK 8** — novější JDK mohou způsobit problémy s Kotlin daemon
- Soubor `RUBBY_texty 9_2026.txt` obsahuje zdrojové texty od zadavatele (encoding: windows-1250)

# ICUK Rubby App

Android aplikace pro humanoidního robota **Pepper (SoftBank Robotics)** vyvinutá pro [Inovační centrum Ústeckého kraje (ICUK)](https://icuk.cz). Robot pojmenovaný **Rubby** slouží jako interaktivní průvodce na akcích a prezentacích — mluví česky, reaguje na hlasové příkazy a zobrazuje informace na svém tabletu.

---

## Pozadavky — povinne

### Android Studio

**Povinne: Android Studio Bumblebee | 2021.1.1 Patch 3**

Novejsi verze Android Studia nemusi byt kompatibilni s Pepper SDK pluginem. Pouzivej vyhradne tuto verzi.

### Pepper SDK Plugin

Plugin pro Android Studio je nutny pro praci s QiSDK a pro spousteni aplikace na robotovi.

- Stahnout: https://plugins.jetbrains.com/plugin/8354-pepper-sdk
- Instalace: `File > Settings > Plugins > Install Plugin from Disk` nebo primo pres JetBrains Marketplace v Android Studiu

### Dalsi pozadavky

- JDK 8
- Pepper robot s nainstalovanym QiSDK (verze 1.x)
- Pripojeni robota a vyvojarskeho pocitace na stejnou Wi-Fi sit (nebo USB pres ADB)

---

## Technologie

| Technologie | Verze / Detail |
|-------------|----------------|
| Jazyk | Java |
| Platforma | Android (API 23–30) |
| Robot SDK | QiSDK 1.7.5 (Pepper / SoftBank Robotics) |
| Build system | Gradle 7.1.2 |
| AndroidX | AppCompat, CardView, ConstraintLayout |

---

## Struktura projektu

```
app/src/main/java/com/.../pepperapptemplate/
├── MainActivity.java                    # Hlavni aktivita, inicializace QiSDK, chat, lifecycle
├── Utils/
│   ├── ChatData.java                    # Sprava QiChat temat, promennych, bookmarku
│   └── CountDownNoInteraction.java      # Casovac necinnosti (5 minut -> SplashFragment)
├── Executors/
│   ├── FragmentExecutor.java            # Prepinani obrazovek pres QiChat prikazy
│   └── VariableExecutor.java            # Aktualizace QiChat promennych z dialogu
└── Fragments/
    ├── SplashFragment.java              # Klidova obrazovka (robot ceka na cloveka)
    ├── LoadingFragment.java             # Nacitaci obrazovka (pri resumeu aktivity)
    ├── MainFragment.java                # Hlavni menu s tlacitky
    └── Screen[One-Eight]Fragment.java   # Tematicke obrazovky 1-8

app/src/main/res/raw/                    # QiChat topic soubory (.top)
```

---

## Obrazovky a jejich obsah

Cislovani odpovida tlacitku v hlavnim menu i hlasovym prikazum.

| Cislo | Fragment trida | Topic soubor | Obsah |
|-------|----------------|--------------|-------|
| 1 | `ScreenOneFragment` | `screenone.top` | Uvodni rec Rubby (kdo jsem) |
| 2 | `ScreenTwoFragment` | `screentwo.top` | ICUK — Inovacni centrum Usteckeho kraje |
| 3 | `ScreenThreeFragment` | `screenthree.top` | Ustecky kraj |
| 4 | `ScreenFourFragment` | `screenfour.top` | UJEP |
| 5 | `ScreenFiveFragment` | `screenfive.top` | ICUK pro vysokoskolaky |
| 6 | `ScreenSixFragment` | `screensix.top` | ICUK butkemp |
| 7 | `ScreenSevenFragment` | `screenseven.top` | Univerzitni inkubator |
| 8 | `ScreenEightFragment` | `screeneight.top` | Marketing Prakticky |

> `ScreenNineFragment` a `ScreenTenFragment` existuji jako Java tridy, ale nejsou pouzivany.
> `screennine.top` a `screenten.top` jsou zaloha — nejsou nacitany (nejsou v `topicNames`).

### Jak pridat novou obrazovku

1. Vytvor novy Fragment (napr. `ScreenNineFragment.java`) — muzes zkopirovat libovolny existujici
2. Pridej topic soubor `screennine.top` do `res/raw/`
3. Pridej `"screennine"` do `topicNames` v `MainActivity.java`
4. Pridej tlacitko do `fragment_main.xml` a namapuj ho v `MainFragment.java`
5. Pridej hlasovy prikaz do `main.top`
6. Pridej case do switche v `FragmentExecutor.java`

---

## QiChat dialogy (`res/raw/`)

| Soubor | Popis |
|--------|-------|
| `main.top` | Hlavni topic — navigace, vtipy, pocasi — **vzdy aktivni** |
| `concepts.top` | Definice konceptu (synonyma, fraze pouzivane ve vsech topicich) |
| `screen[one-eight].top` | Dialogy pro jednotlive obrazovky |
| `screennine.top` | Zaloha — neni nacitano |
| `screenten.top` | Zaloha s ukazkou VariableExecutoru — neni nacitano |
| `everthing.top` | Legacni soubor — neni nacitano (ponechano pro pripad potreby) |

### Hlasove prikazy (main.top)

Vsechny nasledujici prikazy fungujici z libovolne obrazovky:

| Co rict | Akce |
|---------|------|
| `ahoj`, `cau`, `dobry den` | Pozdrav |
| `1` az `8`, `jedna` az `osm`, `cislo 1` atd. | Prepnuti na danou obrazovku |
| `zpet`, `reset`, `dekuji`, `nashledanou` | Navrat na hlavni menu |
| `rekni vtip`, `vtip`, `pobav me` | Robot rekne nahodny IT/roboticky vtip |
| `jake je pocasi`, `prsi venku` | Robot odpovi ze nema pristup k internetu |

### Jak funguje prepinani topicu

Kazdy `Fragment` ma odpovidajici `.top` soubor. Kdyz se zmeni Fragment, `MainActivity.setFragment()` automaticky odvodi nazev topicu z nazvu tridy:

```
ScreenOneFragment -> "screenone" -> screenone.top
ScreenTwoFragment -> "screentwo" -> screentwo.top
```

Topic `main` je **vzdy aktivni** — nikdy se nedeaktivuje. Diky tomu hlasova navigace funguje ze vsech obrazovek, i po navratu z fragmentu nebo po probuzeni robota.

---

## Sestaveni a nasazeni

### Build

```bash
git clone https://github.com/KubMakCZ/ICUK_Rubby_app.git
cd ICUK_Rubby_app
./gradlew assembleDebug
```

### Nasazeni na robota

1. Pripoj Pepper robota pres ADB (USB nebo Wi-Fi na stejne siti)
2. V Android Studiu vyber robota jako cilove zarizeni
3. Spust aplikaci (`Run`)

> Pri prvnim spusteni je nutne, aby mel robot focus (nesmi bezet jina aplikace na popredi).

---

## Veteve

| Vetev | Popis | Obdobi |
|-------|-------|--------|
| `main` | Hlavni produkcni vetev | aktualni |
| `DenKariery2025` | Verze pro Den Kariery 2025 | 04–12/2025 |
| `v5_dod_ujep_02_24` | DOD UJEP verze | 02/2024 |
| `v4_jni` | JNI update verze | 01/2022 |
| `V3` | Treti iterace | 11/2021 |
| `V2` | Druha iterace | 2021 |

---

## Licence

Soukromy repozitar vyvinuty pro ICUK.

## Autor

**KubMak** — [GitHub](https://github.com/KubMakCZ)

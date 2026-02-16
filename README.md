# 🤖 ICUK Rubby App

Android aplikace pro humanoidního robota **Pepper (SoftBank Robotics)** vyvinutá pro [Inovační centrum Ústeckého kraje (ICUK)](https://icuk.cz). Robot Rubby slouží jako interaktivní průvodce na akcích a prezentacích — mluví česky, reaguje na hlasové příkazy a zobrazuje informace na svém tabletu.

## 📋 O projektu

Aplikace umožňuje robotu Pepper:

- **Mluvit česky** — využívá QiChat engine s českými dialogy
- **Reagovat na hlasové příkazy** — rozumí klíčovým slovům a frázím
- **Zobrazovat informace** — 10 tematických obrazovek (fragmentů) s informacemi o ICUK, partnerech a službách
- **Přepínat obrazovky hlasem** — uživatel může říct číslo nebo název sekce
- **Automatický návrat** — po 5 minutách nečinnosti se vrátí na úvodní obrazovku
- **Detekce lidí** — reaguje na přítomnost člověka v okolí

## 🏗️ Architektura

```
app/src/main/java/com/.../pepperapptemplate/
├── MainActivity.java          # Hlavní aktivita, inicializace QiSDK a chatu
├── Utils/
│   ├── ChatData.java          # Správa QiChat témat, proměnných a bookmarků
│   └── CountDownNoInteraction.java  # Časovač nečinnosti
├── Executors/
│   ├── FragmentExecutor.java  # Přepínání obrazovek přes QiChat příkazy
│   └── VariableExecutor.java  # Aktualizace UI při změně QiChat proměnných
└── Fragments/
    ├── SplashFragment.java    # Úvodní splash screen
    ├── LoadingFragment.java   # Načítací obrazovka
    ├── MainFragment.java      # Hlavní menu s tlačítky
    └── Screen[One-Ten]Fragment.java  # Tematické obrazovky (1–10)
```

### QiChat dialogy (`res/raw/`)

| Soubor | Popis |
|--------|-------|
| `main.top` | Hlavní dialogové téma — navigace mezi obrazovkami |
| `concepts.top` | Definice konceptů (synonyma, fráze) |
| `everthing.top` | Obecné reakce robota |
| `screen[one-ten].top` | Dialogy pro jednotlivé obrazovky |

## 🛠️ Technologie

| Technologie | Verze / Detail |
|-------------|---------------|
| **Jazyk** | Java + Kotlin |
| **Platforma** | Android (API 23–30) |
| **Robot SDK** | QiSDK 1.7.5 (Pepper) |
| **Build systém** | Gradle 7.1.2 |
| **Kotlin** | 1.4.21 |
| **AndroidX** | AppCompat, CardView, ConstraintLayout |

## 🚀 Sestavení a nasazení

### Požadavky

- Android Studio (doporučeno Arctic Fox nebo novější)
- Pepper robot s nainstalovaným Qi SDK
- JDK 8+

### Build

```bash
# Klonování repozitáře
git clone https://github.com/KubMakCZ/ICUK_Rubby_app.git
cd ICUK_Rubby_app

# Sestavení
./gradlew assembleDebug
```

### Nasazení na robota

1. Připojte Pepper robota přes ADB (USB nebo Wi-Fi)
2. V Android Studiu zvolte robota jako cílové zařízení
3. Spusťte aplikaci (Run)

## 📂 Větve

| Větev | Popis | Období |
|-------|-------|--------|
| `main` | Hlavní produkční větev | aktuální |
| `DenKariery2025` | Verze pro Den Kariéry 2025 | 04–12/2025 |
| `v5_dod_ujep_02_24` | DOD UJEP verze | 02/2024 |
| `v4_jni` | JNI update verze | 01/2022 |
| `V3` | Třetí iterace | 11/2021 |
| `V2` | Druhá iterace | 2021 |

## 📄 Licence

Tento projekt je soukromý repozitář vyvinutý pro ICUK.

## 👤 Autor

**KubMak** — [GitHub](https://github.com/KubMakCZ)
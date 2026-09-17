# AGENT_MEMORY.md - Projekt ICUK_Rubby_app

## Projekt
- Android aplikace pro robota Pepper (SoftBank Robotics)
- Kotlin + Jetpack Compose, MVVM
- compileSdk 30, minSdk 23 (Android 6.0 pro tablet Pepper)
- AGP 7.1.2, Kotlin 1.4.21, JDK 21

## Úpravy Hermesa (inkling:free)
- Přidán text do HomeScreen.kt: "Klikněte na dlaždici nebo řekněte, co chcete otevřít"
- Spuštěn build: .\gradlew assembleDebug --rerun-tasks
- Kontrola stavu APK a procesu java/gradlew
- Použity pouze PowerShell příkazy (žádný bash/ls/cat)
- Model přepnut během relace: ling-3.0-flash-vl:free -> thinkingmachines/inkling:free

## Úpravy Gemini
- Opraveny problémy v commitu eee5823 (logování, mapování stringů, pozůstatky dema)
- Dokumentace architektury a layout mappingu

## Doporučení
- Build funguje s JDK 21, ale Kotlin daemon může spadnout -> použít .\gradlew --stop a opakovat
- Při každé změně souboru udělat git commit

# Architektur

## Ziel
Kernlogik soll gut testbar bleiben; Android- und Modellintegration sollen klar gekapselt werden.

## Schichten

### app
Verantwortlich für:
- UI
- ViewModel
- User-Interaktionen
- Anzeige von Progress, Erfolg, Fehler und Abbruch

### domain
Verantwortlich für:
- Text-Chunking
- Steuerung des Render-Ablaufs
- Charging-Regel
- Statusverwaltung
- Cancel-Logik

### platform
Verantwortlich für:
- Android-spezifische Anbindung
- Stromstatus auslesen
- WAV-Datei speichern
- echte TTS-Modellintegration (später)

## Integrationsregel
Modellintegration bleibt ausschließlich hinter einer kleinen Schnittstelle:

- `TtsEngine`

Damit kann zuerst mit `FakeTtsEngine` entwickelt und getestet werden. Die echte Engine wird später nur in `platform` ersetzt.

## Zentrale Domänenbausteine
- `RenderRequest` (Text + Style)
- `RenderState` (`Idle`, `Running`, `Success`, `Failed`, `Canceled`)
- `RenderAudioUseCase` (zentraler Ablauf)

## Zielablauf
1. Charging prüfen
2. Text in Chunks zerlegen
3. Chunks sequenziell rendern
4. Fortschritt melden
5. Chunk-Audio zu einer WAV-Datei zusammenführen
6. Datei speichern
7. Ergebniszustand an UI zurückgeben

## Leitprinzip
So viel Logik wie möglich in `domain`, so wenig wie nötig in `platform`.

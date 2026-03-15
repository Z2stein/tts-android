# Teststrategie

## Ziel
Mit minimalem Aufwand eine gut testbare, CI-fähige MVP-Basis schaffen.

## Fokus
Priorität auf stabile, wichtige und automatisierbare Kernlogik.

## Automatisierte Tests

### 1) Unit-Tests auf Domain-Logik (höchster Nutzen)
Zu testen:
- Chunking
- Charging Gate
- Render-State-Wechsel
- Cancel
- Fehlerbehandlung
- Fortschritt

### 2) Wenige Integrationstests
Zu testen:
- kompletter Render-Workflow mit `FakeTtsEngine`
- Erzeugung genau einer WAV-Datei
- korrekter Endzustand (`Success`/`Failed`/`Canceled`)

## Manuelle Smoke-Tests
Bewusst manuell:
- Flugmodus aktiv
- Strom angeschlossen
- langer deutscher Text
- Audio klingt brauchbar
- Neutral vs Expressive hörbar unterschiedlich

## Wichtigste Pipeline-Checks (MVP)
1. Chunking funktioniert
2. Ohne Strom startet Rendering nicht
3. Cancel beendet laufenden Job sauber
4. Es entsteht genau eine WAV-Datei
5. Workflow läuft vollständig mit `FakeTtsEngine`

## Regel
Audioqualität/Natürlichkeit sind Produkt-Checks, keine harten CI-Gates.

## Vorgehen
Fake-first entwickeln und testen; echte Modellintegration erst danach anschließen.

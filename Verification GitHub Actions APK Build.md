# GitHub Actions APK Build Verification

## Kontext
- Ziel dieser Prüfung ist die Einführung eines ersten GitHub-Actions-Workflows für Tests und APK-Artefakt.
- Implementierungs-Commit: `9326ced`.
- Verifikations-Commit: `dieser Commit`.

## Workflow-Übersicht
- Neu hinzugefügte Workflow-Datei: `.github/workflows/android-checks-and-apk.yml`.
- Der Workflow läuft bei `workflow_dispatch`, bei `pull_request` auf `main` und bei `push` auf `main`.
- Der Workflow führt die Checks `gradle :domain:check :app:check` aus.
- Danach prüft der Workflow, ob `:app:assembleDebug` vorhanden ist:
  - Wenn ja, wird `gradle :app:assembleDebug` ausgeführt und das Artefakt `tts-android-debug-apk` hochgeladen.
  - Wenn nein, wird die Einschränkung ehrlich im Workflow-Summary dokumentiert und stattdessen werden Check-Reports als Artefakt hochgeladen.

## Final ausgeführte Testkommandos
- Befehl: `JAVA_HOME=/root/.local/share/mise/installs/java/21.0.2 gradle :domain:check :app:check`
  - Ergebnis: PASS
  - Beobachtung: Build war erfolgreich, Domain- und App-Tests liefen grün (`All domain tests passed.`, `All app tests passed.`).
- Befehl: `JAVA_HOME=/root/.local/share/mise/installs/java/21.0.2 gradle :app:tasks --all | rg -n 'assemble|assembleDebug|apk'`
  - Ergebnis: PASS
  - Beobachtung: Es ist nur `assemble` sichtbar; ein Task `assembleDebug` ist im aktuellen Stand nicht vorhanden.

## Geprüfte Punkte
1. Requirement: GitHub Actions ist im Repository eingeführt
   - Evidence in code/tests/workflow: Neue Datei `.github/workflows/android-checks-and-apk.yml`.
   - Assessment: fulfilled

2. Requirement: Der Workflow ist manuell per `workflow_dispatch` startbar
   - Evidence in code/tests/workflow: Trigger `workflow_dispatch` ist im `on`-Block definiert.
   - Assessment: fulfilled

3. Requirement: Die relevanten automatisierten Checks werden ausgeführt
   - Evidence in code/tests/workflow: Schritt `Run automated checks` mit `gradle :domain:check :app:check`.
   - Assessment: fulfilled

4. Requirement: Ein Debug-APK wird gebaut oder die Einschränkung ist ehrlich dokumentiert
   - Evidence in code/tests/workflow: Bedingter Build-Schritt `Build debug APK` nur bei vorhandenem `assembleDebug`; sonst klare Summary-Meldung.
   - Assessment: fulfilled

5. Requirement: Das APK wird als Artifact hochgeladen oder die Einschränkung ist ehrlich dokumentiert
   - Evidence in code/tests/workflow: Upload `tts-android-debug-apk` bei APK-Support; sonst Upload `tts-android-check-reports` plus ehrliche Einschränkungsdokumentation.
   - Assessment: fulfilled

6. Requirement: README erklärt die Nutzung für einen menschlichen Tester knapp und praktisch
   - Evidence in code/tests/workflow: README-Abschnitt „GitHub Actions: Checks + APK-Artefakt“ mit Start, Artifact-Fundort und Install-Hinweisen.
   - Assessment: fulfilled

## Test-Scopes
- Scope der lokalen Prüfungen:
  - GIVEN der finale Repository-Stand,
  - WHEN die Gradle-Checks lokal mit Java 21 ausgeführt werden,
  - THEN sind die automatisierten Checks reproduzierbar und der aktuelle APK-Task-Stand ist nachvollziehbar.
- Scope des GitHub-Workflows:
  - GIVEN ein Run auf GitHub Actions,
  - WHEN der Workflow gestartet wird (manuell oder durch PR/Push),
  - THEN laufen Checks immer, und APK-Build/-Upload erfolgt nur wenn `:app:assembleDebug` real vorhanden ist.

## PR-Bewertung
- Freigabeeinschätzung: eingeschränkt freigabefähig.
- Die CI-Einführung ist klein, klar und praktisch für den gewünschten Phone-Workflow im GitHub-UI.
- Die relevanten automatisierten Checks sind eingebunden und auf lokalem finalem Stand erfolgreich verifiziert.
- Die APK-Erzeugung ist nicht vorgetäuscht: Der Workflow behandelt den Fall ohne `assembleDebug` transparent und liefert dann zumindest verwertbare Reports.
- Für eine echte Milestone-6-Installation ist weiterhin erforderlich, dass das Projekt als Android-Application-Modul einen realen Debug-APK-Task bereitstellt.

## Restrisiken / offene Punkte
- APK-Installierbarkeit auf realem Gerät ist derzeit offen, weil im aktuellen Build-Setup kein `:app:assembleDebug` vorhanden ist.
- Für echte Verteilung fehlt weiterhin eine Signierung/Release-Strecke; das ist bewusst außerhalb des MVP-Scopes.
- Ein technisches GitHub-Artifact ist nicht gleichbedeutend mit abgeschlossener Produktvalidierung auf realer Hardware.
- Unterschiede zwischen CI-Checks und echter Audio-/Gerätevalidierung (Performance, Stabilität, Klang) bleiben als Milestone-6-Manuelltest bestehen.

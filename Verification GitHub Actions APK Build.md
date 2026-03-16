# GitHub Actions APK Build Verification

## Kontext
- Ziel dieser Prüfung ist die Einführung eines ersten GitHub-Actions-Workflows für Tests und APK-Artefakt.
- Implementierungs-Commit: `2fd0d13`
- Verifikations-Commit: dieser Commit (HEAD)

## Workflow-Übersicht
- Verfeinerte Workflow-Datei: `.github/workflows/ci-android-checks-apk.yml`
- Der Workflow läuft bei `workflow_dispatch`, bei `pull_request` auf `main` und bei `push` auf `main`.
- Der Workflow setzt Java 21 und Gradle per GitHub Actions Setup und führt danach `gradle`-Kommandos aus (ohne `./gradlew`).
- Der Workflow führt `:domain:check` und `:app:check` aus.
- Für APK-Erzeugung wird geprüft, ob `:app:assembleDebug` verfügbar ist; bei Verfügbarkeit wird `:app:assembleDebug` ausgeführt und `app/build/outputs/apk/debug/*.apk` als Artifact `tts-android-debug-apk` hochgeladen.
- Wenn `:app:assembleDebug` nicht verfügbar ist, wird ein erklärendes Fallback-Artifact `tts-android-debug-apk-unavailable` (Datei `apk-unavailable.txt`) hochgeladen.
- Zusätzlich schreibt der Workflow eine klare Abschluss-Zusammenfassung in `GITHUB_STEP_SUMMARY` für beide Ergebnispfade.

## Final ausgeführte Testkommandos
- Befehl: `JAVA_HOME=/root/.local/share/mise/installs/java/21.0.2 gradle --no-daemon --console=plain :domain:check :app:check`
  - Ergebnis: PASS
  - Beobachtung: Build war erfolgreich, inklusive `All domain tests passed.` und `All app tests passed.`.
- Befehl: `JAVA_HOME=/root/.local/share/mise/installs/java/21.0.2 gradle --no-daemon --console=plain :app:tasks --all | rg 'assembleDebug|check'`
  - Ergebnis: PASS
  - Beobachtung: `check` ist vorhanden; `assembleDebug` wird nicht gelistet, damit ist im aktuellen Stand kein Debug-APK-Task verfügbar.

## Geprüfte Punkte
1. Requirement: GitHub Actions ist im Repository eingeführt
   - Evidence in code/tests/workflow: Datei `.github/workflows/ci-android-checks-apk.yml` ist vorhanden.
   - Assessment: fulfilled

2. Requirement: Der Workflow ist manuell per `workflow_dispatch` startbar
   - Evidence in code/tests/workflow: `on.workflow_dispatch` ist konfiguriert.
   - Assessment: fulfilled

3. Requirement: Die relevanten automatisierten Checks werden ausgeführt
   - Evidence in code/tests/workflow: Schritt `Run automated checks` mit `gradle --no-daemon --console=plain :domain:check :app:check`.
   - Assessment: fulfilled

4. Requirement: Ein Debug-APK wird gebaut oder die Einschränkung ist ehrlich dokumentiert
   - Evidence in code/tests/workflow: Bedingte Prüfung `Detect APK task support`; bei Verfügbarkeit `Build debug APK`, sonst Fallback-Artifact und Summary.
   - Assessment: fulfilled

5. Requirement: Das APK wird als Artifact hochgeladen oder die Einschränkung ist ehrlich dokumentiert
   - Evidence in code/tests/workflow: Bei APK-Support Upload `tts-android-debug-apk`; ohne APK-Support Upload `tts-android-debug-apk-unavailable` mit erklärender Textdatei.
   - Assessment: fulfilled

6. Requirement: README erklärt die Nutzung für einen menschlichen Tester knapp und praktisch
   - Evidence in code/tests/workflow: Abschnitt `GitHub Actions: Checks + Debug APK Artifact` erklärt manuellen Start, Artifact-Fundstelle und Bedeutung beider Artifact-Namen.
   - Assessment: fulfilled

## Test-Scopes
- Scope der lokalen Prüfungen:
  - GIVEN der aktuelle Repository-Stand mit Java/Gradle-Modulen
  - WHEN lokale Gradle-Checks und Task-Inspektion ausgeführt werden
  - THEN ist verifiziert, welche Befehle stabil laufen und ob `:app:assembleDebug` verfügbar ist.
- Scope des GitHub-Workflows:
  - GIVEN ein Workflow-Run in GitHub Actions
  - WHEN `workflow_dispatch`, `pull_request` oder `push` auf `main` eintritt
  - THEN laufen die automatisierten Checks; zusätzlich entsteht entweder `tts-android-debug-apk` oder `tts-android-debug-apk-unavailable`.

## PR-Bewertung
- Einschätzung: eingeschränkt freigabefähig
- Begründung: Die Lösung ist jetzt text-only und hängt nicht mehr von committed Wrapper-Binaries ab. Der Workflow bleibt mobil nutzbar und transparent, weil jeder Lauf ein klares Artifact-Ergebnis liefert. Die Prüfschritte bleiben ehrlich: Checks laufen immer, APK-Build nur bei real vorhandenem `:app:assembleDebug`. Einschränkung bleibt unverändert, dass der aktuelle Repository-Stand keinen Debug-APK-Task bereitstellt.

## Restrisiken / offene Punkte
- Die reale APK-Installierbarkeit auf einem Gerät ist aktuell nicht belegt, weil im Repository derzeit kein `:app:assembleDebug` verfügbar ist.
- Ohne zusätzliche Android-App-Buildkonfiguration bleibt das echte APK-Artefakt technisch blockiert.
- Eine fehlende Release-Signierung bleibt korrekt außerhalb des Scopes; ein Debug-Artefakt wäre nur für manuelle Tests geeignet.
- Ein technisches CI-Artefakt ersetzt nicht die echte Produktvalidierung (Hörtest, Offline-Verhalten, Charging-Verhalten) auf realem Gerät.

## Wrapper-Binary-Status
- Es werden keine committed Wrapper-Binary-Dateien genutzt.
- `gradle/wrapper/gradle-wrapper.jar` ist nicht Teil der Lösung.
- Die CI-Ausführung wurde auf GitHub-Setup mit Java 21 + Gradle und `gradle`-Kommandos angepasst.

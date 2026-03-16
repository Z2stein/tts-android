# Milestone 6 Verification

## Kontext
- Ziel dieser Prüfung ist ausschließlich Milestone 6.
- Die eigentliche Produktfrage lautet: Kann ein modernes offenes TTS-Modell wie Chatterbox Multilingual Q4 auf einem Pixel-Klasse-Android-Gerät vollständig offline eine ausreichend natürliche deutsche Erzählerstimme für längere Hörfälle erzeugen?
- Klare Abgrenzung:
  - Der Architektur-Spike aus Milestone 5 ist nicht mehr der Fokus.
  - Audioqualität und Style-Unterschiede sind hier Produktchecks und keine CI-Gates.
- Implementierungs-Commit: `599912b`
- Verifikations-Commit: `HEAD` (dieser Commit)

## Validierungs-Setup
- Tatsächlich verwendete Umgebung:
  - Keine reale Android-Laufumgebung aus diesem Ausführungskontext verfügbar (kein echtes Gerät/kein sinnvoller Emulatorlauf mit Audio-Produkturteil möglich).
  - Build- und Testausführung erfolgte lokal per `javac`, `java` und Gradle.
  - Engine-Modus für reale Läufe ist dokumentiert über `-Dtts.engine=real` plus `CHATTERBOX_TTS_CMD`.
  - Konsistenter Validierungstext liegt in `docs/validation-long-text-de.txt`.
- Es gab in dieser Umgebung keine echte Geräteausführung; diese Einschränkung ist explizit.

## Final ausgeführte Testkommandos
- Befehl: `rm -rf /tmp/m6-domain-classes && mkdir -p /tmp/m6-domain-classes && javac -d /tmp/m6-domain-classes $(rg --files domain/src/main/java domain/src/test/java | tr '\n' ' ') && java -cp /tmp/m6-domain-classes com.ttsandroid.domain.DomainTestRunner`
  - Ergebnis: PASS
  - Beobachtung: Domain-Testsuite vollständig erfolgreich (`All domain tests passed.`).
- Befehl: `rm -rf /tmp/m6-app-classes && mkdir -p /tmp/m6-app-classes && javac -d /tmp/m6-app-classes $(rg --files domain/src/main/java platform/src/main/java app/src/main/java app/src/test/java | tr '\n' ' ') && java -cp /tmp/m6-app-classes com.ttsandroid.app.AppTestRunner`
  - Ergebnis: PASS
  - Beobachtung: App-Testsuite vollständig erfolgreich (`All app tests passed.`).
- Befehl: `JAVA_HOME=/root/.local/share/mise/installs/java/21.0.2 gradle --no-daemon --console=plain :domain:check :app:check`
  - Ergebnis: PASS
  - Beobachtung: Finale Gradle-Checks auf dem Endstand grün (`BUILD SUCCESSFUL`).

## Ergebnisübersicht
- Automatisierte Checks sind grün für Domain- und App-Flow sowie Gradle-Checks.
- Manuelle Produktchecks (Flugmodus, Laden am Kabel, Hörvergleich Neutral/Expressive, Brauchbarkeit) wurden in dieser Umgebung nicht real ausgeführt.
- Damit ist die Produktfrage aktuell nur teilweise beantwortet: Die Validierungsreife ist vorhanden, die entscheidende reale Hör-Evidenz fehlt noch.

## Geprüfte Punkte
1. Requirement: App ist für Real-Engine-Validierung auf echtem Gerät vorbereitet  
   Evidence in code/tests/validation: Real/Fake-Auswahl über `tts.engine` im `AppContainer`; sichtbarer Engine-Modus im `RenderUiState`/`RenderScreen`; Real-Engine-Dokumentation in `docs/milestone-6-device-validation.md`.  
   Assessment: fulfilled

2. Requirement: Offline-/Flugmodus-Anforderung ist für die Validierung sauber adressiert  
   Evidence in code/tests/validation: Flugmodus als verpflichtender manueller Schritt in der Milestone-6-Validierungsanleitung dokumentiert.  
   Assessment: partially fulfilled

3. Requirement: Charging-Anforderung ist für die Validierung sauber adressiert  
   Evidence in code/tests/validation: Charging-Gate bleibt im Domain-Flow aktiv; manuelle Prüfschritte verlangen Ladezustand vor Start und während Rendern.  
   Assessment: fulfilled

4. Requirement: Langer deutscher Text kann für die Validierung konsistent verwendet werden  
   Evidence in code/tests/validation: Fester Referenztext unter `docs/validation-long-text-de.txt` für reproduzierbare Läufe.  
   Assessment: fulfilled

5. Requirement: WAV-Datei-Erzeugung ist nachgewiesen oder der fehlende Nachweis ist ehrlich dokumentiert  
   Evidence in code/tests/validation: Automatisierte App/Domain-Tests sichern WAV-Write-Pfad; reale Geräte-WAV für Milestone 6 noch ausstehend und explizit als offen dokumentiert.  
   Assessment: partially fulfilled

6. Requirement: Neutral und Expressive wurden hörbar verglichen oder der fehlende Nachweis ist ehrlich dokumentiert  
   Evidence in code/tests/validation: Hörvergleich ist als manueller Pflichtschritt dokumentiert; keine echte Hör-Evidenz aus dieser Umgebung vorhanden.  
   Assessment: not fulfilled

7. Requirement: Die Aussage „Ausgabe klingt brauchbar“ wurde manuell bewertet oder der fehlende Nachweis ist ehrlich dokumentiert  
   Evidence in code/tests/validation: Manuelle Produktbewertung ist klar als ausstehend markiert; keine vorgetäuschte Audioqualitäts-Aussage.  
   Assessment: not fulfilled

8. Requirement: Relevante automatisierte Checks sind grün oder Grenzen sind transparent dokumentiert  
   Evidence in code/tests/validation: Finale Kommandos sind PASS; Grenzen der subjektiven Audio-Bewertung sind transparent als manuell markiert.  
   Assessment: fulfilled

## Test- und Validierungs-Scopes
- Scope automatisierte Checks:
  - GIVEN deterministische Fake-/Real-Wiring- und UseCase-Logik
  - WHEN Domain/App-Runner sowie Gradle-Checks ausgeführt werden
  - THEN sind Chunking, Charging-Block, Render-Orchestrierung, Cancel, Fehlerpfade und technischer WAV-Write-Pfad abgesichert.
- Scope manuelle Gerätetests:
  - GIVEN reales Android-Gerät in Flugmodus und am Ladegerät
  - WHEN derselbe lange deutsche Text mit Neutral und Expressive gerendert wird
  - THEN werden hörbarer Stilunterschied und subjektive Brauchbarkeit als Produkturteil bewertet.
- Trennung:
  - Automatisierte Evidenz = technische Stabilität und Ablauf.
  - Manuelle Evidenz = Produktqualität/Höreindruck.

## Produkturteil
Die MVP-Produktfrage ist auf Basis der aktuell vorliegenden Evidenz noch offen beziehungsweise nur teilweise beantwortet. Technisch ist die Validierung vorbereitet: Real-Engine-Pfad ist auswählbar, Status/Output sind sichtbar, und ein reproduzierbarer manueller Prüfablauf liegt vor. Es fehlt jedoch die entscheidende reale Hör-Evidenz auf einem Gerät im Flugmodus bei angeschlossenem Strom inklusive direktem Neutral-vs-Expressive-Vergleich. Ohne diese manuelle Produktbewertung kann keine belastbare Ja/Nein-Antwort zur wahrgenommenen Brauchbarkeit gegeben werden. Entsprechend wird hier keine Audioqualitäts-Aussage vorweggenommen.

## PR-Bewertung
Einschätzung für Milestone 6: **eingeschränkt freigabefähig**. Der Stand ist freigabefähig hinsichtlich Validierungsreife, Nachvollziehbarkeit und ehrlicher Abgrenzung zwischen CI und manueller Produktprüfung. Nicht vollständig freigabefähig im Sinne einer abgeschlossenen Produktentscheidung, weil reale Geräte-Hörtests noch fehlen. Die Dokumentation ist klar genug, damit ein Reviewer die offenen manuellen Schritte direkt ausführen und belegen kann. Das verbleibende Risiko liegt primär im noch nicht erhobenen subjektiven Hörurteil unter echten Gerätebedingungen.

## Restrisiken / offene Punkte
- Technische Restarbeit:
  - Reale Geräteausführung mit produktionsnaher Runtime-Konfiguration muss noch durchgeführt und dokumentiert werden.
- Fehlende reale Gerätevalidierung:
  - Keine echte Evidenz aus Flugmodus + Ladezustand + Hörvergleich in diesem Kontext.
- Subjektive Produktbewertung:
  - „Klingt brauchbar“ kann nur durch menschliches Hören beurteilt werden.
- Nächster konkreter Schritt für einen Menschen:
  - Die Checkliste unten auf einem realen Gerät vollständig ausführen und WAV-Evidenz plus Hörurteil festhalten.

## Empfohlene manuelle Smoke-Checks
1. App auf Gerät starten
2. Flugmodus aktivieren
3. Gerät ans Stromnetz anschließen
4. langen deutschen Text einfügen
5. Rendern mit Neutral
6. WAV-Datei prüfen
7. Rendern mit Expressive
8. beide Ergebnisse hörbar vergleichen
9. Brauchbarkeit der Ausgabe kurz bewerten

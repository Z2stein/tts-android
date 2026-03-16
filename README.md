# tts-android MVP

Minimal Android proof of concept for fully offline on-device German TTS using **Chatterbox Multilingual Q4**.

## MVP goal
Build the thinnest possible one-screen app that:
- accepts long German plain text
- renders locally (offline only) into exactly one WAV file
- uses one fixed narrator voice
- exposes one style choice (Neutral / Expressive)
- chunks text internally for long input
- shows progress and supports cancel
- allows rendering only while charging

## Current status
Milestone 6 focus in this repository state:
- real-engine path remains isolated behind `TtsEngine`
- engine mode is visible in UI state (`FAKE` / `REAL`)
- output path and final status are visible for manual verification
- manual real-device product checks are documented (not faked as CI)

## Repository structure
- `docs/spec.md` — MVP requirements and acceptance criteria
- `docs/architecture.md` — layered architecture and core workflow
- `docs/test-strategy.md` — automated/manual test approach
- `docs/milestone-6-device-validation.md` — practical real-device validation procedure
- `docs/validation-long-text-de.txt` — stable long German sample text for milestone-6 checks
- `app/` — Android UI/application layer
- `domain/` — core logic/use cases
- `platform/` — Android/model integration adapters

## Real-engine validation quickstart (Milestone 6)
1. Build and run checks:
   - `JAVA_HOME=/root/.local/share/mise/installs/java/21.0.2 gradle --no-daemon --console=plain :domain:check :app:check`
2. Select real engine path:
   - Set JVM property: `-Dtts.engine=real`
   - Set runtime command: `CHATTERBOX_TTS_CMD="<your chatterbox command>"`
3. Use long German sample text:
   - `docs/validation-long-text-de.txt`
4. Execute manual device checks in `docs/milestone-6-device-validation.md`:
   - airplane mode ON
   - charging connected
   - render Neutral and Expressive
   - verify one WAV output (`render-output.wav`) and listen for style difference/usability

## Scope reminder
This MVP intentionally excludes cloud fallback, account systems, import pipelines, extra output formats, and multi-voice workflows.

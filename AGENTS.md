# AGENTS.md

## Mission
Build the thinnest possible Android MVP for fully offline on-device German TTS using Chatterbox Multilingual Q4.

The app must stay within this strict MVP:
- one screen
- long German text input
- one fixed narrator voice
- one style choice (Neutral / Expressive)
- internal chunking
- progress + cancel
- one WAV output
- render only while charging

## Hard scope rules
- Do **not** add features outside the MVP unless explicitly requested.
- Do **not** implement cloud fallback, account systems, import pipelines, extra output formats, or multi-voice workflows.
- Do **not** overengineer (no plugin systems, no generic abstraction frameworks, no premature optimization).

## Architecture defaults
Use three layers:
- `app`: UI/ViewModel/user interaction
- `domain`: core workflow/state/chunking/cancel/charging logic
- `platform`: Android + storage + real model/runtime adapters

Keep domain Android-independent where practical.

Model/runtime integration must remain behind a small interface (e.g. `TtsEngine`).
Build fake-first with `FakeTtsEngine`; integrate real runtime later in `platform` only.

## Delivery expectations for coding tasks
For substantial tasks:
1. Start with a short plan and list files to touch.
2. Implement in small, reviewable steps.
3. State assumptions briefly when needed.
4. If a major unknown blocks implementation, stop and describe exactly what is missing.

## Testing expectations
Prioritize automated tests for:
- chunking
- charging gate
- render orchestration
- progress updates
- cancel behavior
- error handling

Audio naturalness/style quality are manual validation items, not CI gates.
Run the smallest relevant checks and report results clearly.

## Definition of done for early repository work
- Repository structure is minimal and clear.
- MVP scope is documented.
- Architecture and test strategy are documented.
- Core logic can be developed/tested with fake components before real model integration.

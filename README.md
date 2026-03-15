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
This repository is initialized for MVP planning and implementation.

Included now:
- concise MVP scope/spec documentation
- minimal architecture definition (`app`, `domain`, `platform`)
- fake-first testing strategy
- agent working rules for keeping scope strict

Not included yet:
- real model/runtime integration
- production UI polish
- non-MVP features

## Repository structure
- `docs/spec.md` — MVP requirements and acceptance criteria
- `docs/architecture.md` — layered architecture and core workflow
- `docs/test-strategy.md` — automated/manual test approach
- `app/` — Android UI/application layer (placeholder)
- `domain/` — core logic/use cases (placeholder)
- `platform/` — Android/model integration adapters (placeholder)

## Next steps
1. Add domain interfaces and core workflow skeleton (`RenderAudioUseCase`, state model, chunking API).
2. Implement `FakeTtsEngine` and test domain orchestration first.
3. Add minimal Android app + ViewModel wiring to domain.
4. Integrate real Chatterbox runtime behind `TtsEngine` in `platform` only.
5. Keep CI focused on deterministic core checks before real model validation.

# Chatterbox Multilingual Q4 – Android MVP Spec

## Purpose
Thinnest possible Android MVP to validate fully offline on-device TTS for long-form German text.

## Core product question
Can Chatterbox Multilingual Q4 produce sufficiently natural German narrator audio fully offline on a Pixel-class Android device for long listening use cases?

## MVP goal
The app must:
- accept German text input
- run fully offline on-device
- generate one WAV file
- use one fixed narrator voice
- expose one simple style control (Neutral / Expressive)
- support long text through internal chunking
- show progress and support cancel
- allow rendering only while charging

## In scope
- Android app
- local inference only
- one model: Chatterbox Multilingual Q4
- one language in UI/workflow: German
- fixed voice preset
- single expressiveness control
- internal chunking + sequential generation + WAV stitching
- one local WAV output
- progress + cancel
- charging gate before/during rendering

## Out of scope
No multi-speaker, dialogue logic, cloning, voice library, file importers, streaming playback, MP3/AAC export, cloud fallback, SSML, advanced settings, job queue, resume, editing, chapter tools, bookshelf/library, sync, accounts.

## Functional requirements
- **FR-01 Text input**: one large plain-text field (no file import, no rich text).
- **FR-02 Fixed voice**: exactly one predefined narrator voice/preset.
- **FR-03 Style control**: exactly one user-facing style selector (Neutral/Expressive).
- **FR-04 Offline inference**: works in airplane mode, no server calls.
- **FR-05 Long text processing**: internal chunking, sentence-aware where possible.
- **FR-06 Audio output**: save exactly one WAV file locally.
- **FR-07 Progress feedback**: idle/running/done/failed (+ optional ETA).
- **FR-08 Cancel**: user can cancel active rendering.
- **FR-09 Charging requirement**: block start when unplugged; if unplugged during run, enforce one consistent failure/cancel behavior.

## Non-functional requirements
- Fully local, no internet dependency
- Minimal UX complexity
- Feasibility first over polish
- Reasonable storage footprint
- Robust end-to-end flow prioritized over feature breadth

## Primary UX (single screen)
- multiline text input
- style selector (Neutral/Expressive)
- Generate Audio button
- progress area
- Cancel button
- result message/path

## Suggested user flow
1. Paste long German text
2. Select style
3. Tap Generate
4. Charging check
5. Internal chunking
6. Sequential local generation
7. WAV stitch + save
8. Success/failure shown

## Acceptance criteria
- Offline generation works in airplane mode
- Long input works without manual splitting
- Exactly one WAV file is created on success
- Neutral vs Expressive yields audible style difference
- Charging gate blocks start when not charging
- Cancel cleanly stops running render

## Non-goals
No proof of production mastering, perfect consistency, consumer polish, best speed/efficiency, resumable orchestration, or document-ingestion workflows.

## Definition of done (MVP)
MVP is done when a tester can paste long German text, keep device in airplane mode + charging state, generate fully local WAV output with audible neutral/expressive difference, and cancel a running job, without added scope.

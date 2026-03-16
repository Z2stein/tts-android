# Milestone 6 Real-Device Validation Procedure

This procedure is intentionally manual for product checks that cannot be treated as CI gates.

## Purpose
Answer the MVP product question with honest real-device evidence:
- fully offline run (airplane mode)
- charging-gated rendering
- long German text
- exactly one WAV output
- audible difference between Neutral and Expressive
- short usability judgment of output sound

## Prerequisites
- Pixel-class Android phone (preferred) with enough storage.
- Chatterbox runtime command available on device/test environment.
- Environment variable for real runtime command:
  - `CHATTERBOX_TTS_CMD="<command that returns raw chunk audio bytes>"`
- App configured for real engine:
  - JVM/system property `tts.engine=real`
- Long sample text:
  - `docs/validation-long-text-de.txt`

## Build / install
1. Run repository checks:
   - `JAVA_HOME=/root/.local/share/mise/installs/java/21.0.2 gradle --no-daemon --console=plain :domain:check :app:check`
2. Build and install app with real engine configuration according to your Android run setup.
3. Confirm app status area shows engine mode `REAL` before test runs.

## Manual validation steps
1. Enable airplane mode.
2. Connect charger.
3. Start app, paste content from `docs/validation-long-text-de.txt`.
4. Select **Neutral**, run render.
5. Confirm final status is `Success` and output path is shown.
6. Verify generated `render-output.wav` exists at shown output path.
7. Select **Expressive**, rerun with same text.
8. Compare both WAV outputs by listening.
9. Record short judgment whether output is usable for long-form narrator listening.

## Evidence to capture
- Screenshot/photo showing:
  - airplane mode indicator
  - charging indicator
  - app status with `Engine: REAL`
  - success status and output path
- File evidence:
  - both WAV files (or hashes + paths)
- Manual notes:
  - observed Neutral vs Expressive difference
  - usability judgment and caveats

## Failure diagnostics
- If app fails immediately with engine error:
  - check `CHATTERBOX_TTS_CMD` is set correctly
  - check runtime command returns non-empty audio bytes
- If render fails after start:
  - keep status/error text and command stderr logs for review
- If charging-related failure appears:
  - verify cable/power source and rerun with charging stable

## Reporting rule
Do not claim product question closed unless listening evidence on real device exists.

# Milestone 9: Real-Device Acceptance (MVP Verdict)

This milestone is intentionally manual. It answers the MVP product question on a real Android device without faking human hearing evidence.

## Preconditions (must all be true)
- Real Android phone available (Pixel-class preferred).
- App APK installed from GitHub Actions artifact (`tts-android-debug-apk`).
- Device charger connected before starting render.
- Airplane mode enabled before opening/running the test.
- Real engine mode selected for runtime (`-Dtts.engine=real`) and runtime command configured as required by your environment (`CHATTERBOX_TTS_CMD`).
- Use exactly one fixed reference text file for repeatability:
  - `docs/validation-long-text-de.txt`


## Fixed German reference text (use exactly this)
Copy this text verbatim for both style runs:

```text
Dies ist ein längerer deutscher Beispieltext für die Milestone-6-Validierung. Er soll nicht literarisch perfekt sein, sondern vor allem reproduzierbar und ausreichend lang, damit Chunking, Fortschritt und die Stabilität über mehrere Abschnitte getestet werden können.

Am frühen Morgen lag über der Stadt ein leichter Nebel, und die Geräusche der Straße klangen gedämpft, fast so, als würde jemand eine Decke über den Tag legen. In den Häusern gingen nach und nach die Lichter an, Kaffeemaschinen summten, und irgendwo klapperte eine Tasse auf eine Untertasse. Eine Erzählerstimme für lange Hörstrecken muss in solchen ruhigen Passagen klar bleiben, ohne zu scharf zu wirken, und in dichteren Passagen verständlich bleiben, ohne hastig zu klingen.

Später füllten sich die Plätze. Fahrräder rollten über Pflastersteine, die Türen der Straßenbahn öffneten und schlossen sich in regelmäßigen Abständen, und in den kleinen Läden wurden Schilder nach draußen gestellt. Der Text wechselt bewusst zwischen kurzen und langen Sätzen. Dadurch lässt sich hören, ob die Prosodie bei unterschiedlichen Satzlängen stabil bleibt und ob die Atempausen natürlich gesetzt werden. Genau diese Punkte sind wichtig, wenn ein Ergebnis nicht nur technisch korrekt, sondern auch für längeres Zuhören brauchbar sein soll.

Gegen Mittag wurde es wärmer, und die Stimmen auf den Gehwegen überlagerten sich zu einem gleichmäßigen Hintergrundrauschen. Eine gute TTS-Ausgabe sollte auch dann fokussiert bleiben, wenn die Inhalte dichter werden: Zahlen, Namen, Nebensätze und längere Aufzählungen dürfen nicht verschwimmen. Für die Validierung ist außerdem entscheidend, dass Neutral und Expressive nicht nur auf dem Papier verschieden sind, sondern im direkten Hörvergleich tatsächlich unterscheidbar bleiben.

Am Abend kehrte wieder Ruhe ein. Fenster wurden geschlossen, Schritte hallten in den Innenhöfen, und über den Dächern zog langsam Wind auf. Mit diesem Abschnitt endet der Testtext. Wenn die Wiedergabe bis hierhin ohne abrupte Qualitätswechsel, ohne hörbare Brüche zwischen Chunks und mit konsistenter Verständlichkeit funktioniert, ist die technische Basis für die Produktentscheidung deutlich belastbarer.
```

Canonical source in repository: `docs/validation-long-text-de.txt`.

## Install path from GitHub artifact
1. Open repository **Actions** tab in GitHub.
2. Open latest successful run of **Android Checks + Debug APK**.
3. Download artifact: **`tts-android-debug-apk`**.
4. Extract ZIP and transfer APK to device (USB / direct download / files app).
5. Install APK on device (enable "Install unknown apps" if prompted).
6. Launch app.

## Device acceptance run (single text, two styles)
1. Confirm **airplane mode ON**.
2. Confirm **charger connected**.
3. Start app and verify engine indicator shows `REAL`.
4. Paste full content of `docs/validation-long-text-de.txt` into the text input.
5. Tap **Neutral**.
6. Tap **Generate** and wait for terminal status.
7. Record status and output path shown in app.
8. Save/copy resulting WAV as `neutral.wav` for comparison.
9. Without changing text, tap **Expressive**.
10. Tap **Generate** and wait for terminal status.
11. Record status and output path shown in app.
12. Save/copy resulting WAV as `expressive.wav` for comparison.

## Verify WAV output was really produced
For each run (Neutral and Expressive):
- App status must end in `Success`.
- App must show non-empty `Output: <path>`.
- In Android Files app (or `adb shell`), check that the referenced file exists and has non-zero size.
- Open each file in a local audio player and confirm it is playable end-to-end.

## Compare both outputs (human check)
- Listen to `neutral.wav` and `expressive.wav` back-to-back on the same device/headphones.
- Focus on audible prosody/expressiveness differences (intonation, emphasis, pacing).
- Record whether the difference is clearly audible.
- Record whether overall long-form narration is usable.

## Evidence checklist
Capture and keep with the milestone result:
- Screenshot/photo showing airplane mode + charging indicator during test.
- App screenshots for both successful runs including status and output path.
- File evidence for both outputs (`neutral.wav`, `expressive.wav`) or path + hash + size.
- Completed verdict template: `docs/milestone-9-verdict-template.md`.

## Honesty rule
Do not mark this milestone complete without explicit real-device and human-listening evidence.
If any item cannot be verified, mark it as pending in the verdict template.

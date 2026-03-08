# MediaStore Example (Vanilla TS)

This example shows how to use `tauri-plugin-android-mediastore` with a plain TypeScript frontend.

## Behavior

- Click **List Music Files** to query audio files from Android MediaStore.
- The list shows only file names.
- Click a file name to show:
  - metadata (`title`, `artist`, `album`, `duration`, `size`, `contentUri`, `firstFourBytes`)
  - file reader diagnostics (open, first 4 bytes, chunk reads, total bytes, close)

## Run

```bash
npm install
npm run tauri dev
```

## Notes

- This plugin is Android-focused. Desktop runs may return mock data.
- MIDI suffixes (`.mid`, `.midi`) are excluded in the list request.

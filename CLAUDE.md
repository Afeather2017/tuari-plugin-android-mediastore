# Development Notes for Claude

This file contains important reminders for working on this Tauri plugin.

## ⚠️ CRITICAL: When Modifying the Frontend API

If you modify **any file in `guest-js/`** (especially `guest-js/index.ts`), you **MUST** rebuild the bundle:

```bash
# From the plugin root directory
pnpm build
```

This runs `rollup -c` which bundles `guest-js/index.ts` into `dist-js/` directory. The example app imports from these pre-built files, not from `guest-js/` directly.

**Failure to rebuild will cause "undefined" errors** when using modified functions in the frontend.

## Build Order When Making Changes

1. **Modify Rust code** → `cargo check` to verify
2. **Modify Kotlin code** → Build the full Android app to verify
3. **Modify guest-js code** → `pnpm build` (ROOT directory), then rebuild example app

## Project Structure

```
tauri-plugin-android-mediastore/
├── src/              # Rust code (plugin core)
│   ├── models.rs     # Request/Response types
│   ├── commands.rs   # Tauri command handlers
│   ├── mobile.rs     # Android/iOS mobile API
│   └── desktop.rs    # Desktop stub implementations
├── guest-js/         # TypeScript API (frontend)
│   └── index.ts      # Main API exports
├── dist-js/          # Bundled JS (GENERATED - do not edit)
│   ├── index.js
│   ├── index.cjs
│   └── index.d.ts
└── android/          # Kotlin Android code
    └── src/main/java/
        ├── MediaStorePlugin.kt      # Tauri plugin entry point
        ├── MediaStoreCommands.kt    # Command handlers
        └── MediaStoreHelper.kt      # MediaStore queries
```

## Adding a New Command

When adding a new command, update ALL of these files:

1. **`src/models.rs`** - Add request/response types
2. **`src/commands.rs`** - Add command handler
3. **`src/mobile.rs`** - Add mobile API method
4. **`src/desktop.rs`** - Add desktop stub (if needed)
5. **`android/src/main/java/MediaStorePlugin.kt`** - Add `@InvokeArg` class and `@Command` method
6. **`android/src/main/java/MediaStoreCommands.kt`** - Add command implementation
7. **`android/src/main/java/MediaStoreHelper.kt`** - Add helper methods (if needed)
8. **`guest-js/index.ts`** - Add TypeScript function and types
9. **Run `pnpm build`** - Rebuild the JS bundle
10. **`examples/tauri-app/src-tauri/capabilities/default.json`** - Add ACL permission

## Android-Specific Notes

- MediaStore queries use the `DATA` column (file path) for filtering by file extension
- The `DATA` column is deprecated since Android 10 (API 29) but still works reliably
- All file operations run on Coroutines with `Dispatchers.IO` for non-blocking I/O
- File readers use `ContentResolver.openInputStream()` which requires content URIs

## Testing

For Android testing, use the example app:
```bash
cd examples/tauri-app
./build-android.sh
```

Note: This requires an Android device connected via `adb` for the final install step.

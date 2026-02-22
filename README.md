# Tauri Plugin Android MediaStore

[![Tauri](https://img.shields.io/badge/tauri-2.0+-blue.svg)](https://tauri.app)
[![Android](https://img.shields.io/badge/platform-android-green.svg)](https://www.android.com)

A Tauri plugin for accessing Android's MediaStore to query and read audio files from the device. This plugin provides APIs to enumerate audio files and read their contents using Android content URIs.

> **Note:** This plugin is Android-only. Desktop platforms return mock implementations for development purposes.

## Features

- Query audio files from Android's MediaStore
- Access file metadata (title, artist, album, duration, content URI, magic bytes)
- Read file contents with a streaming file reader API
- Permission handling for Android 12 and below (`READ_EXTERNAL_STORAGE`) and Android 13+ (`READ_MEDIA_AUDIO`)
- Async iterator support for streaming file reads
- Session-based file reading with seek, read chunk, and read-to-end operations

## Installation

### 1. Add the plugin to your `Cargo.toml`

```toml
[dependencies]
tauri-plugin-android-mediastore = "0.1"
```

Or for a local development version:

```toml
[dependencies]
tauri-plugin-android-mediastore = { path = "../path/to/tauri-plugin-android-mediastore" }
```

### 2. Install the JavaScript/TypeScript bindings

```bash
npm install tauri-plugin-android-mediastore-api
# or
yarn add tauri-plugin-android-mediastore-api
# or
pnpm add tauri-plugin-android-mediastore-api
```

### 3. Register the plugin in your Tauri app

```rust
// src-tauri/src/lib.rs or src-tauri/src/main.rs
use tauri_plugin_android_mediastore::AndroidMediastoreExt;

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_android_mediastore::init())
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
```

### 4. Add Android permissions to your manifest

Add the required permissions to your `src-tauri/gen/android/app/src/main/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- For Android 12 and below -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
                     android:maxSdkVersion="32" />

    <!-- For Android 13+ -->
    <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
</manifest>
```

### 5. Configure ACL permissions (Tauri 2.0+)

Add the plugin permissions to your `src-tauri/capabilities/default.json`:

```json
{
  "capabilities": [
    {
      "identifier": "default",
      "description": "Default capabilities",
      "windows": ["main"],
      "permissions": [
        "core:default",
        "android-mediastore:default"
      ]
    }
  ]
}
```

Or for fine-grained control:

```json
{
  "capabilities": [
    {
      "identifier": "audio-access",
      "windows": ["main"],
      "permissions": [
        "android-mediastore:allow-get-audio-files",
        "android-mediastore:allow-file-reader-open",
        "android-mediastore:allow-file-reader-read",
        "android-mediastore:allow-file-reader-close",
        "android-mediastore:allow-file-reader-seek",
        "android-mediastore:allow-file-reader-read-to-end",
        "android-mediastore:allow-file-reader-info",
        "android-mediastore:allow-ping"
      ]
    }
  ]
}
```

## Usage

### Importing the Plugin

```typescript
import {
  ping,
  getAudioFiles,
  fileReaderOpen,
  fileReaderRead,
  fileReaderClose,
  fileReaderSeek,
  fileReaderReadToEnd,
  fileReaderInfo,
  MediaFileReader,
  base64ToUint8Array,
  type AudioFile,
  type FileReaderOpenResponse,
  type FileReaderReadResponse
} from 'tauri-plugin-android-mediastore-api';
```

### Getting Audio Files

Query all audio files on the device:

```typescript
import { getAudioFiles } from 'tauri-plugin-android-mediastore-api';

async function loadAudioFiles() {
  try {
    const response = await getAudioFiles();
    console.log(`Found ${response.files.length} audio files`);

    for (const file of response.files) {
      console.log(`
        Title: ${file.title}
        Artist: ${file.artist}
        Album: ${file.album}
        Duration: ${file.duration}ms
        URI: ${file.contentUri}
        Magic: ${file.firstFourBytes}
      `);
    }
  } catch (error) {
    console.error('Failed to get audio files:', error);
  }
}
```

### Using the MediaFileReader Class

The `MediaFileReader` class provides a convenient API for reading media files:

```typescript
import { MediaFileReader } from 'tauri-plugin-android-mediastore-api';

async function readAudioFile(contentUri: string) {
  const reader = new MediaFileReader(contentUri);

  try {
    // Open the file
    await reader.open();
    console.log('File opened, session ID:', reader.currentSessionId);

    // Read file info
    const info = await reader.getInfo();
    console.log('File size:', info?.fileSize);

    // Read first 4 bytes (magic bytes)
    const firstChunk = await reader.read(4);
    console.log('First 4 bytes:', firstChunk.data);

    // Read all data using async iterator
    for await (const chunk of reader) {
      console.log('Read chunk:', chunk.length, 'bytes');
      // Process chunk (Uint8Array)
    }

  } finally {
    await reader.close();
  }
}
```

### Reading Entire File

```typescript
import { MediaFileReader } from 'tauri-plugin-android-mediastore-api';

async function readCompleteFile(contentUri: string): Promise<Uint8Array> {
  const reader = new MediaFileReader(contentUri);
  return await reader.readAll(); // Automatically handles open/close
}
```

### Low-Level File Reader API

For more control, use the low-level functions:

```typescript
import {
  fileReaderOpen,
  fileReaderRead,
  fileReaderSeek,
  fileReaderClose
} from 'tauri-plugin-android-mediastore-api';

async function readFileWithSeek(contentUri: string) {
  // Open file reader
  const openResult = await fileReaderOpen(contentUri);
  if (!openResult.success) {
    throw new Error(openResult.error || 'Failed to open file');
  }

  const sessionId = openResult.sessionId;

  try {
    // Seek to position
    await fileReaderSeek(sessionId, 1024);

    // Read chunk
    const readResult = await fileReaderRead(sessionId, 8192);
    if (readResult.success && readResult.data) {
      const bytes = base64ToUint8Array(readResult.data);
      console.log('Read', bytes.length, 'bytes');
    }

  } finally {
    await fileReaderClose(sessionId);
  }
}
```

## API Reference

### Functions

#### `ping(value: string): Promise<string | null>`

Test plugin connection.

#### `getAudioFiles(): Promise<AudioFilesResponse>`

Query all audio files from Android's MediaStore.

**Response:**
```typescript
interface AudioFilesResponse {
  files: AudioFile[];
}

interface AudioFile {
  id: number;
  title: string;
  artist: string;
  album: string;
  duration: number;
  contentUri: string;
  firstFourBytes?: string; // Hex string of first 4 bytes
}
```

#### `fileReaderOpen(contentUri: string): Promise<FileReaderOpenResponse>`

Open a file reader session for a content URI.

**Response:**
```typescript
interface FileReaderOpenResponse {
  success: boolean;
  sessionId: number;
  fileSize?: number;
  error?: string;
}
```

#### `fileReaderRead(sessionId: number, size?: number): Promise<FileReaderReadResponse>`

Read a chunk of data from the file. Default size is 8192 bytes.

**Response:**
```typescript
interface FileReaderReadResponse {
  success: boolean;
  data?: string; // Base64 encoded
  bytesRead: number;
  isEof: boolean;
  error?: string;
}
```

#### `fileReaderClose(sessionId: number): Promise<FileReaderCloseResponse>`

Close a file reader session.

#### `fileReaderSeek(sessionId: number, position: number): Promise<FileReaderSeekResponse>`

Seek to a specific position in the file.

#### `fileReaderReadToEnd(sessionId: number): Promise<FileReaderReadToEndResponse>`

Read all remaining data from current position to end of file.

#### `fileReaderInfo(sessionId: number): Promise<FileReaderInfoResponse>`

Get information about a file reader session.

**Response:**
```typescript
interface SessionInfo {
  sessionId: number;
  contentUri: string;
  position: number;
  fileSize?: number;
  isOpen: boolean;
}
```

### Utility Functions

#### `base64ToUint8Array(base64: string): Uint8Array`

Convert a base64 string to Uint8Array.

#### `uint8ArrayToBase64(bytes: Uint8Array): string`

Convert a Uint8Array to base64 string.

## Building the Example

```bash
# Clone the repository
git clone <repository-url>
cd tauri-plugin-android-mediastore

# Install dependencies
npm install
npm run build

# Build and run the example app
cd examples/tauri-app
npm install
npm run tauri android dev
```

## Platform Support

| Platform | Status |
|----------|--------|
| Android | Fully supported |
| iOS | Not supported |
| Linux/macOS/Windows | Mock implementation for development only |

## Permissions

The plugin automatically handles permission requests for:
- `READ_EXTERNAL_STORAGE` (Android 12 and below)
- `READ_MEDIA_AUDIO` (Android 13+)

Permissions are requested when you call functions that require them.

## License

MIT OR Apache-2.0

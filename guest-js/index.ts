import { invoke, PermissionState } from '@tauri-apps/api/core'

export async function ping(value: string): Promise<string | null> {
  return await invoke<{value?: string}>('plugin:android-mediastore|ping', {
    payload: {
      value,
    },
  }).then((r) => (r.value ? r.value : null));
}

export interface AudioFile {
  id: number;
  title: string;
  artist: string;
  album: string;
  duration: number;
  contentUri: string;
  firstFourBytes?: string;
}

export interface AudioFilesResponse {
  files: AudioFile[];
}

export async function getAudioFiles(): Promise<AudioFilesResponse> {
  return await invoke<AudioFilesResponse>('plugin:android-mediastore|get_audio_files');
}

// File reader types and API

export interface FileReaderOpenResponse {
  success: boolean;
  sessionId: number;
  fileSize?: number;
  error?: string;
}

export interface FileReaderReadResponse {
  success: boolean;
  data?: string; // Base64 encoded
  bytesRead: number;
  isEof: boolean;
  error?: string;
}

export interface FileReaderCloseResponse {
  success: boolean;
  error?: string;
}

export interface FileReaderSeekResponse {
  success: boolean;
  newPosition: number;
  error?: string;
}

export interface FileReaderReadToEndResponse {
  success: boolean;
  data?: string; // Base64 encoded
  bytesRead: number;
  isEof: boolean;
  error?: string;
}

export interface SessionInfo {
  sessionId: number;
  contentUri: string;
  position: number;
  fileSize?: number;
  isOpen: boolean;
}

export interface FileReaderInfoResponse {
  info?: SessionInfo;
  error?: string;
}

export async function fileReaderOpen(contentUri: string): Promise<FileReaderOpenResponse> {
  return await invoke<FileReaderOpenResponse>('plugin:android-mediastore|file_reader_open', {
    payload: {
      contentUri,
    },
  });
}

export async function fileReaderRead(sessionId: number, size: number = 8192): Promise<FileReaderReadResponse> {
  return await invoke<FileReaderReadResponse>('plugin:android-mediastore|file_reader_read', {
    payload: {
      sessionId,
      size,
    },
  });
}

export async function fileReaderClose(sessionId: number): Promise<FileReaderCloseResponse> {
  return await invoke<FileReaderCloseResponse>('plugin:android-mediastore|file_reader_close', {
    payload: {
      sessionId,
    },
  });
}

export async function fileReaderSeek(sessionId: number, position: number): Promise<FileReaderSeekResponse> {
  return await invoke<FileReaderSeekResponse>('plugin:android-mediastore|file_reader_seek', {
    payload: {
      sessionId,
      position,
    },
  });
}

export async function fileReaderReadToEnd(sessionId: number): Promise<FileReaderReadToEndResponse> {
  return await invoke<FileReaderReadToEndResponse>('plugin:android-mediastore|file_reader_read_to_end', {
    payload: {
      sessionId,
    },
  });
}

export async function fileReaderInfo(sessionId: number): Promise<FileReaderInfoResponse> {
  return await invoke<FileReaderInfoResponse>('plugin:android-mediastore|file_reader_info', {
    payload: {
      sessionId,
    },
  });
}

/**
 * Convenience class for reading audio files with async iterator support.
 * Automatically manages the file reader session.
 */
export class MediaFileReader {
  private sessionId?: number;
  private _isOpen = false;

  constructor(private contentUri: string) {}

  /**
   * Opens the file reader session.
   */
  async open(): Promise<void> {
    if (this._isOpen) {
      throw new Error('File reader is already open');
    }

    const result = await fileReaderOpen(this.contentUri);

    if (!result.success) {
      throw new Error(result.error || 'Failed to open file reader');
    }

    this.sessionId = result.sessionId;
    this._isOpen = true;
  }

  /**
   * Reads a chunk of data from the file.
   * @param size - Number of bytes to read (default: 8192)
   * @returns The read result containing base64-encoded data
   */
  async read(size: number = 8192): Promise<FileReaderReadResponse> {
    if (!this._isOpen || this.sessionId === undefined) {
      throw new Error('File reader is not open');
    }

    return await fileReaderRead(this.sessionId, size);
  }

  /**
   * Seeks to a specific position in the file.
   * @param position - Byte position to seek to
   */
  async seek(position: number): Promise<void> {
    if (!this._isOpen || this.sessionId === undefined) {
      throw new Error('File reader is not open');
    }

    const result = await fileReaderSeek(this.sessionId, position);

    if (!result.success) {
      throw new Error(result.error || 'Failed to seek');
    }
  }

  /**
   * Reads all remaining data from the current position to the end of the file.
   */
  async readToEnd(): Promise<FileReaderReadToEndResponse> {
    if (!this._isOpen || this.sessionId === undefined) {
      throw new Error('File reader is not open');
    }

    return await fileReaderReadToEnd(this.sessionId);
  }

  /**
   * Gets information about the current file reader session.
   */
  async getInfo(): Promise<SessionInfo | null> {
    if (!this._isOpen || this.sessionId === undefined) {
      throw new Error('File reader is not open');
    }

    const result = await fileReaderInfo(this.sessionId);
    return result.info || null;
  }

  /**
   * Closes the file reader session.
   */
  async close(): Promise<void> {
    if (!this._isOpen || this.sessionId === undefined) {
      return;
    }

    await fileReaderClose(this.sessionId);
    this._isOpen = false;
    this.sessionId = undefined;
  }

  /**
   * Returns whether the file reader is currently open.
   */
  get isOpen(): boolean {
    return this._isOpen;
  }

  /**
   * Returns the current session ID (if open).
   */
  get currentSessionId(): number | undefined {
    return this.sessionId;
  }

  /**
   * Async iterator for reading chunks of data from the file.
   * Automatically manages reading until EOF.
   *
   * @example
   * ```typescript
   * const reader = new MediaFileReader(contentUri);
   * await reader.open();
   *
   * for await (const chunk of reader) {
   *   console.log('Read chunk:', chunk.length, 'bytes');
   * }
   *
   * await reader.close();
   * ```
   */
  async *[Symbol.asyncIterator](): AsyncIterator<Uint8Array> {
    if (!this._isOpen) {
      throw new Error('File reader is not open. Call open() first.');
    }

    try {
      while (true) {
        const result = await this.read();

        if (result.error || !result.success) {
          if (result.error) {
            throw new Error(result.error);
          }
          break;
        }

        if (!result.data) {
          break;
        }

        const bytes = base64ToUint8Array(result.data);

        if (bytes.length === 0) {
          break;
        }

        yield bytes;

        if (result.isEof) {
          break;
        }
      }
    } catch (e) {
      // Re-throw the error
      throw e;
    }
  }

  /**
   * Helper method to read the entire file as a single Uint8Array.
   */
  async readAll(): Promise<Uint8Array> {
    await this.open();

    try {
      const chunks: Uint8Array[] = [];

      for await (const chunk of this) {
        chunks.push(chunk);
      }

      // Combine all chunks into a single Uint8Array
      const totalLength = chunks.reduce((sum, chunk) => sum + chunk.length, 0);
      const result = new Uint8Array(totalLength);
      let offset = 0;

      for (const chunk of chunks) {
        result.set(chunk, offset);
        offset += chunk.length;
      }

      return result;
    } finally {
      await this.close();
    }
  }
}

/**
 * Converts a base64 string to a Uint8Array.
 */
export function base64ToUint8Array(base64: string): Uint8Array {
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);

  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }

  return bytes;
}

/**
 * Converts a Uint8Array to a base64 string.
 */
export function uint8ArrayToBase64(bytes: Uint8Array): string {
  let binary = '';

  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }

  return btoa(binary);
}


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
  filePath: string;
}

export interface AudioFilesResponse {
  files: AudioFile[];
}

export async function getAudioFiles(): Promise<AudioFilesResponse> {
  return await invoke<AudioFilesResponse>('plugin:android-mediastore|get_audio_files');
}


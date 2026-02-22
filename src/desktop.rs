use serde::de::DeserializeOwned;
use tauri::{plugin::PluginApi, AppHandle, Runtime};

use crate::models::*;

pub fn init<R: Runtime, C: DeserializeOwned>(
  app: &AppHandle<R>,
  _api: PluginApi<R, C>,
) -> crate::Result<AndroidMediastore<R>> {
  Ok(AndroidMediastore(app.clone()))
}

/// Access to the android-mediastore APIs.
pub struct AndroidMediastore<R: Runtime>(AppHandle<R>);

impl<R: Runtime> AndroidMediastore<R> {
  pub async fn ping(&self, payload: PingRequest) -> crate::Result<PingResponse> {
    Ok(PingResponse {
      value: payload.value,
    })
  }

  pub async fn get_audio_files(&self) -> crate::Result<AudioFilesResponse> {
    // Desktop stub implementation - returns empty list
    Ok(AudioFilesResponse {
      files: vec![],
    })
  }

  pub async fn file_reader_open(&self, _payload: FileReaderOpenRequest) -> crate::Result<FileReaderOpenResponse> {
    Err(crate::Error::NotSupported("File reader is only supported on Android".to_string()))
  }

  pub async fn file_reader_read(&self, _payload: FileReaderReadRequest) -> crate::Result<FileReaderReadResponse> {
    Err(crate::Error::NotSupported("File reader is only supported on Android".to_string()))
  }

  pub async fn file_reader_close(&self, _payload: FileReaderCloseRequest) -> crate::Result<FileReaderCloseResponse> {
    Err(crate::Error::NotSupported("File reader is only supported on Android".to_string()))
  }

  pub async fn file_reader_seek(&self, _payload: FileReaderSeekRequest) -> crate::Result<FileReaderSeekResponse> {
    Err(crate::Error::NotSupported("File reader is only supported on Android".to_string()))
  }

  pub async fn file_reader_read_to_end(&self, _payload: FileReaderReadToEndRequest) -> crate::Result<FileReaderReadToEndResponse> {
    Err(crate::Error::NotSupported("File reader is only supported on Android".to_string()))
  }

  pub async fn file_reader_info(&self, _payload: FileReaderInfoRequest) -> crate::Result<FileReaderInfoResponse> {
    Err(crate::Error::NotSupported("File reader is only supported on Android".to_string()))
  }
}

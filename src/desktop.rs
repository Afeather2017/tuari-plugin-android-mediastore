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
  pub fn ping(&self, payload: PingRequest) -> crate::Result<PingResponse> {
    Ok(PingResponse {
      value: payload.value,
    })
  }

  pub fn get_audio_files(&self) -> crate::Result<AudioFilesResponse> {
    // Desktop stub implementation - returns empty list
    Ok(AudioFilesResponse {
      files: vec![],
    })
  }
}

use serde::de::DeserializeOwned;
use serde_json;
use tauri::{
  plugin::{PluginApi, PluginHandle},
  AppHandle, Runtime,
};

use crate::models::*;

#[cfg(target_os = "ios")]
tauri::ios_plugin_binding!(init_plugin_android_mediastore);

// initializes the Kotlin or Swift plugin classes
pub fn init<R: Runtime, C: DeserializeOwned>(
  _app: &AppHandle<R>,
  api: PluginApi<R, C>,
) -> crate::Result<AndroidMediastore<R>> {
  #[cfg(target_os = "android")]
  let handle = api.register_android_plugin("com.plugin.android.mediastore", "MediaStorePlugin")?;
  #[cfg(target_os = "ios")]
  let handle = api.register_ios_plugin(init_plugin_android_mediastore)?;
  Ok(AndroidMediastore(handle))
}

/// Access to the android-mediastore APIs.
pub struct AndroidMediastore<R: Runtime>(PluginHandle<R>);

impl<R: Runtime> AndroidMediastore<R> {
  pub async fn ping(&self, payload: PingRequest) -> crate::Result<PingResponse> {
    let _ = self.check_permissions();
    self
      .0
      .run_mobile_plugin("ping", payload)
      .map_err(Into::into)
  }

  pub async fn get_audio_files(&self) -> crate::Result<AudioFilesResponse> {
    self.check_permissions_and_request_if_needed();
    self
      .0
      .run_mobile_plugin("getAudioFiles", ())
      .map_err(Into::into)
  }

  pub async fn file_reader_open(&self, payload: FileReaderOpenRequest) -> crate::Result<FileReaderOpenResponse> {
    self.check_permissions_and_request_if_needed();
    self
      .0
      .run_mobile_plugin("openFileReader", payload)
      .map_err(Into::into)
  }

  pub async fn file_reader_read(&self, payload: FileReaderReadRequest) -> crate::Result<FileReaderReadResponse> {
    self
      .0
      .run_mobile_plugin("readFile", payload)
      .map_err(Into::into)
  }

  pub async fn file_reader_close(&self, payload: FileReaderCloseRequest) -> crate::Result<FileReaderCloseResponse> {
    self
      .0
      .run_mobile_plugin("closeFileReader", payload)
      .map_err(Into::into)
  }

  pub async fn file_reader_seek(&self, payload: FileReaderSeekRequest) -> crate::Result<FileReaderSeekResponse> {
    self
      .0
      .run_mobile_plugin("seekFile", payload)
      .map_err(Into::into)
  }

  pub async fn file_reader_read_to_end(&self, payload: FileReaderReadToEndRequest) -> crate::Result<FileReaderReadToEndResponse> {
    self
      .0
      .run_mobile_plugin("readToEnd", payload)
      .map_err(Into::into)
  }

  pub async fn file_reader_info(&self, payload: FileReaderInfoRequest) -> crate::Result<FileReaderInfoResponse> {
    self
      .0
      .run_mobile_plugin("getFileReaderInfo", payload)
      .map_err(Into::into)
  }

  fn check_permissions(&self) -> crate::Result<PermissionStatus> {
    self
      .0
      .run_mobile_plugin("checkPermissions", ())
      .map_err(Into::into)
  }

  fn request_permissions(&self, permissions: Option<Vec<String>>) -> crate::Result<PermissionStatus> {
    self
      .0
      .run_mobile_plugin(
        "requestPermissions",
        serde_json::json!({ "permissions": permissions }),
      )
      .map_err(Into::into)
  }

  fn check_permissions_and_request_if_needed(&self) {
    if let Ok(perms) = self.check_permissions() {
      let needs_request = perms.audio.as_ref().map_or(false, |a| matches!(a, PermissionState::Prompt | PermissionState::PromptWithRationale))
        || perms.storage.as_ref().map_or(false, |s| matches!(s, PermissionState::Prompt | PermissionState::PromptWithRationale));

      if needs_request {
        let _ = self.request_permissions(Some(vec!["audio".to_string(), "storage".to_string()]));
      }
    }
  }
}

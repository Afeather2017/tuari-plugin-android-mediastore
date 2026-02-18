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
  let handle = api.register_android_plugin("com.plugin.android.mediastore", "ExamplePlugin")?;
  #[cfg(target_os = "ios")]
  let handle = api.register_ios_plugin(init_plugin_android_mediastore)?;
  Ok(AndroidMediastore(handle))
}

/// Access to the android-mediastore APIs.
pub struct AndroidMediastore<R: Runtime>(PluginHandle<R>);

impl<R: Runtime> AndroidMediastore<R> {
  pub fn ping(&self, payload: PingRequest) -> crate::Result<PingResponse> {
    self.check_permissions();
    self
      .0
      .run_mobile_plugin("ping", payload)
      .map_err(Into::into)
  }

  pub fn get_audio_files(&self) -> crate::Result<AudioFilesResponse> {
    self
      .0
      .run_mobile_plugin("getAudioFiles", ())
      .map_err(Into::into)
  }

  pub fn check_permissions(&self) -> crate::Result<PermissionStatus> {
    println!("check_permissions");
    self
      .0
      .run_mobile_plugin("checkPermissions", ())
      .map_err(Into::into)
  }

  pub fn request_permissions(&self, permissions: Option<Vec<String>>) -> crate::Result<PermissionStatus> {
    self
      .0
      .run_mobile_plugin(
        "requestPermissions",
        serde_json::json!({ "permissions": permissions }),
      )
      .map_err(Into::into)
  }
}

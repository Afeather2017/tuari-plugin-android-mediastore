use tauri::{
  plugin::{Builder, TauriPlugin},
  Manager, Runtime,
};

pub use models::*;

#[cfg(desktop)]
mod desktop;
#[cfg(mobile)]
mod mobile;

mod commands;
mod error;
mod models;

pub use error::{Error, Result};

#[cfg(desktop)]
use desktop::AndroidMediastore;
#[cfg(mobile)]
use mobile::AndroidMediastore;

/// Extensions to [`tauri::App`], [`tauri::AppHandle`] and [`tauri::Window`] to access the android-mediastore APIs.
pub trait AndroidMediastoreExt<R: Runtime> {
  fn android_mediastore(&self) -> &AndroidMediastore<R>;
}

impl<R: Runtime, T: Manager<R>> crate::AndroidMediastoreExt<R> for T {
  fn android_mediastore(&self) -> &AndroidMediastore<R> {
    self.state::<AndroidMediastore<R>>().inner()
  }
}

/// Initializes the plugin.
pub fn init<R: Runtime>() -> TauriPlugin<R> {
  Builder::new("android-mediastore")
    .invoke_handler(tauri::generate_handler![commands::ping, commands::get_audio_files])
    .setup(|app, api| {
      #[cfg(mobile)]
      let android_mediastore = mobile::init(app, api)?;
      #[cfg(desktop)]
      let android_mediastore = desktop::init(app, api)?;
      app.manage(android_mediastore);
      Ok(())
    })
    .build()
}

use tauri::{AppHandle, command, Runtime};

use crate::models::*;
use crate::Result;
use crate::AndroidMediastoreExt;

#[command]
pub(crate) async fn ping<R: Runtime>(
    app: AppHandle<R>,
    payload: PingRequest,
) -> Result<PingResponse> {
    app.android_mediastore().ping(payload)
}

#[command]
pub(crate) async fn get_audio_files<R: Runtime>(
    app: AppHandle<R>,
) -> Result<AudioFilesResponse> {
    app.android_mediastore().get_audio_files()
}


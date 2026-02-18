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

#[command]
pub(crate) async fn check_permissions<R: Runtime>(
    app: AppHandle<R>,
) -> Result<PermissionStatus> {
    app.android_mediastore().check_permissions()
}

#[command]
pub(crate) async fn request_permissions<R: Runtime>(
    app: AppHandle<R>,
    permissions: Option<Vec<String>>,
) -> Result<PermissionStatus> {
    app.android_mediastore().request_permissions(permissions)
}

use tauri::{AppHandle, command, Runtime};

use crate::models::*;
use crate::Result;
use crate::AndroidMediastoreExt;

#[command]
pub(crate) async fn ping<R: Runtime>(
    app: AppHandle<R>,
    payload: PingRequest,
) -> Result<PingResponse> {
    app.android_mediastore().ping(payload).await
}

#[command]
pub(crate) async fn get_audio_files<R: Runtime>(
    app: AppHandle<R>,
    payload: GetAudioFilesRequest,
) -> Result<AudioFilesResponse> {
    app.android_mediastore().get_audio_files(payload).await
}

#[command]
pub(crate) async fn file_reader_open<R: Runtime>(
    app: AppHandle<R>,
    payload: FileReaderOpenRequest,
) -> Result<FileReaderOpenResponse> {
    app.android_mediastore().file_reader_open(payload).await
}

#[command]
pub(crate) async fn file_reader_read<R: Runtime>(
    app: AppHandle<R>,
    payload: FileReaderReadRequest,
) -> Result<FileReaderReadResponse> {
    app.android_mediastore().file_reader_read(payload).await
}

#[command]
pub(crate) async fn file_reader_close<R: Runtime>(
    app: AppHandle<R>,
    payload: FileReaderCloseRequest,
) -> Result<FileReaderCloseResponse> {
    app.android_mediastore().file_reader_close(payload).await
}

#[command]
pub(crate) async fn file_reader_seek<R: Runtime>(
    app: AppHandle<R>,
    payload: FileReaderSeekRequest,
) -> Result<FileReaderSeekResponse> {
    app.android_mediastore().file_reader_seek(payload).await
}

#[command]
pub(crate) async fn file_reader_read_to_end<R: Runtime>(
    app: AppHandle<R>,
    payload: FileReaderReadToEndRequest,
) -> Result<FileReaderReadToEndResponse> {
    app.android_mediastore().file_reader_read_to_end(payload).await
}

#[command]
pub(crate) async fn file_reader_info<R: Runtime>(
    app: AppHandle<R>,
    payload: FileReaderInfoRequest,
) -> Result<FileReaderInfoResponse> {
    app.android_mediastore().file_reader_info(payload).await
}


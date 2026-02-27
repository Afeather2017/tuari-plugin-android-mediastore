use serde::{Deserialize, Serialize};

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct PingRequest {
  pub value: Option<String>,
}

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct PingResponse {
  pub value: Option<String>,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct AudioFile {
  pub id: i64,
  pub title: String,
  pub artist: String,
  pub album: String,
  pub duration: i64,
  pub content_uri: String,
  pub first_four_bytes: Option<String>,
}

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct AudioFilesResponse {
  pub files: Vec<AudioFile>,
}

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct GetAudioFilesRequest {
  #[serde(default)]
  pub exclude_suffixes: Option<Vec<String>>,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct PermissionStatus {
  pub audio: Option<PermissionState>,
  pub storage: Option<PermissionState>,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub enum PermissionState {
  Granted,
  Partial,
  Denied,
  Unknown,
  Prompt,
  #[serde(rename = "prompt-with-rationale")]
  PromptWithRationale,
}

// File reader types

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct FileReaderOpenRequest {
  pub content_uri: String,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct FileReaderOpenResponse {
  pub success: bool,
  pub session_id: i64,
  pub file_size: Option<i64>,
  pub error: Option<String>,
}

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct FileReaderReadRequest {
  pub session_id: i64,
  #[serde(default = "default_read_size")]
  pub size: i32,
}

fn default_read_size() -> i32 {
  8192
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct FileReaderReadResponse {
  pub success: bool,
  pub data: Option<String>,
  pub bytes_read: i32,
  pub is_eof: bool,
  pub error: Option<String>,
}

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct FileReaderCloseRequest {
  pub session_id: i64,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct FileReaderCloseResponse {
  pub success: bool,
  pub error: Option<String>,
}

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct FileReaderSeekRequest {
  pub session_id: i64,
  pub position: i64,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct FileReaderSeekResponse {
  pub success: bool,
  pub new_position: i64,
  pub error: Option<String>,
}

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct FileReaderReadToEndRequest {
  pub session_id: i64,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct FileReaderReadToEndResponse {
  pub success: bool,
  pub data: Option<String>,
  pub bytes_read: i32,
  pub is_eof: bool,
  pub error: Option<String>,
}

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct FileReaderInfoRequest {
  pub session_id: i64,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct SessionInfo {
  pub session_id: i64,
  pub content_uri: String,
  pub position: i64,
  pub file_size: Option<i64>,
  pub is_open: bool,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct FileReaderInfoResponse {
  pub info: Option<SessionInfo>,
  pub error: Option<String>,
}

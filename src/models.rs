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

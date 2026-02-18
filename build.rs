const COMMANDS: &[&str] = &["ping", "get_audio_files"];

fn main() {
  tauri_plugin::Builder::new(COMMANDS)
    .android_path("android")
    .ios_path("ios")
    .build();
}

import {
  MediaFileReader,
  base64ToUint8Array,
  getAudioFiles,
  type AudioFile,
} from "tauri-plugin-android-mediastore-api";

const listFilesBtn = document.querySelector<HTMLButtonElement>("#list-files-btn");
const statusEl = document.querySelector<HTMLElement>("#status");
const fileListEl = document.querySelector<HTMLUListElement>("#file-list");
const detailsEl = document.querySelector<HTMLElement>("#details");

let currentFiles: AudioFile[] = [];
let selectedFileId: number | null = null;

function formatBytes(bytes?: number): string {
  if (!bytes) return "N/A";
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(2)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

function formatDuration(durationMs: number): string {
  const totalSeconds = Math.floor(durationMs / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes}:${seconds.toString().padStart(2, "0")} (${durationMs}ms)`;
}

function setStatus(message: string): void {
  if (statusEl) {
    statusEl.textContent = message;
  }
}

function setDetails(message: string): void {
  if (detailsEl) {
    detailsEl.textContent = message;
  }
}

function renderFileList(files: AudioFile[]): void {
  if (!fileListEl) return;
  fileListEl.innerHTML = "";

  files.forEach((file) => {
    const item = document.createElement("li");
    item.className = "file-item";
    if (selectedFileId === file.id) {
      item.classList.add("selected");
    }
    item.textContent = file.title || `Unknown title (${file.id})`;
    item.addEventListener("click", () => {
      void showFileDetails(file);
    });
    fileListEl.appendChild(item);
  });
}

async function showFileDetails(file: AudioFile): Promise<void> {
  selectedFileId = file.id;
  renderFileList(currentFiles);

  let details = `Title: ${file.title}\n`;
  details += `Artist: ${file.artist}\n`;
  details += `Album: ${file.album}\n`;
  details += `Duration: ${formatDuration(file.duration)}\n`;
  details += `Size: ${formatBytes(file.size)}\n`;
  details += `Content URI: ${file.contentUri}\n`;
  details += `Magic (Metadata): ${file.firstFourBytes ?? "N/A"}\n\n`;

  const reader = new MediaFileReader(file.contentUri);

  try {
    details += "Creating MediaFileReader...\n";
    details += "Opening file...\n";
    setDetails(details);
    await reader.open();
    details += `Session opened (ID: ${reader.currentSessionId})\n`;
    setDetails(details);

    details += "Reading first 4 bytes...\n";
    setDetails(details);
    const first4 = await reader.read(4);
    details += `First 4 bytes response: success=${first4.success}, bytesRead=${first4.bytesRead}, isEof=${first4.isEof}\n`;
    if (first4.error) {
      details += `Error: ${first4.error}\n`;
    }
    if (first4.data) {
      const bytes = base64ToUint8Array(first4.data);
      const hex = Array.from(bytes)
        .map((b) => b.toString(16).padStart(2, "0"))
        .join("")
        .toUpperCase();
      details += `First 4 bytes: ${hex}\n`;
    }

    let totalBytes = 0;
    let chunks = 0;
    const maxChunks = 5;

    while (chunks < maxChunks) {
      const result = await reader.read(8192);
      if (result.error || result.isEof) {
        break;
      }
      if (result.data) {
        totalBytes += result.bytesRead;
        chunks += 1;
        details += `Chunk ${chunks}: ${result.bytesRead} bytes\n`;
      }
    }

    details += `Total read: ${totalBytes} bytes\n`;
    details += `EOF reached: ${chunks < maxChunks ? "Yes" : "No (stopped at limit)"}\n`;

    await reader.close();
    details += "Session closed\n";
  } catch (e) {
    const message = e instanceof Error ? e.message : String(e);
    details += `Error: ${message}\n`;
  }

  setDetails(details);
}

async function listMusicFiles(): Promise<void> {
  if (!listFilesBtn) return;
  listFilesBtn.disabled = true;
  setStatus("Loading audio files...");
  setDetails("Select a music file to view metadata and read diagnostics.");

  try {
    const result = await getAudioFiles({ excludeSuffixes: ["mid", "midi"] });
    currentFiles = result.files;
    selectedFileId = null;
    renderFileList(currentFiles);
    setStatus(`Found ${result.files.length} audio files.`);
  } catch (e) {
    const message = e instanceof Error ? e.message : String(e);
    setStatus(`Failed to load audio files: ${message}`);
    currentFiles = [];
    renderFileList(currentFiles);
  } finally {
    listFilesBtn.disabled = false;
  }
}

if (!listFilesBtn || !statusEl || !fileListEl || !detailsEl) {
  throw new Error("Missing required DOM elements.");
}

listFilesBtn.addEventListener("click", () => {
  void listMusicFiles();
});

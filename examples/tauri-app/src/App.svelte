<script>
  import Greet from './lib/Greet.svelte'
  import { ping, getAudioFiles, MediaFileReader, base64ToUint8Array } from 'tauri-plugin-android-mediastore-api'

	let response = $state('')
	let audioFiles = $state([])
	let selectedFile = $state(null)
	let readerTestResult = $state('')

	function updateResponse(returnValue) {
		response += `[${new Date().toLocaleTimeString()}] ` + (typeof returnValue === 'string' ? returnValue : JSON.stringify(returnValue)) + '<br>'
	}

	function _ping() {
		ping("Pong!").then(updateResponse).catch(updateResponse)
	}

	async function _getAudioFiles() {
		try {
			const result = await getAudioFiles({ excludeSuffixes: ['mid', 'midi'] })
			audioFiles = result.files
			readerTestResult = `Found ${result.files.length} audio files (MIDI files excluded)`
		} catch (e) {
			readerTestResult = `Error: ${e.message}`
		}
	}

	async function testFileReader(file) {
		selectedFile = file
		readerTestResult = `Testing file reader for: ${file.title}\n`
		readerTestResult += `Content URI: ${file.contentUri}\n`

		try {
			readerTestResult += `Creating MediaFileReader...\n`
			const reader = new MediaFileReader(file.contentUri)
			readerTestResult += `Opening file...\n`
			await reader.open()
			readerTestResult += `Session opened (ID: ${reader.currentSessionId})\n`

			// Read first 4 bytes
			readerTestResult += `Reading first 4 bytes...\n`
			const first4 = await reader.read(4)
			readerTestResult += `First 4 bytes response: success=${first4.success}, bytesRead=${first4.bytesRead}, isEof=${first4.isEof}\n`
			if (first4.error) {
				readerTestResult += `Error: ${first4.error}\n`
			}
			if (first4.data) {
				const bytes = base64ToUint8Array(first4.data)
				const hex = Array.from(bytes).map(b => b.toString(16).padStart(2, '0')).join('')
				readerTestResult += `First 4 bytes: ${hex.toUpperCase()}\n`
			}

			// Read 8KB chunks until EOF or 5 chunks
			let totalBytes = 0
			let chunks = 0
			const maxChunks = 5

			while (chunks < maxChunks) {
				const result = await reader.read(8192)
				if (result.error || result.isEof) break
				if (result.data) {
					totalBytes += result.bytesRead
					chunks++
					readerTestResult += `Chunk ${chunks}: ${result.bytesRead} bytes\n`
				}
			}

			readerTestResult += `Total read: ${totalBytes} bytes\n`
			readerTestResult += `EOF reached: ${chunks < maxChunks ? 'Yes' : 'No (stopped at limit)'}\n`

			await reader.close()
			readerTestResult += `Session closed\n`
		} catch (e) {
			readerTestResult += `Error: ${e?.message || String(e)}\n`
			readerTestResult += `Error type: ${typeof e}\n`
			readerTestResult += `Error details: ${JSON.stringify(e)}\n`
		}
	}
</script>

<main class="container">
  <h1>Welcome to Tauri!</h1>

  <div class="row">
    <a href="https://vite.dev" target="_blank">
      <img src="/vite.svg" class="logo vite" alt="Vite Logo" />
    </a>
    <a href="https://tauri.app" target="_blank">
      <img src="/tauri.svg" class="logo tauri" alt="Tauri Logo" />
    </a>
    <a href="https://svelte.dev" target="_blank">
      <img src="/svelte.svg" class="logo svelte" alt="Svelte Logo" />
    </a>
  </div>

  <p>
    Click on the Tauri, Vite, and Svelte logos to learn more.
  </p>

  <div class="row">
    <Greet />
  </div>

  <div>
    <button onclick="{_ping}">Ping</button>
    <button onclick="{_getAudioFiles}">Get Audio Files</button>
    <div>{@html response}</div>
  </div>

  <!-- File list with clickable items -->
  {#if audioFiles.length > 0}
    <div class="file-list">
      <h3>Audio Files (click to test file reader):</h3>
      {#each audioFiles as file}
        <div
          class="file-item"
          class:selected={selectedFile === file}
          onclick={() => testFileReader(file)}
        >
          <div class="file-title">{file.title}</div>
          <div class="file-info">{file.artist} - {file.album}</div>
          <div class="file-magic">Magic: {file.firstFourBytes || 'N/A'}</div>
        </div>
      {/each}
    </div>
  {/if}

  <!-- Test results -->
  {#if readerTestResult}
    <pre class="test-result">{readerTestResult}</pre>
  {/if}

</main>

<style>
  .logo.vite:hover {
    filter: drop-shadow(0 0 2em #747bff);
  }

  .logo.svelte:hover {
    filter: drop-shadow(0 0 2em #ff3e00);
  }

  .file-list {
    margin-top: 20px;
  }

  .file-item {
    padding: 10px;
    margin: 5px 0;
    border: 1px solid #ccc;
    border-radius: 4px;
    cursor: pointer;
  }

  .file-item:hover {
    background-color: #f0f0f0;
  }

  .file-item.selected {
    background-color: #e0e0ff;
    border-color: #0000ff;
  }

  .file-title {
    font-weight: bold;
  }

  .file-info {
    font-size: 0.9em;
    color: #666;
  }

  .file-magic {
    font-size: 0.8em;
    font-family: monospace;
    color: #006600;
  }

  .test-result {
    background-color: #f5f5f5;
    padding: 10px;
    border-radius: 4px;
    white-space: pre-wrap;
    margin-top: 20px;
  }
</style>

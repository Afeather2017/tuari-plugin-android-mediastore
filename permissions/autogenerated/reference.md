## Default Permission

Default permissions for the plugin

#### This default permission set includes the following:

- `allow-ping`
- `allow-get-audio-files`

## Permission Table

<table>
<tr>
<th>Identifier</th>
<th>Description</th>
</tr>


<tr>
<td>

`android-mediastore:allow-read-audio-files`

</td>
<td>

<h4>Allows reading audio files from the device MediaStore.</h4>Enables the get_audio_files command and requests the READ_MEDIA_AUDIO (Android 13+) or READ_EXTERNAL_STORAGE (Android 12 and below) permission.

</td>
</tr>

<tr>
<td>

`android-mediastore:deny-read-audio-files`

</td>
<td>

Denies reading audio files from the device MediaStore without any pre-configured scope.

</td>
</tr>

<tr>
<td>

`android-mediastore:allow-get-audio-files`

</td>
<td>

Enables the get_audio_files command without any pre-configured scope.

</td>
</tr>

<tr>
<td>

`android-mediastore:deny-get-audio-files`

</td>
<td>

Denies the get_audio_files command without any pre-configured scope.

</td>
</tr>

<tr>
<td>

`android-mediastore:allow-ping`

</td>
<td>

Enables the ping command without any pre-configured scope.

</td>
</tr>

<tr>
<td>

`android-mediastore:deny-ping`

</td>
<td>

Denies the ping command without any pre-configured scope.

</td>
</tr>
</table>

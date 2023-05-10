# AudioLink Client
This is the client library for the AudioLinkSystem. To use it, you need to create and run your own server using the [AudioLinkServer Library](https://github.com/MineKing9534/AudioLinkServer).

# Download
```xml
<repository>
	<id>jitpack.io</id>
	<url>https://jitpack.io</url>
</repository>

<dependency>
	<groupId>com.github.MineKing9534</groupId>
	<artifactId>AudioLinkServer</artifactId>
	<version>VERSION</version>
</dependency>
```

# Usage
## Basic Setup
The central class for this is `de.mineking.audiolink.client.main.AudioLinkClient`. Here is how you can create one:
```java
var client = new AudioLinkClient(
	new AudioLinkConfig(
		Duration.ofSeconds(1),
		new AudioLinkSource(null, "my-server-address", true, 443, "my-secure-password")
		//You can use multiple sources. The client will load balance between these
	)
);
```

## Connecting
Every time you open a new connection to a channel you want to send audio to you have to create a new AudioLinkConnection. To do so, you can simply use this code:
```java
client.connect().ifPresentOrElse(
	connection -> {
		//Do something with the connection
	},
	() -> {
		//No new connection could be opened
	}
);
```

## Adding tracks
After you opened a new connection, you probably want to play some tracks. To do so, you can use the playTrack method like this:
```java
client.playTrack(
	PlayerLayer.PRIMARY,
	TrackLoader.fromURL("https://youtu.be/dQw4w9WgXcQ") //You can only use valid URL's here. Something like "ytsearch: rickroll" does not work!
)
```
If you do not want to use the URL directly but instead want to use lavaplayer's search function, you can use AudioLinkClient#searchTrack like this:
```java
client.searchTrack("ytsearch: rickroll",
    result -> {
        if(result instanceof TrackCollection track) {
            connection.playTrack(PlayerLayer.PRIMARY, track);
        }
        
        else {
            //Handle failure response. You can differentiate between NoMatchesResponse and FailureResponse of you want to
        }
    }, 
    () -> {
	    //Handle error
    }
)
```

## Further usage
After playing a track you might want to influence the playback as you would with default lavaplayer. For basic configuration you can use these methods:
- stopTrack
- setPaused
- setVolume
- seek

## Integrating with JDA
To integrate AudioLinkClient to your JDA bot, you can do something similar to what you would when using default lavaplayer. You create an implementation of AudioSendHandler and register it to the AudioManager of the guild. The AudioSendHAndler implementation could look like this:

```java
import de.mineking.audiolink.client.processing.AudioLinkConnection;

public class SendHandler implements AudioSendHandler {
	private AudioLinkConnection connection;
	private byte[] frame;

	public SendHandler(AudioLinkConnection connection) {
		this.connection = connection;
	}

	@Override
	public boolean canProvide() {
		frame = connection.provide();
		return frame != null;
	}

	@Override
	public ByteBuffer provide20MsAudio() {
		return ByteBuffer.wrap(frame);
	}
	
	//Do not override isOpus to return true as you would with lavaplayer! AudioLink does not provide opus encoded audio data!
}
```
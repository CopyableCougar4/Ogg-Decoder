JOrbis source code is courtesy of http://www.jcraft.com/jorbis/

```java
OggDecoder decoder = new OggDecoder(new FileInputStream("click.ogg"));
RawPCM pcm = decoder.getPCM();
int rate = decoder.getRate();
int channels = decoder.getChannels();
BytBuffer pcmData = pcm.getData();
```
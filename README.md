
# TwitterExtractor
Twitter Url Extractor Download Videos
Android based Twitter url extractor
=======================================================

These are the urls to the Twitter Post Which Contains Video files, so you can stream or download them.

* Builds: [![JitPack](https://jitpack.io/v/Ashusolanki/TwitterUrlExtractor.svg)](https://jitpack.io/#Ashusolanki/TwitterUrlExtractor)

## Gradle

To always build from the latest commit with all updates. Add the JitPack repository:

```java
repositories {
    maven { url "https://jitpack.io" }
}
```

And the dependency:

```
dependencies 
 {
    implementation 'com.github.Ashusolanki:TwitterUrlExtractor:0.0.1'
 }
```  

## Usage

#FacebookExtractor
```

      new TwitterExtractor()
      {
          @Override
          protected void onExtractionComplete(TwitterFile twitterFile)
          {
              //Complate
          }
          @Override
          protected void onExtractionFail(String Error) 
          {
              //Fail
          }
      }.Extractor(this.getActivity(), videoURL);

```

#TwitterFile
```
    getQuality();
    getUrl();
    getExt();
    getFilename();
    getAuthor();
    getSize();
    getDuration();

```



package com.ashudevs.twitterurlextractor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;

public abstract class TwitterExtractor {

    protected abstract void onExtractionComplete(TwitterFile twitterFile);

    protected abstract void onExtractionFail(String Error);

    public void Extract(Context context,String twiterKey,String twitterSecret,String url) {
        init(context,twiterKey,twitterSecret);
        getData(url);
    }

    private void init(Context context,String twiterKey,String twitterSecret)
    {
        TwitterConfig config = new TwitterConfig.Builder(context)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(twiterKey, twitterSecret))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }

    private void getData(String url) {
        getTweet(getTweetId(url));
    }

    private Long getTweetId(String s) {
        try {
            String[] split = s.split("\\/");
            String id = split[5].split("\\?")[0];
            return Long.parseLong(id);

        } catch (Exception e) {
            onExtractionFail("getTweetId: " + e.getLocalizedMessage());
            return null;
        }
    }

    private void getTweet(final Long id) {

        final TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();
        final Call<Tweet> tweetCall = statusesService.show(id, null, null, null);
        tweetCall.enqueue(new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                //Check if media is present
                if (result.data.extendedEntities == null && result.data.entities.media == null) {
                    Log.e("Download", "Not Found");
                }
                //Check if gif or mp4 present in the file
                else if (!(result.data.extendedEntities.media.get(0).type).equals("video") && !(result.data.extendedEntities.media.get(0).type).equals("animated_gif")) {
                    Log.e("Download", "Not Found");
                } else {
                    String filename = result.data.user.name +" post "+ id;
                    String url;

                    //Set filename to gif or mp4


                    int i = 0;
                    url = result.data.extendedEntities.media.get(0).videoInfo.variants.get(i).url;
                    while (!url.contains(".mp4")) {
                        try {
                            if (result.data.extendedEntities.media.get(0).videoInfo.variants.get(i) != null) {
                                url = result.data.extendedEntities.media.get(0).videoInfo.variants.get(i).url;
                                i += 1;
                            }
                        } catch (IndexOutOfBoundsException e) {


                            if ((result.data.extendedEntities.media.get(0).type).equals("video")) {
                                new loadTwiterFileData(url,filename,".mp4").execute();
                                return;
                            } else {
                                onExtractionFail("Not Found");
                                return;
                            }
                        }
                    }
                    new loadTwiterFileData(url,filename,".mp4").execute();
                    Log.e("Twiter", "Filename : " + filename + " URL :: " + url);
                }
            }

            @Override
            public void failure(TwitterException exception) {
                onExtractionFail(exception.getLocalizedMessage());
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class loadTwiterFileData extends AsyncTask<Void,Void,TwitterFile>
    {
         private String url;
         private String filename;
         private String ext;

        public loadTwiterFileData(String url, String filename, String ext) {
            this.url = url;
            this.filename = filename;
            this.ext = ext;
        }

        @Override
        protected TwitterFile doInBackground(Void... voids) {
            TwitterFile mTwitterFile=new TwitterFile();
            try
            {
                mTwitterFile.setFilename(filename+ext);
                mTwitterFile.setExt(ext);
                mTwitterFile.setUrl(url);

                HttpsURLConnection twiterFile=(HttpsURLConnection) new URL(url).openConnection();
                twiterFile.connect();
                long x = twiterFile.getContentLength();
                long fileSizeInKB = x / 1024;
                long fileSizeInMB = fileSizeInKB / 1024;
                mTwitterFile.setSize((fileSizeInMB > 1) ? fileSizeInMB + " MB" : fileSizeInKB + " KB");
                mTwitterFile.setAuthor("Twitter");
                twiterFile.disconnect();

                Log.e("Twitter",url);
                return mTwitterFile;
            }
            catch (Exception E)
            {

                E.getLocalizedMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(TwitterFile twitterFile) {
            super.onPostExecute(twitterFile);
            if(twitterFile!=null)
            {
                onExtractionComplete(twitterFile);
            }
            else
            {
                onExtractionFail("Somthing Wrong Check Your Link");
            }
        }
    }
}

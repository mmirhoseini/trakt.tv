package com.mirhoseini.trakttv.core.di.module;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Mohsen on 19/07/16.
 */

@Module
public class ClientModule {

    private static final String CACHE_CONTROL = "Cache-Control";

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(HttpLoggingInterceptor loggingInterceptor,
                                            @Named("networkTimeoutInSeconds") int networkTimeoutInSeconds,
                                            @Named("isDebug") boolean isDebug,
                                            Cache cache,
                                            @Named("cacheInterceptor") Interceptor cacheInterceptor,
                                            @Named("offlineInterceptor") Interceptor offlineCacheInterceptor) {

        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(cacheInterceptor)
                .addInterceptor(offlineCacheInterceptor)
                .cache(cache)
                .connectTimeout(networkTimeoutInSeconds, TimeUnit.SECONDS);

        //show logs if app is in Debug mode
        if (isDebug)
            okHttpClient.addInterceptor(loggingInterceptor);

        return okHttpClient.build();
    }

    @Provides
    @Singleton
    public HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return logging;

    }

    @Provides
    @Singleton
    public Cache provideCache(@Named("cacheDir") File cacheDir, @Named("cacheSize") long cacheSize) {
        Cache cache = null;

        try {
            cache = new Cache(new File(cacheDir.getPath(), "http-cache"), cacheSize);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cache;
    }

    @Provides
    @Singleton
    @Named("cacheInterceptor")
    public Interceptor provideCacheInterceptor(@Named("cacheMaxAge") int maxAgeMin) {
        return chain -> {
            Response response = chain.proceed(chain.request());

            CacheControl cacheControl = new CacheControl.Builder()
                    .maxAge(maxAgeMin, TimeUnit.MINUTES)
                    .build();

            return response.newBuilder()
                    .header(CACHE_CONTROL, cacheControl.toString())
                    .build();
        };
    }


    @Provides
    @Singleton
    @Named("offlineInterceptor")
    public Interceptor provideOfflineCacheInterceptor(@Named("isConnected") boolean isConnected, @Named("cacheMaxStale") int maxStaleDay) {
        return chain -> {
            Request request = chain.request();

            if (!isConnected) {
                CacheControl cacheControl = new CacheControl.Builder()
                        .maxStale(maxStaleDay, TimeUnit.DAYS)
                        .build();

                request = request.newBuilder()
                        .cacheControl(cacheControl)
                        .build();
            }

            return chain.proceed(request);
        };
    }

}

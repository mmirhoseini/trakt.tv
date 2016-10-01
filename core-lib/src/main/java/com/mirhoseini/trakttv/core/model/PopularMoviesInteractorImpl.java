package com.mirhoseini.trakttv.core.model;


import com.mirhoseini.trakttv.core.di.scope.PopularMoviesScope;
import com.mirhoseini.trakttv.core.service.TraktApi;
import com.mirhoseini.trakttv.core.util.Constants;
import com.mirhoseini.trakttv.core.util.SchedulerProvider;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.subjects.ReplaySubject;
import tv.trakt.api.model.Movie;

/**
 * Created by Mohsen on 19/07/16.
 */

@PopularMoviesScope
public class PopularMoviesInteractorImpl implements PopularMoviesInteractor {
    private TraktApi api;
    private SchedulerProvider scheduler;

    private ReplaySubject<Movie[]> moviesDataSubject;
    private Subscription moviesSubscription;

    @Inject
    public PopularMoviesInteractorImpl(TraktApi api, SchedulerProvider scheduler) {
        this.api = api;
        this.scheduler = scheduler;
    }

    @Override
    public Observable<Movie[]> loadPopularMovies(int page, int limit) {
        if (moviesSubscription == null || moviesSubscription.isUnsubscribed()) {
            moviesDataSubject = ReplaySubject.create();

            moviesSubscription = api.getPopularMovies(page, limit, Constants.API_EXTENDED_FULL_IMAGES)
                    .subscribeOn(scheduler.backgroundThread())
                    .observeOn(scheduler.mainThread())
                    .subscribe(moviesDataSubject);
        }

        return moviesDataSubject.asObservable();
    }

    @Override
    public void onDestroy() {
        if (moviesSubscription != null && !moviesSubscription.isUnsubscribed())
            moviesSubscription.unsubscribe();
    }

}
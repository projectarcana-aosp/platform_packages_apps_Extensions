package org.arcana.grimoire.aexstats;

import org.arcana.grimoire.aexstats.models.ServerRequest;
import org.arcana.grimoire.aexstats.models.ServerResponse;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by ishubhamsingh on 25/9/17.
 */

public interface RequestInterface {

    @POST("stats/")
    Observable<ServerResponse> operation(@Body ServerRequest request);

}

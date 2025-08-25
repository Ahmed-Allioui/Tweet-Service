package hbrs.projektseminar.tweetservice.repository;

import hbrs.projektseminar.tweetservice.model.Tweet;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface TweetRepository extends ReactiveCrudRepository<Tweet, Long> {

    /**
     * Methods for tweet table
     */

    Flux<Tweet> findAllByAuthorId(Long authorId);

    Flux<Tweet> findAllByAuthorIdIn(List<Long> ids);

    Flux<Tweet> findAllByIdIn(List<Long> ids);

    Flux<Tweet> findAllByTextContainingIgnoreCase(String word);

    @Query("UPDATE tweet SET retweet_id = NULL WHERE retweet_id = :id")
    Mono<Void> setRetweetIdsNullByTweetId(Long id);

    /**
     * Methods for tweet_like table
     */

    @Query("SELECT user_id FROM tweet_like WHERE tweet_id = :id")
    Flux<Long> getAllLikesByTweetId(Long id);

    @Query("DELETE FROM tweet_like WHERE tweet_id = :tweetId AND user_id = :userId")
    Mono<Void> deleteByTweetIdAndUserId(Long tweetId, Long userId);

    @Query("DELETE FROM tweet_like WHERE tweet_id = :id")
    Mono<Void> deleteAllLikesByTweetId(Long id);

    @Query("SELECT COUNT(user_id) FROM tweet_like WHERE tweet_id = :id")
    Mono<Long> countByTweetId(Long id);

    @Query("INSERT INTO tweet_like (tweet_id, user_id) VALUES (:tweetId, :userId)")
    Mono<Void> addLikeToTweet(Long tweetId, Long userId);

    /**
     * Methods for tweet_picture table
     */

    @Query("SELECT picture_id FROM tweet_picture WHERE tweet_id = :id")
    Flux<Long> getAllPicturesByTweetId(Long id);

    @Query("DELETE FROM tweet_picture WHERE tweet_id = :id")
    Mono<Void> deleteAllPicturesByTweetId(Long id);

    @Query("INSERT INTO tweet_picture (tweet_id, picture_id) VALUES (:tweetId, :pictureId)")
    Mono<Void> addPictureToTweet(Long tweetId, Long pictureId);
}

package hbrs.projektseminar.tweetservice.service;

import hbrs.projektseminar.tweetservice.exceptions.TweetNotFoundException;
import hbrs.projektseminar.tweetservice.model.Tweet;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public interface TweetService {

    /**
     * this method returns tweets of a specific user
     * @param userId
     * @return
     */
    Flux<Tweet> getAllUserTweets(Long userId);

    /**
     * this method returns tweets of persons, that the user follows
     * @param userId
     * @return
     */
    Flux<Tweet> getAllUserFollowingsTweets(Long userId);

    /**
     * this method returns all tweets by hashtag
     * @param hashtag
     * @return
     */
    Flux<Tweet> getAllTweetsByHashtag(String hashtag);

    /**
     * this method returns all tweets where that hashtag appears in their comments
     * @param hashtag
     * @return
     */
    public Flux<Tweet> getAllTweetsByCommentsHashtag(String hashtag);

    /**
     * this method returns all tweets that contains that world
     * @param word
     * @return
     */
    Flux<Tweet> getAllTweetsContaining(String word);

    /**
     * this method returns all tweets where that word appears in their comments
     * @param word
     * @return
     */
    Flux<Tweet> getAllTweetsByCommentsContaining(String word);

    /**
     * this method returns a tweet by id
     * @param id for tweet
     * @return
     * @throws TweetNotFoundException when no tweet found for id
     */
    Mono<Tweet> getTweet(Long id);

    /**
     * this method creates a tweet
     * @param tweet
     * @return
     */
    Mono<Tweet> createTweet(Tweet tweet);

    /**
     * this method adds a like to a tweet
     * @param tweetId id for the tweet to be liked
     * @param userId id of the user liking the tweet
     * @return void mono if tweet id exists, {@link TweetNotFoundException} if id does not exist
     */
    Mono<Void> addLike(Long tweetId, Long userId);

    /**
     * this method adds a picture to a tweet
     * @param tweetId
     * @param pictureId
     * @return
     */
    Mono<Void> addPicture(Long tweetId, Long pictureId);

    /**
     * this method deletes a tweet
     * @param tweetId
     * @return
     */
    Mono<Void> deleteTweet(Long tweetId);

    /**
     * this method deletes a like from a tweet
     * @param tweetId
     * @param userId
     * @return
     */
    Mono<Void> deleteLike(Long tweetId, Long userId);

    /**
     * this method deletes all tweets of a user
     * @param userId
     * @return
     */
    Mono<Void> deleteAllTweetsByUserId(Long userId);
}

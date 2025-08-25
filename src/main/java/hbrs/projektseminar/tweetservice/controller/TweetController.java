package hbrs.projektseminar.tweetservice.controller;

import hbrs.projektseminar.tweetservice.model.Tweet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("api/tweets")
public interface TweetController {

    /**
     * this method returns tweets of a specific user
     * @param userId
     * @return
     */
    @GetMapping(path = "")
    Mono<ResponseEntity<List<Tweet>>> getAllUsersTweets(@RequestParam(name = "user") Long userId);

    /**
     * this method returns tweets of persons, that the user follows
     * @param userId
     * @return
     */
    @GetMapping(path = "follows")
    Mono<ResponseEntity<List<Tweet>>> getAllUserFollowingsTweets(@RequestParam(name = "user") Long userId);

    /**
     * this method returns tweets by hashtag
     * @param hashtag
     * @return
     */
    @GetMapping(path = "tweetHashtags")
    Mono<ResponseEntity<List<Tweet>>> getAllTweetsByHashtag(@RequestParam(name = "hashtag") String hashtag);

    /**
     * this method returns all tweets where that hashtag appears in their comments
     * @param hashtag
     * @return
     */
    @GetMapping(path = "commentHashtags")
    Mono<ResponseEntity<List<Tweet>>> getAllTweetsByCommentsContainingHashtag(@RequestParam(name = "hashtag") String hashtag);

    /**
     * this method returns tweets containing the word
     * @param word
     * @return
     */
    @GetMapping(path = "search/tweet")
    Mono<ResponseEntity<List<Tweet>>> getAllTweetsContaining(@RequestParam(name = "word") String word);

    /**
     *
     * @param word
     * @return
     */
    @GetMapping(path = "search/comment")
    Mono<ResponseEntity<List<Tweet>>> getAllTweetsByCommentsContaining(@RequestParam(name = "word") String word);

    /**
     * this method returns a tweet by id
     * @param id
     * @return
     */
    @GetMapping(path = "{id}")
    Mono<ResponseEntity<Tweet>> getTweet(@PathVariable("id") Long id);

    /**
     * this method creates a tweet
     * @param tweet
     * @return
     */
    @PostMapping(path = "")
    Mono<ResponseEntity<Tweet>> createTweet(@RequestBody Tweet tweet);

    /**
     * this method adds a like to a tweet
     * @param tweetId
     * @param userId
     * @return 200 if tweet id exists, 400 if it doesnt exist
     */
    @PostMapping(path = "{id}/like")
    Mono<ResponseEntity<Void>> addLike(@PathVariable("id") Long tweetId,
                                            @RequestParam(name = "user") Long userId);

    /**
     * this method deletes a tweet
     * @param tweetId
     * @return
     */
    @DeleteMapping(path = "{id}")
    Mono<ResponseEntity<Void>> deleteTweet(@PathVariable("id") Long tweetId);

    /**
     * this method deletes all tweets of a user
     * @param userId
     * @return
     */
    @DeleteMapping(path = "all")
    Mono<ResponseEntity<Void>> deleteAllTweetsByUser(@RequestParam(name = "user") Long userId);

    /**
     * this method deletes a like from a tweet
     * @param tweetId
     * @param userId
     * @return
     */
    @DeleteMapping(path = "{id}/like")
    Mono<ResponseEntity<Void>> unlike(@PathVariable("id") Long tweetId,
                                      @RequestParam(name = "user") Long userId);

}

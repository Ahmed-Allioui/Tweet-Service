package hbrs.projektseminar.tweetservice.controller;

import hbrs.projektseminar.tweetservice.handler.ErrorHandler;
import hbrs.projektseminar.tweetservice.model.Tweet;
import hbrs.projektseminar.tweetservice.service.TweetService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@AllArgsConstructor
public class TweetControllerImpl implements TweetController{

    private final TweetService tweetService;

    @Override
    public Mono<ResponseEntity<List<Tweet>>> getAllUsersTweets(Long userId) {
        return tweetService.getAllUserTweets(userId)
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(ErrorHandler::handleError);
    }

    @Override
    public Mono<ResponseEntity<List<Tweet>>> getAllUserFollowingsTweets(Long userId) {
        return tweetService.getAllUserFollowingsTweets(userId)
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(ErrorHandler::handleError);
    }

    @Override
    public Mono<ResponseEntity<List<Tweet>>> getAllTweetsByHashtag(String hashtag) {
        return tweetService.getAllTweetsByHashtag(hashtag)
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(ErrorHandler::handleError);
    }

    @Override
    public Mono<ResponseEntity<List<Tweet>>> getAllTweetsByCommentsContainingHashtag(String hashtag) {
        return tweetService.getAllTweetsByCommentsHashtag(hashtag)
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(ErrorHandler::handleError);
    }

    @Override
    public Mono<ResponseEntity<List<Tweet>>> getAllTweetsContaining(String word) {
        return tweetService.getAllTweetsContaining(word)
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(ErrorHandler::handleError);
    }

    @Override
    public Mono<ResponseEntity<List<Tweet>>> getAllTweetsByCommentsContaining(String word) {
        return tweetService.getAllTweetsByCommentsContaining(word)
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(ErrorHandler::handleError);
    }

    @Override
    public Mono<ResponseEntity<Tweet>> getTweet(Long id) {
        return tweetService.getTweet(id)
                .map(ResponseEntity::ok)
                .onErrorResume(ErrorHandler::handleError);
    }

    @Override
    public Mono<ResponseEntity<Tweet>> createTweet(Tweet tweet) {
        return tweetService.createTweet(tweet)
                .map(t -> ResponseEntity.status(HttpStatus.CREATED).body(t))
                .onErrorResume(ErrorHandler::handleError);
    }

    @Override
    public Mono<ResponseEntity<Void>> addLike(Long tweetId, Long userId) {
        return tweetService.addLike(tweetId,userId)
                .map(ResponseEntity::ok)
                .onErrorResume(ErrorHandler::handleError);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteTweet(Long tweetId) {
        return tweetService.deleteTweet(tweetId)
                .map(ResponseEntity::ok)
                .onErrorResume(ErrorHandler::handleError);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteAllTweetsByUser(Long userId) {
        return tweetService.deleteAllTweetsByUserId(userId)
                .map(ResponseEntity::ok)
                .onErrorResume(ErrorHandler::handleError);
    }

    @Override
    public Mono<ResponseEntity<Void>> unlike(Long tweetId, Long userId) {
        return tweetService.deleteLike(tweetId, userId)
                .map(ResponseEntity::ok)
                .onErrorResume(ErrorHandler::handleError);
    }
}

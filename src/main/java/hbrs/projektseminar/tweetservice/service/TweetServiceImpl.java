package hbrs.projektseminar.tweetservice.service;

import hbrs.projektseminar.tweetservice.enumeration.ErrorMessage;
import hbrs.projektseminar.tweetservice.factory.UriFactory;
import hbrs.projektseminar.tweetservice.exceptions.TweetNotFoundException;
import hbrs.projektseminar.tweetservice.model.Tweet;
import hbrs.projektseminar.tweetservice.dto.HashtagTextDTO;
import hbrs.projektseminar.tweetservice.populator.TweetPopulator;
import hbrs.projektseminar.tweetservice.repository.TweetRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
@Builder
@Slf4j
public class TweetServiceImpl implements TweetService {

    @Autowired private final TweetRepository tweetRepository;

    @Autowired private final CommentService commentService;

    @Autowired private final TweetPopulator tweetPopulator;

    @Autowired private final WebClient client;

    @Autowired private final UriFactory uriFactory;

    private static final String EMPTY_STRING = "";

    @Override
    public Flux<Tweet> getAllUserTweets(Long userId) {
        if(userId == null) {
            return Flux.error(new IllegalArgumentException(ErrorMessage.USER_ID_MISSING));
        }
        log.info("Getting All tweets for User {}", userId);
        return tweetRepository.findAllByAuthorId(userId)
                .flatMap(tweetPopulator::getAll)
                .map(tweet -> {
                    log.debug("Tweet {} of the user {} received",tweet.getId(), userId);
                    return tweet;
                });
    }

    @Override
    public Flux<Tweet> getAllUserFollowingsTweets(Long userId) {
        if(userId == null) {
            return Flux.error(new IllegalArgumentException(ErrorMessage.USER_ID_MISSING));
        }
        log.info("Getting All tweets of people that the user {} follows", userId);
        return getUsersFollows(userId)
                .flatMapMany(ids -> tweetRepository.findAllByAuthorIdIn(ids)
                        .flatMap(tweetPopulator::getAll))
                .map(tweet -> {
                    log.debug("Tweet {} received", tweet.getId());
                    return tweet;
                });
    }

    @Override
    public Flux<Tweet> getAllTweetsByHashtag(String hashtag) {
        if(hashtag == null) {
            return Flux.error(new IllegalArgumentException(ErrorMessage.HASHTAG_MISSING));
        }
        log.info("Getting All tweets for hashtag {}", hashtag);
        return getTweetIdByHashtag(hashtag)
                .flatMapMany((ids) -> tweetRepository.findAllByIdIn(ids)
                        .flatMap(tweetPopulator::getAll))
                .map(tweet -> {
                    log.debug("Tweet {} received", tweet.getId());
                    return tweet;
                });
    }

    @Override
    public Flux<Tweet> getAllTweetsByCommentsHashtag(String hashtag) {
        if(hashtag == null) {
            return Flux.error(new IllegalArgumentException(ErrorMessage.HASHTAG_MISSING));
        }
        return commentService.getAllTweetIdsByCommentHashtag(hashtag)
                .collectList().flatMapMany((ids) -> tweetRepository.findAllByIdIn(ids)
                        .flatMap(tweetPopulator::getAll))
                .map(tweet -> {
                    log.debug("Tweet {} received", tweet.getId());
                    return tweet;
                });
    }

    @Override
    public Flux<Tweet> getAllTweetsContaining(String word) {
        if(word == null) {
            return Flux.error(new IllegalArgumentException(ErrorMessage.WORD_MISSING));
        }
        log.info("Getting All tweets for the word {}", word);
        return tweetRepository.findAllByTextContainingIgnoreCase(word)
                .flatMap(tweetPopulator::getAll)
                .map(tweet -> {
                    log.debug("Tweet {} received", tweet.getId());
                    return tweet;
                });
    }

    @Override
    public Flux<Tweet> getAllTweetsByCommentsContaining(String word) {
        if(word == null) {
            return Flux.error(new IllegalArgumentException(ErrorMessage.WORD_MISSING));
        }
        return commentService.getAllTweetIdsWhereCommentContains(word)
                .collectList()
                .flatMapMany((ids) -> tweetRepository.findAllByIdIn(ids)
                        .flatMap(tweetPopulator::getAll))
                .map(tweet -> {
                    log.debug("Tweet {} received", tweet.getId());
                    return tweet;
                });
    }

    @Override
    public Mono<Tweet> getTweet(Long id) {
        if(id == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.TWEET_ID_MISSING));
        }
        log.info("Getting tweet {}", id);
        return tweetRepository.findById(id)
                .flatMap(tweetPopulator::getAll)
                .map(tweet -> {
                    log.debug("Tweet {} received", tweet.getId());
                    return tweet;
                });
    }

    @Override
    public Mono<Tweet> createTweet(Tweet tweet) {
        if(tweet.getAuthorId() == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.AUTHOR_MISSING));
        }
        log.info("Creating a new tweet");
        // set createdOn on the local date
        tweet.setCreatedOn(LocalDate.now());
        return tweetRepository.save(tweet).map((t) -> {
            log.debug("Tweet {} created", t.getId());
            List<Long> pictures = tweet.getPictures();
            if(pictures != null) {
                pictures.forEach(picture -> addPicture(tweet.getId(), picture).subscribe());
            }
            sendTextToHashtagService(t.getId(), t.getText());
            t.setPictures(pictures);
            return t;
        });
    }

    @Override
    public Mono<Void> addPicture(Long tweetId, Long pictureId) {
        if(tweetId == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.TWEET_ID_MISSING));
        }
        if(pictureId == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.PICTURE_ID_MISSING));
        }
        log.info("Adding picture {} to the tweet {}", pictureId, tweetId);
        return tweetRepository.addPictureToTweet(tweetId, pictureId);
    }

    @Override
    public Mono<Void> addLike(Long tweetId, Long userId) {
        if(tweetId == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.TWEET_ID_MISSING));
        }
        if(userId == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.USER_ID_MISSING));
        }
        log.info("Adding like from the user {} to the tweet {}", userId, tweetId);
        return tweetRepository.existsById(tweetId).flatMap(exists -> {
            if(!exists){
                log.warn("The tweet {} does not exists", tweetId);
                return Mono.error(new TweetNotFoundException(ErrorMessage.TWEET_NOT_EXISTS));
            }
            tweetRepository.addLikeToTweet(tweetId, userId)
                    .then()
                    .map(any -> {
                        log.debug("Like from the user {} to the tweet {} added", userId, tweetId);
                        return any;
                    })
                    .subscribe();

            //send likes to hashtag service
            this.updateHashtagLikes(tweetId);
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> deleteTweet(Long tweetId) {
        if(tweetId == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.TWEET_ID_MISSING));
        }
        log.info("Deleting the tweet {}", tweetId);
        return tweetRepository.deleteAllLikesByTweetId(tweetId)             // delete all likes of that tweet
                .then(commentService.deleteAllCommentsByTweetId(tweetId))   // delete all comments of that tweet
                .then(tweetRepository.deleteAllPicturesByTweetId(tweetId))   // delete all pictures of that tweet
                .then(tweetRepository.setRetweetIdsNullByTweetId(tweetId))  // set retweetIds to null
                .then(tweetRepository.deleteById(tweetId))
                .map(any -> {     // delete the tweet
                    log.debug("Tweet {} deleted", tweetId);
                    sendTextToHashtagService(tweetId);
                    return any;
                });
    }

    @Override
    public Mono<Void> deleteLike(Long tweetId, Long userId) {
        if(tweetId == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.TWEET_ID_MISSING));
        }
        if(userId == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.USER_ID_MISSING));
        }
        log.info("The user {} unlikes the tweet {}",userId, tweetId);
        return tweetRepository.deleteByTweetIdAndUserId(tweetId,userId)
                .map(any -> {
                    log.debug("Like from user {} to tweet {} deleted",userId, tweetId);
                    this.updateHashtagLikes(tweetId);
                    return any;
                });
    }

    @Override
    public Mono<Void> deleteAllTweetsByUserId(Long userId) {
        return getAllUserTweets(userId)
                .flatMap(tweet -> deleteTweet(tweet.getId()))
                .map(any -> {
                    log.debug("Tweets of the user {} are deleted", userId);
                    return Mono.empty();
                })
                .then();
    }

    private Mono<List<Long>> getUsersFollows(Long userId) {
        // sending request to follow service to get IDs of the users followers
        log.debug("Sending request to Follow Service to get the users followings");
        UriComponentsBuilder followsUri = uriFactory.getFollowsUri();
        if(followsUri == null) {
            return Mono.error(new InternalError(ErrorMessage.FOLLOW_SERVICE_NOT_ACCESSIBLE));
        }
        return client
                .get()
                .uri(followsUri.buildAndExpand(userId).toUri())
                .retrieve()
                .bodyToFlux(Long.class)
                .collectList();
    }

    private void updateHashtagLikes(Long tweetId){
        log.info("Informing the hashtag service about the state of likes");
        log.debug("Fetching the number of like of the tweet {}", tweetId);
        tweetRepository.countByTweetId(tweetId)
                .flatMap(likes -> {
                    log.debug("The number of like of the tweet {} received", tweetId);
                    log.debug("Sending the payload to Hashtag...");
                    UriComponentsBuilder updateTweetLikesUri = uriFactory.updateTweetLikesUri();
                    if(updateTweetLikesUri == null) {
                        log.warn(ErrorMessage.HASHTAG_SERVICE_NOT_ACCESSIBLE);
                        return Mono.empty();
                    }
                    return client.post().uri(updateTweetLikesUri.buildAndExpand(tweetId, likes).toUri())
                            .contentType(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .toBodilessEntity();
                }).map(any -> {
                    log.debug("Likes sent to hashtag service");
                    return any;
                }).then()
                .subscribe();
    }

    private void sendTextToHashtagService(Long id) {
        sendTextToHashtagService(id, EMPTY_STRING);
    }

    private void sendTextToHashtagService(Long id, String text) {
        log.debug("Building the payload to send to the hashtag service");
        // building the object to be sent to the hashtag service
        HashtagTextDTO hashtagTextDTO = HashtagTextDTO.builder()
                .id(id)
                .text(text)
                .build();
        log.debug("Payload built");
        log.debug("Sending the payload...");
        UriComponentsBuilder sendTweetHashtagUri = uriFactory.sendTweetHashtagUri();
        if(sendTweetHashtagUri == null) {
            log.warn(ErrorMessage.HASHTAG_SERVICE_NOT_ACCESSIBLE);
            return;
        }
        client.post()
                .uri(sendTweetHashtagUri.build().toUri())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(hashtagTextDTO)
                .retrieve()
                .toBodilessEntity()
                .map(any -> {
                    log.debug("Payload sent");
                    return any;
                }).then()
                .subscribe();
    }

    private Mono<List<Long>> getTweetIdByHashtag(String hashtag) {
        log.debug("Sending request to hashtag service to get IDs of tweets");
        UriComponentsBuilder tweetsByHashtagUri = uriFactory.tweetsByHashtagUri();
        if(tweetsByHashtagUri == null) {
            return Mono.error(new InternalError(ErrorMessage.HASHTAG_SERVICE_NOT_ACCESSIBLE));
        }
        return client
                .get()
                .uri(tweetsByHashtagUri.buildAndExpand(hashtag).toUri())
                .retrieve()
                .bodyToFlux(Long.class)
                .collectList();
    }
}

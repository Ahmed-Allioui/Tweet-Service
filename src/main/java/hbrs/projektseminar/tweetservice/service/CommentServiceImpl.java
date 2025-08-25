package hbrs.projektseminar.tweetservice.service;

import hbrs.projektseminar.tweetservice.enumeration.ErrorMessage;
import hbrs.projektseminar.tweetservice.factory.UriFactory;
import hbrs.projektseminar.tweetservice.dto.HashtagUpdateLikesDTO;
import hbrs.projektseminar.tweetservice.exceptions.TweetNotFoundException;
import hbrs.projektseminar.tweetservice.model.Comment;
import hbrs.projektseminar.tweetservice.dto.HashtagTextDTO;
import hbrs.projektseminar.tweetservice.populator.CommentPopulator;
import hbrs.projektseminar.tweetservice.repository.CommentRepository;
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

@Builder
@Service
@AllArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Autowired private final CommentRepository commentRepository;

    @Autowired private final CommentPopulator commentPopulator;

    @Autowired private final WebClient client;

    @Autowired private final UriFactory uriFactory;

    private static final String EMPTY_STRING = "";

    @Override
    public Flux<Comment> getComments(Long tweetId) {
        if(tweetId == null) {
            return Flux.error(new IllegalArgumentException(ErrorMessage.TWEET_ID_MISSING));
        }
        log.info("Getting All comments for tweet {}", tweetId);
        return commentRepository.findByTweetId(tweetId)
                .flatMap(commentPopulator::getLikes)
                .map(comments -> {
                    log.debug("All comments of the tweet {} received", tweetId);
                    return comments;
                });
    }


    @Override
    public Flux<Long> getAllTweetIdsByCommentHashtag(String hashtag) {
        if(hashtag == null) {
            return Flux.error(new IllegalArgumentException(ErrorMessage.HASHTAG_MISSING));
        }
        log.info("Getting All tweets for comments hashtag {}", hashtag);
        return getAllCommentIdByHashtag(hashtag)
                .flatMapMany((comments) -> commentRepository.findAllTweetIdsByCommentIdsIn(comments)
                        .map(tweet -> {
                            log.debug("All tweets related to the hashtag {} received", hashtag);
                            return tweet;
                        }));
    }

    @Override
    public Flux<Long> getAllTweetIdsWhereCommentContains(String word) {
        if(word == null) {
            return Flux.error(new IllegalArgumentException(ErrorMessage.WORD_MISSING));
        }
        log.info("Getting All tweets id that has a comment containing the world {}", word);
        return commentRepository.findAllByTextContainingIgnoreCase(word)
                .map(comment -> {
                    log.debug("All tweets that has a comment containing the world {} received", word);
                    return comment.getTweetId();
                });
    }

    @Override
    public Mono<Comment> createComment(Comment comment) {
        if(comment.getAuthorId() == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.AUTHOR_MISSING));
        }
        log.info("Creating a new comment");
        comment.setCreatedOn(LocalDate.now());
        return commentRepository.save(comment).map(c -> {
            log.debug("Comment {} created", c.getId());
            sendCommentToHashtagService(c.getId(), c.getText());
            return c;
        });
    }

    @Override
    public Mono<Void> deleteComment(Long commentId) {
        if(commentId == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.COMMENT_ID_MISSING));
        }
        log.info("Deleting the comment {}", commentId);
        return commentRepository.deleteAllLikesByCommentId(commentId)           // delete all likes of that tweet
                .then(commentRepository.deleteById(commentId)).map(any -> {     // delete the comment
                    log.debug("Comment {} deleted", commentId);
                    sendCommentToHashtagService(commentId);
                    return any;
                });
    }

    @Override
    public Mono<Void> deleteAllCommentsByTweetId(Long tweetId) {
        if(tweetId == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.TWEET_ID_MISSING));
        }
        log.info("Deleting all comments of the tweet {}", tweetId);
        return getComments(tweetId)
                .flatMap(comment -> {
                    sendCommentToHashtagService(comment.getId());
                    return deleteComment(comment.getId());})
                .then().map(any -> {
                    log.debug("All comments of the tweet {} are deleted", tweetId);
                    return any;
                });
    }

    @Override
    public Mono<Void> addLike(Long commentId, Long userId) {
        if(commentId == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.COMMENT_ID_MISSING));
        }
        if(userId == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.USER_ID_MISSING));
        }
        log.info("Adding a like from the user {} to the comment {}",userId, commentId);
        return commentRepository.existsById(commentId).flatMap(exists -> {
            if(!exists){
                log.warn("The comment {} does not exists", commentId);
                return Mono.error(new TweetNotFoundException("You can't like a comment that does not exists"));
            }
            commentRepository.addLikeToComment(commentId, userId).subscribe();
            log.info("Like from the user {} to the comment {} added", userId, commentId);

            //send likes to hashtag service
            this.updateHashtagLikes(commentId);
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> deleteLike(Long commentId, Long userId) {
        if(commentId == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.COMMENT_ID_MISSING));
        }
        if(userId == null) {
            return Mono.error(new IllegalArgumentException(ErrorMessage.USER_ID_MISSING));
        }
        log.info("The user {} unlikes the comment {}",userId, commentId);
        commentRepository.deleteByCommentIdAndUserId(commentId,userId).subscribe();
        log.debug("Like from user {} to comment {} deleted",userId, commentId);
        //send likes to hashtag service
        this.updateHashtagLikes(commentId);
        return Mono.empty();
    }

    private void updateHashtagLikes(Long commentId){
        log.info("Informing the hashtag service about the state of likes");
        log.debug("Fetching the number of like of the comment {}", commentId);
        commentRepository.countByCommentId(commentId)
                .flatMap(likes -> {
                    log.debug("The number of like of the comment {} received", commentId);
                    log.debug("Building the payload to send to the hashtag service");
                    HashtagUpdateLikesDTO hashtagUpdateLikesDTO = HashtagUpdateLikesDTO.builder()
                            .id(commentId)
                            .likes(likes)
                            .build();
                    log.debug("Payload built");
                    log.debug("Sending the payload...");
                    UriComponentsBuilder updateCommentLikesUri = uriFactory.updateCommentLikesUri();
                    if(updateCommentLikesUri == null) {
                        return Mono.empty();
                    }
                    return client.post().uri(updateCommentLikesUri.build().toUri())
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(hashtagUpdateLikesDTO)
                            .retrieve()
                            .toBodilessEntity();
                }).then()
                .subscribe();
        log.debug("Payload sent");
    }

    private Mono<List<Long>> getAllCommentIdByHashtag(String hashtag) {
        // sending request to hashtag service to get IDs of tweets
        log.debug("Sending request to hashtag service to get IDs of comments");
        UriComponentsBuilder commentsByHashtagUri = uriFactory.commentsByHashtagUri();
        if(commentsByHashtagUri == null) {
            return Mono.error(new InternalError(ErrorMessage.HASHTAG_SERVICE_NOT_ACCESSIBLE));
        }
        return client
                .get()
                .uri(commentsByHashtagUri.buildAndExpand(hashtag).toUri())
                .retrieve()
                .bodyToFlux(Long.class)
                .map(any -> {
                    log.debug("Response received from Hashtag Service");
                    return any;
                })
                .collectList();
    }

    private void sendCommentToHashtagService(Long id) {
        sendCommentToHashtagService(id, EMPTY_STRING);
    }

    private void sendCommentToHashtagService(Long id, String text) {
        log.debug("Building the payload to send to the hashtag service");
        HashtagTextDTO hashtagTextDTO = HashtagTextDTO.builder()
                .id(id)
                .text(text)
                .build();
        log.debug("Payload built");
        log.debug("Sending the payload...");
        UriComponentsBuilder sendCommentHashtagUri = uriFactory.sendCommentHashtagUri();
        if(sendCommentHashtagUri == null) {
            log.warn(ErrorMessage.HASHTAG_SERVICE_NOT_ACCESSIBLE);
            return;
        }
        client.post().uri(sendCommentHashtagUri.build().toUri())
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
}

package hbrs.projektseminar.tweetservice.service;

import hbrs.projektseminar.tweetservice.model.Comment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public interface CommentService {

    /**
     * this method return a flux of comments of a specific tweet
     * @param tweetId
     * @return
     */
    Flux<Comment> getComments(Long tweetId);

    /**
     * this method return a flux of ids of tweets, that has a comment having that hashtag
     * @param hashtag
     * @return
     */
    Flux<Long> getAllTweetIdsByCommentHashtag(String hashtag);

    /**
     * this method return a flux of ids of tweets, that has a comment containing that word
     * @param word
     * @return
     */
    Flux<Long> getAllTweetIdsWhereCommentContains(String word);

    /**
     * this method creates a comment and it to a tweet
     * @param comment
     * @return
     */
    Mono<Comment> createComment(Comment comment);

    /**
     * this method deletes a comment from a tweet
     * @param commentId
     * @return
     */
    Mono<Void> deleteComment(Long commentId);

    /**
     *
     * @param tweetId
     * @return
     */
    Mono<Void> deleteAllCommentsByTweetId(Long tweetId);

    /**
     * this method adds a like to a comment
     * @param commentId
     * @param userId
     * @return
     */
    Mono<Void> addLike(Long commentId, Long userId);

    /**
     * this method deletes a like from a comment
     * @param commentId
     * @param userId
     * @return
     */
    Mono<Void> deleteLike(Long commentId, Long userId);
}

package hbrs.projektseminar.tweetservice.repository;

import hbrs.projektseminar.tweetservice.model.Comment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface CommentRepository extends ReactiveCrudRepository<Comment, Long> {

    /**
     * Methods for comment table
     */

    Flux<Comment> findByTweetId(Long tweetId);

    Flux<Comment> findAllByTextContainingIgnoreCase(String word);

    @Query("SELECT tweet_id FROM comment WHERE id IN (:ids)")
    Flux<Long> findAllTweetIdsByCommentIdsIn(List<Long> ids);

    /**
     * Methods for comment_like table
     */

    @Query("SELECT user_id FROM comment_like WHERE comment_id = :id")
    Flux<Long> getAllLikesByCommentId(Long id);

    @Query("DELETE FROM comment_like WHERE comment_id = :commentId AND user_id = :userId")
    Mono<Void> deleteByCommentIdAndUserId(Long commentId, Long userId);

    @Query("DELETE FROM comment_like WHERE comment_id = :id")
    Mono<Void> deleteAllLikesByCommentId(Long id);

    @Query("SELECT COUNT(user_id) FROM comment_like WHERE comment_id = :id")
    Mono<Long> countByCommentId(Long id);

    @Query("INSERT INTO comment_like (comment_id, user_id) VALUES (:commentId, :userId)")
    Mono<Void> addLikeToComment(Long tweetId, Long userId);
}

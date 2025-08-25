package hbrs.projektseminar.tweetservice.controller;

import hbrs.projektseminar.tweetservice.model.Comment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/comments")
public interface CommentController {

    /**
     * this method creates a comment and it to a tweet
     * @param comment
     * @return
     */
    @PostMapping(path = "/")
    Mono<ResponseEntity<Comment>> createComment(@RequestBody Comment comment);

    /**
     * this method deletes a comment from a tweet
     * @param commentId
     * @return
     */
    @DeleteMapping(path = "{id}")
    Mono<ResponseEntity<Void>> deleteComment(@PathVariable("id") Long commentId);

    /**
     * this method adds a like to a comment
     * @param commentId
     * @param userId
     * @return
     */
    @PostMapping(path = "{id}/like")
    Mono<ResponseEntity<Void>> addLike(@PathVariable("id") Long commentId,
                                              @RequestParam(name = "user") Long userId);

    /**
     * this method deletes a like from a comment
     * @param commentId
     * @param userId
     * @return
     */
    @DeleteMapping(path = "{id}/like")
    Mono<ResponseEntity<Void>> deleteLike(@PathVariable("id") Long commentId,
                                                 @RequestParam(name = "user") Long userId);
}

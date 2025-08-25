package hbrs.projektseminar.tweetservice.controller;

import hbrs.projektseminar.tweetservice.handler.ErrorHandler;
import hbrs.projektseminar.tweetservice.model.Comment;
import hbrs.projektseminar.tweetservice.service.CommentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@Slf4j
public class CommentControllerImpl implements CommentController {

    @Autowired
    private final CommentService commentService;

    @Override
    public Mono<ResponseEntity<Comment>> createComment(Comment comment) {
        return commentService.createComment(comment)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c))
                .onErrorResume(ErrorHandler::handleError);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteComment(Long commentId) {
        return commentService.deleteComment(commentId)
                .map(ResponseEntity::ok)
                .onErrorResume(ErrorHandler::handleError);
    }

    @Override
    public Mono<ResponseEntity<Void>> addLike(Long commentId, Long userId) {
        return commentService.addLike(commentId,userId)
                .map(ResponseEntity::ok)
                .onErrorResume(ErrorHandler::handleError);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteLike(Long commentId, Long userId) {
        return commentService.deleteLike(commentId,userId)
                .map(ResponseEntity::ok)
                .onErrorResume(ErrorHandler::handleError);
    }
}

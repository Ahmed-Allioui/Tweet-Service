package hbrs.projektseminar.tweetservice.populator;

import hbrs.projektseminar.tweetservice.model.Comment;
import hbrs.projektseminar.tweetservice.repository.CommentRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class CommentPopulatorImpl implements CommentPopulator{

    @Autowired
    private final CommentRepository commentRepository;

    @Override
    public Mono<Comment> getLikes(Comment comment){
        return commentRepository
                .getAllLikesByCommentId(comment.getId())
                .collectList()
                .flatMap(likes -> {
                    comment.setLikedBy(likes);
                    return Mono.just(comment);
                });
    }
}

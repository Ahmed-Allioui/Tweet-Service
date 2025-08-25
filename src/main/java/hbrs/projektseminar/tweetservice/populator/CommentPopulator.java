package hbrs.projektseminar.tweetservice.populator;

import hbrs.projektseminar.tweetservice.model.Comment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface CommentPopulator {

    Mono<Comment> getLikes(Comment comment);

}

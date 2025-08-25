package hbrs.projektseminar.tweetservice.handler;

import hbrs.projektseminar.tweetservice.exceptions.TweetNotFoundException;
import hbrs.projektseminar.tweetservice.exceptions.UnauthorizedDeleteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

@Slf4j
public class ErrorHandler {

    public static  <T> Mono<ResponseEntity<T>> handleError(Throwable e){
        if(e instanceof TweetNotFoundException){
            log.warn("A TweetNotFoundException thrown which resulted in a 404 response: {}", e.getMessage());
            return Mono.just(ResponseEntity.notFound().build());
        }
        if(e instanceof IllegalArgumentException) {
            log.warn("A IllegalArgumentException thrown which resulted in a 400 response: {}", e.getMessage());
            return Mono.just(ResponseEntity.badRequest().build());
        }
        if(e instanceof UnauthorizedDeleteException) {
            log.warn("A UnauthorizedDeleteException thrown which resulted in a 401 response: {}", e.getMessage());
            return Mono.just(ResponseEntity.status(401).build());
        }
        log.warn("An internal error occurred which resulted in a 500 response: {}", e.getMessage());
        return Mono.just(ResponseEntity.internalServerError().build());
    }

}

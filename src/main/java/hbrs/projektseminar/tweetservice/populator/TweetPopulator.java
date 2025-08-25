package hbrs.projektseminar.tweetservice.populator;

import hbrs.projektseminar.tweetservice.model.Tweet;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface TweetPopulator {
    /**
     * @param tweet to be used in .flatMap of a tweetMono
     * @return Mono of same tweet with populated comments field
     */
    Mono<Tweet> getComments(Tweet tweet);
    /**
     * @param tweet to be used in .flatMap of a tweetMono
     * @return Mono of same tweet with populated likeBy field
     */
    Mono<Tweet> getLikes(Tweet tweet);

    /**
     *
     * @param tweet tweet to be used in .flatMap of a tweetMono
     * @return Mono of same tweet with populated likeBy field
     */
    Mono<Tweet> getPictures(Tweet tweet);
    /**
     * @param tweet to be used in .flatMap of a tweetMono
     * @return Mono of same tweet with retweet populated,
     * internally withComments and withLikes are called on retweet,
     * so it will have comments and likes on.
     */
    Mono<Tweet> getRetweet(Tweet tweet);

    /**
     * get all attributes for a tweet
     * @param tweet
     * @return
     */
    Mono<Tweet> getAll(Tweet tweet);
}

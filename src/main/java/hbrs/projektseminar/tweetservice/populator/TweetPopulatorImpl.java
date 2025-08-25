package hbrs.projektseminar.tweetservice.populator;

import hbrs.projektseminar.tweetservice.model.Tweet;
import hbrs.projektseminar.tweetservice.repository.CommentRepository;
import hbrs.projektseminar.tweetservice.repository.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TweetPopulatorImpl implements TweetPopulator{

    @Autowired
    private CommentPopulator commentPopulator;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TweetRepository tweetRepository;


    @Override
    public Mono<Tweet> getComments(Tweet tweet){
        return commentRepository
                .findByTweetId(tweet.getId())
                .flatMap(commentPopulator::getLikes)
                .collectList()
                .flatMap(comments -> { //Flux<List<Comment>>
                    tweet.setComments(comments);
                    return Mono.just(tweet); //Mono<Tweet>
                });
    }

    @Override
    public Mono<Tweet> getLikes(Tweet tweet){
        return tweetRepository
            .getAllLikesByTweetId(tweet.getId()).collectList()
            .flatMap(likes -> {
                tweet.setLikedBy(likes);
                return Mono.just(tweet);
            });
    }

    @Override
    public Mono<Tweet> getPictures(Tweet tweet){
        return tweetRepository
                .getAllPicturesByTweetId(tweet.getId()).collectList()
                .flatMap(pictures -> {
                    tweet.setPictures(pictures);
                    return Mono.just(tweet);
                });
    }

    @Override
    public Mono<Tweet> getRetweet(Tweet tweet){
        if(tweet.getRetweetId() == null){
            return Mono.just(tweet);
        }
        return tweetRepository
            .findById(tweet.getRetweetId())
            .flatMap(this::getComments)
            .flatMap(this::getLikes)
            .flatMap(retweet -> {
                tweet.setRetweet(retweet);
                return Mono.just(tweet);
            });
    }

    @Override
    public Mono<Tweet> getAll(Tweet tweet) {
        return getLikes(tweet)
                .flatMap(this::getRetweet)
                .flatMap(this::getComments)
                .flatMap(this::getPictures);
    }
}

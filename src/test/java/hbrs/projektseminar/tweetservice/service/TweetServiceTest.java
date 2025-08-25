package hbrs.projektseminar.tweetservice.service;

import hbrs.projektseminar.tweetservice.factory.UriFactory;
import hbrs.projektseminar.tweetservice.model.Tweet;
import hbrs.projektseminar.tweetservice.populator.TweetPopulator;
import hbrs.projektseminar.tweetservice.repository.TweetRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TweetServiceTest {

    @Mock private TweetRepository tweetRepository;

    @Mock private CommentService commentService;

    @Mock private WebClient client;

    @Mock private TweetPopulator tweetPopulator;

    @Mock private DiscoveryClient discoveryClient;

    @InjectMocks UriFactory uriFactory;

    private TweetService underTest;

    @BeforeEach
    void setUp() {
//        UriFactory uriFactory = UriFactory.builder()
//                .discovery(discoveryClient)
//                .followServiceName("follow-service")
//                .hashtagServiceName("hashtag-service")
//                .getFollowsVar("follow-var")
//                .tweetByHashtagVar("tweet-by-hashtag")
//                .build();
        underTest = TweetServiceImpl.builder()
                .commentService(commentService)
                .tweetRepository(tweetRepository)
                .client(client)
                .tweetPopulator(tweetPopulator)
                .uriFactory(uriFactory)
                .build();
    }

    @AfterEach
    void tearDown() {
        underTest = null;
    }

    @Test
    void getAllUserTweetsCallsTheRightMethods() {
        // given
        Long userId = 1L;
        Flux<Tweet> tweets = Flux.just(Tweet.builder()
                .id(1L)
                .text("Some text")
                .authorId(1L)
                .build());
        given(tweetRepository.findAllByAuthorId(userId)).willReturn(tweets);

        // when
        underTest.getAllUserTweets(userId);
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);

        // then
        verify(tweetRepository).findAllByAuthorId(userIdCaptor.capture());
        assertThat(userIdCaptor.getValue()).isEqualTo(userId);
    }

    @Test
    void getAllUserTweetsReturnTheRightResult() {
        // given
        Long userId = 1L;
        Tweet tweet = Tweet.builder()
                .id(1L)
                .text("Some text")
                .build();
        Flux<Tweet> tweets = Flux.just(tweet);
        ArgumentCaptor<Tweet> tweetCaptor = ArgumentCaptor.forClass(Tweet.class);
        given(tweetRepository.findAllByAuthorId(userId)).willReturn(tweets);
        given(tweetPopulator.getAll(tweetCaptor.capture())).willReturn(Mono.just(tweet));

        // when
        Flux<Tweet> result = underTest.getAllUserTweets(userId);
        result.subscribe();

        // then
        assertThat(result).isNotNull();
        Iterator<Tweet> ti = tweets.toIterable().iterator();
        Iterator<Tweet> ri = result.toIterable().iterator();
        while(ri.hasNext() && ti.hasNext()) {
            assertThat(ri.next()).isEqualTo(ti.next());
        }
        assertThat(ri.hasNext()).isFalse();
        assertThat(ti.hasNext()).isFalse();
    }

    @Test
    void getAllUserFollowingsTweetsCallsTheRightMethod() {
        //given
        Long userid = 1L;
        List<Long> followings = Arrays.asList(3L, 2L);
        Tweet tweet1 = Tweet.builder()
                .id(1L)
                .text("Some text")
                .authorId(2L)
                .build();
        Flux<Tweet> tweets = Flux.just(tweet1);
        ArgumentCaptor<Tweet> tweetCaptor = ArgumentCaptor.forClass(Tweet.class);
        ArgumentCaptor<List<Long>> followingsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

        final var uriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        final var requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        final var responseSpecMock = mock(WebClient.ResponseSpec.class);

        given(tweetRepository.findAllByAuthorIdIn(followingsCaptor.capture())).willReturn(tweets);
        given(tweetPopulator.getAll(tweetCaptor.capture())).willReturn(Mono.just(tweet1));
        given(discoveryClient.getInstances(stringArgumentCaptor.capture())).willReturn(Arrays.asList(new DefaultServiceInstance()));

        //when
        Mockito.when(client.get()).thenReturn(uriSpecMock);
        Mockito.when(uriSpecMock.uri(ArgumentMatchers.<URI>notNull())).thenReturn(requestBodySpecMock);
        Mockito.when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
        Mockito.when(responseSpecMock.bodyToFlux(ArgumentMatchers.<Class<Long>>notNull())).thenReturn(Flux.fromIterable(followings));
        uriFactory.setFollowServiceName("name");
        uriFactory.setGetFollowsVar("var");
        underTest.getAllUserFollowingsTweets(userid).map(tweet -> {
            assertThat(tweet.getId()).isEqualTo(tweet1.getId());
            return tweet;
        }).then().subscribe();

        //then
        Mockito.verify(tweetRepository).findAllByAuthorIdIn(followingsCaptor.capture());
    }

    @Test
    void getAllTweetsByHashtagCallsRightMethod() {
        //given
        String string = "hashtag";
        Tweet tweet = Tweet.builder()
                .id(1L)
                .text("Some text")
                .authorId(2L)
                .build();
        Flux<Tweet> tweets = Flux.just(tweet);
        List<Long> tweetids = Arrays.asList(1L);
        given(tweetRepository.findAllByIdIn(tweetids)).willReturn(tweets);
        ArgumentCaptor<List<Long>> tweetIdCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Tweet> tweetCaptor = ArgumentCaptor.forClass(Tweet.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        given(tweetPopulator.getAll(tweetCaptor.capture()))
                .willReturn(Mono.just(tweet));
        given(discoveryClient.getInstances(stringArgumentCaptor.capture())).willReturn(Arrays.asList(new DefaultServiceInstance()));

        final var uriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        final var requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        final var responseSpecMock = mock(WebClient.ResponseSpec.class);

        //when
        Mockito.when(client.get()).thenReturn(uriSpecMock);
        Mockito.when(uriSpecMock.uri(ArgumentMatchers.<URI>notNull())).thenReturn(requestBodySpecMock);
        Mockito.when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
        Mockito.when(responseSpecMock.bodyToFlux(ArgumentMatchers.<Class<Long>>notNull())).thenReturn(Flux.fromIterable(tweetids));
        uriFactory.setHashtagServiceName("name");
        uriFactory.setTweetByHashtagVar("var");
        underTest.getAllTweetsByHashtag(string).map(result -> {
            assertThat(tweet.getId()).isEqualTo(result.getId());
            return tweet;
        }).then().subscribe();

        //then
        verify(tweetRepository).findAllByIdIn(tweetIdCaptor.capture());
        List<Long> followingsValue = tweetIdCaptor.getValue();
        assertThat(followingsValue.get(0)).isEqualTo(1L);
    }


    @Test
    void getAllTweetsByCommentsHashtagCallsRightMethod() {
        //given
        String hashtag = "hashtag";
        Tweet tweet = Tweet.builder()
                .id(1L)
                .text("Some text")
                .authorId(2L)
                .build();
        Flux<Long> tweetIds = Flux.just(1L);
        ArgumentCaptor<List<Long>> tweetIdCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Tweet> tweetCaptor = ArgumentCaptor.forClass(Tweet.class);
        given(commentService.getAllTweetIdsByCommentHashtag(hashtag)).willReturn(tweetIds);
        given(tweetRepository.findAllByIdIn(tweetIdCaptor.capture())).willReturn(Flux.just(tweet));
        given(tweetPopulator.getAll(tweetCaptor.capture())).willReturn(Mono.just(tweet));

        //when
        underTest.getAllTweetsByCommentsHashtag(hashtag).map(result -> {
                    assertThat(tweet.getId()).isEqualTo(result.getId());
                    return tweet;
                }).then().subscribe();;

        //then
        verify(tweetRepository).findAllByIdIn(tweetIdCaptor.capture());
    }

    @Test
    void getAllTweetsContainingCallsRightMethod() {
        //given
        String string = "text";
        Flux<Tweet> tweets = Flux.just(Tweet.builder()
                .id(1L)
                .text("Some text")
                .authorId(1L)
                .build());
        given(tweetRepository.findAllByTextContainingIgnoreCase(string)).willReturn(tweets);

        //when
        underTest.getAllTweetsContaining(string);
        ArgumentCaptor<String> stringArgumentCaptorIdCaptor = ArgumentCaptor.forClass(String.class);

        // then
        verify(tweetRepository).findAllByTextContainingIgnoreCase(stringArgumentCaptorIdCaptor.capture());
        assertThat(stringArgumentCaptorIdCaptor.getValue()).isEqualTo(string);
    }

    @Test
    void getAllTweetsContainingReturnRightResult(){
        // given
        String string = "text";
        Tweet tweet = Tweet.builder()
                .id(1L)
                .text("Some text")
                .build();
        Flux<Tweet> tweets = Flux.just(tweet);
        ArgumentCaptor<Tweet> tweetCaptor = ArgumentCaptor.forClass(Tweet.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        given(tweetRepository.findAllByTextContainingIgnoreCase(string)).willReturn(tweets);
        given(tweetPopulator.getAll(tweetCaptor.capture())).willReturn(Mono.just(tweet));

        // when
        Flux<Tweet> result = underTest.getAllTweetsContaining(string);
        result.subscribe();

        // then
        assertThat(result).isNotNull();
        Iterator<Tweet> ti = tweets.toIterable().iterator();
        Iterator<Tweet> ri = result.toIterable().iterator();
        while(ri.hasNext() && ti.hasNext()) {
            assertThat(ri.next()).isEqualTo(ti.next());
        }
        assertThat(ri.hasNext()).isFalse();
        assertThat(ti.hasNext()).isFalse();
    }

    @Test
    void getAllTweetsByCommentsContainingCallsRightMethod() {
        //given
        String hashtag = "text";
        Flux<Tweet> tweets = Flux.just(Tweet.builder()
                .id(1L)
                .text("Some text")
                .authorId(1L)
                .build());
        Flux<Long> tweetIds = Flux.just(1L);
        List<Long> tweetIdsList = Arrays.asList(1L);
        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Tweet> tweetCaptor = ArgumentCaptor.forClass(Tweet.class);
        given(commentService.getAllTweetIdsWhereCommentContains(hashtag)).willReturn(tweetIds);
        given(tweetRepository.findAllByIdIn(idsCaptor.capture())).willReturn(tweets);
        given(tweetPopulator.getAll(tweetCaptor.capture())).willReturn(tweets.next());

        //when
        underTest.getAllTweetsByCommentsContaining(hashtag).subscribe();
        ArgumentCaptor<List<Long>> tweetIdCaptor = ArgumentCaptor.forClass(List.class);

        //then
        verify(tweetRepository).findAllByIdIn(tweetIdCaptor.capture());
        assertThat(tweetIdCaptor.getValue().get(0)).isEqualTo(tweetIdsList.get(0));
    }

    @Test
    void getAllTweetsByCommentsContainingReturnRightResult() {
        //given
        List<Long> image = Arrays.asList(0L);
        String string = "text";
        Tweet tweet1 = Tweet.builder()
                .id(1L)
                .text("Some text")
                .authorId(2L)
                .pictures(image)
                .build();
        Flux<Tweet> tweets = Flux.just(tweet1);
        Flux<Long> tweetIds = Flux.just(1L);
        List<Long> tweetIdsList = Arrays.asList(1L);
        ArgumentCaptor<Tweet> tweetCaptor = ArgumentCaptor.forClass(Tweet.class);

        given(tweetRepository.findAllByIdIn(tweetIdsList)).willReturn(tweets);
        given(commentService.getAllTweetIdsWhereCommentContains(string)).willReturn(tweetIds);
        given(tweetPopulator.getAll(tweetCaptor.capture())).willReturn(Mono.just(tweet1));

        //when
        Flux<Tweet> result = underTest.getAllTweetsByCommentsContaining(string);
        result.subscribe();

        //then
        assertThat(result).isNotNull();
        Iterator<Tweet> ti = tweets.toIterable().iterator();
        Iterator<Tweet> ri = result.toIterable().iterator();
        while(ri.hasNext() && ti.hasNext()) {
            assertThat(ri.next()).isEqualTo(ti.next());
        }
        assertThat(ri.hasNext()).isFalse();
        assertThat(ti.hasNext()).isFalse();
    }

    @Test
    void getTweetCallsTheRightMethods() {
        //given
        Long tweetid = 1L;
        Mono<Tweet> tweet = Mono.just(Tweet.builder().
                id(tweetid).
                text("text").
                build());
        given(tweetRepository.findById(tweetid)).willReturn(tweet);
        //when
        underTest.getTweet(tweetid);
        ArgumentCaptor<Long> tweetidcaptor = ArgumentCaptor.forClass(Long.class);
        // then
        verify(tweetRepository).findById(tweetidcaptor.capture());
        assertThat(tweetidcaptor.getValue()).isEqualTo(tweetid);
    }

    @Test
    void getTweetReturnRightResult() {
        // given;
        Long tweetid = 1L;
        Tweet tweet = Tweet.builder()
                .id(1L)
                .text("text")
                .build();
        Mono<Tweet> tweets = Mono.just(tweet);
        ArgumentCaptor<Tweet> tweetCaptor = ArgumentCaptor.forClass(Tweet.class);
        given(tweetRepository.findById(tweetid)).willReturn(tweets);
        given(tweetPopulator.getAll(tweetCaptor.capture()))
                .willReturn(Mono.just(tweet));

        //when
        Mono<Tweet> result = underTest.getTweet(tweetid);
        result.subscribe();

        //then
        assertThat(result).isNotNull();
        Tweet resulttweet = result.block();
        assertThat(resulttweet).isEqualTo(tweet);
    }

    @Test
    void createTweetCallsTheRightMethods() {
        //given
        Tweet tweet = Tweet.builder().
                id(1L).
                text("text").
                authorId(1L)
                .build();

        Mono<Tweet> tweetMono= Mono.just(tweet);
        given(tweetRepository.save(tweet)).willReturn(tweetMono);

        //when
        underTest.createTweet(tweet);
        ArgumentCaptor<Tweet> tweetCaptor = ArgumentCaptor.forClass(Tweet.class);

        //then
        verify(tweetRepository).save(tweetCaptor.capture());
        assertThat(tweetCaptor.getValue()).isEqualTo(tweet);
    }

    @Test
    void createTweetReturnRightResult() {
        //given
        Tweet tweet = Tweet.builder().
                id(1L)
                .text("text")
                .authorId(1L)
                .build();
        Mono<Tweet> tweetMono = Mono.just(tweet);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        given(tweetRepository.save(tweet)).willReturn(tweetMono);
        given(discoveryClient.getInstances(stringArgumentCaptor.capture())).willReturn(Arrays.asList(new DefaultServiceInstance()));

        final var uriSpecMock = mock(WebClient.RequestBodyUriSpec.class);
        final var requestHeaderSpecMock = mock(WebClient.RequestHeadersSpec.class);
        final var requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        final var responseSpecMock = mock(WebClient.ResponseSpec.class);
        //when
        Mono<Tweet> resultMono = underTest.createTweet(tweet);
        Mockito.when(client.post()).thenReturn(uriSpecMock);
        Mockito.when(uriSpecMock.uri(ArgumentMatchers.<URI>notNull())).thenReturn(requestBodySpecMock);
        Mockito.when(requestBodySpecMock.contentType(notNull())).thenReturn(requestBodySpecMock);
        Mockito.when(requestBodySpecMock.bodyValue(notNull())).thenReturn(requestHeaderSpecMock);
        Mockito.when(requestHeaderSpecMock.retrieve()).thenReturn(responseSpecMock);
        Mockito.when(responseSpecMock.toBodilessEntity()).thenReturn(Mono.empty());
        resultMono.subscribe();

        //then
        assertThat(resultMono).isNotNull();
        Tweet result = resultMono.block();
        assertThat(result).isEqualTo(tweet);
    }

    @Test
    void addLikeCallsTheRightMethods() {
        //given
        Long tweetid = 1L;
        Long userid= 1L;
        ArgumentCaptor<Long>  tweetidcaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long>  useridcaptor = ArgumentCaptor.forClass(Long.class);
        given(tweetRepository.addLikeToTweet(tweetid,userid)).willReturn(Mono.empty());

        //when
        Mockito.when(tweetRepository.existsById(tweetid)).thenReturn(Mono.just(true));
        Mockito.when(tweetRepository.addLikeToTweet(tweetid, userid)).thenReturn(Mono.empty());
        Mockito.when(tweetRepository.countByTweetId(tweetid)).thenReturn(Mono.just(1L));
        underTest.addLike(tweetid,userid).subscribe();


        //then
        verify(tweetRepository).addLikeToTweet(tweetidcaptor.capture(),useridcaptor.capture());
        assertThat(tweetidcaptor.getValue()).isEqualTo(tweetid);
        assertThat(useridcaptor.getValue()).isEqualTo(userid);
    }

    @Test
    void deleteTweetCallsTheRightMethods() {
        //given
        Long id = 1L;
        ArgumentCaptor<Long> tweetIdCaptor = ArgumentCaptor.forClass(long.class);

        //when
        Mockito.when(tweetRepository.deleteAllLikesByTweetId(tweetIdCaptor.capture())).thenReturn(Mono.empty());
        Mockito.when(commentService.deleteAllCommentsByTweetId(tweetIdCaptor.capture())).thenReturn(Mono.empty());
        Mockito.when(tweetRepository.deleteAllPicturesByTweetId(tweetIdCaptor.capture())).thenReturn(Mono.empty());
        Mockito.when(tweetRepository.setRetweetIdsNullByTweetId(tweetIdCaptor.capture())).thenReturn(Mono.empty());
        Mockito.when(tweetRepository.deleteById(tweetIdCaptor.capture())).thenReturn(Mono.empty());
        underTest.deleteTweet(id).subscribe();

        //then
        verify(tweetRepository).deleteAllLikesByTweetId(tweetIdCaptor.capture());
        assertThat(tweetIdCaptor.getValue()).isEqualTo(id);
        verify(commentService).deleteAllCommentsByTweetId(tweetIdCaptor.capture());
        assertThat(tweetIdCaptor.getValue()).isEqualTo(id);
        verify(tweetRepository).setRetweetIdsNullByTweetId(tweetIdCaptor.capture());
        assertThat(tweetIdCaptor.getValue()).isEqualTo(id);
        verify(tweetRepository).deleteById(tweetIdCaptor.capture());
        assertThat(tweetIdCaptor.getValue()).isEqualTo(id);
    }

    @Test
    void deleteLikeCallsTheRightMethods() {
        //given
        Long tweetid = 1L;
        Long userid= 1L;
        ArgumentCaptor<Long>  tweetidcaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long>  useridcaptor = ArgumentCaptor.forClass(Long.class);
        given(tweetRepository.deleteByTweetIdAndUserId(tweetid,userid)).willReturn(Mono.empty());

        //when
        underTest.deleteLike(tweetid,userid).subscribe();

        //then
        verify(tweetRepository).deleteByTweetIdAndUserId(tweetidcaptor.capture(),useridcaptor.capture());
        assertThat(tweetidcaptor.getValue()).isEqualTo(tweetid);
        assertThat(useridcaptor.getValue()).isEqualTo(userid);
    }

}
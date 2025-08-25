package hbrs.projektseminar.tweetservice.controller;

import hbrs.projektseminar.tweetservice.enumeration.CommentPath;
import hbrs.projektseminar.tweetservice.enumeration.TweetPath;
import hbrs.projektseminar.tweetservice.factory.UriFactory;
import hbrs.projektseminar.tweetservice.model.Tweet;
import hbrs.projektseminar.tweetservice.repository.CommentRepository;
import hbrs.projektseminar.tweetservice.repository.TweetRepository;
import hbrs.projektseminar.tweetservice.service.CommentService;
import hbrs.projektseminar.tweetservice.service.TweetServiceImpl;
import hbrs.projektseminar.tweetservice.factory.ServerUriGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = TweetController.class)
@Import(TweetServiceImpl.class)
class TweetControllerTest {

    @MockBean TweetRepository tweetRepository;

    @MockBean CommentRepository commentRepository;

    @MockBean CommentService commentService;

    @MockBean ConnectionFactoryInitializer connectionFactoryInitializer;

    @MockBean DiscoveryClient discoveryClient;

    @MockBean UriFactory uriFactory;

    @MockBean WebClient webClient;

    @Autowired private WebTestClient webTestClient;

    @Autowired private ServerUriGenerator serverUriGenerator;

    UriComponentsBuilder uriBuilder;

    @BeforeEach
    void setUp() {
        uriBuilder = serverUriGenerator.serverUri();
    }

    @AfterEach
    void tearDown() {
        uriBuilder = null;
    }

    @Test
    void getAllUserFollowingsTweets() {
        // given
        List<Long> followings = Arrays.asList(2L, 3L);
        Long userId = 1L;
        ArgumentCaptor<Long> tweetIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<List<Long>> followingsCaptor = ArgumentCaptor.forClass(List.class);

        final var uriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        final var requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        final var responseSpecMock = mock(WebClient.ResponseSpec.class);

        // when
        when(tweetRepository.findAllByAuthorIdIn(followingsCaptor.capture())).thenReturn(Flux.empty());
        when(commentRepository.findByTweetId(notNull())).thenReturn(Flux.empty());
        when(tweetRepository.getAllLikesByTweetId(notNull())).thenReturn(Flux.empty());
        when(tweetRepository.findById(tweetIdCaptor.capture())).thenReturn(Mono.empty());
        when(uriFactory.getFollowsUri()).thenReturn(UriComponentsBuilder.newInstance());

        when(webClient.get()).thenReturn(uriSpecMock);
        when(uriSpecMock.uri(ArgumentMatchers.<URI>notNull())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToFlux(ArgumentMatchers.<Class<Long>>notNull())).thenReturn(Flux.fromIterable(followings));

        webTestClient.get()
                .uri(uriBuilder
                        .path(TweetPath.GET_ALL_FOLLOWINGS.toString())
                        .queryParam("user", userId)
                        .build()
                        .toUri())
                .exchange()
                .expectStatus().isOk();
        // then
        List<Long> followingsValue = followingsCaptor.getValue();
        assertThat(followingsValue.get(0)).isEqualTo(2L);
        assertThat(followingsValue.get(1)).isEqualTo(3L);
        verify(tweetRepository, times(1)).findAllByAuthorIdIn(followingsValue); //method is called one time
    }

    @Test
    void getTweet() {
    }

    @Test
    void createTweetReturnCreatedStatusAndCallServiceOneTime() {
        // given
        Tweet tweet = Tweet.builder()
                .text("here is a comment")
                .authorId(1L)
                .build();
        ArgumentCaptor<Tweet> tweetCaptor = ArgumentCaptor.forClass(Tweet.class);

        final var uriSpecMock = mock(WebClient.RequestBodyUriSpec.class);
        final var requestHeaderSpecMock = mock(WebClient.RequestHeadersSpec.class);
        final var requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        final var responseSpecMock = mock(WebClient.ResponseSpec.class);

        // when
        Mockito.when(tweetRepository.save(tweetCaptor.capture())).thenReturn(Mono.just(tweet));
        Mockito.when(commentRepository.save(notNull())).thenReturn(Mono.empty());

        when(webClient.post()).thenReturn(uriSpecMock);
        when(uriSpecMock.uri(ArgumentMatchers.<URI>notNull())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.contentType(notNull())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.bodyValue(notNull())).thenReturn(requestHeaderSpecMock);
        when(requestHeaderSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.toBodilessEntity()).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder.path(TweetPath.CREATE.toString()).build().toUri())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tweet)
                .exchange()
                .expectStatus().isCreated();

        // then
        Tweet tweetValue = tweetCaptor.getValue();
        verify(tweetRepository, times(1)).save(tweetValue); //method is called one time
        assertThat(tweetValue.getText()).isEqualTo(tweet.getText());    // verify that the text is correct
        assertThat(tweetValue.getAuthorId()).isEqualTo(tweet.getAuthorId());    // verify that the author id is correct
    }

    @Test
    void createTweetReturnBadRequestIfTweetHasNoAuthorId() {

        // given
        Tweet tweet = Tweet.builder().build();
        ArgumentCaptor<Tweet> tweetArgumentCaptor = ArgumentCaptor.forClass(Tweet.class);

        // when
        Mockito.when(tweetRepository.save(tweetArgumentCaptor.capture())).thenReturn(Mono.just(tweet));
        webTestClient.post()
                .uri(uriBuilder.path(TweetPath.CREATE.toString()).build().toUri())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tweet)
                .exchange()
                .expectStatus().isBadRequest();

        // then
    }

    @Test
    void addLikeReturnsNotFoundIfTweetDoesNotExists() {

        // given
        ArgumentCaptor<Long> tweetIdCaptor = ArgumentCaptor.forClass(Long.class);

        // when
        Mockito.when(tweetRepository.existsById(tweetIdCaptor.capture())).thenReturn(Mono.just(false));
        webTestClient.post()
                .uri(uriBuilder
                        .path(TweetPath.ADD_LIKE.toString())
                        .queryParam("user", "1")
                        .buildAndExpand(1L)
                        .toUri())
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
        // then
    }

    @Test
    void addLikeCallTheRightMethods() {

        // given
        Long userId = 1L, tweetId = 1L;
        ArgumentCaptor<Long> tweetIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);

        final var mock = mock(WebClient.class);
        final var uriSpecMock = mock(WebClient.RequestBodyUriSpec.class);
        final var requestHeaderSpecMock = mock(WebClient.RequestHeadersSpec.class);
        final var requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        final var responseSpecMock = mock(WebClient.ResponseSpec.class);

        // when
        Mockito.when(tweetRepository.existsById(tweetIdCaptor.capture())).thenReturn(Mono.just(true));
        Mockito.when(tweetRepository.addLikeToTweet(tweetIdCaptor.capture(), userIdCaptor.capture()))
                .thenReturn(Mono.empty());
        Mockito.when(tweetRepository.countByTweetId(tweetIdCaptor.capture())).thenReturn(Mono.just(1L));

        when(mock.post()).thenReturn(uriSpecMock);
        when(uriSpecMock.uri(ArgumentMatchers.<String>notNull())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.contentType(notNull())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.bodyValue(notNull())).thenReturn(requestHeaderSpecMock);
        when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(ArgumentMatchers.<Class<String>>notNull()))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder
                        .path(TweetPath.ADD_LIKE.toString())
                        .queryParam("user", userId)
                        .buildAndExpand(tweetId)
                        .toUri())
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        // then
        verify(tweetRepository, times(1)).addLikeToTweet(
                tweetIdCaptor.capture(),
                userIdCaptor.capture());
        assertThat(userIdCaptor.getValue()).isEqualTo(userId);
        assertThat(tweetIdCaptor.getValue()).isEqualTo(tweetId);
    }

    @Test
    void deleteTweetCallTheRightMethodsInTheCorrectOrder() {

        // given
        Long id = 1L;
        Tweet.builder().build();
        ArgumentCaptor<Long> tweetIdCaptor = ArgumentCaptor.forClass(Long.class);

        // when
        Mockito.when(tweetRepository.deleteAllLikesByTweetId(tweetIdCaptor.capture())).thenReturn(Mono.empty());
        Mockito.when(tweetRepository.deleteAllPicturesByTweetId(tweetIdCaptor.capture())).thenReturn(Mono.empty());
        Mockito.when(commentService.deleteAllCommentsByTweetId(tweetIdCaptor.capture())).thenReturn(Mono.empty());
        Mockito.when(tweetRepository.setRetweetIdsNullByTweetId(tweetIdCaptor.capture())).thenReturn(Mono.empty());
        Mockito.when(tweetRepository.deleteById(tweetIdCaptor.capture())).thenReturn(Mono.empty());
        webTestClient.delete()
                .uri(uriBuilder.path(TweetPath.DELETE.toString()).buildAndExpand(id).toUri())
                .exchange()
                .expectStatus().isOk();
        InOrder inOrder = Mockito.inOrder(tweetRepository, commentService);

        // then
        Long idValue = tweetIdCaptor.getValue();
        verify(tweetRepository, times(1)).deleteAllLikesByTweetId(idValue); //method is called one time
        verify(commentService, times(1)).deleteAllCommentsByTweetId(idValue); //method is called one time
        verify(tweetRepository, times(1)).setRetweetIdsNullByTweetId(idValue); //method is called one time
        verify(tweetRepository, times(1)).deleteById(idValue); //method is called one time
        // check the order
        inOrder.verify(tweetRepository).deleteAllLikesByTweetId(idValue);
        inOrder.verify(commentService).deleteAllCommentsByTweetId(idValue);
        inOrder.verify(tweetRepository).setRetweetIdsNullByTweetId(idValue);
        inOrder.verify(tweetRepository).deleteById(idValue);
    }

    @Test
    void deleteTweetReturnBadRequestIfTweetIdIsNull() {

        // given
        ArgumentCaptor<Long> tweetIdCaptor = ArgumentCaptor.forClass(Long.class);

        // when
        Mockito.when(tweetRepository.deleteAllLikesByTweetId(tweetIdCaptor.capture())).thenReturn(Mono.empty());
        Mockito.when(commentService.deleteAllCommentsByTweetId(tweetIdCaptor.capture())).thenReturn(Mono.empty());
        Mockito.when(tweetRepository.setRetweetIdsNullByTweetId(tweetIdCaptor.capture())).thenReturn(Mono.empty());
        Mockito.when(tweetRepository.deleteById(tweetIdCaptor.capture())).thenReturn(Mono.empty());
        webTestClient.delete()
                .uri(uriBuilder.path(CommentPath.DELETE.toString()).buildAndExpand("a").toUri())
                .exchange()
                .expectStatus().isBadRequest();

        // then
    }

    @Test
    void unlikeLike() {
        // given
        Long userId = 1L, commentId = 1L;
        ArgumentCaptor<Long> tweetIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);

        final var mock = Mockito.mock(WebClient.class);
        final var uriSpecMock = Mockito.mock(WebClient.RequestBodyUriSpec.class);
        final var requestHeaderSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
        final var requestBodySpecMock = Mockito.mock(WebClient.RequestBodySpec.class);
        final var responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);

        // when
        Mockito.when(tweetRepository.deleteByTweetIdAndUserId(tweetIdCaptor.capture(), userIdCaptor.capture()))
                .thenReturn(Mono.empty());
        Mockito.when(tweetRepository.countByTweetId(tweetIdCaptor.capture())).thenReturn(Mono.just(1L));

        when(mock.post()).thenReturn(uriSpecMock);
        when(uriSpecMock.uri(ArgumentMatchers.<String>notNull())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.contentType(notNull())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.bodyValue(notNull())).thenReturn(requestHeaderSpecMock);
        when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.toBodilessEntity()).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(uriBuilder
                        .path(TweetPath.DELETE_LIKE.toString())
                        .queryParam("user", userId)
                        .buildAndExpand(commentId)
                        .toUri())
                .exchange()
                .expectStatus().isOk();

        // then
        verify(tweetRepository, times(1)).deleteByTweetIdAndUserId(
                tweetIdCaptor.capture(),
                userIdCaptor.capture());
        assertThat(userIdCaptor.getValue()).isEqualTo(userId);
        assertThat(tweetIdCaptor.getValue()).isEqualTo(commentId);
    }
}
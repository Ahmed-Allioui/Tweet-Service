package hbrs.projektseminar.tweetservice.controller;

import hbrs.projektseminar.tweetservice.factory.ServerUriGenerator;
import hbrs.projektseminar.tweetservice.enumeration.CommentPath;
import hbrs.projektseminar.tweetservice.factory.UriFactory;
import hbrs.projektseminar.tweetservice.model.Comment;
import hbrs.projektseminar.tweetservice.repository.CommentRepository;
import hbrs.projektseminar.tweetservice.repository.TweetRepository;
import hbrs.projektseminar.tweetservice.service.CommentServiceImpl;
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
import reactor.core.publisher.Mono;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = CommentController.class)
@Import(CommentServiceImpl.class)
class CommentControllerTest {

    @MockBean CommentRepository commentRepository;

    @MockBean TweetRepository tweetRepository;

    @MockBean ConnectionFactoryInitializer connectionFactoryInitializer;

    @MockBean DiscoveryClient discoveryClient;

    @MockBean UriFactory uriFactory;

    @Autowired private WebTestClient webClient;

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
    void createCommentReturnCreatedStatusAndCallServiceOneTime() {

        // given
        Comment comment = Comment.builder()
                .text("here is a comment")
                .authorId(1L)
                .build();
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        // when
        Mockito.when(commentRepository.save(commentCaptor.capture())).thenReturn(Mono.just(comment));
        webClient.post()
                .uri(uriBuilder.path(CommentPath.CREATE.toString()).build().toUri())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(comment)
                .exchange()
                .expectStatus().isCreated();

        // then
        Comment commentValue = commentCaptor.getValue();
        verify(commentRepository, times(1)).save(commentValue); //method is called one time
        assertThat(commentValue.getText()).isEqualTo(comment.getText());    // verify that the text is correct
        assertThat(commentValue.getAuthorId()).isEqualTo(comment.getAuthorId());    // verify that the author id is correct
    }

    @Test
    void createCommentReturnBadRequestIfCommentHasNoAuthorId() {

        // given
        Comment comment = Comment.builder().build();
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        // when
        Mockito.when(commentRepository.save(commentCaptor.capture())).thenReturn(Mono.just(comment));
        webClient.post()
                .uri(uriBuilder.path(CommentPath.CREATE.toString()).build().toUri())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(comment)
                .exchange()
                .expectStatus().isBadRequest();

        // then
    }

    @Test
    void deleteCommentCallTheRightMethodsInTheCorrectOrder() {

        // given
        Long id = 1L;
        ArgumentCaptor<Long> commentIdCaptor = ArgumentCaptor.forClass(Long.class);

        // when
        Mockito.when(commentRepository.deleteAllLikesByCommentId(commentIdCaptor.capture())).thenReturn(Mono.empty());
        Mockito.when(commentRepository.deleteById(commentIdCaptor.capture())).thenReturn(Mono.empty());
        webClient.delete()
                .uri(uriBuilder.path(CommentPath.DELETE.toString()).buildAndExpand(id).toUri())
                .exchange()
                .expectStatus().isOk();
        InOrder inOrder = Mockito.inOrder(commentRepository);

        // then
        Long idValue = commentIdCaptor.getValue();
        verify(commentRepository, times(1)).deleteAllLikesByCommentId(idValue); //method is called one time
        verify(commentRepository, times(1)).deleteById(idValue); //method is called one time
        // check the order
        inOrder.verify(commentRepository).deleteAllLikesByCommentId(idValue);
        inOrder.verify(commentRepository).deleteById(idValue);
    }

    @Test
    void deleteCommentReturnBadRequestIfCommentIdIsNull() {

        // given
        ArgumentCaptor<Long> commentIdCaptor = ArgumentCaptor.forClass(Long.class);

        // when
        Mockito.when(commentRepository.deleteAllLikesByCommentId(commentIdCaptor.capture())).thenReturn(Mono.empty());
        Mockito.when(commentRepository.deleteById(commentIdCaptor.capture())).thenReturn(Mono.empty());
        webClient.delete()
                .uri(uriBuilder.path(CommentPath.DELETE.toString()).buildAndExpand("a").toUri())
                .exchange()
                .expectStatus().isBadRequest();

        // then
    }

    @Test
    void addLikeReturnsNotFoundIfTweetDoesNotExists() {

        // given
        ArgumentCaptor<Long> commentIdCaptor = ArgumentCaptor.forClass(Long.class);

        // when
        Mockito.when(commentRepository.existsById(commentIdCaptor.capture())).thenReturn(Mono.just(false));
        webClient.post()
                .uri(uriBuilder
                        .path(CommentPath.ADD_LIKE.toString())
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
        Long userId = 1L, commentId = 1L;
        ArgumentCaptor<Long> commentIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);

        final var mock = mock(WebClient.class);
        final var uriSpecMock = mock(WebClient.RequestBodyUriSpec.class);
        final var requestHeaderSpecMock = mock(WebClient.RequestHeadersSpec.class);
        final var requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        final var responseSpecMock = mock(WebClient.ResponseSpec.class);

        // when
        Mockito.when(commentRepository.existsById(commentIdCaptor.capture())).thenReturn(Mono.just(true));
        Mockito.when(commentRepository.addLikeToComment(commentIdCaptor.capture(), userIdCaptor.capture()))
                .thenReturn(Mono.empty());
        Mockito.when(commentRepository.countByCommentId(commentIdCaptor.capture())).thenReturn(Mono.just(1L));

        when(mock.post()).thenReturn(uriSpecMock);
        when(uriSpecMock.uri(ArgumentMatchers.<String>notNull())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.contentType(notNull())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.bodyValue(notNull())).thenReturn(requestHeaderSpecMock);
        when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(ArgumentMatchers.<Class<String>>notNull()))
                .thenReturn(Mono.empty());

        webClient.post()
                .uri(uriBuilder
                        .path(CommentPath.ADD_LIKE.toString())
                        .queryParam("user", userId)
                        .buildAndExpand(commentId)
                        .toUri())
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        // then
        verify(commentRepository, times(1)).addLikeToComment(
                commentIdCaptor.capture(),
                userIdCaptor.capture());
        assertThat(userIdCaptor.getValue()).isEqualTo(userId);
        assertThat(commentIdCaptor.getValue()).isEqualTo(commentId);
    }

    @Test
    void deleteLike() {
        // given
        Long userId = 1L, commentId = 1L;
        ArgumentCaptor<Long> commentIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);

        final var mock = Mockito.mock(WebClient.class);
        final var uriSpecMock = Mockito.mock(WebClient.RequestBodyUriSpec.class);
        final var requestHeaderSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
        final var requestBodySpecMock = Mockito.mock(WebClient.RequestBodySpec.class);
        final var responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);

        // when
        Mockito.when(commentRepository.deleteByCommentIdAndUserId(commentIdCaptor.capture(), userIdCaptor.capture()))
                .thenReturn(Mono.empty());
        Mockito.when(commentRepository.countByCommentId(commentIdCaptor.capture())).thenReturn(Mono.just(1L));

        when(mock.post()).thenReturn(uriSpecMock);
        when(uriSpecMock.uri(ArgumentMatchers.<String>notNull())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.contentType(notNull())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.bodyValue(notNull())).thenReturn(requestHeaderSpecMock);
        when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.toBodilessEntity()).thenReturn(Mono.empty());

        webClient.delete()
                .uri(uriBuilder
                        .path(CommentPath.DELETE_LIKE.toString())
                        .queryParam("user", userId)
                        .buildAndExpand(commentId)
                        .toUri())
                .exchange()
                .expectStatus().isOk();

        // then
        verify(commentRepository, times(1)).deleteByCommentIdAndUserId(
                commentIdCaptor.capture(),
                userIdCaptor.capture());
        assertThat(userIdCaptor.getValue()).isEqualTo(userId);
        assertThat(commentIdCaptor.getValue()).isEqualTo(commentId);
    }
}
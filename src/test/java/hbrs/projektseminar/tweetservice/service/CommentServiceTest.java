package hbrs.projektseminar.tweetservice.service;

import hbrs.projektseminar.tweetservice.factory.UriFactory;
import hbrs.projektseminar.tweetservice.model.Comment;
import hbrs.projektseminar.tweetservice.populator.CommentPopulator;
import hbrs.projektseminar.tweetservice.repository.CommentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
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
class CommentServiceTest {

    @Mock private WebClient client;

    @Mock private CommentPopulator commentPopulator;

    @Mock private CommentRepository commentRepository;

    @Mock private DiscoveryClient discoveryClient;

    private CommentService underTest;

    @InjectMocks UriFactory uriFactory;

    @BeforeEach
    void setUp() {
//        UriFactory uriFactory = UriFactory.builder()
//                .discovery(discoveryClient)
//                .followServiceName("name")
//                .hashtagServiceName("name")
//                .commentByHashtagVar("name")
//                .build();
        underTest = CommentServiceImpl.builder()
                .client(client)
                .commentPopulator(commentPopulator)
                .commentRepository(commentRepository)
                .uriFactory(uriFactory)
                .build();
    }

    @AfterEach
    void tearDown() {
        underTest = null;
    }

    @Test
    void getCommentsCallsRightMethod(){
        //given
        Long tweetId = 1L;
        Flux<Comment> commentFlux = Flux.just(Comment.builder()
                .id(1L)
                .text("Some text")
                .tweetId(1L)
                .build());
        given(commentRepository.findByTweetId(tweetId)).willReturn(commentFlux);

        // when
        underTest.getComments(tweetId);
        ArgumentCaptor<Long> tweetIdCaptor = ArgumentCaptor.forClass(Long.class);

        // then
        verify(commentRepository).findByTweetId(tweetIdCaptor.capture());
        assertThat(tweetIdCaptor.getValue()).isEqualTo(tweetId);
    }
    @Test
    void getCommentsReturnRightResult(){
        //given
        Long tweetId = 1L;
        Comment comment= Comment.builder()
                .id(1L)
                .text("Some text")
                .tweetId(1L)
                .build();
        Flux<Comment> commentFlux = Flux.just(comment);
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        given(commentRepository.findByTweetId(tweetId)).willReturn(commentFlux);
        given(commentPopulator.getLikes(commentCaptor.capture())).willReturn(Mono.just(comment));

        //when
        Flux<Comment> result = underTest.getComments(tweetId);

        //then
        assertThat(result).isNotNull();
        Iterator<Comment> ti = commentFlux.toIterable().iterator();
        Iterator<Comment> ri = result.toIterable().iterator();
        while(ri.hasNext() && ti.hasNext()) {
            assertThat(ri.next()).isEqualTo(ti.next());
        }
        assertThat(ri.hasNext()).isFalse();
        assertThat(ti.hasNext()).isFalse();

    }

    @Test
    void getAllTweetIdsByCommentHashtagCallsRightMethod(){
        //given
        String hashtag = "hashtag";
        List<Long> commenids = Arrays.asList(1L);
        Flux<Long> tweetids = Flux.just(1L);
        ArgumentCaptor<List<Long>> commentId = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        given(commentRepository.findAllTweetIdsByCommentIdsIn(commenids)).willReturn(tweetids);
        given(discoveryClient.getInstances(stringArgumentCaptor.capture())).willReturn(Arrays.asList(new DefaultServiceInstance()));

        final var uriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        final var requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        final var responseSpecMock = mock(WebClient.ResponseSpec.class);

        //when
        Mockito.when(client.get()).thenReturn(uriSpecMock);
        Mockito.when(uriSpecMock.uri(ArgumentMatchers.<URI>notNull())).thenReturn(requestBodySpecMock);
        Mockito.when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
        Mockito.when(responseSpecMock.bodyToFlux(ArgumentMatchers.<Class<Long>>notNull())).thenReturn(Flux.fromIterable(commenids));
        uriFactory.setHashtagServiceName("name");
        uriFactory.setCommentByHashtagVar("var");
        underTest.getAllTweetIdsByCommentHashtag(hashtag).subscribe();

        //then
        verify(commentRepository).findAllTweetIdsByCommentIdsIn(commentId.capture());
        List<Long> commentIdValueValue = commentId.getValue();
        assertThat(commentIdValueValue.get(0)).isEqualTo(1L);
    }
    @Test
    void getAllTweetIdsByCommentHashtagReturnRightResult(){
        String hashtag = "hashtag";
        List<Long> commenids = Arrays.asList(1L);
        Flux<Long> tweetids = Flux.just(1L);
        ArgumentCaptor<List<Long>> commentId = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        given(commentRepository.findAllTweetIdsByCommentIdsIn(commenids)).willReturn(tweetids);
        given(discoveryClient.getInstances(stringArgumentCaptor.capture())).willReturn(Arrays.asList(new DefaultServiceInstance()));

        final var uriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        final var requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        final var responseSpecMock = mock(WebClient.ResponseSpec.class);

        //when
        Mockito.when(client.get()).thenReturn(uriSpecMock);
        Mockito.when(uriSpecMock.uri(ArgumentMatchers.<URI>notNull())).thenReturn(requestBodySpecMock);
        Mockito.when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
        Mockito.when(responseSpecMock.bodyToFlux(ArgumentMatchers.<Class<Long>>notNull())).thenReturn(Flux.fromIterable(commenids));
        uriFactory.setHashtagServiceName("name");
        uriFactory.setCommentByHashtagVar("var");
        Flux<Long> result = underTest.getAllTweetIdsByCommentHashtag(hashtag);
        result.subscribe();

        //then
        assertThat(result).isNotNull();
        Iterator<Long> ti = tweetids.toIterable().iterator();
        Iterator<Long> ri = result.toIterable().iterator();
        while(ri.hasNext() && ti.hasNext()) {
            assertThat(ri.next()).isEqualTo(ti.next());
        }
        assertThat(ri.hasNext()).isFalse();
        assertThat(ti.hasNext()).isFalse();

    }

    @Test
    void getAllTweetIdsWhereCommentContainsCallsRightMethod(){
        //given
        String string = "text";
        Flux<Comment> comments = Flux.just(Comment.builder()
                .id(1L)
                .text("Some text")
                .tweetId(1L)
                .build());
        given(commentRepository.findAllByTextContainingIgnoreCase(string)).willReturn(comments);

        //when
        underTest.getAllTweetIdsWhereCommentContains(string);
        ArgumentCaptor<String> stringArgumentCaptorIdCaptor = ArgumentCaptor.forClass(String.class);

        //then
        verify(commentRepository).findAllByTextContainingIgnoreCase(stringArgumentCaptorIdCaptor.capture());
        assertThat(stringArgumentCaptorIdCaptor.getValue()).isEqualTo(string);

    }
    @Test
    void getAllTweetIdsWhereCommentContainsReturnRightResult(){
        String string = "text";
        Comment comment = Comment.builder()
                .id(1L)
                .text("Some text")
                .tweetId(1L)
                .build();
        Flux<Comment> commentFlux = Flux.just(comment);
        Flux<Long> tweetids = Flux.just(1L);
        given(commentRepository.findAllByTextContainingIgnoreCase(string)).willReturn(commentFlux);

        Flux<Long> result = underTest.getAllTweetIdsWhereCommentContains(string);

        //then
        assertThat(result).isNotNull();
        Iterator<Long> ti = tweetids.toIterable().iterator();
        Iterator<Long> ri = result.toIterable().iterator();
        while(ri.hasNext() && ti.hasNext()) {
            assertThat(ri.next()).isEqualTo(ti.next());
        }
        assertThat(ri.hasNext()).isFalse();
        assertThat(ti.hasNext()).isFalse();

    }

    @Test
    void createCommentCallsRightMethod(){
        //given
        Comment comment= Comment.builder()
                .id(1L)
                .text("Some text")
                .tweetId(1L)
                .authorId(1L)
                .build();

        Mono<Comment> commentMono = Mono.just(comment);
        given(commentRepository.save(comment)).willReturn(commentMono);

        //when
        underTest.createComment(comment);
        ArgumentCaptor<Comment> commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);

        //then
        verify(commentRepository).save(commentArgumentCaptor.capture());
        assertThat(commentArgumentCaptor.getValue()).isEqualTo(comment);
    }
    @Test
    void createCommentReturnRightResult(){
        //given
        Comment comment= Comment.builder()
                .id(1L)
                .text("Some text")
                .tweetId(1L)
                .authorId(1L)
                .build();

        Mono<Comment> commentMono = Mono.just(comment);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        given(commentRepository.save(comment)).willReturn(commentMono);
        given(discoveryClient.getInstances(stringArgumentCaptor.capture())).willReturn(Arrays.asList(new DefaultServiceInstance()));

        final var uriSpecMock = mock(WebClient.RequestBodyUriSpec.class);
        final var requestHeaderSpecMock = mock(WebClient.RequestHeadersSpec.class);
        final var requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        final var responseSpecMock = mock(WebClient.ResponseSpec.class);

        //when
        Mono<Comment> resultMono = underTest.createComment(comment);
        Mockito.when(client.post()).thenReturn(uriSpecMock);
        Mockito.when(uriSpecMock.uri(ArgumentMatchers.<URI>notNull())).thenReturn(requestBodySpecMock);
        Mockito.when(requestBodySpecMock.contentType(notNull())).thenReturn(requestBodySpecMock);
        Mockito.when(requestBodySpecMock.bodyValue(notNull())).thenReturn(requestHeaderSpecMock);
        Mockito.when(requestHeaderSpecMock.retrieve()).thenReturn(responseSpecMock);
        Mockito.when(responseSpecMock.toBodilessEntity()).thenReturn(Mono.empty());
        resultMono.subscribe();

        //then
        assertThat(resultMono).isNotNull();
        Comment result = resultMono.block();
        assertThat(result).isEqualTo(comment);

    }

    @Test
    void deleteCommentCallsRightMethod(){
        //given
        Long id = 1L;
        ArgumentCaptor<Long> commentIdCaptor = ArgumentCaptor.forClass(long.class);

        //when
        Mockito.when(commentRepository.deleteAllLikesByCommentId(1L)).thenReturn(Mono.empty());
        Mockito.when(commentRepository.deleteById(id)).thenReturn(Mono.empty());
        underTest.deleteComment(id);

        //then
        verify(commentRepository).deleteAllLikesByCommentId(commentIdCaptor.capture());
        assertThat(commentIdCaptor.getValue()).isEqualTo(id);
        verify(commentRepository).deleteById(commentIdCaptor.capture());
        assertThat(commentIdCaptor.getValue()).isEqualTo(id);
    }

    @Test
    void deleteAllCommentsByTweetIdCallsRightMethod(){ //given
        Long id = 1L;
        Comment comment= Comment.builder()
                .id(1L)
                .text("Some text")
                .tweetId(1L)
                .build();
        Flux<Comment> commentFlux = Flux.just(comment);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> commentIdCaptor = ArgumentCaptor.forClass(long.class);

        final var uriSpecMock = mock(WebClient.RequestBodyUriSpec.class);
        final var requestHeaderSpecMock = mock(WebClient.RequestHeadersSpec.class);
        final var requestBodySpecMock = mock(WebClient.RequestBodySpec.class);
        final var responseSpecMock = mock(WebClient.ResponseSpec.class);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        given(commentRepository.findByTweetId(id)).willReturn(commentFlux);
        given(discoveryClient.getInstances(stringArgumentCaptor.capture())).willReturn(Arrays.asList(new DefaultServiceInstance()));
        given(commentPopulator.getLikes(commentCaptor.capture())).willReturn(Mono.just(comment));

        //when
        Mockito.when(commentRepository.deleteAllLikesByCommentId(1L)).thenReturn(Mono.empty());
        Mockito.when(commentRepository.deleteById(1L)).thenReturn(Mono.empty());
        Mockito.when(client.post()).thenReturn(uriSpecMock);
        Mockito.when(uriSpecMock.uri(ArgumentMatchers.<URI>notNull())).thenReturn(requestBodySpecMock);
        Mockito.when(requestBodySpecMock.contentType(notNull())).thenReturn(requestBodySpecMock);
        Mockito.when(requestBodySpecMock.bodyValue(notNull())).thenReturn(requestHeaderSpecMock);
        Mockito.when(requestHeaderSpecMock.retrieve()).thenReturn(responseSpecMock);
        Mockito.when(responseSpecMock.toBodilessEntity()).thenReturn(Mono.empty());
        underTest.deleteAllCommentsByTweetId(id).subscribe();

        //then
        verify(commentRepository).deleteAllLikesByCommentId(commentIdCaptor.capture());
        assertThat(commentIdCaptor.getValue()).isEqualTo(id);
        verify(commentRepository).deleteById(commentIdCaptor.capture());
        assertThat(commentIdCaptor.getValue()).isEqualTo(id);
    }

    @Test
    void addLikeCallsRightMethods(){
        //given
        Long commentid = 1L;
        Long userid= 1L;
        ArgumentCaptor<Long>  commenttidcaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long>  useridcaptor = ArgumentCaptor.forClass(Long.class);
        given(commentRepository.addLikeToComment(commentid,userid)).willReturn(Mono.empty());

        //when
        Mockito.when(commentRepository.existsById(commentid)).thenReturn(Mono.just(true));
        Mockito.when(commentRepository.addLikeToComment(commentid, userid)).thenReturn(Mono.empty());
        Mockito.when(commentRepository.countByCommentId(commentid)).thenReturn(Mono.just(1L));
        underTest.addLike(commentid,userid).subscribe();

        //then
        verify(commentRepository).addLikeToComment(commenttidcaptor.capture(),useridcaptor.capture());
        assertThat(commenttidcaptor.getValue()).isEqualTo(commentid);
        assertThat(useridcaptor.getValue()).isEqualTo(userid);

    }

    @Test
    void deleteLikeCallsRightMethod(){
        //given
        Long commentid = 1L;
        Long userid= 1L;
        ArgumentCaptor<Long>  commenttidcaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long>  useridcaptor = ArgumentCaptor.forClass(Long.class);
        given(commentRepository.deleteByCommentIdAndUserId(commentid,userid)).willReturn(Mono.empty());

        //when
        Mockito.when(commentRepository.countByCommentId(commentid)).thenReturn(Mono.just(0L));
        underTest.deleteLike(commentid,userid).subscribe();

        //then
        verify(commentRepository).deleteByCommentIdAndUserId(commenttidcaptor.capture(),useridcaptor.capture());
        assertThat(commenttidcaptor.getValue()).isEqualTo(commentid);
        assertThat(useridcaptor.getValue()).isEqualTo(userid);
    }

}

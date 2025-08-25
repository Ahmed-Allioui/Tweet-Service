package hbrs.projektseminar.tweetservice.factory;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Setter
@Slf4j
@Component
public class UriFactory {

    @Autowired
    private DiscoveryClient discovery;

    @Value("${services.hashtag-service.service-name}")
    private String hashtagServiceName;

    @Value("${services.hashtag-service.send-tweet-hashtag.path}")
    private String sendTweetHashtagPath;

    @Value("${services.hashtag-service.send-comment-hashtag.path}")
    private String sendCommentHashtagPath;

    @Value("${services.hashtag-service.tweets-by-hashtag.path}")
    private String tweetByHashtagPath;

    @Value("${services.hashtag-service.tweets-by-hashtag.variable}")
    private String tweetByHashtagVar;

    @Value("${services.hashtag-service.comments-by-hashtag.path}")
    private String commentByHashtagPath;

    @Value("${services.hashtag-service.comments-by-hashtag.variable}")
    private String commentByHashtagVar;

    @Value("${services.hashtag-service.update-tweet-likes.path}")
    private String updateTweetLikesPath;

    @Value("${services.hashtag-service.update-tweet-likes.var-id}")
    private String updateTweetLikesVarId;

    @Value("${services.hashtag-service.update-tweet-likes.var-likes}")
    private String updateTweetLikesVarLikes;

    @Value("${services.hashtag-service.update-comment-likes.path}")
    private String updateCommentLikesPath;

    @Value("${services.hashtag-service.update-comment-likes.var-id}")
    private String updateCommentLikesVarId;

    @Value("${services.hashtag-service.update-comment-likes.var-likes}")
    private String updateCommentLikesVarLikes;

    @Value("${services.follow-service.service-name}")
    private String followServiceName;

    @Value("${services.follow-service.get-follows.path}")
    private String getFollowsPath;

    @Value("${services.follow-service.get-follows.variable}")
    private String getFollowsVar;

    public UriComponentsBuilder sendTweetHashtagUri() {
        return getUri(hashtagServiceName, sendTweetHashtagPath);
    }

    public UriComponentsBuilder sendCommentHashtagUri() {
        return getUri(hashtagServiceName, sendCommentHashtagPath);
    }

    public UriComponentsBuilder tweetsByHashtagUri() {
        UriComponentsBuilder uri = getUri(hashtagServiceName, tweetByHashtagPath);
        if(uri != null) uri.queryParam(tweetByHashtagVar, "{hashtag}");
        return uri;
    }

    public UriComponentsBuilder commentsByHashtagUri() {
        UriComponentsBuilder uri = getUri(hashtagServiceName, commentByHashtagPath);
        if(uri != null) uri.queryParam(commentByHashtagVar, "{hashtag}");
        return uri;
    }


    public UriComponentsBuilder updateTweetLikesUri() {
        UriComponentsBuilder uri = getUri(hashtagServiceName, updateTweetLikesPath);
        if(uri != null) {
            uri.queryParam(updateTweetLikesVarId, "{id}");
            uri.queryParam(updateTweetLikesVarLikes, "{likes}");
        }
        return uri;
    }

    public UriComponentsBuilder updateCommentLikesUri() {
        UriComponentsBuilder uri = getUri(hashtagServiceName, updateCommentLikesPath);
        if(uri != null) {
            uri.queryParam(updateCommentLikesVarId, "{id}");
            uri.queryParam(updateTweetLikesVarLikes, "{likes}");
        }
        return uri;
    }

    public UriComponentsBuilder getFollowsUri() {
        UriComponentsBuilder uri = getUri(followServiceName, getFollowsPath);
        if(uri != null) uri.queryParam(getFollowsVar, "{userId}");
        return uri;
    }

    private UriComponentsBuilder getUri(String serviceName, String path) {
        URI uri = getUriFromDiscovery(serviceName);
        if(uri == null) {
            return null;
        }
        UriComponentsBuilder.fromUri(uri).path(path);
        return UriComponentsBuilder.fromUri(uri).path(path);
    }

    private URI getUriFromDiscovery(String serviceName) {
        ServiceInstance si;
        List<ServiceInstance> services = discovery.getInstances(serviceName);
        if(!services.isEmpty()) {
            si = services.get(0);
            log.debug("Service found: " + si.toString());
            UriComponents uri = UriComponentsBuilder.newInstance().host(si.getHost()).scheme("https").build();
            return uri.toUri();
        } else {
            log.warn(serviceName + " not found in registry.");
        }
        return null;
    }
}

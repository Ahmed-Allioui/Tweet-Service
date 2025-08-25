package hbrs.projektseminar.tweetservice.factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.web.util.UriComponentsBuilder;

@TestComponent
public class ServerUriGenerator {

    @Value("${server.port}")
    private String port;

    public UriComponentsBuilder serverUri() {
        return UriComponentsBuilder
                .newInstance()
                .scheme("http")
                .host("localhost")
                .port(port);
    }

}

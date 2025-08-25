package hbrs.projektseminar.tweetservice.enumeration;

public enum TweetPath {

    GET_ALL_USER(""),
    GET_ALL_FOLLOWINGS("/follows"),
    GET_TWEET("/{id}"),
    CREATE(""),
    DELETE("/{id}"),
    ADD_LIKE("/{id}/like"),
    DELETE_LIKE("/{id}/like"),
    ;

    private static final String origin = "api/tweets";

    private final String path;

    TweetPath(final String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return origin + path;
    }
}

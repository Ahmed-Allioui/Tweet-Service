package hbrs.projektseminar.tweetservice.enumeration;

public enum CommentPath {

    CREATE("/"),
    DELETE("/{id}"),
    ADD_LIKE("/{id}/like"),
    DELETE_LIKE("/{id}/like"),
    ;

    private static final String origin = "api/comments";

    private final String path;

    CommentPath(final String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return origin + path;
    }
}

-- SQL schema of the Tweet Service to be executed during application startup in order to initialise the database schema

CREATE TABLE IF NOT EXISTS tweet (
    id SERIAL PRIMARY KEY,
    text VARCHAR(280),
    author_id INT NOT NULL,
    created_on DATE,
    retweet_id INT,
    CONSTRAINT FK_retweet FOREIGN KEY (retweet_id) REFERENCES tweet(id)
);

CREATE TABLE IF NOT EXISTS comment (
    id SERIAL PRIMARY KEY,
    tweet_id INT NOT NULL,
    text VARCHAR(280),
    author_id INT NOT NULL,
    created_on DATE,
    CONSTRAINT tweet_comment_tweet FOREIGN KEY (tweet_id) REFERENCES tweet(id),
    CONSTRAINT tweet_comment_unique UNIQUE (id, tweet_id)
);

CREATE TABLE IF NOT EXISTS tweet_like (
    tweet_id INT NOT NULL,
    user_id INT NOT NULL,
    CONSTRAINT tweet_like_tweet FOREIGN KEY (tweet_id) REFERENCES tweet(id),
    CONSTRAINT tweet_like_unique UNIQUE (tweet_id, user_id)
);

CREATE TABLE IF NOT EXISTS tweet_picture (
    tweet_id INT NOT NULL,
    picture_id INT NOT NULL,
    CONSTRAINT tweet_picture_tweet FOREIGN KEY (tweet_id) REFERENCES tweet(id),
    CONSTRAINT tweet_picture_unique UNIQUE (tweet_id, picture_id)
);

CREATE TABLE IF NOT EXISTS comment_like (
    comment_id INT NOT NULL,
    user_id INT NOT NULL,
    CONSTRAINT comment_like_comment FOREIGN KEY (comment_id) REFERENCES comment(id),
    CONSTRAINT comment_like_unique UNIQUE (comment_id, user_id)
);
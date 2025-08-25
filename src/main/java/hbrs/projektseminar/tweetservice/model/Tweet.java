package hbrs.projektseminar.tweetservice.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.List;

@Table("tweet")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tweet {

    @Id
    private Long id;
    private String text;
    private Long authorId;
    private LocalDate createdOn;
    private Long retweetId;

    @Transient private Tweet retweet;
    @Transient private List<Comment> comments;
    @Transient private List<Long> likedBy;
    @Transient private List<Long> pictures;
}
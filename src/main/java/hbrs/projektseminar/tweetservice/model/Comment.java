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

@Table("comment")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    @Id
    private Long id;
    private String text;
    private Long authorId;
    private Long tweetId;
    private LocalDate createdOn;

    @Transient
    private List<Long> likedBy;

}

package hbrs.projektseminar.tweetservice.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class UnauthorizedDeleteException extends RuntimeException {
    private String message;
}

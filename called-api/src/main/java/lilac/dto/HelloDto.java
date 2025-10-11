package lilac.dto;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class HelloDto implements Serializable {
    private String message;
    private String description;
}

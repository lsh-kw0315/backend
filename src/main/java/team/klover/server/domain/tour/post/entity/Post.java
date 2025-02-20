package team.klover.server.domain.tour.post.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Post {
    @Id
    private String tourPlaceId;
    private String tourPlaceName;
    private String tourPlaceArea;
    private String tourPhotoUrl;
    private String tourType;
    private String map_x;
    private String map_y;
}

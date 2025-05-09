package team.klover.server.domain.tour.tourPost.dto.res;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;
import team.klover.server.global.elasticsearch.tourpost.doc.TourPostDoc;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class TourPostDto {
    private Long contentId;
    private Long commonPlaceId;
    private Double avgRating;
    private Long reviewCount;
    private String title;
    private String addr1;
    private String firstImage;
    private Double mapX;
    private Double mapY;

    public TourPostDto(TourPostDoc tourPostDoc){
        this.contentId = tourPostDoc.getContent_id();
        this.commonPlaceId = tourPostDoc.getCommon_place_id();
        this.avgRating = tourPostDoc.getRating_average().doubleValue();
        this.title = tourPostDoc.getTitle();
        this.addr1 = tourPostDoc.getAddr1();
        this.firstImage = tourPostDoc.getFirst_image();
        this.mapX = tourPostDoc.getLocation().getLon();
        this.mapY = tourPostDoc.getLocation().getLat();
        this.reviewCount = tourPostDoc.getReview_count();
    }
}

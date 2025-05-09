package team.klover.server.domain.tour.tourPost.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import team.klover.server.domain.member.v1.enums.Country;
import team.klover.server.domain.tour.enums.Area;
import team.klover.server.domain.tour.enums.ContentType;
import team.klover.server.domain.tour.enums.TourPostSort;
import team.klover.server.domain.tour.tourPost.dto.res.TourPostDto;
import team.klover.server.domain.tour.tourPost.entity.TourPost;

public interface TourPostRepositoryCustom {
    Page<TourPostDto> search(String keyword, Pageable pageable, Double mapX, Double mapY, Country language, Area area, ContentType contentType, boolean hasExotic, boolean hasHealing, boolean hasTraditional, boolean hasActive, boolean searchByTitle, boolean searchByOverview, TourPostSort sort);
}

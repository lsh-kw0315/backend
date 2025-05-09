package team.klover.server.domain.tour.tourPost.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import team.klover.server.domain.community.commPost.dto.req.XYForm;
import team.klover.server.domain.member.v1.enums.Country;
import team.klover.server.domain.tour.enums.Area;
import team.klover.server.domain.tour.enums.ContentType;
import team.klover.server.domain.tour.enums.TourPostSort;
import team.klover.server.domain.tour.tourPost.dto.res.DetailTourPostDto;
import team.klover.server.domain.tour.tourPost.dto.res.TourPostDto;

public interface TourPostService {
    // 사용자 위치 주변 게시글(관광지&사용자) 조회
    Page<TourPostDto> findPostsWithinRadius(@Valid XYForm xyForm, Pageable pageable);

    // 사용자 언어 & 지역기반 관광지 데이터 조회
    Page<TourPostDto> findByLanguageAndAreaCode(String language, String areaCode, Pageable pageable);

    // 해당 관광지 상세 조회
    DetailTourPostDto findByContentId(Long contentId);

    // 해당 사용자가 저장한 관광지 조회
    Page<TourPostDto> getSavedTourPostByMember(Long memberId, Pageable pageable);

    // 사용자 언어 & 관광지명/지역명 검색
    Page<TourPostDto> searchByLanguageAndKeyword(String language, String keyword, Pageable pageable);

    // 해당 관광지 저장
    void addCollectionTourPost(Long currentMemberId, Long contentId);

    // 해당 관광지 저장 취소
    void deleteCollectionTourPost(Long currentMemberId, Long contentId);

    Page<TourPostDto> search(String keyword, Pageable pageable, Double mapX, Double mapY, Country language, Area area, ContentType contentType, boolean hasExotic, boolean hasHealing, boolean hasTraditional, boolean hasActive, boolean searchByTitle, boolean searchByOverview, TourPostSort sort);
}

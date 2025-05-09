package team.klover.server.domain.tour.review.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import team.klover.server.domain.tour.review.dto.req.ReviewForm;
import team.klover.server.domain.tour.review.dto.res.ReviewDto;

public interface ReviewService {
    // 해당 관광지 게시글에 작성된 리뷰 조회
    Page<ReviewDto> findByCommonPlaceId(String commonPlaceId, Pageable pageable);

    // 해당 관광지 게시글에 리뷰 생성
    void addReview(Long currentMemberId, Long commonPlaceId, @Valid ReviewForm reviewForm);

    // 본인 리뷰 수정
    void updateReview(Long currentMemberId, Long reviewId, @Valid ReviewForm reviewForm);

    void updateReviewTest(Long reviewId, @Valid ReviewForm reviewForm);

    // 본인 리뷰 삭제
    void deleteReview(Long currentMemberId, Long reviewId);

    void deleteReviewTest(Long reviewId);
}

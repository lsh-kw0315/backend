package team.klover.server.domain.tour.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import team.klover.server.domain.tour.review.dto.req.ReviewForm;
import team.klover.server.domain.tour.review.dto.res.ReviewDto;
import team.klover.server.domain.tour.review.entity.ReviewPage;
import team.klover.server.domain.tour.review.service.ReviewService;
import team.klover.server.global.common.response.ApiResponse;
import team.klover.server.global.common.response.KloverPage;
import team.klover.server.global.exception.ReturnCode;
import team.klover.server.global.translation.service.TranslationHelper;
import team.klover.server.global.util.AuthUtil;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value="/api/v1/tour-post/review",produces = APPLICATION_JSON_VALUE)
@Tag(name="ApiV1ReviewController",description = "Review API")
@RequiredArgsConstructor
public class ApiV1ReviewController {
    private final ReviewService reviewService;
    private final TranslationHelper translationHelper; // 번역

    // 해당 관광지 게시글에 작성된 리뷰 조회
    // http://localhost:8080/api/v1/tour-post/review/617?page=0&size=10
    @GetMapping("/{commonPlaceId}")
    @Operation(summary="해당 관광지 게시글에 작성된 리뷰 조회")
    public ApiResponse<ReviewDto> findByCommonPlaceId(@ModelAttribute ReviewPage request, @PathVariable("commonPlaceId") String commonPlaceId) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<ReviewDto> reviewPage = reviewService.findByCommonPlaceId(commonPlaceId, pageable);
        // 번역 로직 추가
//        reviewPage.getContent().forEach(review -> {
//            if (review.getContent() != null && !review.getContent().isEmpty()) {
//                review.setContent(translationHelper.translateForCurrentLanguage(review.getContent()));
//            }
//        });
        //ddd
        return ApiResponse.of(KloverPage.of(reviewPage));
    }

    // 해당 관광지 게시글에 리뷰 생성
    // http://localhost:8080/api/v1/tour-post/review/264107
    @PostMapping("/{commonPlaceId}")
    @Operation(summary="해당 관광지 게시글에 리뷰 생성")
    public ApiResponse<String> addReview(@PathVariable("commonPlaceId") Long commonPlaceId, @RequestBody @Valid ReviewForm reviewForm) {
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        reviewService.addReview(currentMemberId, commonPlaceId, reviewForm);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 본인 리뷰 수정
    // http://localhost:8080/api/v1/tour-post/review/1
    @PutMapping("/{reviewId}")
    @Operation(summary="본인 리뷰 수정")
    public ApiResponse<String> updateReview(@PathVariable("reviewId") Long reviewId, @RequestBody @Valid ReviewForm reviewForm) {
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        reviewService.updateReview(currentMemberId, reviewId, reviewForm);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 본인 리뷰 수정
    // http://localhost:8080/api/v1/tour-post/review/1
    @PutMapping("/updateTest/{reviewId}")
    @Operation(summary="본인 리뷰 수정")
    public ApiResponse<String> updateReviewTest(@PathVariable("reviewId") Long reviewId, @RequestBody @Valid ReviewForm reviewForm) {
        reviewService.updateReviewTest(reviewId, reviewForm);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 본인 리뷰 삭제
    // http://localhost:8080/api/v1/tour-post/review/1
    @DeleteMapping("/{reviewId}")
    @Operation(summary="본인 리뷰 삭제")
    public ApiResponse<String> deleteReview(@PathVariable("reviewId") Long reviewId) {
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        reviewService.deleteReview(currentMemberId, reviewId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 본인 리뷰 삭제
    // http://localhost:8080/api/v1/tour-post/review/1
    @DeleteMapping("/deleteTest/{reviewId}")
    @Operation(summary="본인 리뷰 삭제")
    public ApiResponse<String> deleteReviewTest(@PathVariable("reviewId") Long reviewId) {
        reviewService.deleteReviewTest(reviewId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }
}

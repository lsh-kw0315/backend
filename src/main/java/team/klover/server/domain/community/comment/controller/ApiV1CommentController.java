package team.klover.server.domain.community.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import team.klover.server.domain.community.comment.dto.req.CommentForm;
import team.klover.server.domain.community.comment.dto.res.CommentDto;
import team.klover.server.domain.community.comment.entity.CommentPage;
import team.klover.server.domain.community.comment.service.CommentService;
import team.klover.server.global.common.response.ApiResponse;
import team.klover.server.global.common.response.KloverPage;
import team.klover.server.global.exception.ReturnCode;
import team.klover.server.global.translation.service.TranslationHelper;
import team.klover.server.global.util.AuthUtil;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value="/api/v1/comm-post/comment",produces = APPLICATION_JSON_VALUE)
@Tag(name="ApiV1CommentController",description = "Comment API")
@RequiredArgsConstructor
public class ApiV1CommentController {
    private final CommentService commentService;
    private final TranslationHelper translationHelper; // 추가: 번역 헬퍼

    // 해당 게시글에 작성된 모든 댓글 조회
    // http://localhost:8080/api/v1/comm-post/comment/1
    @GetMapping("/{commPostId}")
    @Operation(summary = "댓글 전체 조회")
    public ApiResponse<CommentDto> findByCommPostId(@ModelAttribute CommentPage request, @PathVariable("commPostId") Long commPostId){
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<CommentDto> commentPage = commentService.findByCommPostId(commPostId, pageable);
        // 번역 로직 추가
//        commentPage.getContent().forEach(comment -> {
//            // 댓글 내용 번역
//            if (comment.getContent() != null && !comment.getContent().isEmpty()) {
//                comment.setContent(translationHelper.translateForCurrentLanguage(comment.getContent()));
//            }
//        });
        return ApiResponse.of(KloverPage.of(commentPage));
    }

    // 댓글 좋아요
    // http://localhost:8080/api/v1/comm-post/comment/like/1
    @PostMapping("/like/{commentId}")
    @Operation(summary = "댓글 좋아요")
    public ApiResponse<String> addCommentLike(@PathVariable("commentId") Long commentId){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        commentService.addCommentLike(currentMemberId, commentId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 댓글 좋아요 취소
    // http://localhost:8080/api/v1/comm-post/comment/like/1
    @DeleteMapping("/like/{commentId}")
    @Operation(summary = "좋아요 취소")
    public ApiResponse<String> deleteCommentLike(@PathVariable("commentId") Long commentId){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        commentService.deleteCommentLike(currentMemberId, commentId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 해당 게시글에 댓글 생성
    // http://localhost:8080/api/v1/comm-post/comment/1
    @PostMapping("/{commPostId}")
    @Operation(summary = "댓글 생성")
    public ApiResponse<String> addComment(@PathVariable("commPostId") Long commPostId, @RequestBody @Valid CommentForm commentForm){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        commentService.addComment(currentMemberId, commPostId, commentForm);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 해당 댓글 수정
    // http://localhost:8080/api/v1/comm-post/comment/1
    @PutMapping("/{commentId}")
    @Operation(summary = "댓글 수정")
    public ApiResponse<String> updateComment(@PathVariable("commentId") Long commentId, @RequestBody @Valid CommentForm commentForm){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        commentService.updateComment(currentMemberId, commentId, commentForm);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 해당 댓글 삭제
    // http://localhost:8080/api/v1/comm-post/comment/1
    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제")
    public ApiResponse<String> deleteComment(@PathVariable("commentId") Long commentId){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        commentService.deleteComment(currentMemberId, commentId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 해당 댓글 삭제
    // http://localhost:8080/api/v1/comm-post/comment/1
    @DeleteMapping("/deleteTest/{commentId}")
    @Operation(summary = "댓글 삭제")
    public ApiResponse<String> deleteCommentTest(@PathVariable("commentId") Long commentId){
        commentService.deleteCommentTest(commentId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }
}

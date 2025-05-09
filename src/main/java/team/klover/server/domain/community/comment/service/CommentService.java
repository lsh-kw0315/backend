package team.klover.server.domain.community.comment.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import team.klover.server.domain.community.comment.dto.req.CommentForm;
import team.klover.server.domain.community.comment.dto.res.CommentDto;

public interface CommentService {
    // 해당 게시글에 작성된 모든 댓글 조회
    Page<CommentDto> findByCommPostId(Long commPostId, Pageable pageable);

    // 댓글 좋아요
    void addCommentLike(Long currentMemberId, Long commentId);

    // 댓글 좋아요 취소
    void deleteCommentLike(Long currentMemberId, Long commentId);

    // 해당 게시글에 댓글 생성
    void addComment(Long currentMemberId, Long commPostId, @Valid CommentForm commentForm);

    // 해당 댓글 수정
    void updateComment(Long currentMemberId, Long commentId,@Valid CommentForm commentForm);

    // 해당 댓글 삭제
    void deleteComment(Long currentMemberId, Long commentId);

    void deleteCommentTest(Long commentId);

    // 해당 게시글의 모든 댓글 삭제
    void deleteAllComments(Long commPostId);
}

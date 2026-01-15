package team.klover.server.domain.community.comment.serviceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.klover.server.domain.community.commPost.dto.res.CommPostDto;
import team.klover.server.domain.community.commPost.entity.CommPost;
import team.klover.server.domain.community.commPost.enums.CommPostSort;
import team.klover.server.domain.community.commPost.repository.CommPostRepository;
import team.klover.server.domain.community.comment.dto.req.CommentForm;
import team.klover.server.domain.community.comment.dto.res.CommentDto;
import team.klover.server.domain.community.comment.entity.Comment;
import team.klover.server.domain.community.comment.entity.CommentLike;
import team.klover.server.domain.community.comment.entity.CommentPage;
import team.klover.server.domain.community.comment.event.CommentCreatedEvent;
import team.klover.server.domain.community.comment.event.CommentLikedEvent;
import team.klover.server.domain.community.comment.repository.CommentLikeRepository;
import team.klover.server.domain.community.comment.repository.CommentRepository;
import team.klover.server.domain.community.comment.service.CommentService;
import team.klover.server.domain.member.v1.entity.Member;
import team.klover.server.domain.member.v1.enums.Country;
import team.klover.server.domain.member.v1.repository.MemberV1Repository;
import team.klover.server.global.elasticsearch.commpost.springevent.event.CommPostCountEvent;
import team.klover.server.global.exception.KloverRequestException;
import team.klover.server.global.exception.ReturnCode;
import team.klover.server.global.util.AuthUtil;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommPostRepository commPostRepository;
    private final MemberV1Repository memberV1Repository;
    private final ApplicationEventPublisher publisher;
    private final CommentLikeRepository commentLikeRepository;

    // 해당 게시글에 작성된 모든 댓글 조회
    @Override
    @Transactional(readOnly = true)
    public Page<CommentDto> findByCommPostId(Long commPostId, Pageable pageable){
        checkPageSize(pageable.getPageSize());
        Page<Comment> comments = commentRepository.findByCommPostIdOrderByCreateDateDesc(commPostId, pageable);
        return comments.map(this::convertToCommentDto);
    }

    // 댓글 좋아요
    @Override
    @Transactional
    public void addCommentLike(Long currentMemberId, Long commentId){
        Member member = memberV1Repository.findById(currentMemberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        boolean alreadySaved = comment.getLikedMembers()
                .stream()
                .anyMatch(savedMember -> savedMember.getMember() != null && savedMember.getMember().getId().equals(member.getId()));
        if (alreadySaved) {
            throw new KloverRequestException(ReturnCode.ALREADY_EXIST);
        }
        CommentLike commentLike = new CommentLike(member, comment);
        comment.getLikedMembers().add(commentLike);

        // 이벤트 생성 및 발행
        publisher.publishEvent(new CommentLikedEvent(this, comment, member));
    }

    // 댓글 좋아요 취소
    @Override
    @Transactional
    public void deleteCommentLike(Long currentMemberId, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        Member member = memberV1Repository.findById(currentMemberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        CommentLike commentLike = comment.getLikedMembers()
                .stream()
                .filter(m -> m.getMember()!=null && m.getMember().getId().equals(currentMemberId))
                .findFirst()
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        comment.getLikedMembers().remove(commentLike);
    }

    // 해당 게시글에 댓글 생성
    @Override
    @Transactional
    public void addComment(Long currentMemberId, Long commPostId, @Valid CommentForm commentForm){
        // 현재 로그인한 사용자의 member 객체를 가져오는 메서드
        Member member = memberV1Repository.findById(currentMemberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        CommPost commPost = commPostRepository.findById(commPostId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        Comment comment = Comment.builder()
                .member(member)
                .commPost(commPost)
                .content(commentForm.getContent())
                .superCommentId(commentForm.getSuperCommentId())
                .build();
        commentRepository.save(comment);

        // 이벤트 생성 및 발행 (알림 + 엘라스틱서치)
        publisher.publishEvent(new CommentCreatedEvent(this, commPost, comment));

        publisher.publishEvent(new CommPostCountEvent(this, commPost));
    }

    // 해당 댓글 수정
    @Override
    @Transactional
    public void updateComment(Long currentMemberId, Long commentId, @Valid CommentForm commentForm){
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        // 작성자 검증 - 현재 로그인한 사용자의 ID를 가져와서 검증
        Member member = memberV1Repository.findById(currentMemberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        if (!comment.getMember().getId().equals(member.getId())) {
            throw new KloverRequestException(ReturnCode.NOT_AUTHORIZED);
        }
        comment.setContent(commentForm.getContent());
        commentRepository.save(comment);
    }

    // 해당 댓글 삭제
    @Override
    @Transactional
    public void deleteComment(Long currentMemberId, Long commentId){
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        // 작성자 검증 - 현재 로그인한 사용자의 ID를 가져와서 검증
        Member member = memberV1Repository.findById(currentMemberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        Long currentUserId = member.getId();
        if (!comment.getMember().getId().equals(currentUserId)) {
            throw new KloverRequestException(ReturnCode.NOT_AUTHORIZED);
        }
        deleteChildComments(commentId);
        commentRepository.save(comment); // 답글 삭제 후 더티 체킹
        commentRepository.delete(comment);
        //댓글 삭제 이벤트(갯수 정산은 삭제 종료 후 발생해도 되므로)
        publisher.publishEvent(new CommPostCountEvent(this, comment.getCommPost()));
    }

    @Transactional
    public void deleteCommentTest(Long commentId){
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        deleteChildComments(commentId);
        commentRepository.save(comment); // 답글 삭제 후 더티 체킹
        commentRepository.delete(comment);

        //댓글 삭제 이벤트(갯수 정산은 삭제 종료 후 발생해도 되므로)
        publisher.publishEvent(new CommPostCountEvent(this, comment.getCommPost()));
    }

    // 해당 게시글의 모든 댓글 삭제
    @Override
    @Transactional
    public void deleteAllComments(Long commPostId){
        CommPost commPost = commPostRepository.findById(commPostId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        List<Comment> comments = commentRepository.findByCommPost(commPost);
        //댓글 삭제 이벤트
        commentRepository.deleteAll(comments);

        publisher.publishEvent(new CommPostCountEvent(this, commPost));
    }

    // 해당 댓글의 모든 하위 댓글 삭제
    private void deleteChildComments(Long superCommentId) {
        List<Comment> childComments = commentRepository.findBySuperCommentId(superCommentId);
        for (Comment childComment : childComments) {
            deleteChildComments(childComment.getId()); // 재귀 호출
            commentRepository.delete(childComment);
        }
    }

    // 요청 페이지 수 제한
    private void checkPageSize(int pageSize) {
        int maxPageSize = CommentPage.getMaxPageSize();
        if (pageSize > maxPageSize) {
            throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);
        }
    }

    // Comment를 CommentDto로 변환
    private CommentDto convertToCommentDto(Comment comment) {
        Long currentMemberId = AuthUtil.getCurrentMemberIdRoughly();
        Boolean isLiked = currentMemberId != null && commentLikeRepository.haveLiked(comment.getId(),currentMemberId).isPresent();
        return CommentDto.builder()
                .id(comment.getId())
                .memberId(comment.getMember().getId())
                .nickname(comment.getMember().getNickname())
                .profileImageUrl(comment.getMember().getProfileUrl())
                .likeCount(comment.getLikedMembers().size())
                .content(comment.getContent())
                .isLiked(isLiked)
                .superCommentId(comment.getSuperCommentId())
                .createDate(comment.getCreateDate())
                .build();
    }
}

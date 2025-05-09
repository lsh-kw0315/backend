package team.klover.server.domain.member.v1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.klover.server.domain.chat.chatMessage.entity.ChatMessage;
import team.klover.server.domain.chat.chatMessage.repository.ChatMessageRepository;
import team.klover.server.domain.chat.chatMessage.service.ChatMessageService;
import team.klover.server.domain.chat.chatRoom.entity.ChatRoomMember;
import team.klover.server.domain.chat.chatRoom.repository.ChatRoomMemberRepository;
import team.klover.server.domain.chat.chatRoom.service.ChatRoomService;
import team.klover.server.domain.community.commPost.entity.CommPost;
import team.klover.server.domain.community.commPost.entity.CommPostLike;
import team.klover.server.domain.community.commPost.entity.CommPostSave;
import team.klover.server.domain.community.commPost.repository.CommPostLikeRepository;
import team.klover.server.domain.community.commPost.repository.CommPostRepository;
import team.klover.server.domain.community.commPost.repository.CommPostSaveRepository;
import team.klover.server.domain.community.commPost.service.CommPostService;
import team.klover.server.domain.community.comment.entity.Comment;
import team.klover.server.domain.community.comment.entity.CommentLike;
import team.klover.server.domain.community.comment.repository.CommentLikeRepository;
import team.klover.server.domain.community.comment.repository.CommentRepository;
import team.klover.server.domain.community.comment.service.CommentService;
import team.klover.server.domain.member.v1.dto.MemberDto;
import team.klover.server.domain.member.v1.dto.MemberInfo;
import team.klover.server.domain.member.v1.dto.MemberUpdateParam;
import team.klover.server.domain.member.v1.entity.Member;
import team.klover.server.domain.member.v1.enums.SocialProvider;
import team.klover.server.domain.member.v1.repository.MemberV1Repository;
import team.klover.server.domain.tour.review.entity.Review;
import team.klover.server.domain.tour.review.entity.ReviewTourPost;
import team.klover.server.domain.tour.review.repository.ReviewRepository;
import team.klover.server.domain.tour.review.repository.ReviewTourPostRepository;
import team.klover.server.domain.tour.review.service.ReviewService;
import team.klover.server.domain.tour.tourPost.entity.TourPost;
import team.klover.server.domain.tour.tourPost.entity.TourPostSave;
import team.klover.server.domain.tour.tourPost.repository.TourPostSaveRepository;
import team.klover.server.domain.tour.tourPost.service.TourPostService;
import team.klover.server.global.elasticsearch.commpost.springevent.event.CommPostDeleteEvent;
import team.klover.server.global.elasticsearch.commpost.springevent.event.CommPostCountEvent;
import team.klover.server.global.elasticsearch.commpost.springevent.event.NicknameUpdateEvent;
import team.klover.server.global.elasticsearch.member.springevent.event.MemberDeleteEvent;
import team.klover.server.global.elasticsearch.member.springevent.event.MemberUpdateEvent;
import team.klover.server.global.elasticsearch.tourpost.springevent.event.TourPostCountEvent;
import team.klover.server.global.exception.KloverException;
import team.klover.server.global.exception.KloverLogicException;
import team.klover.server.global.exception.KloverRequestException;
import team.klover.server.global.exception.ReturnCode;
import team.klover.server.global.jpa.BaseEntity;
import team.klover.server.global.s3.S3Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberV1Service {
    private final MemberV1Repository memberRepository;
    private final S3Service s3Service;

    private final CommPostRepository commPostRepository;
    private final CommPostLikeRepository commPostLikeRepository;
    private final CommPostSaveRepository commPostSaveRepository;
    private final CommPostService commPostService;

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentService commentService;

    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final TourPostSaveRepository tourPostSaveRepository;
    private final TourPostService tourPostService;
    private final ReviewTourPostRepository reviewTourPostRepository;

    private final ChatRoomService chatRoomService;

    private final ChatRoomMemberRepository chatRoomMemberRepository;

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageService chatMessageService;

    private final ApplicationEventPublisher publisher;
    @Transactional
    public void updateMember(Long memberId, MemberUpdateParam param
                                     , MultipartFile imageFile
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));


        String imageUrl = null;
        try {
            if (imageFile != null && !imageFile.isEmpty()) {

                if(member.getProfileUrl() != null) s3Service.deleteFile(member.getProfileUrl());

                System.out.println("업로드를 하는 중입니다.");
                imageUrl = s3Service.uploadFile(imageFile, "profile-images");
            }
        } catch (IOException e) {
            throw new KloverLogicException(ReturnCode.INTERNAL_ERROR);
        }

        String prevNickname = member.getNickname();
        member.update(param, imageUrl);
        String curNickname = member.getNickname();

        if(!prevNickname.equals(curNickname)) {
            publisher.publishEvent(new NicknameUpdateEvent(this, member));
        }

        publisher.publishEvent(new MemberUpdateEvent(this, member));
    }

    @Transactional
    public void resetImage(Long currentMemberId){
        Member member = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        if(member.getProfileUrl() != null) s3Service.deleteFile(member.getProfileUrl());

        member.setProfileUrl(null);
        publisher.publishEvent(new MemberUpdateEvent(this, member));
    }

    public MemberDto getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        return MemberDto.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileUrl(member.getProfileUrl())
                .build();
    }

    @Transactional
    public void deleteMember(Long memberId){
        Optional<Member> target = memberRepository.findById(memberId);
        if(target.isEmpty()) throw new KloverException(ReturnCode.NOT_FOUND_ENTITY);

        //관련된 친구들은 여기서 싹 다 삭제를 갈기고 떠난다.

        Member member = target.get();

        //채팅 메시지 먼저 삭제, 채팅방 입장 기록을 삭제
        List<ChatMessage> messages = chatMessageRepository.findAllByMember(member);
        List<ChatRoomMember> enteredChatRoom = chatRoomMemberRepository.findAllByMember(member);

        messages.forEach(message -> chatMessageService.deleteChatMessage(memberId, message.getId()));;
        enteredChatRoom.forEach(chatRoomMember -> chatRoomService.leaveChatRoomMember(chatRoomMember.getChatRoom().getId(),memberId));

        //댓글 처리
        List<CommentLike> commentLikes = commentLikeRepository.findAllByMember(member);
        commentLikes.forEach(commentLike -> commentService.deleteCommentLike(memberId,commentLike.getComment().getId()));

        List<Comment> comments = commentRepository.findAllByMember(member);
        List<Long> commentsId = comments.stream().map(BaseEntity::getId).toList();
        List<CommPost> updateList = commentRepository.getAllCommPostIdsInComments(commentsId);

        comments.forEach(comment -> commentService.deleteComment(memberId, comment.getId()));

        if(!updateList.isEmpty()){
            updateList.forEach(commPost -> {
                publisher.publishEvent(new CommPostCountEvent(this, commPost));
            });
        }

        //게시물 처리
        List<CommPostLike> commPostLikes = commPostLikeRepository.findAllByMember(member);
        List<CommPostSave> commPostSaves = commPostSaveRepository.findAllByMember(member);
        List<CommPost> commPosts = commPostRepository.findAllByMember(member);

        Set<CommPost> likeUpdateList = new HashSet<>();
        commPostLikes.forEach(commPostLike -> {
            likeUpdateList.add(commPostLike.getCommPost());
            commPostService.deleteCommPostLike(memberId, commPostLike.getCommPost().getId());
        });

        if(!likeUpdateList.isEmpty()){
            likeUpdateList.forEach(commPost -> {
                publisher.publishEvent(new CommPostCountEvent(this, commPost));
            });
        }
        commPostSaves.forEach(commPostSave -> commPostService.deleteCollectionCommPost(memberId, commPostSave.getCommPost().getId()));
        commPosts.forEach(commPost -> {
            commPostService.deleteCommPost(memberId, commPost.getId());
            publisher.publishEvent(new CommPostDeleteEvent(this, commPost));
        });

        //관광 정보 관련(리뷰, 저장) 처리
        List<Review> reviews = reviewRepository.findAllByMember(member);
        List<TourPostSave> tourPostSaves = tourPostSaveRepository.findAllByMember(member);
        Map<Long, List<TourPost>> placeTourPostMap = new HashMap<>();
        Set<ReviewTourPost> tourPostsRelated = new HashSet<>();
        reviews.forEach(review -> {
            tourPostsRelated.addAll(reviewTourPostRepository.findAllByReview(review));
            reviewService.deleteReview(memberId, review.getId());
        });

        if(!tourPostsRelated.isEmpty()) {
            placeTourPostMap = tourPostsRelated.stream()
                    .map(ReviewTourPost::getTourPost)
                    .collect(Collectors.groupingBy(TourPost::getCommonPlaceId));

            placeTourPostMap.forEach(
                    (commonPlaceId, tourposts) -> {
                        tourposts.forEach(tourpost -> {
                            publisher.publishEvent(new TourPostCountEvent(this, tourpost));
                        });
                    }
            );
        }

        tourPostSaves.forEach(tourPostSave -> tourPostService.deleteCollectionTourPost(memberId, tourPostSave.getTourPost().getContentId()));


        if(member.getProfileUrl()!=null) {
            s3Service.deleteFile(member.getProfileUrl());
        }

        memberRepository.deleteById(memberId);
        publisher.publishEvent(new MemberDeleteEvent(this, member));
    }

    public Member findByEmail(String email) {
        return memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
    }

    public Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() ->new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
    }

    public Member findServerMember(String email) {
        return memberRepository.findMemberByEmailAndSocialProvider(email, SocialProvider.SERVER)
                .orElseThrow(() ->  new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
    }

    public MemberInfo getMemberInfo(Long memberId){
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        return new MemberInfo(member);
    }

    public Page<MemberDto> searchMemberByNickname(String nickname, Pageable pageable){
        Page<Member> members = memberRepository.searchMember(nickname, pageable);

        return new PageImpl<>(
        members.stream().map(MemberDto::new).collect(Collectors.toList()),
                members.getPageable(),
                members.getTotalElements()
        );
    }



}

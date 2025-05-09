package team.klover.server.domain.community.commPost.serviceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.klover.server.domain.community.commPost.dto.req.CommPostForm;
import team.klover.server.domain.community.commPost.dto.req.XYForm;
import team.klover.server.domain.community.commPost.dto.res.CombinedPostResponse;
import team.klover.server.domain.community.commPost.dto.res.CommPostDto;
import team.klover.server.domain.community.commPost.dto.res.DetailCommPostDto;
import team.klover.server.domain.community.commPost.entity.*;
import team.klover.server.domain.community.commPost.enums.CommPostSort;
import team.klover.server.domain.community.commPost.event.CommPostLikedEvent;
import team.klover.server.domain.community.commPost.repository.CommPostLikeRepository;
import team.klover.server.domain.community.commPost.repository.CommPostRepository;
import team.klover.server.domain.community.commPost.repository.CommPostSaveRepository;
import team.klover.server.domain.community.commPost.service.CommPostService;
import team.klover.server.domain.community.comment.repository.CommentRepository;
import team.klover.server.domain.community.comment.service.CommentService;
import team.klover.server.domain.member.v1.entity.Member;
import team.klover.server.domain.member.v1.enums.Country;
import team.klover.server.domain.member.v1.repository.MemberV1Repository;
import team.klover.server.domain.tour.tourPost.dto.res.TourPostDto;
import team.klover.server.domain.tour.tourPost.service.TourPostService;
import team.klover.server.global.elasticsearch.commpost.springevent.event.CommPostCountEvent;
import team.klover.server.global.elasticsearch.commpost.springevent.event.CommPostDeleteEvent;
import team.klover.server.global.elasticsearch.commpost.springevent.event.CommPostUpdateEvent;
import team.klover.server.global.exception.KloverRequestException;
import team.klover.server.global.exception.ReturnCode;
import team.klover.server.global.s3.S3Service;
import team.klover.server.global.util.AuthUtil;
import team.klover.server.global.util.LanguageDetect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommPostServiceImpl implements CommPostService {
    private final CommPostRepository commPostRepository;
    private final MemberV1Repository memberV1Repository;
    private final TourPostService tourPostService;
    private final LanguageDetect languageDetect;
    private final ApplicationEventPublisher publisher;
    private final CommentService commentService;
    private final S3Service s3Service;
    private final CommPostLikeRepository commPostLikeRepository;
    private final CommentRepository commentRepository;
    private final CommPostSaveRepository commPostSaveRepository;

    // 사용자 위치 주변 게시글(관광지&사용자) 조회
    @Override
    @Transactional(readOnly = true)
    public CombinedPostResponse findPostsWithinRadius(@Valid XYForm xyForm, Pageable pageable){
        checkPageSize(pageable.getPageSize());
        Page<CommPostDto> commPosts = commPostRepository.findPostsWithinRadius(
                xyForm.getMapX(), xyForm.getMapY(), xyForm.getRadius(), pageable
        ).map(this::convertToCommPostDto);

        Page<TourPostDto> tourPosts = tourPostService.findPostsWithinRadius(xyForm, pageable);

        return new CombinedPostResponse(commPosts, tourPosts);
    }

    // 해당 사용자가 작성한 게시글 조회
    @Override
    @Transactional(readOnly = true)
    public Page<DetailCommPostDto> findByMemberId(Long memberId, Pageable pageable){
        checkPageSize(pageable.getPageSize());
        Page<CommPost> commPosts = commPostRepository.findByMemberId(memberId, pageable);
        return commPosts.map(this::convertToDetailCommPostDto);
    }

    // 해당 게시글 상세 조회
    @Override
    @Transactional(readOnly = true)
    public DetailCommPostDto findById(Long commPostId){
        CommPost commPost = commPostRepository.findById(commPostId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        return convertToDetailCommPostDto(commPost);
    }

    // 해당 사용자가 저장한 게시글 조회
    @Override
    @Transactional(readOnly = true)
    public Page<CommPostDto> getSavedCommPostByMember(Long memberId, Pageable pageable){
        checkPageSize(pageable.getPageSize());
        Page<CommPost> commPosts = commPostRepository.findSavedCommPostByMemberId(memberId, pageable);
        return commPosts.map(this::convertToCommPostDto);
    }

    // 사용자 닉네임 & 게시글 내용 검색
    @Override
    @Transactional(readOnly = true)
    public Page<CommPostDto> searchByKeyword(String keyword, Pageable pageable){
        checkPageSize(pageable.getPageSize());
        Page<CommPost> commPosts = commPostRepository.searchByKeyword(keyword, pageable);
        return commPosts.map(this::convertToCommPostDto);
    }

    // 해당 게시글 저장
    @Override
    @Transactional
    public void addCollectionCommPost(Long currentMemberId, Long commPostId){
        Member member = memberV1Repository.findById(currentMemberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        CommPost commPost = commPostRepository.findById(commPostId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        boolean alreadySaved = commPost.getSavedMembers()
                .stream()
                .anyMatch(commPostSave -> commPostSave.getMember() !=null && commPostSave.getMember().getId().equals(member.getId()));
        if (alreadySaved) {
            throw new KloverRequestException(ReturnCode.ALREADY_EXIST);
        }
        CommPostSave commPostSave = new CommPostSave(member, commPost);
        commPost.getSavedMembers().add(commPostSave);
    }

    // 해당 게시글 저장 취소
    @Override
    @Transactional
    public void deleteCollectionCommPost(Long currentMemberId, Long commPostId){
        CommPost commPost = commPostRepository.findById(commPostId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        Member member = memberV1Repository.findById(currentMemberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        CommPostSave commPostSave = commPost.getSavedMembers()
                .stream()
                .filter(m -> m.getMember() != null && m.getMember().getId().equals(currentMemberId))
                .findFirst()
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        commPost.getSavedMembers().remove(commPostSave);
    }

    // 게시글 좋아요
    @Override
    @Transactional
    public void addCommPostLike(Long memberId, Long id){
        Member member = memberV1Repository.findById(memberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        CommPost commPost = commPostRepository.findById(id).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        boolean alreadySaved = commPost.getLikedMembers()
                .stream()
                .anyMatch(commPostLike -> commPostLike.getMember() != null && commPostLike.getMember().getId().equals(member.getId()));
        if (alreadySaved) {
            throw new KloverRequestException(ReturnCode.ALREADY_EXIST);
        }
        CommPostLike commPostLike = new CommPostLike(member, commPost);
        commPost.getLikedMembers().add(commPostLike);

        // 이벤트 발행 및 생성(알림 + 엘라스틱서치)
        publisher.publishEvent(new CommPostLikedEvent(this, commPost, member));

        publisher.publishEvent(new CommPostCountEvent(this, commPost));
    }


    // 게시글 좋아요 취소
    @Override
    @Transactional
    public void deleteCommPostLike(Long currentMemberId, Long commPostId){
        CommPost commPost = commPostRepository.findById(commPostId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        Member member = memberV1Repository.findById(currentMemberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        CommPostLike commPostLike = commPost.getLikedMembers()
                .stream()
                .filter(m -> m.getMember()!=null && m.getMember().getId().equals(currentMemberId))
                .findFirst()
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        commPost.getLikedMembers().remove(commPostLike);

        publisher.publishEvent(new CommPostCountEvent(this, commPost ));
    }


    // 게시글 생성
    @Override
    @Transactional
    public CommPostDto addCommPost(Long currentMemberId, @Valid CommPostForm commPostForm, List<MultipartFile> imageFiles) {
        // 현재 로그인한 사용자의 member 객체를 가져오는 메서드
        Member member = memberV1Repository.findById(currentMemberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        // 입력 받은 이미지들 S3에 저장
        List<String> imageUrls = new ArrayList<>();
        if (imageFiles.size() > 4 || imageFiles.isEmpty()) {
            throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);
        } else{
            for (MultipartFile imageFile : imageFiles) {
                try {
                    String imageUrl = s3Service.uploadFile(imageFile, "commPost-images");
                    imageUrls.add(imageUrl);
                } catch (IOException e) {
                    throw new KloverRequestException(ReturnCode.INTERNAL_ERROR);
                }
            }
        }
        Country country = languageDetect.execute(commPostForm.getContent());
        CommPost commPost = CommPost.builder()
                .member(member)
                .content(commPostForm.getContent())
                .mapX(commPostForm.getMapX())
                .mapY(commPostForm.getMapY())
                .imageUrls(imageUrls)
                .language(country)
                .build();
        CommPost post = commPostRepository.save(commPost);

        return convertToCommPostDto(post);
    }

    // 해당 게시글 수정
    @Override
    @Transactional
    public void updateCommPost(Long currentMemberId, Long commPostId, @Valid CommPostForm commPostForm, List<MultipartFile> imageFiles) {
        CommPost commPost = commPostRepository.findById(commPostId)
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        // 작성자 검증 - 현재 로그인한 사용자의 ID를 가져와서 검증
        Member member = memberV1Repository.findById(currentMemberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        if (!commPost.getMember().getId().equals(member.getId())) {
            throw new KloverRequestException(ReturnCode.NOT_AUTHORIZED);
        }

        // 기존 이미지들 삭제 후 입력 받은 이미지들 S3에 저장
        List<String> imageUrls = new ArrayList<>();
        if(imageFiles != null && !imageFiles.isEmpty()) {
            s3Service.deleteAllFile(commPost.getImageUrls());
            imageUrls = new ArrayList<>();
            for (MultipartFile imageFile : imageFiles) {
                try {
                    String imageUrl = s3Service.uploadFile(imageFile, "commPost-images");
                    imageUrls.add(imageUrl);
                } catch (IOException e) {
                    throw new KloverRequestException(ReturnCode.INTERNAL_ERROR);
                }
            }
        }
        commPost.setMapX(commPostForm.getMapX());
        commPost.setMapY(commPostForm.getMapY());
        commPost.setContent(commPostForm.getContent());
        if(!imageUrls.isEmpty()) {
            commPost.setImageUrls(imageUrls);
        }
        //language는 작성 당시의 language만을 따라갑니다.
        commPostRepository.save(commPost);

        //게시글 수정 이벤트
        publisher.publishEvent(new CommPostUpdateEvent(this,commPost));

    }

    // 해당 게시글 수정
    @Transactional
    public void updateCommPostTest(Long commPostId, @Valid CommPostForm commPostForm, List<MultipartFile> imageFiles){
        CommPost commPost = commPostRepository.findById(commPostId)
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        // 기존 이미지들 삭제 후 입력 받은 이미지들 S3에 저장
        List<String> imageUrls = new ArrayList<>();
        if(imageFiles != null && !imageFiles.isEmpty()) {
            s3Service.deleteAllFile(commPost.getImageUrls());
            imageUrls = new ArrayList<>();
            for (MultipartFile imageFile : imageFiles) {
                try {
                    String imageUrl = s3Service.uploadFile(imageFile, "commPost-images");
                    imageUrls.add(imageUrl);
                } catch (IOException e) {
                    throw new KloverRequestException(ReturnCode.INTERNAL_ERROR);
                }
            }
        }

        commPost.setMapX(commPostForm.getMapX());
        commPost.setMapY(commPostForm.getMapY());
        commPost.setContent(commPostForm.getContent());
        if(!imageUrls.isEmpty()) {
            commPost.setImageUrls(imageUrls);
        }
        //language는 작성 당시의 language만을 따라갑니다.
        commPostRepository.save(commPost);

        //게시글 수정 이벤트
        publisher.publishEvent(new CommPostUpdateEvent(this,commPost));
    }

    // 해당 게시글 삭제
    @Override
    @Transactional
    public void deleteCommPost(Long currentMemberId, Long commPostId){
        CommPost commPost = commPostRepository.findById(commPostId)
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        // 작성자 검증 - 현재 로그인한 사용자의 ID를 가져와서 검증
        Member member = memberV1Repository.findById(currentMemberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        if (!commPost.getMember().getId().equals(member.getId())) {
            throw new KloverRequestException(ReturnCode.NOT_AUTHORIZED);
        }
        commentService.deleteAllComments(commPostId);
        s3Service.deleteAllFile(commPost.getImageUrls());
        commPostRepository.delete(commPost);

        //커뮤니티 게시글 삭제 이벤트
        publisher.publishEvent(new CommPostDeleteEvent(this, commPost));
    }

    @Transactional
    public void deleteCommPostTest(Long commPostId){
        CommPost commPost = commPostRepository.findById(commPostId)
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        commentService.deleteAllComments(commPostId);
        s3Service.deleteAllFile(commPost.getImageUrls());
        commPostRepository.delete(commPost);

        //커뮤니티 게시글 삭제 이벤트
        publisher.publishEvent(new CommPostDeleteEvent(this, commPost));
    }

    // 요청 페이지 수 제한
    public void checkPageSize(int pageSize) {
        int maxPageSize = CommPostPage.getMaxPageSize();
        if (pageSize > maxPageSize) {
            throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);
        }
    }

    // CommPost를 CommPostDto로 변환
    private CommPostDto convertToCommPostDto(CommPost commPost) {
        return CommPostDto.builder()
                .id(commPost.getId())
                .memberId(commPost.getMember().getId())
                .nickname(commPost.getMember().getNickname())
                .mapX(commPost.getMapX())
                .mapY(commPost.getMapY())
                .imageUrls(commPost.getImageUrls())
                .createDate(commPost.getCreateDate())
                .build();
    }

    // CommPost를 DetailCommPostDto로 변환
    private DetailCommPostDto convertToDetailCommPostDto(CommPost commPost) {
        Long currentMemberId = AuthUtil.getCurrentMemberIdRoughly();
        Boolean isLiked = currentMemberId != null && commPostLikeRepository.haveLiked(commPost.getId(), currentMemberId).isPresent();
        Boolean isSaved = currentMemberId != null && commPostSaveRepository.haveSaved(commPost.getId(), currentMemberId).isPresent();

        return DetailCommPostDto.builder()
                .id(commPost.getId())
                .memberId(commPost.getMember().getId())
                .profileImageUrl(commPost.getMember().getProfileUrl())
                .nickname(commPost.getMember().getNickname())
                .likeCount(commPost.getLikedMembers().size())
                .commentCount(commentRepository.countCommPostComment(commPost.getId()))
                .isLiked(isLiked)
                .isSaved(isSaved)
                .mapX(commPost.getMapX())
                .mapY(commPost.getMapY())
                .content(commPost.getContent())
                .imageUrls(commPost.getImageUrls())
                .createDate(commPost.getCreateDate())
                .build();
    }

    public Page<CommPostDto> search(String keyword, Pageable pageable, Double mapX, Double mapY, Country language, boolean searchByContent, boolean searchByNickname, CommPostSort sort){
        checkPageSize(pageable.getPageSize());
        return commPostRepository.search(keyword, pageable, mapX, mapY, language, searchByContent, searchByNickname, sort)
                .map(this::convertToCommPostDto);
    }
}

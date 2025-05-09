package team.klover.server.domain.community.commPost.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team.klover.server.domain.community.commPost.dto.req.CommPostForm;
import team.klover.server.domain.community.commPost.dto.req.XYForm;
import team.klover.server.domain.community.commPost.dto.res.CombinedPostResponse;
import team.klover.server.domain.community.commPost.dto.res.CommPostDto;
import team.klover.server.domain.community.commPost.dto.res.DetailCommPostDto;
import team.klover.server.domain.community.commPost.entity.CommPostPage;
import team.klover.server.domain.community.commPost.enums.CommPostSort;
import team.klover.server.domain.community.commPost.service.CommPostService;
import team.klover.server.domain.member.v1.enums.Country;
import team.klover.server.global.common.response.ApiResponse;
import team.klover.server.global.common.response.KloverPage;
import team.klover.server.global.elasticsearch.commpost.service.CommPostDocService;
import team.klover.server.global.exception.KloverRequestException;
import team.klover.server.global.exception.ReturnCode;
import team.klover.server.global.util.AuthUtil;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value="/api/v1/comm-post",produces = APPLICATION_JSON_VALUE)
@Tag(name="ApiV1CommPostController",description = "CommPost API")
@RequiredArgsConstructor
public class ApiV1CommPostController {
    private final CommPostService commPostService;
    private final CommPostDocService commPostDocService;

    // 사용자 위치 주변 게시글(관광지&사용자) 조회
    // http://localhost:8080/api/v1/comm-post/surroundings
    @GetMapping("/surroundings")
    @Operation(summary = "사용자 위치 주변 게시글(관광지&사용자) 조회")
    public CombinedPostResponse getSurroundings(@ModelAttribute CommPostPage request, @RequestBody @Valid XYForm xyForm) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return commPostService.findPostsWithinRadius(xyForm, pageable);
    }

    // 해당 사용자가 작성한 게시글 조회
    // http://localhost:8080/api/v1/comm-post/1
    @GetMapping("/post/{memberId}")
    @Operation(summary = "본인 게시글 조회")
    public ApiResponse<DetailCommPostDto> getMemberCommPost(@ModelAttribute CommPostPage request, @PathVariable("memberId") Long memberId) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return ApiResponse.of(KloverPage.of(commPostService.findByMemberId(memberId, pageable)));
    }

    // 해당 게시글 상세 조회
    // http://localhost:8080/api/v1/comm-post/detail/1
    @GetMapping("/detail/{commPostId}")
    @Operation(summary = "해당 게시글 상세 조회")
    public ApiResponse<DetailCommPostDto> getDetailCommPost(@PathVariable("commPostId") Long commPostId) {
        return ApiResponse.of(commPostService.findById(commPostId));
    }

    // 해당 사용자가 저장한 게시글 조회
    // http://localhost:8080/api/v1/comm-post/collection
    @GetMapping("/collection/{memberId}")
    @Operation(summary = "사용자가 저장한 게시글 조회")
    public ApiResponse<CommPostDto> getMemberCollectionCommPost(@ModelAttribute CommPostPage request, @PathVariable("memberId") Long memberId) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return ApiResponse.of(KloverPage.of(commPostService.getSavedCommPostByMember(memberId, pageable)));
    }

    // 사용자 닉네임 & 게시글 내용 검색
    // http://localhost:8080/api/v1/comm-post?keyword=게시글
    @GetMapping
    @Operation(summary = "사용자 닉네임 & 게시글 내용 검색")
    public ApiResponse<CommPostDto> searchCommPost(@ModelAttribute CommPostPage request, @RequestParam("keyword") String keyword) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return ApiResponse.of(KloverPage.of(commPostService.searchByKeyword(keyword, pageable)));
    }

    // 해당 게시글 저장
    // http://localhost:8080/api/v1/comm-post/collection/1
    @PostMapping("/collection/{commPostId}")
    @Operation(summary = "해당 게시글 저장")
    public ApiResponse<String> addCollectionCommPost(@PathVariable("commPostId") Long commPostId){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        commPostService.addCollectionCommPost(currentMemberId, commPostId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 해당 게시글 저장 취소
    // http://localhost:8080/api/v1/comm-post/collection/1
    @DeleteMapping("/collection/{commPostId}")
    @Operation(summary = "해당 게시글 저장 취소")
    public ApiResponse<String> deleteCollectionCommPost(@PathVariable("commPostId") Long commPostId){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        commPostService.deleteCollectionCommPost(currentMemberId, commPostId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 게시글 좋아요
    // http://localhost:8080/api/v1/comm-post/like/1
    @PostMapping("/like/{commPostId}")
    @Operation(summary = "게시글 좋아요")
    public ApiResponse<String> addCommPostLike(@PathVariable("commPostId") Long commPostId){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        commPostService.addCommPostLike(currentMemberId, commPostId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }


    // 게시글 좋아요 취소
    // http://localhost:8080/api/v1/comm-post/like/1
    @DeleteMapping("/like/{commPostId}")
    @Operation(summary = "게시글 좋아요 취소")
    public ApiResponse<String> deleteCommPostLike(@PathVariable("commPostId") Long commPostId){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        commPostService.deleteCommPostLike(currentMemberId, commPostId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }


    // 게시글 생성
    // http://localhost:8080/api/v1/comm-post
    @PostMapping
    @Operation(summary = "게시글 생성")
    public ApiResponse<CommPostDto> addCommPost(@RequestPart(value = "commPostForm") @Valid CommPostForm commPostForm,
                                           @RequestPart(value = "imageFile") List<MultipartFile> imageFiles) {
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        CommPostDto dto = commPostService.addCommPost(currentMemberId, commPostForm, imageFiles);
        return ApiResponse.of(dto);
    }

    // 해당 게시글 수정
    // http://localhost:8080/api/v1/comm-post/1
    @PutMapping("/{commPostId}")
    @Operation(summary = "게시글 수정")
    public ApiResponse updateCommPost(@PathVariable("commPostId") Long commPostId,
                                              @RequestPart(value = "commPostForm") @Valid CommPostForm commPostForm,
                                              @RequestPart(value = "imageFile", required = false) List<MultipartFile> imageFiles) {
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        commPostService.updateCommPost(currentMemberId, commPostId, commPostForm, imageFiles);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 해당 게시글 수정
    // http://localhost:8080/api/v1/comm-post/1
    @PutMapping("/updateTest/{commPostId}")
    @Operation(summary = "게시글 수정")
    public ApiResponse updateCommPostTest(@PathVariable("commPostId") Long commPostId,
                                      @RequestPart(value = "commPostForm") @Valid CommPostForm commPostForm,
                                      @RequestPart(value = "imageFile", required = false) List<MultipartFile> imageFiles) {
        commPostService.updateCommPostTest(commPostId, commPostForm, imageFiles);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 해당 게시글 삭제
    // http://localhost:8080/api/v1/comm-post/1
    @DeleteMapping("/{commPostId}")
    @Operation(summary = "게시글 삭제")
    public ApiResponse<String> deleteCommPost(@PathVariable("commPostId") Long commPostId) {
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        commPostService.deleteCommPost(currentMemberId, commPostId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 해당 게시글 삭제
    // http://localhost:8080/api/v1/comm-post/1
    @DeleteMapping("/deleteTest/{commPostId}")
    @Operation(summary = "게시글 삭제")
    public ApiResponse<String> deleteCommPostTest(@PathVariable("commPostId") Long commPostId) {
        commPostService.deleteCommPostTest(commPostId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    //http://localhost:8080/api/v1/comm-post/search
    @GetMapping("/search")
    @Operation(summary = "게시글 검색(엘라스틱서치)")
    public ApiResponse<CommPostDto> search(@RequestParam(value = "page",defaultValue = "0") int page,
                                               @RequestParam(value = "size",defaultValue = "20") int size,
                                               @RequestParam(value = "keyword",defaultValue = "")String keyword,
                                               @RequestParam(value = "sort", required = false)CommPostSort sort,
                                               @RequestParam(value = "language")Country language,
                                               @RequestParam(value = "content", defaultValue = "false") boolean searchByContent,
                                               @RequestParam(value = "nickname", defaultValue = "false") boolean searchByNickname,
                                               @RequestParam(value = "mapX", required = false) Double mapX,
                                               @RequestParam(value = "mapY", required = false) Double mapY){
        if(page < 0 || size <= 0){
            throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);
        }

        if(sort!=null && sort.equals(CommPostSort.DISTANCE) && (mapX ==null || mapY==null)){
            throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);
        }

        Pageable pageable = PageRequest.of(page, size);
        //둘 중 하나라도 true가 아니고 keyword가 안 비었다면
        if(!(searchByContent || searchByNickname) && keyword !=null && !keyword.isBlank()) searchByContent = true;

        Page<CommPostDto> list = commPostDocService.search(keyword,pageable,mapX,mapY,language,searchByContent,searchByNickname,sort);
        KloverPage<CommPostDto> kloverPage = KloverPage.of(list);
        return ApiResponse.of(kloverPage);
    }

    //http://localhost:8080/api/v1/comm-post/search
    @GetMapping("/searchQueryDsl")
    @Operation(summary = "게시글 검색(쿼리DSL)")
    public ApiResponse<CommPostDto> searchQueryDsl(@RequestParam(value = "page",defaultValue = "0") int page,
                                           @RequestParam(value = "size",defaultValue = "20") int size,
                                           @RequestParam(value = "keyword",defaultValue = "")String keyword,
                                           @RequestParam(value = "sort", required = false)CommPostSort sort,
                                           @RequestParam(value = "language")Country language,
                                           @RequestParam(value = "content", defaultValue = "false") boolean searchByContent,
                                           @RequestParam(value = "nickname", defaultValue = "false") boolean searchByNickname,
                                           @RequestParam(value = "mapX", required = false) Double mapX,
                                           @RequestParam(value = "mapY", required = false) Double mapY){
        if(page < 0 || size <= 0){
            throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);
        }

        if(sort!=null && sort.equals(CommPostSort.DISTANCE) && (mapX ==null || mapY==null)){
            throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);
        }

        Pageable pageable = PageRequest.of(page, size);
        //둘 중 하나라도 true가 아니고 keyword가 안 비었다면
        if(!(searchByContent || searchByNickname) && keyword !=null && !keyword.isBlank()) searchByContent = true;

        Page<CommPostDto> list = commPostService.search(keyword,pageable,mapX,mapY,language,searchByContent,searchByNickname,sort);
        KloverPage<CommPostDto> kloverPage = KloverPage.of(list);
        return ApiResponse.of(kloverPage);
    }
}

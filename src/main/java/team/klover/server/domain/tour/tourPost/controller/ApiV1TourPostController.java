package team.klover.server.domain.tour.tourPost.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import team.klover.server.domain.community.commPost.enums.CommPostSort;
import team.klover.server.domain.member.v1.enums.Country;
import team.klover.server.domain.tour.enums.Area;
import team.klover.server.domain.tour.enums.ContentType;
import team.klover.server.domain.tour.enums.TourPostSort;
import team.klover.server.domain.tour.tourPost.dto.res.DetailTourPostDto;
import team.klover.server.domain.tour.tourPost.dto.res.TourPostDto;
import team.klover.server.domain.tour.tourPost.entity.TourPostPage;
import team.klover.server.domain.tour.tourPost.service.TourPostService;
import team.klover.server.global.common.response.ApiResponse;
import team.klover.server.global.common.response.KloverPage;
import team.klover.server.global.elasticsearch.tourpost.service.TourPostDocService;
import team.klover.server.global.exception.KloverRequestException;
import team.klover.server.global.exception.ReturnCode;
import team.klover.server.global.util.AuthUtil;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value="/api/v1/tour-post",produces = APPLICATION_JSON_VALUE)
@Tag(name="ApiV1TourPostController",description = "TourPost API")
@RequiredArgsConstructor
public class ApiV1TourPostController {
    private final TourPostService tourPostService;
    private final TourPostDocService tourPostDocService;
    // areaCode: 서울(1) 인천(2) 부산(6) 제주(39)
    // language: KorService1(한국어) EngService1(영어) JpnService1(일본어) ChsService1(중국어간체)

    // 사용자 언어 & 지역기반 관광지 데이터 조회
    // http://localhost:8080/api/v1/tour-post/EN/1?page=0&size=15
    @GetMapping("/{language}/{areaCode}")
    @Operation(summary = "사용자 언어 & 지역기반 관광지 데이터 조회")
    public ApiResponse<TourPostDto> getAreaPost(@ModelAttribute TourPostPage request, @PathVariable("language") String language,
                                                @PathVariable("areaCode") String areaCode) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return ApiResponse.of(KloverPage.of(tourPostService.findByLanguageAndAreaCode(language, areaCode, pageable)));
    }

    // 해당 관광지 상세 조회
    // http://localhost:8080/api/v1/tour-post/detail/264107
    @GetMapping("/detail/{contentId}")
    @Operation(summary = "해당 관광지 상세 조회")
    public ApiResponse<DetailTourPostDto> getDetailTourPost(@PathVariable("contentId") Long contentId) {
        return ApiResponse.of(tourPostService.findByContentId(contentId));
    }

    // 해당 사용자가 저장한 관광지 조회
    // http://localhost:8080/api/v1/tour-post/collection?page=0&size=15
    @GetMapping("/collection/{memberId}")
    @Operation(summary = "사용자가 저장한 관광지 조회")
    public ApiResponse<TourPostDto> getMemberCollectionTourPost(@ModelAttribute TourPostPage request, @PathVariable("memberId") Long memberId) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return ApiResponse.of(KloverPage.of(tourPostService.getSavedTourPostByMember(memberId, pageable)));
    }

    // 사용자 언어 & 관광지명/지역명 검색
    // http://localhost:8080/api/v1/tour-post/EN?keyword=압구정&page=0&size=15
    @GetMapping("/{language}")
    @Operation(summary = "사용자 언어 & 관광지명/지역명 검색")
    public ApiResponse<TourPostDto> searchTourPost(@ModelAttribute TourPostPage request, @PathVariable("language") String language,
                                                   @RequestParam("keyword") String keyword) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return ApiResponse.of(KloverPage.of(tourPostService.searchByLanguageAndKeyword(language, keyword, pageable)));
    }

    // 해당 관광지 저장
    // http://localhost:8080/api/v1/tour-post/collection/264107
    @PostMapping("/collection/{contentId}")
    @Operation(summary = "해당 관광지 저장")
    public ApiResponse<String> addCollectionTourPost(@PathVariable("contentId") Long contentId){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        tourPostService.addCollectionTourPost(currentMemberId, contentId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 해당 관광지 저장 취소
    // http://localhost:8080/api/v1/tour-post/collection/264107
    @DeleteMapping("/collection/{contentId}")
    @Operation(summary = "해당 관광지 저장 취소")
    public ApiResponse<String> deleteCollectionTourPost(@PathVariable("contentId") Long contentId){
        Long currentMemberId = AuthUtil.getCurrentMemberId();
        tourPostService.deleteCollectionTourPost(currentMemberId, contentId);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }


    // http://localhost:8080/api/v1/tour-post/search
    @GetMapping("/search")
    public ApiResponse<TourPostDto> search(@RequestParam(value = "page",defaultValue = "0") int page,
                                         @RequestParam(value = "size",defaultValue = "20") int size,
                                         @RequestParam(value = "keyword",defaultValue = "")String keyword,
                                         @RequestParam(value = "sort", required = false) TourPostSort sort,
                                         @RequestParam(value = "language") Country language,
                                         @RequestParam(value = "area",required = false) Area area,
                                         @RequestParam(value = "contenttype",required = false) ContentType contentType,
                                         @RequestParam(value = "title", defaultValue = "false") boolean searchByTitle,
                                         @RequestParam(value = "overview", defaultValue = "false") boolean searchByOverview,
                                         @RequestParam(value = "exotic", defaultValue = "false") boolean hasExotic,
                                         @RequestParam(value = "healing", defaultValue = "false") boolean hasHealing,
                                         @RequestParam(value = "active", defaultValue = "false") boolean hasActive,
                                         @RequestParam(value = "traditional", defaultValue = "false") boolean hasTraditional,
                                         @RequestParam(value = "mapX", required = false) Double mapX,
                                         @RequestParam(value = "mapY", required = false) Double mapY){
        if(page<0 || size<=0) throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);

        if(sort!=null && sort.equals(TourPostSort.DISTANCE) && (mapX == null || mapY == null)){
            throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);
        }

        //둘 중 하나라도 true가 아니고 keyword가 안 비었다면
        if(!(searchByTitle || searchByOverview) && keyword !=null && !keyword.isBlank()) searchByTitle = true;
//        if(area == null && contentType == null) area = Area.SEOUL;

        Pageable pageable = PageRequest.of(page,size);
        Page<TourPostDto> list = tourPostDocService.search(keyword,pageable,mapX,mapY,language,area,contentType,hasExotic,hasHealing,hasTraditional,hasActive,searchByTitle,searchByOverview,sort);
        return ApiResponse.of(KloverPage.of(list));
    }

    // http://localhost:8080/api/v1/tour-post/searchDsl
    @GetMapping("/searchDsl")
    public ApiResponse<TourPostDto> searchDsl(@RequestParam(value = "page",defaultValue = "0") int page,
                                              @RequestParam(value = "size",defaultValue = "20") int size,
                                              @RequestParam(value = "keyword",defaultValue = "")String keyword,
                                              @RequestParam(value = "sort", required = false) TourPostSort sort,
                                              @RequestParam(value = "language") Country language,
                                              @RequestParam(value = "area",required = false) Area area,
                                              @RequestParam(value = "contenttype",required = false) ContentType contentType,
                                              @RequestParam(value = "title", defaultValue = "false") boolean searchByTitle,
                                              @RequestParam(value = "overview", defaultValue = "false") boolean searchByOverview,
                                              @RequestParam(value = "exotic", defaultValue = "false") boolean hasExotic,
                                              @RequestParam(value = "healing", defaultValue = "false") boolean hasHealing,
                                              @RequestParam(value = "active", defaultValue = "false") boolean hasActive,
                                              @RequestParam(value = "traditional", defaultValue = "false") boolean hasTraditional,
                                              @RequestParam(value = "mapX", required = false) Double mapX,
                                              @RequestParam(value = "mapY", required = false) Double mapY){
       // System.out.println("메서드에 진입");
        if(page<0 || size<=0) {
            throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);
        }

        if(sort!=null && sort.equals(TourPostSort.DISTANCE) && (mapX == null || mapY == null)){
            throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);
        }

        //둘 중 하나라도 true가 아니고 keyword가 안 비었다면
        if(!(searchByTitle || searchByOverview) && keyword !=null && !keyword.isBlank()) searchByTitle = true;

        Pageable pageable = PageRequest.of(page,size);
        Page<TourPostDto> list = tourPostService.search(keyword,pageable,mapX,mapY,language,area,contentType,hasExotic,hasHealing,hasTraditional,hasActive,searchByTitle,searchByOverview,sort);
        return ApiResponse.of(KloverPage.of(list));
    }


}

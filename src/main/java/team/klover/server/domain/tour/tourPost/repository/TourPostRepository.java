package team.klover.server.domain.tour.tourPost.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import team.klover.server.domain.tour.tourPost.entity.TourPost;

import java.util.List;

@Repository
public interface TourPostRepository extends JpaRepository<TourPost, Long>, TourPostRepositoryCustom {
    // 저장된 Apis 데이터에서 선별
    void deleteByCat3NotIn(List<String> cat3List);

    // 관광지별 개요 데이터 추가를 위해 관광지별 고유 ID 가져오기
    @Query("SELECT p.contentId FROM TourPost p WHERE (p.homepage IS NULL OR p.homepage = '') AND (p.overview IS NULL OR p.overview = '')")
    List<Long> findAllContentIds();

    // 관광지별 개요 데이터 추가를 위해 관광지별 고유 ID 가져오기(홈패이지주소&개요 모두 없는 데이터만)
    TourPost findByContentId(Long contentId);

    // 관광지 동일 위치로 데이터 가져오기
    List<TourPost> findByCommonPlaceId(Long commonPlaceId);

    // 사용자 언어 & 지역기반 관광지 데이터 조회
    Page<TourPost> findByLanguageAndAreaCode(String language, String areaCode, Pageable pageable);

    // 사용자 언어 & 관광지명/지역명 검색
    @Query("SELECT t FROM TourPost t WHERE t.language = :language AND (t.title LIKE %:keyword% OR t.addr1 LIKE %:keyword%)")
    Page<TourPost> searchByLanguageAndKeyword(@Param("language") String language, @Param("keyword") String keyword, Pageable pageable);

    // 사용자가 저장한 관광지 조회
    @Query("SELECT p FROM TourPost p JOIN p.savedMembers sm WHERE sm.member.id = :memberId")
    Page<TourPost> findSavedTourPostsByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    // 모든 관광지 데이터 가져오기
    @Query("SELECT t FROM TourPost t")
    List<TourPost> findAllPosts();

    // X, Y 기준 그룹핑 개수가 4개가 아닌 경우 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM TourPost t WHERE t.mapX = :mapX AND t.mapY = :mapY")
    void deleteByMapCoordinates(@Param("mapX") String mapX, @Param("mapY") String mapY);

    @Modifying
    @Transactional
    @Query(value = "CREATE TABLE IF NOT EXISTS tour_post_backup AS " +
            "SELECT * FROM tour_post ORDER BY content_id ASC", nativeQuery = true)
    void backupSortedData();  // 1. 정렬된 데이터를 백업 테이블에 저장

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE review, tour_post RESTART IDENTITY CASCADE", nativeQuery = true)
    void deleteAllPostsAndReviews();  // `tour_post`와 `review` 함께 삭제

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO tour_post SELECT * FROM tour_post_backup", nativeQuery = true)
    void restoreSortedData();  // 3. 정렬된 데이터를 원래 테이블로 복원

    @Modifying
    @Transactional
    @Query(value = "DROP TABLE IF EXISTS tour_post_backup", nativeQuery = true)
    void dropBackupTable();  // 4. 백업 테이블 삭제 (선택 사항)

    // 사용자 위치 주변 게시글(관광지&사용자) 조회
    @Query(value = """
        SELECT * FROM tour_post 
        WHERE earth_distance(ll_to_earth(:mapY, :mapX), ll_to_earth(mapy, mapx)) <= :radius
        ORDER BY create_date DESC
        """, nativeQuery = true)
    Page<TourPost> findPostsWithinRadius(@Param("mapX") Double mapX, @Param("mapY") Double mapY, @Param("radius") Integer radius, Pageable pageable);

}

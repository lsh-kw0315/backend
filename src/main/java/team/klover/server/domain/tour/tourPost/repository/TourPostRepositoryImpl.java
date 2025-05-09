package team.klover.server.domain.tour.tourPost.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import team.klover.server.domain.member.v1.enums.Country;
import team.klover.server.domain.tour.enums.Area;
import team.klover.server.domain.tour.enums.ContentType;
import team.klover.server.domain.tour.enums.TourPostSort;
import team.klover.server.domain.tour.review.entity.QReview;
import team.klover.server.domain.tour.review.entity.QReviewTourPost;
import team.klover.server.domain.tour.tourPost.dto.res.TourPostDto;
import team.klover.server.domain.tour.tourPost.entity.QTourPost;
import team.klover.server.domain.tour.tourPost.entity.TourPost;
import team.klover.server.global.common.constant.SearchConstant;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
public class TourPostRepositoryImpl implements TourPostRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final EntityManager em;


    @Override
    public Page<TourPostDto> search(String keyword, Pageable pageable, Double mapX, Double mapY, Country language, Area area, ContentType contentType, boolean hasExotic, boolean hasHealing, boolean hasTraditional, boolean hasActive, boolean searchByTitle, boolean searchByOverview, TourPostSort sort) {
        QTourPost tourPost = QTourPost.tourPost;
        QReview review = QReview.review;
        QReviewTourPost reviewTourPost = QReviewTourPost.reviewTourPost;


        NumberExpression<Double> distance = null;
        BooleanExpression withinEarthBox = null;
        if(sort == TourPostSort.DISTANCE){
            distance = Expressions.numberTemplate(Double.class,
                    "earth_distance(ll_to_earth({0},{1}), ll_to_earth({2}, {3}))",
                    mapY, mapX,
                    QTourPost.tourPost.mapY, QTourPost.tourPost.mapX
            ); //실제 거리를 계산

            /*
            withinEarthBox = Expressions.booleanTemplate(
                    "ll_to_earth({0}, {1}) <@ earth_box(ll_to_earth({2}, {3}), {4})",
                    QTourPost.tourPost.mapY, QTourPost.tourPost.mapX, // 대상 좌표
                    mapY, mapX, // 기준 좌표
                    5000 // 거리 (미터)
            ); //각 mapX, mapY가 나타내는 3차원 좌표가 earth box의 범위 안에 있는지 없는지 걸러냄
            */

            //XXXTemplate는 주로 SQL에서 사용되는 함수를 QueryDsl에서 사용하기 위해 쓰임.

            /*
            // 기존 earth_distance를 ST_Distance로 변경
            distance = Expressions.numberTemplate(Double.class,
                    "ST_Distance(ST_SetSRID(ST_MakePoint({0}, {1}), 4326), ST_SetSRID(ST_MakePoint({2}, {3}), 4326))",
                    mapX, mapY, // 기준 좌표
                    QTourPost.tourPost.mapX, QTourPost.tourPost.mapY // 대상 좌표
            );



            // ST_DWithin을 사용하여 공간 범위 필터링
            withinEarthBox = Expressions.booleanTemplate(
                    "CAST(ST_DWithin(ST_SetSRID(ST_MakePoint({0}, {1}), 4326), ST_SetSRID(ST_MakePoint({2}, {3}), 4326), {4}) AS boolean)",
                    QTourPost.tourPost.mapX, QTourPost.tourPost.mapY, // 대상 좌표
                    mapX, mapY, // 기준 좌표
                    5000 // 거리 (미터)
            );

             */
        }

        String areaCode = null;
        if(area != null){
            switch (area){
                case SEOUL -> areaCode = SearchConstant.SEOUL;
                case BUSAN -> areaCode = SearchConstant.BUSAN;
                case INCHEON -> areaCode = SearchConstant.INCHEON;
                case JEJU -> areaCode = SearchConstant.JEJU;
            }
        }

        String contentTypeCode = null;
        //ATTRACTION, CULTURAL_FACILITY, DINING, ACCOMMODATION, ACTIVITY, SHOPPING, EVENT
        if(contentType != null){
            if(language.equals(Country.KO)){
                switch (contentType){
                    case EVENT -> contentTypeCode = SearchConstant.EVENT_KOREAN;
                    case ATTRACTION -> contentTypeCode = SearchConstant.ATTRACTION_KOREAN;
                    case CULTURAL_FACILITY -> contentTypeCode = SearchConstant.CULTURAL_FACILITY_KOREAN;
                    case DINING -> contentTypeCode = SearchConstant.DINING_KOREAN;
                    case ACCOMMODATION -> contentTypeCode = SearchConstant.ACCOMMODATION_KOREAN;
                    case ACTIVITY -> contentTypeCode = SearchConstant.ACTIVITY_KOREAN;
                    case SHOPPING -> contentTypeCode = SearchConstant.SHOPPING_KOREAN;
                }
            }else{
                switch (contentType){
                    case EVENT -> contentTypeCode = SearchConstant.EVENT_FOREIGN;
                    case ATTRACTION -> contentTypeCode = SearchConstant.ATTRACTION_FOREIGN;
                    case CULTURAL_FACILITY -> contentTypeCode = SearchConstant.CULTURAL_FACILITY_FOREIGN;
                    case DINING -> contentTypeCode = SearchConstant.DINING_FOREIGN;
                    case ACCOMMODATION -> contentTypeCode = SearchConstant.ACCOMMODATION_FOREIGN;
                    case ACTIVITY -> contentTypeCode = SearchConstant.ACTIVITY_FOREIGN;
                    case SHOPPING -> contentTypeCode = SearchConstant.SHOPPING_FOREIGN;
                }
            }
        }

        Set<String> categories = null;
        if(hasExotic || hasHealing || hasTraditional || hasActive){
            categories = new HashSet<>();
        }

        if(hasExotic){
            categories.addAll(Set.of(SearchConstant.EXOTIC));
        }
        if(hasActive){
            categories.addAll(Set.of(SearchConstant.ACTIVE));
        }
        if(hasHealing){
            categories.addAll(Set.of(SearchConstant.HEALING));
        }
        if(hasTraditional){
            categories.addAll(Set.of(SearchConstant.TRADITIONAL));
        }
        JPAQuery<Long> count =null;
        JPAQuery<TourPostDto> query = null;

        if(distance == null){
            count = queryFactory
                    .select(tourPost.count())
                    .from(tourPost)
                    .where(
                            tourPost.language.eq(language.name()),
                            categories != null ? tourPost.cat3.in(categories) : null,
                            contentTypeCode != null ? tourPost.contentTypeId.eq(contentTypeCode) : null,
                            searchByTitle ? tourPost.title.containsIgnoreCase(keyword) : null,
                            searchByOverview ? tourPost.overview.containsIgnoreCase(keyword) : null
                    );

            query = queryFactory
                    .select(
                            Projections.constructor(
                                    TourPostDto.class,
                                    tourPost.contentId,
                                    tourPost.commonPlaceId,
                                    review.rating.avg().as("avgRating"),
                                    review.count().as("reviewCount"),
                                    tourPost.title,
                                    tourPost.addr1,
                                    tourPost.firstImage,
                                    tourPost.mapX,
                                    tourPost.mapY
                            )
                    )
                    .from(tourPost)
                    .leftJoin(reviewTourPost).on(reviewTourPost.tourPost.contentId.eq(tourPost.contentId))
                    .leftJoin(review).on(reviewTourPost.review.id.eq(review.id))
                    .fetchJoin()
                    .where(
                            tourPost.language.eq(language.name()),
                            categories != null ? tourPost.cat3.in(categories) : null,
                            contentTypeCode != null ? tourPost.contentTypeId.eq(contentTypeCode) : null,
                            areaCode != null ? tourPost.areaCode.eq(areaCode) : null,
                            searchByTitle ? tourPost.title.containsIgnoreCase(keyword) : null,
                            searchByOverview ? tourPost.overview.containsIgnoreCase(keyword) : null
                    )
                    .groupBy(tourPost.commonPlaceId, tourPost.contentId);

            if (sort != null) {
                switch (sort) {
                    case RATING_AVERAGE -> query.orderBy(review.rating.avg().desc());
                    case REVIEW_COUNT -> query.orderBy(review.count().desc());
                }
            }

        }else {
            List<Long> placeIds = em.createNativeQuery("""
  SELECT content_id
  FROM tour_post tp
  WHERE ll_to_earth(tp.mapy, tp.mapx) <@ earth_box(ll_to_earth(:mapY, :mapX), :radius)
""")
                    .setParameter("mapX", mapX)
                    .setParameter("mapY", mapY)
                    .setParameter("radius", 5000)
                    .getResultList();


            count = queryFactory
                    .select(tourPost.count())
                    .from(tourPost)
                    .where(
                            tourPost.contentId.in(placeIds),
                            tourPost.language.eq(language.name()),
                            categories != null ? tourPost.cat3.in(categories) : null,
                            contentTypeCode != null ? tourPost.contentTypeId.eq(contentTypeCode) : null,
                            searchByTitle ? tourPost.title.containsIgnoreCase(keyword) : null,
                            searchByOverview ? tourPost.overview.containsIgnoreCase(keyword) : null
                    );

            query = queryFactory
                    .select(
                            Projections.constructor(
                                    TourPostDto.class,
                                    tourPost.contentId,
                                    tourPost.commonPlaceId,
                                    review.rating.avg().as("avgRating"),
                                    review.count().as("reviewCount"),
                                    tourPost.title,
                                    tourPost.addr1,
                                    tourPost.firstImage,
                                    tourPost.mapX,
                                    tourPost.mapY
                            )
                    )
                    .from(tourPost)
                    .leftJoin(reviewTourPost).on(reviewTourPost.tourPost.contentId.eq(tourPost.contentId))
                    .leftJoin(review).on(reviewTourPost.review.id.eq(review.id))
                    .fetchJoin()
                    .where(
                            tourPost.contentId.in(placeIds),
                            tourPost.language.eq(language.name()),
                            categories != null ? tourPost.cat3.in(categories) : null,
                            contentTypeCode != null ? tourPost.contentTypeId.eq(contentTypeCode) : null,
                            areaCode != null ? tourPost.areaCode.eq(areaCode) : null,
                            searchByTitle ? tourPost.title.containsIgnoreCase(keyword) : null,
                            searchByOverview ? tourPost.overview.containsIgnoreCase(keyword) : null
                    )
                    .groupBy(tourPost.commonPlaceId, tourPost.contentId);

            if (sort != null) {
                switch (sort) {
                    case DISTANCE -> query.orderBy(distance.asc());
                    case RATING_AVERAGE -> query.orderBy(review.rating.avg().desc());
                    case REVIEW_COUNT -> query.orderBy(review.count().desc());
                }
            }

        }

        List<TourPostDto> pageContent = query
                .orderBy(tourPost.createDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(
                pageContent, pageable, count::fetchOne
        );
    }
}

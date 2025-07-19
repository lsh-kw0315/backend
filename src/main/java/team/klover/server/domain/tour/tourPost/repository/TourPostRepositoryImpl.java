package team.klover.server.domain.tour.tourPost.repository;

import com.jooq.project.generated.tables.Review;
import com.jooq.project.generated.tables.ReviewTourPost;
import com.jooq.project.generated.tables.TourPost;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import team.klover.server.global.common.constant.SearchConstant;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TourPostRepositoryImpl implements TourPostRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final EntityManager em;
    private final DSLContext dslContext;

    @Override
    public Page<TourPostDto> search(String keyword, Pageable pageable, Double mapX, Double mapY, Country language, Area area, ContentType contentType, boolean hasExotic, boolean hasHealing, boolean hasTraditional, boolean hasActive, boolean searchByTitle, boolean searchByOverview, TourPostSort sort) {


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

        TourPost tp = TourPost.TOUR_POST.as("tp");
        Review rv = Review.REVIEW.as("rv");
        ReviewTourPost rtp = ReviewTourPost.REVIEW_TOUR_POST.as("rtp");

        List<OrderField<?>> orderList = new ArrayList<>();
        switch (sort){
            case RATING_AVERAGE -> {
                orderList.add(DSL.field("avg_rating", SQLDataType.DOUBLE).desc());
            }
            case REVIEW_COUNT -> {
                orderList.add(DSL.field("review_count", SQLDataType.BIGINT).desc());
            }
            case DISTANCE -> {
                if(mapX != null && mapY != null) {
                    orderList.add(DSL.field(
                            "CAST(ST_SetSRID(ST_MakePoint({0}, {1}), 4326) AS geography) <-> location_earth",
                            mapX, mapY
                    ).asc());
                }
            }
        }
        orderList.add(tp.CONTENT_ID.desc());
        orderList.add(tp.CREATE_DATE.desc());

        List<Condition> conditions = new ArrayList<>();
        conditions.add(tp.LANGUAGE.eq(language.name()));
        if (categories != null) conditions.add(tp.CAT3.in(categories));
        if (contentTypeCode != null) conditions.add(tp.CONTENT_TYPE_ID.eq(contentTypeCode));
        if (searchByTitle) conditions.add(tp.TITLE.containsIgnoreCase(keyword));
        if (searchByOverview) conditions.add(tp.OVERVIEW.containsIgnoreCase(keyword));
        if (areaCode != null) conditions.add(tp.AREA_CODE.eq(areaCode));
        if (mapX != null && mapY != null)
            conditions.add(DSL.condition("ST_DWithin(ST_SetSRID(ST_MakePoint({0}, {1}), 4326)::geography, tp.location_earth, 5000)", mapX, mapY));

        Table<Record9<Long, Long, String, String, String, Double, Double, LocalDateTime, Object>> filteredTourPost =
                 dslContext.select(
                        tp.CONTENT_ID.as("content_id"),
                        tp.COMMON_PLACE_ID.as("common_place_id"),
                        tp.TITLE.as("title"),
                        tp.ADDR1.as("addr1"),
                        tp.FIRST_IMAGE.as("first_image"),
                        tp.MAPX.as("mapx"),
                        tp.MAPY.as("mapy"),
                        tp.CREATE_DATE.as("create_date"),
                        DSL.field("location_earth", Object.class).as("location_earth")
                ).from(tp)
                .where(conditions)
                .asTable("tp");


        Table<Record3<Long, Integer, Long>> reviewAndRtp =
                dslContext.select(
                                rtp.TOUR_POST_ID.as("tp_id"),
                                rv.RATING.as("rating"),
                                rv.ID.as("rv_id")
                        ).from(rv)
                        .leftJoin(rtp).on(rtp.REVIEW_ID.eq(rv.ID)).asTable().as("rv");


        List<TourPostDto> contents = dslContext.select(
                            filteredTourPost.field("content_id", Long.class),
                            filteredTourPost.field("common_place_id", Long.class),
                            DSL.coalesce(DSL.avg(reviewAndRtp.field("rating", Double.class)), BigDecimal.ZERO).as("avg_rating"),
                            DSL.count(reviewAndRtp.field("rv_id", Integer.class)).as("review_count"),
                            filteredTourPost.field("title", String.class),
                            filteredTourPost.field("addr1", String.class),
                            filteredTourPost.field("first_image", String.class),
                            filteredTourPost.field("mapx", Double.class),
                            filteredTourPost.field("mapy", Double.class))
                    .from(
                            filteredTourPost
                    )
                    .leftJoin(
                            reviewAndRtp
                    ).on(filteredTourPost.field("content_id", Long.class).eq(reviewAndRtp.field("tp_id", Long.class)))
                    .groupBy(
                            filteredTourPost.field("content_id", Long.class),
                            filteredTourPost.field("common_place_id", Long.class),
                            filteredTourPost.field("title", String.class),
                            filteredTourPost.field("addr1", String.class),
                            filteredTourPost.field("first_image", String.class),
                            filteredTourPost.field("mapx", Double.class),
                            filteredTourPost.field("mapy", Double.class),
                            filteredTourPost.field("create_date", LocalDateTime.class),
                            filteredTourPost.field("location_earth", Object.class))
                    .orderBy(orderList)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize()).stream().map(row ->
                        TourPostDto.builder()
                                .contentId(row.get("content_id", Long.class))
                                .commonPlaceId(row.get("common_place_id", Long.class))
                                .avgRating(row.get("avg_rating", Double.class) != null
                                        ? row.get("avg_rating", Double.class)
                                        : 0.0)
                                .reviewCount(row.get("review_count", Long.class))
                                .title(row.get("title", String.class))
                                .addr1(row.get("addr1", String.class))
                                .firstImage(row.get("first_image", String.class))
                                .mapX(row.get("mapx", Double.class))
                                .mapY(row.get("mapy", Double.class))
                                .build()
                ).toList();


        Long count = dslContext.select(DSL.count(tp))
                .from(tp)
                .leftJoin(rtp).on(tp.CONTENT_ID.eq(rtp.TOUR_POST_ID))
                .leftJoin(rv).on(rtp.REVIEW_ID.eq(rv.ID))
                .where(
                    conditions
                ).fetchOne(0,Long.class);


/*

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
                                    review.rating.avg().coalesce(0.0).as("avgRating"),
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
  WHERE ll_to_earth(tp.mapx, tp.mapy) <@ earth_box(ll_to_earth(:mapX, :mapY), :radius)
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
                                    review.rating.avg().coalesce(0.0).as("avgRating"),
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

         */

        return new PageImpl<>(
                contents,
                pageable,
                count != null ? count : 0L
        );



    }
}

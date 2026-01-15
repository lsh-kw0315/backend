package team.klover.server.domain.community.commPost.repository;

import com.jooq.project.generated.tables.*;
import com.jooq.project.generated.tables.Comment;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import team.klover.server.domain.community.commPost.dto.res.CommPostDto;
import team.klover.server.domain.community.commPost.entity.QCommPost;
import team.klover.server.domain.community.commPost.entity.QCommPostLike;
import team.klover.server.domain.community.commPost.enums.CommPostSort;
import team.klover.server.domain.community.comment.entity.QComment;
import team.klover.server.domain.member.v1.entity.QMember;
import team.klover.server.domain.member.v1.enums.Country;
import team.klover.server.domain.tour.tourPost.dto.res.TourPostDto;
import team.klover.server.global.common.constant.SearchConstant;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.key;
import static team.klover.server.domain.community.commPost.enums.CommPostSort.COMMENT_COUNT;
import static team.klover.server.domain.community.commPost.enums.CommPostSort.LIKE_COUNT;

@RequiredArgsConstructor
public class CommPostRepositoryImpl implements CommPostRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final EntityManager em;
    private final DSLContext dslContext;

    @Override
    public Page<CommPostDto> search(String keyword, Pageable pageable, Double mapX, Double mapY, Country language, boolean searchByContent, boolean searchByNickname, CommPostSort sort) {


        /*

        QCommPost commPost = QCommPost.commPost;
        QCommPostLike commPostLike = QCommPostLike.commPostLike;
        QComment comment = QComment.comment;
        QMember member = QMember.member;

        NumberExpression<Double> distance = null;

        if(sort == CommPostSort.DISTANCE){
            distance = Expressions.numberTemplate(Double.class,
                    "earth_distance(ll_to_earth({0}, {1}), ll_to_earth({2}, {3}))",
                    mapY, mapX,
                    QCommPost.commPost.mapY, QCommPost.commPost.mapX // 비교할 위도, 경도 (예: 서울)
            );
        }

        JPAQuery<Long> count =null;
        JPAQuery<CommPost> query = null;

        if(distance == null){
            count = queryFactory
                    .select(commPost.count())
                    .from(commPost)
                    .where(
                            commPost.language.eq(language),
                            contentLike(keyword, searchByContent),
                            nicknameLike(keyword, searchByNickname)
                    );

            query = queryFactory
                    .select(commPost)
                    .from(commPost)
                    .leftJoin(commPostLike).on(commPostLike.commPost.eq(commPost))
                    .leftJoin(comment).on(comment.commPost.eq(commPost))
                    .leftJoin(commPost.member, member).fetchJoin()
                    .where(
                            contentLike(keyword, searchByContent),
                            nicknameLike(keyword, searchByNickname),
                            commPost.language.eq(language)
                    )
                    .groupBy(commPost.id, member.id);

            if (sort != null) {
                switch (sort) {
                    case LIKE_COUNT: {
                        query.orderBy(commPostLike.countDistinct().desc());
                        break;
                    }
                    case COMMENT_COUNT: {
                        query.orderBy(comment.countDistinct().desc());
                        break;
                    }
                }
            }

        }else {
            List<Long> ids = em.createNativeQuery("""
  SELECT id
  FROM comm_post cp
  WHERE ll_to_earth(cp.mapy, cp.mapx) <@ earth_box(ll_to_earth(:mapY, :mapX), :radius)
""")
                    .setParameter("mapX", mapX)
                    .setParameter("mapY", mapY)
                    .setParameter("radius", 5000)
                    .getResultList();


            count = queryFactory
                    .select(commPost.count())
                    .from(commPost)
                    .where(
                            commPost.id.in(ids),
                            commPost.language.eq(language),
                            contentLike(keyword, searchByContent),
                            nicknameLike(keyword, searchByNickname)
                    );

            query = queryFactory
                    .select(commPost)
                    .from(commPost)
                    .leftJoin(commPostLike).on(commPostLike.commPost.eq(commPost))
                    .leftJoin(comment).on(comment.commPost.eq(commPost))
                    .leftJoin(commPost.member, member).fetchJoin()
                    .where(
                            commPost.id.in(ids),
                            commPost.language.eq(language),
                            contentLike(keyword, searchByContent),
                            nicknameLike(keyword, searchByNickname)
                    )
                    .groupBy(commPost.id, member.id);

            if (sort != null) {
                switch (sort) {
                    case DISTANCE:{
                        query.orderBy(distance.asc());
                        break;
                    }
                    case LIKE_COUNT: {
                        query.orderBy(commPostLike.countDistinct().desc());
                        break;
                    }
                    case COMMENT_COUNT: {
                        query.orderBy(comment.countDistinct().desc());
                        break;
                    }
                }
            }

        }

        List<CommPost> pageContent = query
                .orderBy(commPost.createDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
         return PageableExecutionUtils.getPage(
                pageContent, pageable, count::fetchOne
        );


         */




        CommPost cp = CommPost.COMM_POST.as("cp");
        CommPostLike cpl = CommPostLike.COMM_POST_LIKE.as("cpl");
        Comment c = Comment.COMMENT.as("c");
        Members m = Members.MEMBERS.as("m");


        List<OrderField<?>> orderList = new ArrayList<>();
        if(sort!=null){
            switch (sort){
                case  COMMENT_COUNT-> {
                    orderList.add(DSL.field("comment_count", SQLDataType.BIGINT).desc());
                }
                case LIKE_COUNT -> {
                    orderList.add(DSL.field("like_count", SQLDataType.BIGINT).desc());
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
        }
        orderList.add(cp.ID.desc());
        orderList.add(cp.CREATE_DATE.desc());

        List<Condition> conditions = new ArrayList<>();
        conditions.add(cp.LANGUAGE.eq(language.name()));
        if (searchByContent) conditions.add(condition("{0} &@~ {1}",cp.CONTENT, keyword));
        if (searchByNickname) conditions.add(DSL.field("nickname").likeIgnoreCase(keyword));
        if (mapX != null && mapY != null)
            conditions.add(condition("ST_DWithin(ST_SetSRID(ST_MakePoint({0}, {1}), 4326)::geography, cp.location_earth, 5000)", mapX, mapY));

        Table<Record8<Long, Long,String, Double, Double, LocalDateTime, JSONB, Object>> filteredCommPost =
                dslContext.select(
                                cp.ID.as("id"),
                                cp.MEMBER_ID.as("member_id"),
                                cp.CONTENT.as("content"),
                                cp.MAPX.as("mapx"),
                                cp.MAPY.as("mapy"),
                                cp.CREATE_DATE.as("create_date"),
                                cp.IMAGE_URLS.as("image_urls"),
                                DSL.field("location_earth", Object.class).as("location_earth")
                        ).from(cp)
                        .where(conditions)
                        .asTable("cp");


        List<CommPostDto> contents = dslContext.select(
                        filteredCommPost.field("id", Long.class),
                        filteredCommPost.field("content", String.class),
                        filteredCommPost.field("member_id", Long.class),
                        DSL.count(cpl.ID).as("like_count"),
                        DSL.count(c.ID).as("comment_count"),
                        filteredCommPost.field("mapx", Double.class),
                        filteredCommPost.field("mapy", Double.class),
                        m.NICKNAME.as("member_nickname"),
                        filteredCommPost.field("create_date", LocalDateTime.class),
                        filteredCommPost.field("image_urls", JSONB.class)
                ).from(
                        filteredCommPost
                )
                .leftJoin(
                        cpl
                ).on(filteredCommPost.field("id", Long.class).eq(cpl.COMM_POST_ID))
                .leftJoin(
                        c
                ).on(filteredCommPost.field("id", Long.class).eq(c.COMM_POST_ID))
                .leftJoin(
                        m
                ).on(filteredCommPost.field("member_id", Long.class).eq(m.ID))

                .groupBy(
                        filteredCommPost.field("id", Long.class),
                        filteredCommPost.field("content", Long.class),
                        filteredCommPost.field("member_id", Long.class),
                        filteredCommPost.field("mapx", Double.class),
                        filteredCommPost.field("mapy", Double.class),
                        m.NICKNAME.as("member_nickname"),
                        filteredCommPost.field("create_date", LocalDateTime.class),
                        filteredCommPost.field("image_urls", JSONB.class))
                .orderBy(orderList)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()).stream().map(row ->
                        new CommPostDto(row.get("id", Long.class)
                                ,row.get("member_id", Long.class)
                                ,row.get("member_nickname", String.class)
                                ,row.get("mapx", Double.class)
                                ,row.get("mapy", Double.class)
                                ,row.get("image_urls", String.class)
                                ,row.get("create_date", LocalDateTime.class))
                ).toList();


        Long count = dslContext.select(DSL.count(cp))
                .from(cp)
                .leftJoin(m).on(cp.MEMBER_ID.eq(m.ID))
                .leftJoin(c).on(cp.ID.eq(c.COMM_POST_ID))
                .where(
                        conditions
                ).fetchOne(0,Long.class);

        return new PageImpl<>(
                contents, pageable, count != null ? count : 0
        );



        //return null;
    }


    private BooleanExpression contentLike(String keyword, boolean searchByContent) {
        if(!searchByContent) {
            return null;
        }
        return QCommPost.commPost.content.containsIgnoreCase(keyword);
    }

    private BooleanExpression nicknameLike(String keyword, boolean searchByNickname) {
        if(!searchByNickname) {
            return null;
        }

        return QCommPost.commPost.member.nickname.containsIgnoreCase(keyword);
    }


}

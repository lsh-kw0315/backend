package team.klover.server.domain.community.commPost.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import team.klover.server.domain.community.commPost.entity.CommPost;
import team.klover.server.domain.community.commPost.entity.QCommPost;
import team.klover.server.domain.community.commPost.entity.QCommPostLike;
import team.klover.server.domain.community.commPost.enums.CommPostSort;
import team.klover.server.domain.community.comment.entity.QComment;
import team.klover.server.domain.member.v1.entity.QMember;
import team.klover.server.domain.member.v1.enums.Country;
import team.klover.server.domain.tour.tourPost.dto.res.TourPostDto;

import java.util.List;

import static team.klover.server.domain.community.commPost.enums.CommPostSort.COMMENT_COUNT;
import static team.klover.server.domain.community.commPost.enums.CommPostSort.LIKE_COUNT;

@RequiredArgsConstructor
public class CommPostRepositoryImpl implements CommPostRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    @Override
    public Page<CommPost> search(String keyword, Pageable pageable, Double mapX, Double mapY, Country language, boolean searchByContent, boolean searchByNickname, CommPostSort sort) {
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

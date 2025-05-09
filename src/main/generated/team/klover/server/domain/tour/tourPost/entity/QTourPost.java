package team.klover.server.domain.tour.tourPost.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTourPost is a Querydsl query type for TourPost
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTourPost extends EntityPathBase<TourPost> {

    private static final long serialVersionUID = 1038803324L;

    public static final QTourPost tourPost = new QTourPost("tourPost");

    public final StringPath addr1 = createString("addr1");

    public final StringPath areaCode = createString("areaCode");

    public final StringPath cat1 = createString("cat1");

    public final StringPath cat2 = createString("cat2");

    public final StringPath cat3 = createString("cat3");

    public final NumberPath<Long> commonPlaceId = createNumber("commonPlaceId", Long.class);

    public final NumberPath<Long> contentId = createNumber("contentId", Long.class);

    public final StringPath contentTypeId = createString("contentTypeId");

    public final StringPath cpyrhtDivCd = createString("cpyrhtDivCd");

    public final DateTimePath<java.time.LocalDateTime> createDate = createDateTime("createDate", java.time.LocalDateTime.class);

    public final StringPath firstImage = createString("firstImage");

    public final StringPath homepage = createString("homepage");

    public final StringPath language = createString("language");

    public final NumberPath<Double> mapX = createNumber("mapX", Double.class);

    public final NumberPath<Double> mapY = createNumber("mapY", Double.class);

    public final StringPath overview = createString("overview");

    public final ListPath<team.klover.server.domain.tour.review.entity.ReviewTourPost, team.klover.server.domain.tour.review.entity.QReviewTourPost> reviewTourPosts = this.<team.klover.server.domain.tour.review.entity.ReviewTourPost, team.klover.server.domain.tour.review.entity.QReviewTourPost>createList("reviewTourPosts", team.klover.server.domain.tour.review.entity.ReviewTourPost.class, team.klover.server.domain.tour.review.entity.QReviewTourPost.class, PathInits.DIRECT2);

    public final ListPath<TourPostSave, QTourPostSave> savedMembers = this.<TourPostSave, QTourPostSave>createList("savedMembers", TourPostSave.class, QTourPostSave.class, PathInits.DIRECT2);

    public final StringPath sigungucode = createString("sigungucode");

    public final StringPath title = createString("title");

    public QTourPost(String variable) {
        super(TourPost.class, forVariable(variable));
    }

    public QTourPost(Path<? extends TourPost> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTourPost(PathMetadata metadata) {
        super(TourPost.class, metadata);
    }

}


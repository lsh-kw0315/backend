package team.klover.server.domain.tour.review.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReviewTourPost is a Querydsl query type for ReviewTourPost
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReviewTourPost extends EntityPathBase<ReviewTourPost> {

    private static final long serialVersionUID = 628229620L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReviewTourPost reviewTourPost = new QReviewTourPost("reviewTourPost");

    public final team.klover.server.global.jpa.QBaseEntity _super = new team.klover.server.global.jpa.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final QReview review;

    public final team.klover.server.domain.tour.tourPost.entity.QTourPost tourPost;

    public QReviewTourPost(String variable) {
        this(ReviewTourPost.class, forVariable(variable), INITS);
    }

    public QReviewTourPost(Path<? extends ReviewTourPost> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReviewTourPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReviewTourPost(PathMetadata metadata, PathInits inits) {
        this(ReviewTourPost.class, metadata, inits);
    }

    public QReviewTourPost(Class<? extends ReviewTourPost> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.review = inits.isInitialized("review") ? new QReview(forProperty("review"), inits.get("review")) : null;
        this.tourPost = inits.isInitialized("tourPost") ? new team.klover.server.domain.tour.tourPost.entity.QTourPost(forProperty("tourPost")) : null;
    }

}


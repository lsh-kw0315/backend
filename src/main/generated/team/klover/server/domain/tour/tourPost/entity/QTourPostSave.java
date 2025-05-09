package team.klover.server.domain.tour.tourPost.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTourPostSave is a Querydsl query type for TourPostSave
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTourPostSave extends EntityPathBase<TourPostSave> {

    private static final long serialVersionUID = -1567819495L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTourPostSave tourPostSave = new QTourPostSave("tourPostSave");

    public final team.klover.server.global.jpa.QBaseEntity _super = new team.klover.server.global.jpa.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final team.klover.server.domain.member.v1.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final QTourPost tourPost;

    public QTourPostSave(String variable) {
        this(TourPostSave.class, forVariable(variable), INITS);
    }

    public QTourPostSave(Path<? extends TourPostSave> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTourPostSave(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTourPostSave(PathMetadata metadata, PathInits inits) {
        this(TourPostSave.class, metadata, inits);
    }

    public QTourPostSave(Class<? extends TourPostSave> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new team.klover.server.domain.member.v1.entity.QMember(forProperty("member")) : null;
        this.tourPost = inits.isInitialized("tourPost") ? new QTourPost(forProperty("tourPost")) : null;
    }

}


package team.klover.server.domain.community.commPost.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCommPostLike is a Querydsl query type for CommPostLike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCommPostLike extends EntityPathBase<CommPostLike> {

    private static final long serialVersionUID = 1811400728L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCommPostLike commPostLike = new QCommPostLike("commPostLike");

    public final team.klover.server.global.jpa.QBaseEntity _super = new team.klover.server.global.jpa.QBaseEntity(this);

    public final QCommPost commPost;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final team.klover.server.domain.member.v1.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public QCommPostLike(String variable) {
        this(CommPostLike.class, forVariable(variable), INITS);
    }

    public QCommPostLike(Path<? extends CommPostLike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCommPostLike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCommPostLike(PathMetadata metadata, PathInits inits) {
        this(CommPostLike.class, metadata, inits);
    }

    public QCommPostLike(Class<? extends CommPostLike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.commPost = inits.isInitialized("commPost") ? new QCommPost(forProperty("commPost"), inits.get("commPost")) : null;
        this.member = inits.isInitialized("member") ? new team.klover.server.domain.member.v1.entity.QMember(forProperty("member")) : null;
    }

}


package team.klover.server.domain.community.commPost.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCommPostSave is a Querydsl query type for CommPostSave
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCommPostSave extends EntityPathBase<CommPostSave> {

    private static final long serialVersionUID = 1811601918L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCommPostSave commPostSave = new QCommPostSave("commPostSave");

    public final team.klover.server.global.jpa.QBaseEntity _super = new team.klover.server.global.jpa.QBaseEntity(this);

    public final QCommPost commPost;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final team.klover.server.domain.member.v1.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public QCommPostSave(String variable) {
        this(CommPostSave.class, forVariable(variable), INITS);
    }

    public QCommPostSave(Path<? extends CommPostSave> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCommPostSave(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCommPostSave(PathMetadata metadata, PathInits inits) {
        this(CommPostSave.class, metadata, inits);
    }

    public QCommPostSave(Class<? extends CommPostSave> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.commPost = inits.isInitialized("commPost") ? new QCommPost(forProperty("commPost"), inits.get("commPost")) : null;
        this.member = inits.isInitialized("member") ? new team.klover.server.domain.member.v1.entity.QMember(forProperty("member")) : null;
    }

}


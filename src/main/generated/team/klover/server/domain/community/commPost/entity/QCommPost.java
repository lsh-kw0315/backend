package team.klover.server.domain.community.commPost.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCommPost is a Querydsl query type for CommPost
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCommPost extends EntityPathBase<CommPost> {

    private static final long serialVersionUID = -682689311L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCommPost commPost = new QCommPost("commPost");

    public final team.klover.server.global.jpa.QBaseEntity _super = new team.klover.server.global.jpa.QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final ListPath<String, StringPath> imageUrls = this.<String, StringPath>createList("imageUrls", String.class, StringPath.class, PathInits.DIRECT2);

    public final EnumPath<team.klover.server.domain.member.v1.enums.Country> language = createEnum("language", team.klover.server.domain.member.v1.enums.Country.class);

    public final ListPath<CommPostLike, QCommPostLike> likedMembers = this.<CommPostLike, QCommPostLike>createList("likedMembers", CommPostLike.class, QCommPostLike.class, PathInits.DIRECT2);

    public final NumberPath<Double> mapX = createNumber("mapX", Double.class);

    public final NumberPath<Double> mapY = createNumber("mapY", Double.class);

    public final team.klover.server.domain.member.v1.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final ListPath<CommPostSave, QCommPostSave> savedMembers = this.<CommPostSave, QCommPostSave>createList("savedMembers", CommPostSave.class, QCommPostSave.class, PathInits.DIRECT2);

    public QCommPost(String variable) {
        this(CommPost.class, forVariable(variable), INITS);
    }

    public QCommPost(Path<? extends CommPost> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCommPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCommPost(PathMetadata metadata, PathInits inits) {
        this(CommPost.class, metadata, inits);
    }

    public QCommPost(Class<? extends CommPost> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new team.klover.server.domain.member.v1.entity.QMember(forProperty("member")) : null;
    }

}


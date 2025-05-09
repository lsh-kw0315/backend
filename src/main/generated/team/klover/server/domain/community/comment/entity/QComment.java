package team.klover.server.domain.community.comment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QComment is a Querydsl query type for Comment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QComment extends EntityPathBase<Comment> {

    private static final long serialVersionUID = 722631439L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QComment comment = new QComment("comment");

    public final team.klover.server.global.jpa.QBaseEntity _super = new team.klover.server.global.jpa.QBaseEntity(this);

    public final team.klover.server.domain.community.commPost.entity.QCommPost commPost;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final ListPath<CommentLike, QCommentLike> likedMembers = this.<CommentLike, QCommentLike>createList("likedMembers", CommentLike.class, QCommentLike.class, PathInits.DIRECT2);

    public final team.klover.server.domain.member.v1.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final NumberPath<Long> superCommentId = createNumber("superCommentId", Long.class);

    public QComment(String variable) {
        this(Comment.class, forVariable(variable), INITS);
    }

    public QComment(Path<? extends Comment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QComment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QComment(PathMetadata metadata, PathInits inits) {
        this(Comment.class, metadata, inits);
    }

    public QComment(Class<? extends Comment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.commPost = inits.isInitialized("commPost") ? new team.klover.server.domain.community.commPost.entity.QCommPost(forProperty("commPost"), inits.get("commPost")) : null;
        this.member = inits.isInitialized("member") ? new team.klover.server.domain.member.v1.entity.QMember(forProperty("member")) : null;
    }

}


package team.klover.server.domain.member.v1.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = 1929018655L;

    public static final QMember member = new QMember("member1");

    public final team.klover.server.global.jpa.QBaseEntity _super = new team.klover.server.global.jpa.QBaseEntity(this);

    public final EnumPath<team.klover.server.domain.member.v1.enums.Country> country = createEnum("country", team.klover.server.domain.member.v1.enums.Country.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final StringPath email = createString("email");

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public final StringPath profileUrl = createString("profileUrl");

    public final StringPath providerId = createString("providerId");

    public final EnumPath<team.klover.server.domain.member.v1.enums.MemberRole> role = createEnum("role", team.klover.server.domain.member.v1.enums.MemberRole.class);

    public final EnumPath<team.klover.server.domain.member.v1.enums.SocialProvider> socialProvider = createEnum("socialProvider", team.klover.server.domain.member.v1.enums.SocialProvider.class);

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}


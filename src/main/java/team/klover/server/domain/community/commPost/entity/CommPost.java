package team.klover.server.domain.community.commPost.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import team.klover.server.domain.community.comment.entity.Comment;
import team.klover.server.domain.member.v1.entity.Member;
import team.klover.server.domain.member.v1.enums.Country;
import team.klover.server.global.jpa.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CommPost extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "commPost", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommPostLike> likedMembers = new ArrayList<>();

    @OneToMany(mappedBy = "commPost", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommPostSave> savedMembers = new ArrayList<>();


    @Column(length = 3000)
    @Size(max = 3000)
    private String content;

    private Double mapX;
    private Double mapY;

    @Type(JsonType.class) // Hibernate JSON 타입 적용
    @Column(columnDefinition = "jsonb") // PostgreSQL의 jsonb 타입 사용
    private List<String> imageUrls;

    @Enumerated(EnumType.STRING)
    private Country language;

    @Column(columnDefinition = "geography(POINT, 4326)", name="location_earth")
    private Geometry locationEarth;

    @Column(columnDefinition = "earth", name="loc_earth")
    private Geometry locEarth;
}

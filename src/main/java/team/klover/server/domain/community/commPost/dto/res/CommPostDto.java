package team.klover.server.domain.community.commPost.dto.res;

import lombok.*;
import team.klover.server.global.elasticsearch.commpost.doc.CommPostDoc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class CommPostDto {
    private Long id;
    private Long memberId;
    private String nickname;
    private Double mapX;
    private Double mapY;
    private List<String> imageUrls;
    private LocalDateTime createDate;

    public CommPostDto(Long id, Long memberId, String nickname, Double mapX, Double mapY, String imageUrls, LocalDateTime createDate){
        this.id = id;
        this.memberId = memberId;
        this.nickname =nickname;
        this.mapX =mapX;
        this.mapY = mapY;
        this.createDate = createDate;

        if(imageUrls != null){
            String response = imageUrls;
            response = response.replace("[","").replace("]","").replace("\"","").strip();
            if(response.isBlank()) return;
            String[] urls = response.split(",");
            this.imageUrls = new ArrayList<>();
            for(String url : urls){
                String plainUrl = url.strip();
                this.imageUrls.add(plainUrl);
            }
        }
    }

    public CommPostDto(Long id, Long memberId, String nickname, Double mapX, Double mapY, List<String> imageUrls, LocalDateTime createDate){
        this.id = id;
        this.memberId = memberId;
        this.nickname =nickname;
        this.mapX =mapX;
        this.mapY = mapY;
        this.createDate = createDate;
        this.imageUrls = imageUrls;
    }

    public CommPostDto(CommPostDoc commPostDoc){
        id = commPostDoc.getId();
        memberId = commPostDoc.getMember_id();
        nickname = commPostDoc.getNickname();
        mapX = commPostDoc.getLocation().getLon();
        mapY = commPostDoc.getLocation().getLat();
        createDate = commPostDoc.getCreate_date().toLocalDateTime();

        if(commPostDoc.getImage_urls() != null){
            String response = commPostDoc.getImage_urls();
            response = response.replace("[","").replace("]","").replace("\"","").strip();
            if(response.isBlank()) return;
            String[] urls = response.split(",");
            imageUrls = new ArrayList<>();
            for(String url : urls){
                String plainUrl = url.strip();
                imageUrls.add(plainUrl);
            }
        }
    }
}

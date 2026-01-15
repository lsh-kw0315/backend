package team.klover.server.global.elasticsearch.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import team.klover.server.domain.community.commPost.repository.CommPostLikeRepository;
import team.klover.server.domain.community.comment.repository.CommentRepository;
import team.klover.server.domain.tour.review.repository.ReviewRepository;
import team.klover.server.global.redis.RedisService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ElasticUpdateBatch {
    private final RedisService redisService;
    private final CommentRepository commentRepository;
    private final CommPostLikeRepository commPostLikeRepository;
    private final ReviewRepository reviewRepository;
    private final ElasticsearchClient client;

    @SneakyThrows
    @Scheduled(fixedRate = 30*1000)
    @Async
    public void bulkUpdate(){

        Map<Long, Object> commPostDeletionTarget = redisService.getALLCommPostDeletion();
        Map<Long, Map<Object,Object>> commPostModificationTarget = redisService.getAllCommPostModification();
        Map<Long, Object> nicknameModificationTarget = redisService.getAllNickname();
        Map<Long, Object> commPostCountTarget = redisService.getAllCommPostCount();
        Map<Long, Map<Object,Object>> tourPostCountTarget = redisService.getAllTourPostCount();
        Map<Long, Map<Object,Object>> memberUpdateTarget = redisService.getAllMemberModification();
        Set<Object> memberDeletionTarget = redisService.getAllMemberDeletion();

        boolean haveToBulk = !(commPostCountTarget.isEmpty() && commPostModificationTarget.isEmpty() && nicknameModificationTarget.isEmpty() && commPostDeletionTarget.isEmpty() && tourPostCountTarget.isEmpty() && memberDeletionTarget.isEmpty() && memberUpdateTarget.isEmpty());
        if(!haveToBulk) return;

        //수정사항이 있었으나 지워진 게시글이 있으면 수정 요청을 못 하도록 없애버림
        commPostDeletionTarget.forEach(
                (k,v)->{
                    commPostCountTarget.remove(k);
                    commPostModificationTarget.remove(k);

                }
        );

        //count와 row 업데이트, 닉네임 업데이트가 겹치는 친구들은 합침
        commPostModificationTarget.forEach(
                (k,v)->{
                    if(commPostCountTarget.containsKey(k)){
                        v.put("comment_count",commentRepository.countCommPostComment(k));
                        v.put("like_count",commPostLikeRepository.countCommPostLike(k));
                        commPostCountTarget.remove(k);
                    }
                }
        );


        memberDeletionTarget.forEach(
                (k)->{
                    Long realKey = Long.parseLong((String)k);
                    memberUpdateTarget.remove(realKey);
                }
        );

        BulkRequest.Builder bqb = new BulkRequest.Builder();
        commPostDeletionTarget.forEach(
                (k,v)->{
                    String index;
                    switch ((String)v){
                        case "KO"->index = "commpostkor";
                        case "JA"->index = "commpostjpn";
                        case "ZH"->index = "commpostchs";
                        default->index = "commposteng";
                    }
                    String finalIndex = index;
                    bqb.operations(bo ->
                            bo.delete(d->d.index(finalIndex).id(String.valueOf(k)))
                    );
                }
        );

        commPostModificationTarget.forEach(
                (k,v)->{
                    String index;
                    switch ((String)v.get("language")){
                        case "KO" -> index = "commpostkor";
                        case "JA" -> index = "commpostjpn";
                        case "ZH" -> index = "commpostchs";
                        default -> index = "commposteng";

                    }

                    ZonedDateTime zoned_create_date = ((LocalDateTime)v.get("create_date")).atZone(ZoneId.of("Asia/Seoul"));
                    String create_date_str = zoned_create_date.format(DateTimeFormatter.ISO_INSTANT);

                    ZonedDateTime zoned_modify_date = ((LocalDateTime)v.get("modify_date")).atZone(ZoneId.of("Asia/Seoul"));
                    String modify_date_str = zoned_modify_date.format(DateTimeFormatter.ISO_INSTANT);

                    v.put("create_date",create_date_str);
                    v.put("modify_date",modify_date_str);

                    Double lon = (Double) v.get("mapx");
                    Double lat = (Double) v.get("mapy");
                    Map<String, Object> location = new HashMap<>();
                    location.put("lat",lat);
                    location.put("lon",lon);
                    v.put("location",location);
                    v.remove("mapx");
                    v.remove("mapy");


                    String finalIndex = index;
                    bqb.operations(bo->
                            bo.update(
                                    u->u.id(String.valueOf(k))
                                            .index(finalIndex)
                                            .action(a->a.doc(v).docAsUpsert(false))
                            )
                    );
                }
        );

        commPostCountTarget.forEach(
                (k,v)->{
                    String index;
                    switch ((String)v){
                        case "KO"-> index = "commpostkor";
                        case "JA"-> index = "commpostjpn";
                        case "ZH"-> index = "commpostchs";
                        default-> index = "commposteng";
                    }

                    String finalIndex = index;
                    Map<String, Object> scalars = new HashMap<>();
                    scalars.put("comment_count",commentRepository.countCommPostComment(k));
                    scalars.put("like_count",commPostLikeRepository.countCommPostLike(k));

                    bqb.operations(bo->
                            bo.update(
                                    u->u.id(String.valueOf(k))
                                            .index(finalIndex)
                                            .action(a->a.doc(scalars).docAsUpsert(false))
                            )
                    );

                }
        );

        nicknameModificationTarget.forEach(
                (k,v)->{
                    Map<String, Object> ids = new HashMap<>();
                    try {
                        SearchRequest sq =SearchRequest.of(
                                s->s.index("commpostkor")
                                        .query(q -> q.term(t->t.field("member_id").value(k)))
                                        .source(src->src.filter(f->f.includes("language")))   // _source 필드 제외
                                        .storedFields("_id")
                        );

                        SearchResponse<JsonData> korResp = client.search(
                                SearchRequest.of(
                                        s->s.index("commpostkor")
                                                .query(q -> q.term(t->t.field("member_id").value(k)))
                                                .source(src->src.filter(f->f.includes("language")))  // _source 필드 제외
                                                .storedFields("_id")
                                ),JsonData.class
                        );

                        SearchResponse<JsonData> jpnResp = client.search(
                                SearchRequest.of(
                                        s->s.index("commpostjpn")
                                                .query(q -> q.term(t->t.field("member_id").value(k)))
                                                .source(src->src.filter(f->f.includes("language")))   // _source 필드 제외
                                                .storedFields("_id")
                                ),JsonData.class
                        );


                        SearchResponse<JsonData> chsResp = client.search(
                                SearchRequest.of(
                                        s->s.index("commpostchs")
                                                .query(q -> q.term(t->t.field("member_id").value(k)))
                                                .source(src->src.filter(f->f.includes("language")))  // _source 필드 제외
                                                .storedFields("_id")
                                ),JsonData.class
                        );


                        SearchResponse<JsonData> engResp = client.search(
                                SearchRequest.of(
                                        s->s.index("commposteng")
                                                .query(q -> q.term(t->t.field("member_id").value(k)))
                                                .source(src->src.filter(f->f.includes("language")))  // _source 필드 제외
                                                .storedFields("_id")
                                ),JsonData.class
                        );

                        korResp.hits().hits().forEach(
                                hit-> {
                                    String id = hit.id();
                                    Object language = Objects.requireNonNull(hit.source()).toJson().asJsonObject().get("language").toString();
                                    ids.put(id, language);
                                }
                        );
                        jpnResp.hits().hits().forEach(
                                hit-> {
                                    String id = hit.id();
                                    Object language = Objects.requireNonNull(hit.source()).toJson().asJsonObject().get("language").toString();
                                    ids.put(id, language);
                                }
                        );
                        chsResp.hits().hits().forEach(
                                hit-> {
                                    String id = hit.id();
                                    Object language = Objects.requireNonNull(hit.source()).toJson().asJsonObject().get("language").toString();
                                    ids.put(id, language);
                                }
                        );
                        engResp.hits().hits().forEach(
                                hit-> {
                                    String id = hit.id();
                                    Object language = Objects.requireNonNull(hit.source()).toJson().asJsonObject().get("language").toString();
                                    ids.put(id, language);
                                }
                        );

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ids.forEach((id,language)->{
                        String index;
                        String lang = (String) language;
                        lang = lang.replace("\"","");
                        index = switch (lang) {
                            case "KO" -> "commpostkor";
                            case "JA" -> "commpostjpn";
                            case "ZH" -> "commpostchs";
                            default -> "commposteng";
                        };
                        String finalIndex = index;
                        Map<String, Object> doc = new HashMap<>();
                        doc.put("nickname",v);
                        bqb.operations(bo-> bo.update(u->u.index(finalIndex)
                                .id(id)
                                .action(a->a.doc(doc).docAsUpsert(false))));
                    });
                }
        );

        tourPostCountTarget.forEach(
                (k,v)->{
                    String index;
                    switch ((String)v.get("language")){
                        case "KO"->index = "tourpostkor";
                        case "JA"->index = "tourpostjpn";
                        case "ZH"-> index = "tourpostchs";
                        default-> index = "tourposteng";
                    }

                    String finalIndex = index;

                    Map<String, Object> scalars = new HashMap<>();
                    scalars.put("review_count",reviewRepository.countTourPostReview((Long)v.get("common_place_id")));
                    scalars.put("rating_average",commPostLikeRepository.countCommPostLike((Long)v.get("common_place_oid")));

                    bqb.operations(
                            bo -> bo.update(
                                    u->u.index(finalIndex)
                                            .id(String.valueOf(k))
                                            .action(a->a.doc(scalars).docAsUpsert(false))
                            )
                    );
                }
        );

        memberDeletionTarget.forEach(
                (k)->
                    bqb.operations(bo->bo.delete(d->d.index("members").id(String.valueOf(k))))

        );

        memberUpdateTarget.forEach(
                (k,v)-> bqb.operations(bo->bo.update(u->u.index("members").id(String.valueOf(k))
                        .action(a->a.doc(v).docAsUpsert(false))))
        );


        BulkRequest bq = bqb.build();
        BulkResponse bulkResponse = client.bulk(bq);


        System.out.println("Bulk operation:" + bulkResponse);


    }
}

package team.klover.server.domain.community.commPost.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import team.klover.server.domain.community.commPost.dto.res.CommPostDto;
import team.klover.server.domain.community.commPost.entity.CommPost;
import team.klover.server.domain.community.commPost.enums.CommPostSort;
import team.klover.server.domain.member.v1.enums.Country;

public interface CommPostRepositoryCustom {
    Page<CommPost> search(String keyword, Pageable pageable, Double mapX, Double mapY, Country language, boolean searchByContent, boolean searchByNickname, CommPostSort sort);
}

package team.klover.server.domain.tour.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.klover.server.domain.tour.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}

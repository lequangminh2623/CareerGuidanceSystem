package com.lqm.repositories;

import com.lqm.models.ForumReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForumReplyRepository extends JpaRepository<ForumReply, Integer> {

    @Query("""
    SELECT fr
    FROM ForumReply fr
    WHERE fr.forumPost.id = :forumPostId
      AND fr.parent.id = fr.id
      AND (:kw IS NULL OR LOWER(CAST(fr.content AS string)) LIKE LOWER(CONCAT('%', :kw, '%')))
    ORDER BY fr.createdDate DESC
""")
    Page<ForumReply> findTopLevelReplies(
            @Param("forumPostId") int forumPostId,
            @Param("kw") String kw,
            Pageable pageable
    );

    @Query("""
        SELECT fr
        FROM ForumReply fr
        WHERE fr.forumPost.id = :forumPostId
          AND fr.parent.id = :parentId
          AND fr.parent.id <> fr.id
        ORDER BY fr.createdDate DESC
        """)
    Page<ForumReply> findChildReplies(
            @Param("forumPostId") int forumPostId,
            @Param("parentId") int parentId,
            Pageable pageable
    );
    @Query("""
    SELECT fr FROM ForumReply fr
    WHERE (:kw IS NULL OR LOWER(CAST(fr.content AS string)) LIKE LOWER(CONCAT('%', :kw, '%')))
      AND (:userId IS NULL OR fr.user.id = :userId)
    ORDER BY fr.createdDate DESC
""")
    Page<ForumReply> findAllReplies(
            @Param("kw") String kw,
            @Param("userId") Integer userId,
            Pageable pageable
    );


}

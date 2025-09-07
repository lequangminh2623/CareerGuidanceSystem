package com.lqm.repositories;

import com.lqm.models.ForumReply;
import com.lqm.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForumReplyRepository extends JpaRepository<ForumReply, Integer>, JpaSpecificationExecutor<ForumReply> {

    @Query("""
    SELECT fr
    FROM ForumReply fr
    WHERE fr.forumPost.id = :forumPostId
      AND fr.parent.id IS NULL
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
          AND fr.parent.id IS NOT NULL
        ORDER BY fr.createdDate DESC
        """)
    Page<ForumReply> findChildReplies(
            @Param("forumPostId") int forumPostId,
            @Param("parentId") int parentId,
            Pageable pageable
    );


}

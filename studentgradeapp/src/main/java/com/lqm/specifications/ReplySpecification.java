package com.lqm.specifications;

import com.lqm.models.ForumReply;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReplySpecification {

    public static Specification<ForumReply> filterByParams(Map<String, String> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params != null) {
                // Tìm kiếm theo từ khóa nội dung
                String kw = params.get("kw");
                if (kw != null && !kw.isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get("content").as(String.class)), "%" + kw.toLowerCase() + "%"));
                }

                // Lọc theo userId
                String userId = params.get("user");
                if (userId != null && !userId.isEmpty()) {
                    predicates.add(cb.equal(root.get("user").get("id"), Integer.parseInt(userId)));
                }

                // Lọc theo forumPostId
                String postId = params.get("post");
                if (postId != null && !postId.isEmpty()) {
                    predicates.add(cb.equal(root.get("forumPost").get("id"), Integer.parseInt(postId)));
                }
            }

            query.orderBy(cb.desc(root.get("id"))); // Sắp xếp mới nhất trước
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

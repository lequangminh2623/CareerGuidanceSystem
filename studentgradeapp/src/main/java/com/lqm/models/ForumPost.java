package com.lqm.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lqm.utils.CollectionUpdater;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "forum_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NamedQueries({
        @NamedQuery(name = "ForumPost.findAll", query = "SELECT f FROM ForumPost f"),
        @NamedQuery(name = "ForumPost.findById", query = "SELECT f FROM ForumPost f WHERE f.id = :id"),
        @NamedQuery(name = "ForumPost.findByTitle", query = "SELECT f FROM ForumPost f WHERE f.title = :title"),
        @NamedQuery(name = "ForumPost.findByImage", query = "SELECT f FROM ForumPost f WHERE f.image = :image"),
        @NamedQuery(name = "ForumPost.findByCreatedDate", query = "SELECT f FROM ForumPost f WHERE f.createdDate = :createdDate"),
        @NamedQuery(name = "ForumPost.findByUpdatedDate", query = "SELECT f FROM ForumPost f WHERE f.updatedDate = :updatedDate")
})
public class ForumPost implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer id;

    @Basic(optional = false)
    @NotNull
    @Column(name = "title")
    private String title;

    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "content")
    private String content;

    @Size(max = 255)
    @Column(name = "image")
    private String image;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Date createdDate;

    @Column(name = "updated_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Date updatedDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "classroom_id", referencedColumnName = "id")
    @JsonIgnore
    private Classroom classroom;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToMany(
            mappedBy = "forumPost",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private Set<ForumReply> forumReplySet = new LinkedHashSet<>();

    @Transient
    @JsonIgnore
    private MultipartFile file;

    // Custom method để đồng bộ 2 chiều
    public void addForumReply(ForumReply reply) {
        if (reply == null) return;
        forumReplySet.add(reply);
        reply.setForumPost(this);
    }

    public void removeForumReply(ForumReply reply) {
        if (reply == null) return;
        if (forumReplySet.remove(reply)) {
            reply.setForumPost(null);
        }
    }

    // Setter tuỳ chỉnh để cập nhật collection an toàn, tránh xóa toàn bộ rồi thêm lại
    public void setForumReplySet(Set<ForumReply> newReplies) {
        CollectionUpdater.updateSet(
                forumReplySet,
                newReplies,
                this::addForumReply,
                this::removeForumReply
        );
    }
}

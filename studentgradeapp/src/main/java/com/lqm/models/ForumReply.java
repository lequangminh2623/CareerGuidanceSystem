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
@Table(name = "forum_reply")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NamedQueries({
        @NamedQuery(name = "ForumReply.findAll", query = "SELECT f FROM ForumReply f"),
        @NamedQuery(name = "ForumReply.findById", query = "SELECT f FROM ForumReply f WHERE f.id = :id"),
        @NamedQuery(name = "ForumReply.findByImage", query = "SELECT f FROM ForumReply f WHERE f.image = :image"),
        @NamedQuery(name = "ForumReply.findByCreatedDate", query = "SELECT f FROM ForumReply f WHERE f.createdDate = :createdDate"),
        @NamedQuery(name = "ForumReply.findByUpdatedDate", query = "SELECT f FROM ForumReply f WHERE f.updatedDate = :updatedDate")
})
public class ForumReply implements Serializable {

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
    @JoinColumn(name = "forum_post_id", referencedColumnName = "id")
    @JsonIgnore
    private ForumPost forumPost;

    @ManyToOne
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    @JsonIgnore
    private ForumReply parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private Set<ForumReply> forumReplySet = new LinkedHashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Transient
    @JsonIgnore
    private MultipartFile file;

    // Đồng bộ 2 chiều cho forumReplySet và parent
    public void addForumReply(ForumReply reply) {
        if (reply == null) return;
        forumReplySet.add(reply);
        reply.setParent(this);
    }

    public void removeForumReply(ForumReply reply) {
        if (reply == null) return;
        if (forumReplySet.remove(reply)) {
            reply.setParent(null);
        }
    }

    // Setter custom để cập nhật collection đúng cách
    public void setForumReplySet(Set<ForumReply> newReplies) {
        CollectionUpdater.updateSet(
                forumReplySet,
                newReplies,
                this::addForumReply,
                this::removeForumReply
        );
    }

}

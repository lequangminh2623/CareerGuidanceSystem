package com.lqm.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lqm.utils.CollectionUpdater;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "user")
@NamedQueries({
        @NamedQuery(name = "User.findAll", query = "SELECT u FROM User u"),
        @NamedQuery(name = "User.findById", query = "SELECT u FROM User u WHERE u.id = :id"),
        @NamedQuery(name = "User.findByFirstName", query = "SELECT u FROM User u WHERE u.firstName = :firstName"),
        @NamedQuery(name = "User.findByLastName", query = "SELECT u FROM User u WHERE u.lastName = :lastName"),
        @NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email = :email"),
        @NamedQuery(name = "User.findByPassword", query = "SELECT u FROM User u WHERE u.password = :password"),
        @NamedQuery(name = "User.findByAvatar", query = "SELECT u FROM User u WHERE u.avatar = :avatar"),
        @NamedQuery(name = "User.findByCreatedDate", query = "SELECT u FROM User u WHERE u.createdDate = :createdDate"),
        @NamedQuery(name = "User.findByUpdatedDate", query = "SELECT u FROM User u WHERE u.updatedDate = :updatedDate"),
        @NamedQuery(name = "User.findByGender", query = "SELECT u FROM User u WHERE u.gender = :gender"),
        @NamedQuery(name = "User.findByRole", query = "SELECT u FROM User u WHERE u.role = :role")
})
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer id;

    @Basic(optional = false)
    @Size(min = 1, max = 255)
    @Column(name = "first_name")
    private String firstName;

    @Basic(optional = false)
    @Size(min = 1, max = 255)
    @Column(name = "last_name")
    private String lastName;

    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@ou\\.edu\\.vn$", message = "{user.email.invalid}")
    @Basic(optional = false)
    @Size(min = 1, max = 255)
    @Column(name = "email")
    private String email;

    @Size(max = 255)
    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Basic(optional = false)
    @Column(name = "avatar")
    private String avatar = "https://res.cloudinary.com/dqw4mc8dg/image/upload/v1744183632/kagdbiirsk2aca0y9scy.png";

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "UTC")
    private Date createdDate;

    @Column(name = "updated_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "UTC")
    private Date updatedDate;

    @Basic(optional = false)
    @NotNull
    @Column(name = "gender")
    private boolean gender;

    @Basic(optional = false)
    @Size(min = 1, max = 13)
    @Column(name = "role")
    private String role = "ROLE_STUDENT";

    // Các collection dùng custom setter để đồng bộ 2 chiều
    @Setter(AccessLevel.NONE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    @JsonIgnore
    private Set<ForumPost> forumPostSet = new LinkedHashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Student student;

    @Setter(AccessLevel.NONE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "teacher")
    @JsonIgnore
    private Set<Classroom> classroomSet = new LinkedHashSet<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    @JsonIgnore
    private Set<ForumReply> forumReplySet = new LinkedHashSet<>();

    @Transient
    @JsonIgnore
    private MultipartFile file;

    public User() {
    }

    public User(Integer id) {
        this.id = id;
    }

    public User(Integer id, String firstName, String lastName, String email, String avatar, boolean gender, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.avatar = avatar;
        this.gender = gender;
        this.role = role;
    }

    // Custom setter đồng bộ 2 chiều cho forumPostSet
    public void addForumPost(ForumPost forumPost) {
        if (forumPost == null) return;
        forumPostSet.add(forumPost);
        forumPost.setUser(this);
    }

    public void removeForumPost(ForumPost forumPost) {
        if (forumPost == null) return;
        if (forumPostSet.remove(forumPost)) {
            forumPost.setUser(null);
        }
    }

    public void setForumPostSet(Set<ForumPost> newForumPosts) {
        CollectionUpdater.updateSet(
                forumPostSet,
                newForumPosts,
                this::addForumPost,
                this::removeForumPost
        );
    }

    // Custom setter đồng bộ 2 chiều cho classroomSet
    public void addClassroom(Classroom classroom) {
        if (classroom == null) return;
        classroomSet.add(classroom);
        classroom.setTeacher(this);
    }

    public void removeClassroom(Classroom classroom) {
        if (classroom == null) return;
        if (classroomSet.remove(classroom)) {
            classroom.setTeacher(null);
        }
    }

    public void setClassroomSet(Set<Classroom> newClassrooms) {
        CollectionUpdater.updateSet(
                classroomSet,
                newClassrooms,
                this::addClassroom,
                this::removeClassroom
        );
    }

    // Custom setter đồng bộ 2 chiều cho forumReplySet
    public void addForumReply(ForumReply forumReply) {
        if (forumReply == null) return;
        forumReplySet.add(forumReply);
        forumReply.setUser(this);
    }

    public void removeForumReply(ForumReply forumReply) {
        if (forumReply == null) return;
        if (forumReplySet.remove(forumReply)) {
            forumReply.setUser(null);
        }
    }

    public void setForumReplySet(Set<ForumReply> newForumReplies) {
        CollectionUpdater.updateSet(
                forumReplySet,
                newForumReplies,
                this::addForumReply,
                this::removeForumReply
        );
    }
}

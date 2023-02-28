package com.blog.app.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer postID;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    private String imageName;

    private Date createdDate;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Category category;

    @ManyToOne()
    @JoinColumn(nullable = false)
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private Set<Comment> comments;
}

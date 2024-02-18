package com.blog.app.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

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

    private Date createdDate;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Category category;

    @ManyToOne()
    @JoinColumn(nullable = false)
    private User user;
}

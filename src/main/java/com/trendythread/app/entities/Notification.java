package com.trendythread.app.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notification")
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Blogger recipient;

    /** One of: COMMENT, REPLY, FOLLOW. Plain string rather than an enum column to keep future additions migration-free. */
    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 500)
    private String message;

    /** The post this notification relates to, if any (null for FOLLOW notifications). */
    @Column(name = "post_id")
    private Integer postId;

    @Column(nullable = false)
    private boolean read = false;
}

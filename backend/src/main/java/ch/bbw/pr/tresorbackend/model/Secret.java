package ch.bbw.pr.tresorbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Secret
 *
 * @author Peter Rutschmann
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "secret")
public class Secret {
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "secret_uuid")
    private String secretUuid;

    @JsonIgnore
    @Column(nullable = false, name = "user_id")
    private Long userId;

    @Column(nullable = false, name = "content")
    private String content;

    public Secret(Long id, Long userId, String content) {
        this.id = id;
        this.userId = userId;
        this.content = content;
    }

    @PrePersist
    public void prePersist() {
        if (secretUuid == null || secretUuid.isBlank()) {
            secretUuid = UUID.randomUUID().toString();
        }
    }
}

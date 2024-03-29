package projectManagement.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import projectManagement.utils.Provider;
import projectManagement.utils.Validation;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Provider provider;


    public void setPassword(String password) {
        this.password = password;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }


//    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Board> boards;

//    @OneToMany(mappedBy = "assignedToUser", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Item> assignedItems;

//    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Item> ItemsCreated;

    //    @OneToMany(mappedBy = "commentedUser", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Comment> commentedUsers;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
//    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    @JsonIgnore
    Notification userNotification;

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public static User CreateUser(String name, String email, String password, Provider provider) {
        User user = new User();
        user.name = name;
        user.email = email;
        user.password = password;
        user.provider = provider;
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return email.equals(user.email) &&
                password.equals(user.password) &&
                name.equals(user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password, name);
    }
}

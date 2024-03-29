package projectManagement.entities;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String title;

//    @JsonIncludeProperties(value = {"id", "status"})
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(nullable = false)
    @Column(nullable = false)
    private String status;

    @Enumerated(EnumType.STRING)
    private ItemImportance importance;

    private String description;

    @Column(nullable = false)
    private String type = "";

    private LocalDate dueDate;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "parent", cascade = CascadeType.REMOVE,orphanRemoval = true)//remove
    private Set<Item> children;

    @JsonIncludeProperties(value = {"id"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn()
    private Item parent;

    @JsonIncludeProperties(value = {"id"})
    @ManyToOne()
    @JoinColumn(nullable = false)
    private Board board;

    @JsonIncludeProperties(value = {"id", "name"})
    @ManyToOne()
    @JoinColumn()
    private User assignedToUser;

    @JsonIncludeProperties(value = {"id" , "name"})
    @ManyToOne()
    @JoinColumn(nullable = false)
    private User creator;

    @JsonIncludeProperties(value = {"id", "comment", "commentedUser", "dateTime"})
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Comment> comments;

    public void removeComment(Comment comment){
        this.comments.remove(comment);
    }

    public Item(String title, String status, Board board, User creator) {
        this.title = title;
        this.status = status;
        this.board = board;
        this.creator = creator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return id == item.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Item createItemFromParent(String title, String status, Board board, User creator, Item parent) {
        Item item = new Item(title,status,board,creator);
        item.parent = parent;
        return item;
    }








}

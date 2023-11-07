package guru.springframework.spring6restmvc.entities;

import guru.springframework.spring6restmvc.model.BeerStyle;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.sql.JDBCType.SMALLINT;

// @Data zna biti performansno problematičan pa ćemo radije smanjiti scopre Lombok generiranja koda sa @Getter i @Setter
// Milan Medić: Lombok @Data Generates getters for all fields, a useful toString method, and hashCode and equals implementations that check all non-transient fields. Will also generate setters for all non-final fields, as well as a constructor.
@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Beer {


    @jakarta.persistence.Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false)
    private UUID id;

    @Version
    private Integer version;

    @NotBlank
    @NotNull
    @Size(max = 50)
    @Column (length = 50)
    private String beerName;

    @NotNull
    @Column(columnDefinition = "SMALLINT") /*Milan: 13.10.2023 morao sam ovo skrpati, jer mi po defaultu izgenerira TINYINT, a ne SMALLINT, valjda zbog ENUM-a */
    private BeerStyle beerStyle;

    @NotBlank
    @NotNull
    @Size(max = 255)
    private String upc;

    private Integer quantityOnHand;

    @NotNull
    private BigDecimal price;

    @OneToMany(mappedBy = "beer")
    private Set<BeerOrderLine> beerOrderLines;

    @Builder.Default
    @ManyToMany
    /* u join kolonu prema veznoj tablici (beer_category) prvo ide kolona iz ovog objekta-tablice (Beer), a inverzna kolona je druga vezna kolona, odnosno category_id,
    koja je dio kompozitnog ključa u veznoj tablici.

    Što kada su eventualno 3 vezne kolone?
    */
    @JoinTable(name = "beer_category", joinColumns =  @JoinColumn(name = "beer_id"),   // beer.id -> beer_category.beer_id
    inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>(); //inicijaliziramo na empty set, tako da property nikad nije null

    public void addCategory(Category category) {
        this.categories.add(category);

        category.getBeers().add(this);
    }

    public void removeCategory(Category category) {

        this.categories.remove(category);
        category.getBeers().remove(category);
    }



    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updateDate;

}

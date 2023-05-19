package guru.springframework.spring6restmvc.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.time.LocalDateTime;
import java.util.UUID;

//
// @Data zna biti performansno problematičan pa ćemo radije smanjiti scopre Lombok generiranja koda sa @Getter i @Setter
@Getter
@Setter
// Milan Medić: Lombok @Data Generates getters for all fields, a useful toString method, and hashCode and equals implementations that check all non-transient fields. Will also generate setters for all non-final fields, as well as a constructor.
@Builder
@Entity
@AllArgsConstructor    /*  Neka nam Lombok izgenerira i AllArgsConstructor i NoArgsConstructor */
@NoArgsConstructor
public class Customer {

    // @Id
    @EmbeddedId
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID id;
    private String name;

    /*
    @Version anotacija je potrebna interno za Hibernate versioning, kreće od 0 i inkrementira se postupno.
    Hibernate koristi taj property za usporedbu sa vrijednošću iz baze da li je neki drugi proces mijenjao zapis u bazi i ako jest, baci Exception.
    */
    @Version
    private Integer version;
    private LocalDateTime createdDate;
    private LocalDateTime updateDate;

}

/*
 *  Copyright 2019 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package guru.springframework.spring6restmvc.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
// mičemo Lombokov AllArgs constructor, jer autoru u tečaju nije dobro inicijaliziralo beerOrder. @AllArgsConstructor
// kod mene je i bez ovoga radilo, ali da kod bude isti napravit ću "ručno" AllArgs constructor

@Builder
public class BeerOrder {

    public BeerOrder(UUID id, Long version, Timestamp createdDate, Timestamp lastModifiedDate, String customerRef, Customer customer, Set<BeerOrderLine> beerOrderLines, BeerOrderShipment beerOrderShipment) {
        this.id = id;
        this.version = version;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.customerRef = customerRef;

        // ovo mijenjamo  u ručnom constructoru, umjesto direktno preko setCustomer setiramo this.customer = customer;
        this.setCustomer(customer);

        // ovo isto dodajem da ručno setiram beerOrderShipment objekt preko helper metode setBeerOrderShipment
        this.setBeerOrderShipment(beerOrderShipment);

        this.beerOrderLines = beerOrderLines;
        this.beerOrderShipment = beerOrderShipment;
    }

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false )
    private UUID id;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdDate;

    @UpdateTimestamp
    private Timestamp lastModifiedDate;

    public boolean isNew() {
        return this.id == null;
    }

    private String customerRef;

    // ovdje uspostavljam bidirekcionalnu vezu prema Customer obketu, many na strani BeerOreder, one na strani Customer. U data modelu će ovo iskreirati customer_id kolonu, koja će referencirati customer.id
    @ManyToOne
    private Customer customer;

    // helper metoda, radimo novi setter za customer-a


    public void setCustomer(Customer customer) {
        this.customer = customer;

        customer.getBeerOrders().add(this);  //osiguravam vezu između beerOrder-a i customera
    }

    public void setBeerOrderShipment(BeerOrderShipment beerOrderShipment) {
        this.beerOrderShipment = beerOrderShipment;
        beerOrderShipment.setBeerOrder(this);
    }



    @OneToMany(mappedBy = "beerOrder")  //ovo kaže da referencira BeerOrderLine preko kolone/atributa beerOrder, koji još ne posotji, ali dodat ćemo ga u BeerOrderLine
    private Set<BeerOrderLine> beerOrderLines;

    @OneToOne(cascade = CascadeType.PERSIST)  //ovo će pomoći da sam Hibernate izgenerira id za BeerOrder tip objekta prije spremanja u bazu
    private  BeerOrderShipment beerOrderShipment;

}

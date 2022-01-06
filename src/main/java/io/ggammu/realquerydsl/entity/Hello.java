package io.ggammu.realquerydsl.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Hello {

    @Id @GeneratedValue
    private Long id;

}

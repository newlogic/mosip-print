package io.mosip.print.model;

import org.springframework.hateoas.RepresentationModel;
import lombok.Data;

@Data
public class Type extends RepresentationModel<Type> {
    private String namespace;
    private String name;
}

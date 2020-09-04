package io.mosip.print.model;

import java.util.ArrayList;
import org.springframework.hateoas.RepresentationModel;
import lombok.Data;

@Data
public class Event extends RepresentationModel<Event> {
    private String id; // uuid
    private String transactionId; // privided by the publisher.
    private String version;
    Type type;
    private String timestamp; // ISO format
    private String dataShareUri; // URL
    // JSONObject
    ArrayList<Object> data = new ArrayList<Object>();
}

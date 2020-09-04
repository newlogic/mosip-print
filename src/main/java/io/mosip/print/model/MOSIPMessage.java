package io.mosip.print.model;

import lombok.Data;
import org.springframework.hateoas.RepresentationModel;


@Data
public class MOSIPMessage extends RepresentationModel<MOSIPMessage> {
   private String publisher;
   private String topic;
   private String publishedOn;
   // Reverse the event and payload based on serialize or deserialize
   private String payload; // JWT <header>.<eventpayload>.<signature>
   // interchange with payload do not serialize
   private Event event; // JWT <eventpayload> deserilized

   private final String content;
}

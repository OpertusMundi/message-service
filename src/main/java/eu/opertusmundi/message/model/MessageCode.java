package eu.opertusmundi.message.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = GenericMessageCode.class)
public interface MessageCode {

    String key();

}

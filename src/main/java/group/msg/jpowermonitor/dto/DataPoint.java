package group.msg.jpowermonitor.dto;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class DataPoint implements PowerQuestionable {
    String name;
    BigDecimal value;
    String unit;
    LocalDateTime time;
}

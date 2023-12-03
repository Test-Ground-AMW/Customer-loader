package lk.ijse.dep11.app.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestTO implements Serializable {
    @NotNull(message = "Query can't be empty")
    private String query;
    @Positive(message = "Invalid page")
    @NotNull(message = "Page can't be empty")
    private Integer page;
    @Positive(message = "Invalid size")
    @NotNull(message = "Size can't be empty")
    private Integer size;
}

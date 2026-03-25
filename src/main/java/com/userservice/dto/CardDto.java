package com.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

@Data
public class CardDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String number;
    @NotBlank(message = "Holder name is required")
    private String holder;
    @Pattern(regexp = "(0[1-9]|1[0-2])/[2-9]\\d", message = "Format MM/YY")
    private String expirationDate;
    private boolean active;
}
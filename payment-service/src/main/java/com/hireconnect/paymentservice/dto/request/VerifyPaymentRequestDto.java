package com.hireconnect.paymentservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
/**
 * Data transfer object representing VerifyPaymentRequest data.
 *
 * @author Disha Gujar
 */

@Data
public class VerifyPaymentRequestDto {

    @NotBlank
    private String razorpayOrderId;

    @NotBlank
    private String razorpayPaymentId;

    @NotBlank
    private String razorpaySignature;
}

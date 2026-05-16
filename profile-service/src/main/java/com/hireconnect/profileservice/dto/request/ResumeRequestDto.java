package com.hireconnect.profileservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * Data transfer object representing ResumeRequest data.
 *
 * @author Disha Gujar
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeRequestDto {

    private String fileName;
    private String fileUrl;
}

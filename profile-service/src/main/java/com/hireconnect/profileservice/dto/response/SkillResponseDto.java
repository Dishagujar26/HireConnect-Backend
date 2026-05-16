package com.hireconnect.profileservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * Data transfer object representing SkillResponse data.
 *
 * @author Disha Gujar
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillResponseDto {

    private Long id;
    private String name;
    private String level;
}

package com.company.notify.service.dto;

import com.company.notify.common.enums.ChangeCategory;
import com.company.notify.common.enums.ReleaseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 版本保存（含更新条目）入参 */
@Data
public class VersionSaveDTO {
    private Long id;
    @NotNull
    private Long productId;
    @NotBlank
    private String versionNo;
    private String description;
    private ReleaseType releaseType;
    private LocalDateTime planReleaseTime;
    private List<ChangeItemDTO> items;

    @Data
    public static class ChangeItemDTO {
        @NotNull
        private ChangeCategory category;
        @NotBlank
        private String title;
        private String content;
        private Integer sort;
    }
}

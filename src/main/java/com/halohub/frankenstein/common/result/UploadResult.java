package com.halohub.frankenstein.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResult {

    private String url;
    private boolean success;
    private String message;
}

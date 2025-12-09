package com.prod.facades.data;

import com.prod.models.ENUM.Type_Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageInfo {
    private String url;
    private String name;
    private Type_Image typeImage;
}

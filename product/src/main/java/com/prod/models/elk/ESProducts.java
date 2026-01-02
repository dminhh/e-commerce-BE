package com.prod.models.elk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

@Document(indexName = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESProducts {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String dbId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Double) // Kiểu Double cho giá
    private double price;

    @Field(type = FieldType.Keyword)
    private List<String> images;

    @Field(type = FieldType.Double) // Kiểu Double cho giảm giá
    private double discount;

    @Field(type = FieldType.Double) // Kiểu Double cho rating
    private double rating;

    @Field(type = FieldType.Double)
    private double score; // Trường để lưu điểm sản phẩm (được tính tự động)

    @Field(type = FieldType.Integer) // Kiểu Integer cho số lượng bán
    private int sold;

    @Field(type = FieldType.Date)
    private Date  createAt = new Date(); // Set default value
    @Field(type = FieldType.Date)
    private Date updateAt = new Date();  // Set default value

}

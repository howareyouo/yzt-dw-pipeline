package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;


@Getter
@Setter
public class ProductSpec {
    private Integer id;
    private Integer shopId;
    private String name;
    private Integer seq;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
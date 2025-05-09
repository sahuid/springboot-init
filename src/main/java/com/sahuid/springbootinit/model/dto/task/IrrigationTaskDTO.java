package com.sahuid.springbootinit.model.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
@Data
public class IrrigationTaskDTO {
    // getters and setters
    private String type;
    private Data data;
    private List<UnitParam> unitParams;

    @lombok.Data
    public static class Data {
        @JsonProperty("fieldId")
        private String fieldId;

        @JsonProperty("taskId")
        private String taskId;

        @JsonProperty("startTime")
        private String startTime;

        @JsonProperty("type")
        private Integer type;

        @JsonProperty("water")
        private Double water;

        @JsonProperty("fertilizerN")
        private Double fertilizerN;

        @JsonProperty("fertilizerP")
        private Double fertilizerP;

        @JsonProperty("fertilizerK")
        private Double fertilizerK;

        @JsonProperty("fieldUnitId")
        private String fieldUnitId; // 这个字段在JSON中是数组，可能需要特殊处理
    }
    @lombok.Data
    public static class UnitParam {
        @JsonProperty("fieldUnitId")
        private String fieldUnitId;

        @JsonProperty("water")
        private Double water;

        @JsonProperty("fertilizerN")
        private Double fertilizerN;

        @JsonProperty("fertilizerP")
        private Double fertilizerP;

        @JsonProperty("fertilizerK")
        private Double fertilizerK;

        // getters and setters
        public String getFieldUnitId() {
            return fieldUnitId;
        }

        public void setFieldUnitId(String fieldUnitId) {
            this.fieldUnitId = fieldUnitId;
        }

        public Double getWater() {
            return water;
        }

        public void setWater(Double water) {
            this.water = water;
        }

        public Double getFertilizerN() {
            return fertilizerN;
        }

        public void setFertilizerN(Double fertilizerN) {
            this.fertilizerN = fertilizerN;
        }

        public Double getFertilizerP() {
            return fertilizerP;
        }

        public void setFertilizerP(Double fertilizerP) {
            this.fertilizerP = fertilizerP;
        }

        public Double getFertilizerK() {
            return fertilizerK;
        }

        public void setFertilizerK(Double fertilizerK) {
            this.fertilizerK = fertilizerK;
        }
    }
}

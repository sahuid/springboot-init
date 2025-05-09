package com.sahuid.springbootinit.model.dto.task;

import lombok.Data;

import java.util.List;

@Data
public class TaskRequestDTO {
    private String type;
    private TaskDataDTO data;
    
    @Data
    public static class TaskDataDTO {
        private String taskId;
        private String fieldId;
        private List<String> fieldUnitIds;
        private String startTime;
        private Double water;
        private Double fertilizerN;
        private Double fertilizerP;
        private Double fertilizerK;
    }
}

package com.visionvera.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.impl.cmd.GetBpmnModelCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;

public class ProcessInstanceDiagramCmd implements Command<InputStream> {

	protected String processInstanceId;

    public ProcessInstanceDiagramCmd(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public InputStream execute(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = commandContext
            .getExecutionEntityManager();
        ExecutionEntity executionEntity = executionEntityManager
            .findExecutionById(processInstanceId);
        List<String> activiityIds = executionEntity.findActiveActivityIds();
        String processDefinitionId = executionEntity.getProcessDefinitionId();

        GetBpmnModelCmd getBpmnModelCmd = new GetBpmnModelCmd(
                processDefinitionId);
        BpmnModel bpmnModel = getBpmnModelCmd.execute(commandContext);

//        InputStream is = DefaultProcessDiagramGenerator.generateDiagram(bpmnModel,
//                "png", activiityIds);
        DefaultProcessDiagramGenerator ddg = new DefaultProcessDiagramGenerator();
//        InputStream is = ddg.generateDiagram(bpmnModel, "png", activiityIds);
        InputStream is = ddg.generateDiagram(
    			bpmnModel, 
    			"png", 
    			activiityIds, 
    			new ArrayList<String>(), 
    			"宋体", 
    			"宋体", 
    			null, 
    			1.0);
        return is;
    }

}

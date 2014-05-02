/**
 * Copyright (C) 2012-2014 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.tests.data;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.bonitasoft.studio.common.DataTypeLabels;
import org.bonitasoft.studio.common.DataUtil;
import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.data.operation.RefactorDataOperation;
import org.bonitasoft.studio.model.connectorconfiguration.ConnectorConfiguration;
import org.bonitasoft.studio.model.connectorconfiguration.ConnectorConfigurationFactory;
import org.bonitasoft.studio.model.connectorconfiguration.ConnectorParameter;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.bonitasoft.studio.model.expression.Operation;
import org.bonitasoft.studio.model.expression.Operator;
import org.bonitasoft.studio.model.process.AbstractProcess;
import org.bonitasoft.studio.model.process.Activity;
import org.bonitasoft.studio.model.process.Connector;
import org.bonitasoft.studio.model.process.Data;
import org.bonitasoft.studio.model.process.DataType;
import org.bonitasoft.studio.model.process.Element;
import org.bonitasoft.studio.model.process.MainProcess;
import org.bonitasoft.studio.model.process.MultiInstantiation;
import org.bonitasoft.studio.model.process.Pool;
import org.bonitasoft.studio.model.process.ProcessFactory;
import org.bonitasoft.studio.model.process.SequenceFlow;
import org.bonitasoft.studio.model.process.SequenceFlowConditionType;
import org.bonitasoft.studio.model.process.util.ProcessAdapterFactory;
import org.bonitasoft.studio.refactoring.core.RefactoringOperationType;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Aurelien Pupier
 * 
 */
public class TestDataRefactor {

    private Data processData;

    private Data localData;

    private RefactorDataOperation refactorDataOperation;

    private Pool process;

    private EditingDomain editingDomain;

    @Test
    public void testNameRefactorWithGlobalDataReferencedInMultiInstanciation() throws InvocationTargetException, InterruptedException {
        final String newDataName = "newDataName";
        AbstractProcess process = initTestForGlobalDataRefactor(newDataName);

        // Data referenced in multi-instanciation
        Activity activity = ProcessFactory.eINSTANCE.createActivity();
        final MultiInstantiation multiInstantiation = ProcessFactory.eINSTANCE.createMultiInstantiation();
        multiInstantiation.setCollectionDataToMultiInstantiate(processData);
        multiInstantiation.setListDataContainingOutputResults(processData);
        activity.setMultiInstantiation(multiInstantiation);
        process.getElements().add(activity);

        processData.setName(newDataName);

        assertEquals("There are too many datas. The old one migth not be removed.", 1, process.getData().size());
        assertEquals("Data name has not been updated correctly in multinstantiation", newDataName, multiInstantiation.getCollectionDataToMultiInstantiate()
                .getName());
        assertEquals("Data name has not been updated correctly in multinstantiation", newDataName, multiInstantiation.getListDataContainingOutputResults()
                .getName());

    }

    @Test
    public void testNameRefactorWithLocalDataReferencedInMultiInstanciation() throws InvocationTargetException, InterruptedException {
        final String newDataName = "newDataName";
        AbstractProcess process = initTestForLocalDataRefactor(newDataName);

        // Data referenced in multi-instanciation
        Activity activity = (Activity) process.getElements().get(0);
        final MultiInstantiation multiInstantiation = ProcessFactory.eINSTANCE.createMultiInstantiation();
        multiInstantiation.setInputData(localData);
        multiInstantiation.setOutputData(localData);
        activity.setMultiInstantiation(multiInstantiation);
        process.getElements().add(activity);

        localData.setName(newDataName);

        assertEquals("There are too many datas. The old one migth not be removed.", 1, process.getData().size());
        assertEquals("Data name has not been updated correctly in multinstantiation", newDataName, multiInstantiation.getInputData().getName());
        assertEquals("Data name has not been updated correctly in multinstantiation", newDataName, multiInstantiation.getOutputData().getName());

    }

    @Test
    public void testNameRefactorWithLocalDataReferencedInVariableExpression() throws InvocationTargetException, InterruptedException {
        final String newDataName = "newDataName";
        AbstractProcess process = initTestForLocalDataRefactor(newDataName);

        // Data referenced in expression
        Activity activity = (Activity) process.getElements().get(0);
        final MultiInstantiation multiInstantiation = ProcessFactory.eINSTANCE.createMultiInstantiation();
        final Expression variableExpression = ExpressionFactory.eINSTANCE.createExpression();
        variableExpression.setType(ExpressionConstants.VARIABLE_TYPE);
        variableExpression.setName(localData.getName());
        variableExpression.setContent(localData.getName());
        variableExpression.getReferencedElements().add(localData);
        variableExpression.setReturnType(DataUtil.getTechnicalTypeFor(localData));
        multiInstantiation.setCardinality(variableExpression);
        activity.setMultiInstantiation(multiInstantiation);
        process.getElements().add(activity);

        refactorDataOperation.run(new NullProgressMonitor());
        //editingDomain.getCommandStack().execute(cc);
        assertEquals("There are too many datas. The old one migth not be removed.", 1, process.getData().size());
        assertEquals("Data name has not been updated correctly in expression", newDataName,
                ((Element) variableExpression.getReferencedElements().get(0)).getName());
        assertEquals("Data name has not been updated correctly in expression", newDataName, variableExpression.getName());
        assertEquals("Data name has not been updated correctly in expression", newDataName, variableExpression.getContent());

    }
    
    @Test
    public void testRenameAndModifyTypeWithReferenceInScriptOperation() throws InvocationTargetException, InterruptedException{
        final String newDataName = "newDataName";
        final String newDataType = DataTypeLabels.integerDataType;
        AbstractProcess process = initTestForGlobalDataRefactor(newDataName, newDataType);
        Activity activity = (Activity) process.getElements().get(0);
        Operation operationWithScriptUsingData = ExpressionFactory.eINSTANCE.createOperation();
        Operator assignOperator = ExpressionFactory.eINSTANCE.createOperator();
        assignOperator.setType(ExpressionConstants.ASSIGNMENT_OPERATOR);
		operationWithScriptUsingData.setOperator(assignOperator);
        final Expression variableExpression = ExpressionFactory.eINSTANCE.createExpression();
        variableExpression.setType(ExpressionConstants.VARIABLE_TYPE);
        variableExpression.setName(processData.getName());
        variableExpression.setContent(processData.getName());
        variableExpression.getReferencedElements().add(EcoreUtil.copy(processData));
        variableExpression.setReturnType(DataUtil.getTechnicalTypeFor(processData));
		operationWithScriptUsingData.setLeftOperand(variableExpression);
		Expression scriptUsingData = ExpressionFactory.eINSTANCE.createExpression();
		scriptUsingData.setType(ExpressionConstants.SCRIPT_TYPE);
		scriptUsingData.setName(processData.getName());
		scriptUsingData.setContent(processData.getName());
		scriptUsingData.getReferencedElements().add(EcoreUtil.copy(processData));
		scriptUsingData.setReturnType(DataUtil.getTechnicalTypeFor(processData));
		operationWithScriptUsingData.setRightOperand(scriptUsingData);
		activity.getOperations().add(operationWithScriptUsingData);
		process.getElements().add(activity);
		
		final String initialDataName = processData.getName();
		
		refactorDataOperation.run(new NullProgressMonitor());
        //editingDomain.getCommandStack().execute(cc);
        assertEquals("There are too many datas. The old one might not be removed.", 1, process.getData().size());
        assertEquals("Data has not been renamed", newDataName, process.getData().get(0).getName());
        assertEquals("Data name has not been updated correctly in expression", newDataName,
                ((Element) variableExpression.getReferencedElements().get(0)).getName());
        assertEquals("Data name has not been updated correctly in expression of left operand operation", newDataName, variableExpression.getName());
        assertEquals("Data name has not been updated correctly in expression of left operand operation", newDataName, variableExpression.getContent());
		
        assertEquals("Data name has not been updated correctly in expression of right operand operation", newDataName, scriptUsingData.getContent());
        assertEquals("Data name has not been updated correctly in expression of right operand operation", newDataName, ((Data)scriptUsingData.getReferencedElements().get(0)).getName());
        
        editingDomain.getCommandStack().undo();
        
        assertEquals("There are too many datas. The old one might not be removed.", 1, process.getData().size());
        assertEquals("Data has not been renamed after undo", initialDataName, process.getData().get(0).getName());
        assertEquals("Data name has not been updated correctly in expression", initialDataName,
                ((Element) variableExpression.getReferencedElements().get(0)).getName());
        assertEquals("Data name has not been updated correctly in expression of left operand operation after undo", initialDataName, variableExpression.getName());
        assertEquals("Data name has not been updated correctly in expression of left operand operation after undo", initialDataName, variableExpression.getContent());
		
        assertEquals("Data name has not been updated correctly in expression of right operand operation after undo", initialDataName, scriptUsingData.getContent());
        assertEquals("Data name has not been updated correctly in expression of right operand operation after undo", initialDataName, ((Data)scriptUsingData.getReferencedElements().get(0)).getName());
    }
    
    @Test
    public void testRenameInGroovyScriptConnector() throws InvocationTargetException, InterruptedException{
    	final String newDataName = "newDataName";
    	final String newDataType = DataTypeLabels.integerDataType;
    	AbstractProcess process = initTestForGlobalDataRefactor(newDataName, newDataType);
    	Activity activity = (Activity) process.getElements().get(0);
    	Connector groovyScriptConnector = ProcessFactory.eINSTANCE.createConnector();

    	ConnectorConfiguration groovyScriptConnectorConfiguration = ConnectorConfigurationFactory.eINSTANCE.createConnectorConfiguration();
    	ConnectorParameter connectorParameter = ConnectorConfigurationFactory.eINSTANCE.createConnectorParameter();
    	Expression scriptUsingData = ExpressionFactory.eINSTANCE.createExpression();
		scriptUsingData.setType(ExpressionConstants.SCRIPT_TYPE);
		scriptUsingData.setName(processData.getName());
		scriptUsingData.setContent(processData.getName());
		scriptUsingData.getReferencedElements().add(EcoreUtil.copy(processData));
		scriptUsingData.setReturnType(DataUtil.getTechnicalTypeFor(processData));
    	connectorParameter.setExpression(scriptUsingData);
		groovyScriptConnectorConfiguration.getParameters().add(connectorParameter);
    	groovyScriptConnector.setConfiguration(groovyScriptConnectorConfiguration);

    	activity.getConnectors().add(groovyScriptConnector);

    	final String initialDataName = processData.getName();


    	refactorDataOperation.run(new NullProgressMonitor());
    	
    	assertEquals("Data has not been renamed", newDataName, process.getData().get(0).getName());
        assertEquals("Data name has not been updated correctly in expression of right operand operation", newDataName, scriptUsingData.getContent());
        assertEquals("Data name has not been updated correctly in expression of right operand operation", newDataName, ((Data)scriptUsingData.getReferencedElements().get(0)).getName());
        
        editingDomain.getCommandStack().undo();
        
        assertEquals("Data has not been renamed after undo", initialDataName, process.getData().get(0).getName());
        assertEquals("Data name has not been updated correctly in expression of right operand operation after undo", initialDataName, scriptUsingData.getContent());
        assertEquals("Data name has not been updated correctly in expression of right operand operation after undo", initialDataName, ((Data)scriptUsingData.getReferencedElements().get(0)).getName());
 
    }
    
    @Test
    public void testDeleteData() throws InvocationTargetException, InterruptedException{
    	AbstractProcess process = initTestForGlobalDataRefactor(null);
    	refactorDataOperation.run(new NullProgressMonitor());
    	assertEquals("The data has not been removed", 0, process.getData().size());
    	
    	 editingDomain.getCommandStack().undo();
    	 
    	 assertEquals("The data has not been removed", 1, process.getData().size());
    }
    
    @Test
    public void testDeleteDataWithReferenceInScript() throws InvocationTargetException, InterruptedException{
    	AbstractProcess process = initTestForGlobalDataRefactor(null);
    	
        Activity activity = (Activity) process.getElements().get(0);
        Operation operationWithScriptUsingData = ExpressionFactory.eINSTANCE.createOperation();
        Operator assignOperator = ExpressionFactory.eINSTANCE.createOperator();
        assignOperator.setType(ExpressionConstants.ASSIGNMENT_OPERATOR);
		operationWithScriptUsingData.setOperator(assignOperator);
        final Expression variableExpression = ExpressionFactory.eINSTANCE.createExpression();
        variableExpression.setType(ExpressionConstants.VARIABLE_TYPE);
        variableExpression.setName(processData.getName());
        variableExpression.setContent(processData.getName());
        variableExpression.getReferencedElements().add(EcoreUtil.copy(processData));
        variableExpression.setReturnType(DataUtil.getTechnicalTypeFor(processData));
		operationWithScriptUsingData.setLeftOperand(variableExpression);
		Expression scriptUsingData = ExpressionFactory.eINSTANCE.createExpression();
		scriptUsingData.setType(ExpressionConstants.SCRIPT_TYPE);
		scriptUsingData.setName(processData.getName());
		scriptUsingData.setContent(processData.getName());
		scriptUsingData.getReferencedElements().add(EcoreUtil.copy(processData));
		scriptUsingData.setReturnType(DataUtil.getTechnicalTypeFor(processData));
		operationWithScriptUsingData.setRightOperand(scriptUsingData);
		activity.getOperations().add(operationWithScriptUsingData);
		process.getElements().add(activity);
    
    	refactorDataOperation.run(new NullProgressMonitor());
    	assertEquals("The data has not been removed", 0, process.getData().size());
    	assertEquals("Referenced Data has been removed from script", 0, scriptUsingData.getReferencedElements().size());
    	
    	 editingDomain.getCommandStack().undo();
    	 assertEquals("The data has not been readded on undo", 1, process.getData().size());
    	 assertEquals("Referenced Data has been removed from script", 1, scriptUsingData.getReferencedElements().size());
    }
    
    @Test
    public void testDeleteDataWithReferenceInCondition() throws InvocationTargetException, InterruptedException{
    	AbstractProcess process = initTestForGlobalDataRefactor(null);
    	
    	final Activity activity = ProcessFactory.eINSTANCE.createActivity();
    	process.getElements().add(activity);
    	SequenceFlow sequenceFlow = ProcessFactory.eINSTANCE.createSequenceFlow();
    	sequenceFlow.setConditionType(SequenceFlowConditionType.EXPRESSION);
    	Expression conditionExpression = ExpressionFactory.eINSTANCE.createExpression();
    	conditionExpression.setType(ExpressionConstants.CONDITION_TYPE);
    	conditionExpression.setContent(processData.getName()+" == \"plop\"");
    	conditionExpression.setName("conditionExpression");
    	conditionExpression.getReferencedElements().add(EcoreUtil.copy(processData));
    	sequenceFlow.setCondition(conditionExpression);
    	process.getConnections().add(sequenceFlow);
    	
    	refactorDataOperation.run(new NullProgressMonitor());
    	assertEquals("The data has not been removed", 0, process.getData().size());
    	assertEquals("Referenced Data has been removed from script", 0, conditionExpression.getReferencedElements().size());
    	
   	 	editingDomain.getCommandStack().undo();
   	 	assertEquals("The data has not been readded on undo", 1, process.getData().size());
   	 	assertEquals("Referenced Data has been removed from script", 1, conditionExpression.getReferencedElements().size());
    }
    
    @Test
    @Ignore("not implemented")
    public void testDeleteDataWithReferenceInPatternExpression() throws InvocationTargetException, InterruptedException{
    	AbstractProcess process = initTestForGlobalDataRefactor(null);
    	refactorDataOperation.run(new NullProgressMonitor());
    	assertEquals("The data has not been removed", 0, process.getData().size());
    }
    
    @Test
    public void testRenameDataWithReferenceInCondition() throws InvocationTargetException, InterruptedException{
    	final String newDataName = "newDataName";
    	AbstractProcess process = initTestForGlobalDataRefactor(newDataName);
    	
    	final Activity activity = ProcessFactory.eINSTANCE.createActivity();
    	process.getElements().add(activity);
    	SequenceFlow sequenceFlow = ProcessFactory.eINSTANCE.createSequenceFlow();
    	sequenceFlow.setConditionType(SequenceFlowConditionType.EXPRESSION);
    	Expression conditionExpression = ExpressionFactory.eINSTANCE.createExpression();
    	conditionExpression.setType(ExpressionConstants.CONDITION_TYPE);
    	conditionExpression.setContent(processData.getName()+" == \"plop\"");
    	conditionExpression.setName("conditionExpression");
    	conditionExpression.getReferencedElements().add(EcoreUtil.copy(processData));
    	sequenceFlow.setCondition(conditionExpression);
    	process.getConnections().add(sequenceFlow);
    	final String initialDataName = processData.getName();
    	
    	refactorDataOperation.run(new NullProgressMonitor());
    	assertEquals("The old data might not have been updated", 1, process.getData().size());
    	assertEquals("The data has not been renamed in condition", newDataName+" == \"plop\"",conditionExpression.getContent());
    	assertEquals("The data has not been removed from dependency", newDataName,((Data)conditionExpression.getReferencedElements().get(0)).getName());
    	
    	editingDomain.getCommandStack().undo();
    	
    	assertEquals("The old data might not have been updated", 1, process.getData().size());
    	assertEquals("The data has not been renamed in condition", initialDataName+" == \"plop\"",conditionExpression.getContent());
    	assertEquals("The data dependency has not been back on undo", initialDataName,((Data)conditionExpression.getReferencedElements().get(0)).getName());
    }
    

	private AdapterFactoryEditingDomain createEditingDomain() {
        ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
        adapterFactory.addAdapterFactory(new ProcessAdapterFactory());

        // command stack that will notify this editor as commands are executed
        BasicCommandStack commandStack = new BasicCommandStack();

        // Create the editing domain with our adapterFactory and command stack.
        return new AdapterFactoryEditingDomain(adapterFactory, commandStack, new HashMap<Resource, Boolean>());
    }

    @Before
    public void setUp() throws Exception {
    	process = null;
        createProcessWithData();
    }

    /**
     * @return an AbstractProcess with a global data and a local data on an activity.
     */

    private AbstractProcess createProcessWithData() {
        if (process == null) {
            final MainProcess mainProcess = ProcessFactory.eINSTANCE.createMainProcess();
            ModelHelper.addDataTypes(mainProcess);
            process = ProcessFactory.eINSTANCE.createPool();
            mainProcess.getElements().add(process);

            processData = ProcessFactory.eINSTANCE.createData();
            processData.setDatasourceId("BOS");
            processData.setName("globalData");
            processData.setDataType(ModelHelper.getDataTypeForID(mainProcess, DataTypeLabels.stringDataType));
            process.getData().add(processData);

            final Activity activity = ProcessFactory.eINSTANCE.createActivity();
            localData = ProcessFactory.eINSTANCE.createData();
            localData.setDatasourceId("BOS");
            localData.setName("localData");
            localData.setDataType(ModelHelper.getDataTypeForID(mainProcess, DataTypeLabels.stringDataType));
            activity.getData().add(localData);
            process.getElements().add(activity);
        }
        return process;
    }

    private AbstractProcess initTestForLocalDataRefactor(final String newDataName) {
        return initTestForDataRefactor(newDataName, localData);
    }

    private AbstractProcess initTestForGlobalDataRefactor(final String newDataName) {
        return initTestForDataRefactor(newDataName,processData);
    }

    private AbstractProcess initTestForDataRefactor(String newDataName,	Data dataToRefactor) {
		return initTestForDataRefactor(newDataName, dataToRefactor.getDataType().getName(), dataToRefactor);
	}

	private AbstractProcess initTestForGlobalDataRefactor(String newDataName, String newDataType) {
    	return initTestForDataRefactor(newDataName, newDataType, processData);
	}

	private AbstractProcess initTestForDataRefactor(final String newDataName, final String newDataType, final Data dataToRefactor) {
        final AbstractProcess process = createProcessWithData();
        if(newDataName != null){
        	refactorDataOperation = new RefactorDataOperation(RefactoringOperationType.UPDATE);
        } else {
        	refactorDataOperation = new RefactorDataOperation(RefactoringOperationType.REMOVE);
        }
        refactorDataOperation.setContainer(process);
        refactorDataOperation.setOldData(dataToRefactor);
        if(newDataName != null){
        	final Data newProcessData = createNewProcessData(newDataName, ModelHelper.getDataTypeForID(process, newDataType), dataToRefactor);
        	refactorDataOperation.setNewData(newProcessData);
        }
        editingDomain = createEditingDomain();
        refactorDataOperation.setEditingDomain(editingDomain);
        return process;
    }

	private Data createNewProcessData(final String newDataName, final DataType newDataType, final Data dataToRefactor) {
		final Data newProcessData = EcoreUtil.copy(dataToRefactor);
        newProcessData.setName(newDataName);
        newProcessData.setDataType(newDataType);
		return newProcessData;
	}

}

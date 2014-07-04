package org.wso2.carbon.connector.evernote.notebook;

import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.SharedNotebook;
import com.evernote.edam.type.SharedNotebookPrivilegeLevel;
import com.evernote.thrift.TException;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.evernote.util.EvernoteUtil;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

public class CreateSharedNotebook extends AbstractConnector {
    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        try {
            SynapseLog log = getLog(messageContext);
            log.auditLog("Start : createSharedNotebook");
            NoteStoreClient noteStoreClient = EvernoteUtil.getNoteStoreClient(messageContext);
            SharedNotebook sharedNotebook = new SharedNotebook();

            String guid = EvernoteUtil.lookupTemplateParamater(messageContext,EvernoteUtil.NOTEBOOK_GUID);
            String email = EvernoteUtil.lookupTemplateParamater(messageContext,EvernoteUtil.EMAIL);
            SharedNotebookPrivilegeLevel privilege = SharedNotebookPrivilegeLevel.READ_NOTEBOOK;
            sharedNotebook.setPrivilege(privilege);
            if(email!=null&&!email.trim().equalsIgnoreCase("")){
                sharedNotebook.setEmail(email);
            }

            if(guid!=null&&!guid.equalsIgnoreCase("")){
                sharedNotebook.setNotebookGuid(guid);
            }

            SharedNotebook createdSharedNotebook = noteStoreClient.createSharedNotebook(sharedNotebook);
            OMElement omResponse = EvernoteUtil.parseResponse("sharednotebook.create.success");
            OMElement omSharedNotebook = EvernoteUtil.createOMElement("sharednotebook");
            EvernoteUtil.addAttribute(omSharedNotebook,"id",createdSharedNotebook.getId()+"");
            EvernoteUtil.addElement(omSharedNotebook,"notebookGuid",createdSharedNotebook.getNotebookGuid());
            EvernoteUtil.addElement(omSharedNotebook,"email",createdSharedNotebook.getEmail());
            EvernoteUtil.addElement(omSharedNotebook,"shareKey",createdSharedNotebook.getShareKey());
            EvernoteUtil.addElement(omSharedNotebook,"privilege",createdSharedNotebook.getPrivilege().name());
            omResponse.addChild(omSharedNotebook);
            EvernoteUtil.preparePayload(messageContext,omResponse);
            log.auditLog("Stop : createSharedNotebook");
        }  catch (TException e) {
            log.error(e.getMessage());
            EvernoteUtil.handleException(e, e.getMessage(), "20", messageContext);
            throw new SynapseException(e);
        } catch (EDAMUserException e) {
            log.error(e.getParameter());
            EvernoteUtil.handleException(e,e.getParameter(), e.getErrorCode().getValue()+"", messageContext);
            throw new SynapseException(e);
        } catch (EDAMSystemException e) {
            log.error(e.getMessage());
            EvernoteUtil.handleException(e,e.getMessage() ,e.getErrorCode().getValue()+"", messageContext);
            throw new SynapseException(e);
        } catch (EDAMNotFoundException e) {
            log.error(e.getIdentifier());
            EvernoteUtil.handleException(e,e.getIdentifier() ,"22", messageContext);
            throw new SynapseException(e);
        } catch (Exception e){
            log.error(e.getMessage());
            EvernoteUtil.handleException(e,"Invalid Input" ,"21", messageContext);
            throw new SynapseException(e);
        }
    }
}

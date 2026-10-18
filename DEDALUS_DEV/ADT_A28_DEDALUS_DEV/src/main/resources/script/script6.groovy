/*
 The integration developer needs to create the method processData 
 This method takes Message object of package com.sap.gateway.ip.core.customdev.util 
which includes helper methods useful for the content developer:
The methods available are:
    public java.lang.Object getBody()
	public void setBody(java.lang.Object exchangeBody)
    public java.util.Map<java.lang.String,java.lang.Object> getHeaders()
    public void setHeaders(java.util.Map<java.lang.String,java.lang.Object> exchangeHeaders)
    public void setHeader(java.lang.String name, java.lang.Object value)
    public java.util.Map<java.lang.String,java.lang.Object> getProperties()
    public void setProperties(java.util.Map<java.lang.String,java.lang.Object> exchangeProperties) 
    public void setProperty(java.lang.String name, java.lang.Object value)
    public java.util.List<com.sap.gateway.ip.core.customdev.util.SoapHeader> getSoapHeaders()
    public void setSoapHeaders(java.util.List<com.sap.gateway.ip.core.customdev.util.SoapHeader> soapHeaders) 
       public void clearSoapHeaders()
 */
import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.json.*;
def Message processData(Message message) {
// get an exception java class instance
    def mapProps = message.getProperties();
    def mapHeaders = message.getHeaders();
    def ex = mapProps.get("CamelExceptionCaught");
    
    def sFullExceptionMessage = ex.getResponseBody() as String;
    def sJSONExceptionData = sFullExceptionMessage.substring(sFullExceptionMessage.indexOf("{"),sFullExceptionMessage.lastIndexOf("}")+1);
    
    def jsonSlurper = new JsonSlurper();
    def jsonExceptionDataObject = jsonSlurper.parseText(sJSONExceptionData);
    def sMessageProcessingLogID = jsonExceptionDataObject.MessageProcessingLogID;
    def sActivityContent = new JsonBuilder(jsonExceptionDataObject.content).toString();
    
    jsonExceptionDataObject.remove("MessageProcessingLogID");
    jsonExceptionDataObject.remove("content");
    sJSONExceptionData = new JsonBuilder(jsonExceptionDataObject).toString();
    
    mapProps.put("ErrorContent",sJSONExceptionData);
    mapProps.put("MessageProcessingLogID",sMessageProcessingLogID);
    mapProps.put("ActivityContent",sActivityContent);
    
    message.setBody("");
    
    return message;
}
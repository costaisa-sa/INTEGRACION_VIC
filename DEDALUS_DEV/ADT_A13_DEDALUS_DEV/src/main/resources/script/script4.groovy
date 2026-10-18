import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
def Message processData(Message message) {
    //Body 
    def body = message.getBody();
    def map = message.getHeaders();
    map.put("Content-Type",   "application/json");
    map.put("CamelHttpResponseCode",   400);
  
    message.setBody("{\"status\": \"Hey Bond,please check CPI monitoring for error details\"}");
    
    return message;
}
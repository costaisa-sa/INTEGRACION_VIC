import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def messageLog = messageLogFactory.getMessageLog(message)
    
    if (messageLog != null) {
        // Genera la cabecera personalizada siempre con el valor "X"
        messageLog.addCustomHeaderProperty("ErrorHIS", "X")
    }

    return message
}
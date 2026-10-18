import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def messageLog = messageLogFactory.getMessageLog(message)
    if (messageLog == null) return message // Salvaguarda si el log no está disponible

    // 1. Obtener headers existentes
    def headers = message.getHeaders()

    // 2. Procesar cabeceras fijas (Center y Status)
    // Usamos el operador elvis (?:) para poner "N/A" si vienen nulas o vacías
    messageLog.addCustomHeaderProperty("Center", headers.get("Center") ?: "N/A")
    messageLog.addCustomHeaderProperty("Status", headers.get("Status") ?: "N/A")
    
    // Mantengo tu cabecera original por si la sigues necesitando
    messageLog.addCustomHeaderProperty("ActivityCheck", headers.get("ActivityCheck") ?: "N/A")

    // 3. Procesar cabeceras condicionales (ErrorHIS y ErrorHISDesc)
    // Solo se añaden si tienen contenido (no null y no vacías)
    def errorHis = headers.get("ErrorHIS")
    if (errorHis) { 
        messageLog.addCustomHeaderProperty("ErrorHIS", errorHis)
    }

    def errorDesc = headers.get("ErrorHISDesc")
    if (errorDesc) {
        messageLog.addCustomHeaderProperty("ErrorHISDesc", errorDesc)
    }

    return message
}
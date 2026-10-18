import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    def body = message.getBody(String)
    def messageLog = messageLogFactory.getMessageLog(message);
    def headers = message.getHeaders()
    
    // --- MAPA DE CLIENTES ---
    def ipMapping = [
        "193.146.181.2" : "Dedalus",
        "83.39.198.106" : "Costaisa"
    ]

    // 1. Extraemos la header (Asegúrate de haberla puesto en 'Allowed Headers')
    def headerValue = headers.get("x-cf-true-client-ip")
    
    // 2. Limpieza profunda: convertimos a String, quitamos corchetes y espacios
    String clientIp = headerValue ? headerValue.toString().replaceAll(/[\[\]\s]/, "") : ""
    
    // 3. Lógica de asignación
    def requesterId = ipMapping.get(clientIp) ?: "Unknown (IP: ${clientIp})"

    // 4. Seteo de la Custom Header final
    if (messageLog != null) {
        messageLog.addCustomHeaderProperty("RequesterID", requesterId)
    }
    message.setProperty("RequesterID", requesterId)

    // --- Tu lógica de XML original ---
    def pathMap = [
        "MessageID" : ["MSH", "MSH.10"],
        "PatientN"  : ["PID", "PID.2", "CX.1"],
        "ActivityID": ["PV1", "PV1.52", "XCN.1"]
    ]

    pathMap.each { key, pathList ->
        def value = getValueFromPath(body, pathList)
        if (messageLog != null) {
            messageLog.addCustomHeaderProperty(key, value ?: "N/A")
        }
    }
    
    return message
}

def getValueFromPath(xml, pathList) {
    try {
        def node = new XmlSlurper().parseText(xml)
        pathList.each { part -> node = node[part] }
        return node.text()
    } catch (Exception e) { return null }
}
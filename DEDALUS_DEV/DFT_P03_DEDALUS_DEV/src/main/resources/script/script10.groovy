import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    def headers = message.getHeaders()
    
    // 1. Obtenemos los valores de las headers
    def status = headers.get("Status")
    def errorDesc = headers.get("ErrorHISDesc")

    // 2. Verificación de la condición de error
    // Usamos .toString() para evitar problemas de tipos y compparamos
    if (status?.toString() == "F") {
        
        // Si ErrorHISDesc está vacío, ponemos un mensaje genérico por defecto
        def exceptionMessage = errorDesc ?: "Error no especificado en la cabecera ErrorHISDesc"
        
        // 3. Lanzamos la excepción que hará crashear el iFlow
        throw new Exception("Error provocado por Status F: " + exceptionMessage)
    }

    return message
}
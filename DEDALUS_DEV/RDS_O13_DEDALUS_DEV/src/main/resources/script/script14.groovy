import com.sap.gateway.ip.core.customdev.util.Message
import groovy.xml.XmlUtil
import groovy.xml.QName

def Message processData(Message message) {

    // =========================================================================
    // 🛠️ CONFIGURACIÓN DE MAPEOS
    // =========================================================================
    def menuDeMapeos = [
        // 1. Salvar el valor actual de PV1.50/CX.1 en PV1.52/XCN.1
        [
            parentSegment: "PV1",
            sourcePath: ["PV1.50", "CX.1"],
            targetPath: ["PV1.52", "XCN.1"] 
        ],
        // 2. Pasar el valor de PV1.19/CX.1 a PV1.50/CX.1
        [
            parentSegment: "PV1",
            sourcePath: ["PV1.19", "CX.1"],
            targetPath: ["PV1.50", "CX.1"] 
        ],
        // 3. De PV2.3.1 (CWE.1) a PV2.3.4 (CWE.4)
        [
            parentSegment: "PV2",
            sourcePath: ["PV2.3", "CWE.1"],
            targetPath: ["PV2.3", "CWE.4"]
        ],
        // 4. NUEVO: Anidar Fecha de Admisión (Crear TS.1 si PV1.44 viene plano)
        [
            parentSegment: "PV1",
            sourcePath: ["PV1.44"],
            targetPath: ["PV1.44", "TS.1"],
            isNesting: true // Flag especial para detectar y generar el sub-nodo
        ],
        // 5. NUEVO: Anidar Fecha de Alta (Crear TS.1 si PV1.45 viene plano)
        [
            parentSegment: "PV1",
            sourcePath: ["PV1.45"],
            targetPath: ["PV1.45", "TS.1"],
            isNesting: true // Flag especial para detectar y generar el sub-nodo
        ]
    ]
    // =========================================================================

    def body = message.getBody(java.lang.String) as String
    if (!body) return message

    def root = new XmlParser().parseText(body)

    // Helper: Obtener nombre sin Namespace compatible con CPI
    def getLocalName = { name ->
        return (name instanceof QName) ? name.localPart : name.toString()
    }

    // Helper: Navega por la ruta destino y CREA los nodos si no existen
    def getOrCreatePath = { parentNode, pathList ->
        def current = parentNode
        def nsURI = (parentNode.name() instanceof QName) ? parentNode.name().namespaceURI : ""
        
        pathList.each { step ->
            def nextNode = current.children().find { it instanceof groovy.util.Node && getLocalName(it.name()) == step }
            if (!nextNode) {
                def qName = nsURI ? new QName(nsURI, step) : step
                nextNode = current.appendNode(qName)
            }
            current = nextNode
        }
        return current
    }

    // Ejecutar mapeos dinámicos
    menuDeMapeos.each { regla ->
        
        def parentNodes = root.'**'.findAll { it instanceof groovy.util.Node && getLocalName(it.name()) == regla.parentSegment }

        parentNodes.each { parentNode ->
            
            def currentNode = parentNode
            for (String paso : regla.sourcePath) {
                currentNode = currentNode.children().find { it instanceof groovy.util.Node && getLocalName(it.name()) == paso }
                if (!currentNode) break 
            }

            if (currentNode) {
                if (regla.isNesting) {
                    // Validar si el nodo no tiene etiquetas hijas (está plano) y contiene texto
                    def hasChildNodes = currentNode.children().any { it instanceof groovy.util.Node }
                    
                    if (!hasChildNodes && currentNode.text()) {
                        def valorExtraido = currentNode.text()
                        
                        // Eliminar el texto plano del padre para evitar XML inválido
                        currentNode.setValue([]) 
                        
                        // Crear la estructura destino completa con el hijo
                        def targetNode = getOrCreatePath(parentNode, regla.targetPath)
                        targetNode.setValue(valorExtraido)
                    }
                } else if (currentNode.text()) {
                    // Flujo estándar para el resto de reglas
                    def valorExtraido = currentNode.text()
                    def targetNode = getOrCreatePath(parentNode, regla.targetPath)
                    targetNode.setValue(valorExtraido)
                }
            }
        }
    }

    // Serializar el resultado manteniendo el encoding original
    def modifiedXml = XmlUtil.serialize(root)
    message.setBody(modifiedXml)

    return message
}
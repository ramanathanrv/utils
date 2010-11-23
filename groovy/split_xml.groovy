@Grapes(
    @Grab(group='org.codehaus.woodstox', module='woodstox-core-lgpl', version='4.0.8')
)

import javax.xml.stream.*

pieces = 10
input = "SchemeMaster.xml"
output = "output_%04d.xml"
eventFactory = XMLEventFactory.newInstance()
fileNumber = elementCount = 0

def createEventReader() {
    reader = XMLInputFactory.newInstance().createXMLEventReader(new FileInputStream(input))
    start = reader.next()
    root = reader.nextTag()
    firstChild = reader.nextTag()
    return reader
}

def createNextEventWriter () {
    println "Writing to '${filename = String.format(output, ++fileNumber)}'"
    writer = XMLOutputFactory.newInstance().createXMLEventWriter(new FileOutputStream(filename), start.characterEncodingScheme)
    writer.add(start)
    writer.add(root)
    return writer
}

elements = createEventReader().findAll { it.startElement && it.name == firstChild.name }.size()
println "Splitting ${elements} <${firstChild.name.localPart}> elements into ${pieces} pieces"
chunkSize = elements / pieces
writer = createNextEventWriter()
writer.add(firstChild)
createEventReader().each { 
    if (it.startElement && it.name == firstChild.name) {
        if (++elementCount > chunkSize) {
            writer.add(eventFactory.createEndDocument())
            writer.flush()
            writer = createNextEventWriter()
            elementCount = 0
        }
    }
    writer.add(it)
}


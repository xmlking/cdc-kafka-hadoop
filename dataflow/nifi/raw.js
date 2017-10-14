var util = Java.type("com.crossbusiness.nifi.processors.NiFiUtils");

var flowFile = session.get();

if (flowFile !== null) {


    function isPk(key) {
        return key.startsWith("pk.")
    }

    function slicePk(key) {
        return key.slice(3)
    }

    function extractPk(keyJson) {
        return Object.keys(keyJson).filter(isPk).map(slicePk);
    }

    keyJson = JSON.parse(flowFile.getAttribute("kafka.key"));

    var pks = extractPk(keyJson);

    lcrJson = JSON.parse(util.flowFileToString(flowFile, session));

    var version = flowFile.getAttribute("version");

    var attMap = new java.util.HashMap();
    attMap.put('database', lcrJson.database);
    attMap.put('table', lcrJson.table);
    attMap.put('type', lcrJson.type);
    attMap.put('ts', lcrJson.ts.toString());
    attMap.put('pks', pks.join(", "));
    attMap.put('correlation-identifier', lcrJson.database + "-" + lcrJson.table + "-" + version + "-" + lcrJson.type);
    attMap.put('version', version);

    lcrJson.pks = pks;

    session.remove(flowFile);
    flowFile = util.stringToFlowFile(JSON.stringify(lcrJson), session);
    flowFile = session.putAllAttributes(flowFile, attMap);

    session.transfer(flowFile, REL_SUCCESS);

}

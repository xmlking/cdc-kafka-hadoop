var util = Java.type("com.crossbusiness.nifi.processors.NiFiUtils");

var flowFile = session.get();

if (flowFile !== null) {


    function isPk(key) {
        return key.startsWith("pk.")
    }

    function extractPk(keyJson) {
        return Object.keys(keyJson).filter(isPk);
    }

    var key = flowFile.getAttribute("kafka.key");
    keyJson = JSON.parse(key);

    var payload = [];
    extractPk(keyJson).forEach(function (akey) {
        payload.push(akey, keyJson[akey]);
    });

    lcrJson = JSON.parse(util.flowFileToString(flowFile, session));

    var version = flowFile.getAttribute("version");

    var attMap = new java.util.HashMap();
    attMap.put('database', lcrJson.database);
    attMap.put('table', lcrJson.table);
    attMap.put('type', lcrJson.type);
    attMap.put('ts', lcrJson.ts.toString());
    attMap.put('correlation-identifier', lcrJson.database + "-" + lcrJson.table + "-" + version + "-" + lcrJson.type);
    attMap.put('version', version);


    payload.unshift(attMap['ts'], lcrJson.type, lcrJson.database, lcrJson.table);
    payload = payload.join(", ");

    Object.keys(lcrJson.data).forEach(function (key) {
        var fFile = util.stringToFlowFile(payload + ", " + key + ", " + lcrJson.data[key], session);
        fFile = session.putAllAttributes(fFile, attMap);
        fFile = session.putAttribute(fFile, 'key', key);
        session.transfer(fFile, REL_SUCCESS)
    });

    session.remove(flowFile);
}

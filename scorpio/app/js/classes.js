(function() { // self executing function closure
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open("GET", '/app/classes', false); //N.B. NAUGHTY SYNCHRONOUS REQUEST!
    xmlHttp.send();
    var deps = [];
    JSON.parse(xmlHttp.responseText).forEach(function (file) {
        deps.push("classes/" + file.replace(".js", ""));
    });

//This causes Bower to load every dependency listed in depType
    define(deps, function () {
        return;
    });
})();
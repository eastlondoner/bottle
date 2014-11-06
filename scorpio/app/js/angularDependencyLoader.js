function angularDependencyLoader(requireDependencies, dependenciesFolder, ngName, ngDependencies) {
    var depsToLoad = requireDependencies || [];

    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open("GET", '/app/' + dependenciesFolder, false); //N.B. NAUGHTY SYNCHRONOUS REQUEST!
    xmlHttp.send();

    JSON.parse(xmlHttp.responseText).forEach(function (file) {
        depsToLoad.push(dependenciesFolder + "/" + file.replace(".js", ""));
    });

    var moduleName = dependenciesFolder + 'Module';
    define(moduleName, ['angular'], function (angular) {
        return angular.module(ngName, ngDependencies);
    });

    depsToLoad.unshift(moduleName);

    //This causes Bower to load every dependency listed in depType
    define(dependenciesFolder, depsToLoad, function (module) {
        //Returns depType for chaining
        return module;
    });
}
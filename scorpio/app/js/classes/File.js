define("File", ['underscore'], function () {
    'use strict';

    function Folder(){

    }

    function Container(){

    }

    var FOLDERS = {};

    function buildFolderTree(file){
        FOLDERS[file.container] = FOLDERS[file.container] || new Container();
        var container = FOLDERS[file.container];

        var parts = file.name.split('/');
        parts.pop(); //remove the last element (i.e. the file name)
        if(parts.length > 0) {
            _.reduce(parts, function (memo, part) {
                memo[part] = memo[part] || new Folder();
                return memo[part];
            }, container);

            return parts.join('/'); //returns the parent folder
        }
        return "";
    }

    /**
     * File class
     */
    function File(obj) {
        _.extend(this, obj);
        if (!this.id) this.id = this.name;
        if (!this.validate()) {
            throw "Invalid File Defn";
        }
        this.folder = buildFolderTree(this);
    }

    File.prototype.validate = function () {
        var props = _.chain(this).pick(['name', 'id', 'container']).values().value();
        var checkTypes = _.every(props, _.isString);
        var checkNotEmpty = !_.some(props, _.isEmpty);
        return checkTypes && checkNotEmpty;
    };


    //Static methods

    File.getFolderTree = function (containerId) {
        return FOLDERS[containerId];
    };

    File.build = function (data) {
        return new File(data)
    };

    return File;
});

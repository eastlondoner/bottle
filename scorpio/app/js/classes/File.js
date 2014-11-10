define("File", ['underscore'], function () {
    'use strict';
    /**
     * File class
     */
    function File(obj) {
        _.extend(this, obj);
        if (!this.id) this.id = name;
        if (!this.validate()) {
            throw "Invalid File Defn";
        }
    }

    File.prototype.validate = function () {
        var props = ['name', 'id', 'containerId'];
        var checkTypes = _.every(props, _.isString);
        var checkNotEmpty = !_.some(props, _.isEmpty);
        return checkTypes && checkNotEmpty;
    };

    File.build = function (data) {
        return new File(data)
    };

    return File;
});

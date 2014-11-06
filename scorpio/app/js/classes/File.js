define("File", [], function () {
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

    return File;
});
